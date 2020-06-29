package com.aleksandar.novic.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.Log.e
import android.util.Rational
import android.util.Size
import android.view.View
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aleksandar.novic.MainActivity
import com.aleksandar.novic.R
import com.aleksandar.novic.data.Repository
import com.aleksandar.novic.data.model.Animation
import com.aleksandar.novic.tflite.AnimationResponseClassifier
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

/*Ovo je drugi ekran u aplikaciji.
* Sluzi za otvaranje kamere i prosledjivanje slike sistemu za raspoznavanje objekata na slici*/
class CameraFragment : Fragment(R.layout.fragment_camera){

    /*Ovaj fragment ima referencu na animaciju koju smo izabrali iz liste kako bismo je ocenili
    * to se postize tako sto se koristi sistemska implementacija komunikacije dva fragmenta.
    * Implementaicja se ogleda u postojanju bundle objekta u koji mozemo da stavimo key - value par
    * Medjutim, ovde se ne vidi nijedan kljuc koji prosledjujemo jer koristimo plugin koji u build time generise klase koje otpakuju bundle,
    * kreiraju klasu i daju nam podatke preko referenci koje su definisane kao kljucevi pr. animation*/
    private val animation: Animation by lazy { CameraFragmentArgs.fromBundle(arguments!!).animation }

    /*Ovo je referenca na objekat koji koristimo u klasifikaciji objekata sa slike*/
    private lateinit var animationResponseClassifier: AnimationResponseClassifier

    /*Ovo je callback metod fragment-a koji nam kaze da je view kreiran i da mozemo da ga setujemo*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*Ovde kreiramo objekat za klasifikaciju, njegova implementacija se nalazi u paketu tflite ali implementaicja je prekopirana
        * iz primera koriscenja tensorFlowLite biblioteke*/
        animationResponseClassifier = AnimationResponseClassifier.create(requireActivity())
        /*ImageAnalyzer je implementacija Analizer-a koji dolazi sa bibliotekom TensorFlowLite. Takodje je kopirana i doradjena za konkretan primer
        * Implementacija se nalazi na dnu ovog file-a*/
        ImageAnalyzer.animationResponseClassifier = animationResponseClassifier
        /*Sledeca linija poziva metode za dobijanje reference nad objektom koji je sistemska implementacija za trajno skladistenje podataka u formi
        * key - value para, ta referenca je sp*/
        val sp = requireContext().getSharedPreferences("Animationary", Context.MODE_PRIVATE)
        /*like i dislike su nase postavke za pronadjene objekte na slici koji daju rezultate na osnovu kojih saljemo like ili dislike animacije*/
        val like = sp.getString("like", "")!!
        val dislike = sp.getString("dislike", "")!!
        /*Proveravamo da ako pre ovog ekrana nismo definisali kriterijume za like i dislike onda izlazimo sa ekrana i vracamo se na prethodni*/
        if (like.isBlank() || dislike.isBlank()) findNavController().navigateUp()
        /*ImageAnalyzeru setujemo sta ce biti like i dislike*/
        ImageAnalyzer.like = like
        ImageAnalyzer.dislike = dislike
        text_like.text = "Like: \n${like}"
        text_dislike.text = "Dislike: \n${dislike}"
        text_like_shadow.text = "Like: \n${like}"
        text_dislike_shadow.text = "Dislike: \n${dislike}"
        /*Ovde setujemo callback koji se okida kada AI prepozna objekat koji ima znacenje like-a ili dislike-a i u callback-u dobijamo true ako je like
        * ili false ako je dislike*/
        ImageAnalyzer.navigatesCallback = {
            /*Kada se okine callback onda opet pomocu navController-a, generisanih pravaca pomocu xml u res/navigation i generisanih bundle-a za komunikaciju
            * fragmenata idemo na sledeci ekran koji je zaduzen za slanje like-a ili dislike-a na server*/
            if (this@CameraFragment.isAdded)
            (requireActivity() as MainActivity).navController.navigate(CameraFragmentDirections.actionCameraFragmentToAnimationResponseFragment(it, animation.id))
        }
        ImageAnalyzer.candidatesCallback = {
            if (text_answer != null && text_answer_shadow != null) {
                text_answer.text = it
                text_answer_shadow.text = it
            }
        }
        /*ovde proveravamo da li je korisnik na svom ekranu dao permisije za koriscenje kamere i ako jeste onda palimo kameru pomocu metoda bindCameraUseCase*/
        if (hasPermission()){
            camera_view.post{
                bindCameraUseCases()
            }
        }
        /*u ova dva metoda prikazujemo jos jednom animaciju koju smo odabrali koja se prikazuje preko kamere*/
        view_animation.imageAssetsFolder = "images/"
        view_animation.setAnimationFromJson(animation.lottieFile, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*Ovde, na ovom ekranu sklanjamo actionBar - setujemo ga da je nevidljiv*/
        (requireActivity() as MainActivity).app_bar_layout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        /*Ovde od naseg activity-a (Prvenstveno u Androidu jedan ekran - MainActivity klasa) zahtevamo da skloni sistemske komponent-e (status bar, navigation bar)*/
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        /*U ovom odeljku pitamo da li je korisnik dao permisisje za koriscenje kamere i ukoliko nije onda ga pitamo pomocu metoda requestPermissions*/
        if (!hasPermission()){
            requestPermissions(arrayOf(Manifest.permission.CAMERA),1001)
        }
    }

    /*Obican callback metod koji se okida kada odlazimo sa ekrana
    * u ovom trenutku zbog dokumentacije o koriscenju tensorFlowLite bibliotke moramo da zatvorimo klasifikator*/
    override fun onDestroyView() {
        super.onDestroyView()
        animationResponseClassifier.close()
    }

    /*Ovo je sistemski callback koji se okida kada se korisnik vrati sa sistemskog dijaloga koji pita za permissije
    * ovde proveravamo da li je korisnik odobrio
    * Ukoliko je odobrio onda pokrecemo kameru pomocu bindCameraUseCase
    * Ukoliko nije odobrio, onda vracamo korisnika na prethodni ekran sto je lista animacija*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Snackbar.make(requireView(), "Permission request granted", Snackbar.LENGTH_SHORT).show()
            camera_view.post{
                bindCameraUseCases()
            }
        }else{
            Snackbar.make(requireView(), "Permission request denied", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    /*Ovo je metod za pokretanje kamere
    * U ovoj aplikaciji se koristi najnovija verzija API-a za koriscenje kamere u Android-u
    * Postoji vise useCase-eva za koriscenje kamere a mi koristimo Preview (to je samo da ti se iscrtava slika na ekranu) i ImageAnalyser
    * koji ima funkciju da prosledjujemo frame-ove kamere na ispitivanje.
    * Sama implementacija je opet preuzeta iz primera koriscenja biblioteke*/
    private fun bindCameraUseCases() {

        val metrics = DisplayMetrics().also { camera_view.display.getRealMetrics(it) }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val viewFinderConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(camera_view.display.rotation)
        }.build()

        val preview = AutoFitPreviewBuilder.build(viewFinderConfig, camera_view)

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            val analyzerThread = HandlerThread(
                "LuminosityAnalysis").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(camera_view.display.rotation)
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = ImageAnalyzer
        }

        CameraX.bindToLifecycle(viewLifecycleOwner, preview, analyzerUseCase)
    }

    private fun hasPermission() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

object ImageAnalyzer:ImageAnalysis.Analyzer{
    var animationResponseClassifier: AnimationResponseClassifier? = null
    private var lastAnalyzedTimestamp = 0L

    lateinit var like: String
    lateinit var dislike: String

    var navigatesCallback: ((Boolean) -> Unit)? = null
    var candidatesCallback: ((String) -> Unit)? = null

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        val currentTimestamp = System.currentTimeMillis()
        // Calculate the average luma no more often than every second
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)) {
            // Since format in ImageAnalysis is YUV, image.planes[0]
            // contains the Y (luminance) plane

            val bitmap = getImageFromProxy(image)
            val results = animationResponseClassifier?.recognizeImage(cropBitmap(bitmap))

            if (results?.isNotEmpty() == true){
                Handler(Looper.getMainLooper()).post {
                    candidatesCallback?.invoke(results[0].title)
                }
                if(results[0].title == like){
                    Repository.background = bitmap
                    e("TAG", "it's a $like!!!")
                    Handler(Looper.getMainLooper()).post{
                        navigatesCallback?.invoke(true)
                    }
                }else if(results[0].title == dislike){
                    Repository.background = bitmap
                    e("TAG", "it's a $dislike!!!")
                    Handler(Looper.getMainLooper()).post{
                        navigatesCallback?.invoke(false)
                    }
                }
            }

            lastAnalyzedTimestamp = currentTimestamp
        }
    }

    private fun getImageFromProxy(image: ImageProxy): Bitmap {

        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun cropBitmap(bitmap: Bitmap): Bitmap = Bitmap.createBitmap(bitmap,bitmap.width/2-112,bitmap.height/2-112,224,224)
}