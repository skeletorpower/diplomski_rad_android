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

class CameraFragment : Fragment(R.layout.fragment_camera){

    private val animation: Animation by lazy { CameraFragmentArgs.fromBundle(arguments!!).animation }

    private lateinit var animationResponseClassifier: AnimationResponseClassifier

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        animationResponseClassifier = AnimationResponseClassifier.create(requireActivity())
        ImageAnalyzer.animationResponseClassifier = animationResponseClassifier
        val sp = requireContext().getSharedPreferences("Animationary", Context.MODE_PRIVATE)
        val like = sp.getString("like", "")!!
        val dislike = sp.getString("dislike", "")!!
        if (like.isBlank() || dislike.isBlank()) findNavController().navigateUp()
        ImageAnalyzer.like = like
        ImageAnalyzer.dislike = dislike
        ImageAnalyzer.navigatesCallback = {
            if (this@CameraFragment.isAdded)
            (requireActivity() as MainActivity).navController.navigate(CameraFragmentDirections.actionCameraFragmentToAnimationResponseFragment(it, animation.id))
        }
        if (hasPermission()){
            camera_view.post{
                bindCameraUseCases()
            }
        }
        view_animation.imageAssetsFolder = "images/"
        view_animation.setAnimationFromJson(animation.lottieFile, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).app_bar_layout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        if (!hasPermission()){
            requestPermissions(arrayOf(Manifest.permission.CAMERA),1001)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animationResponseClassifier.close()
    }

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