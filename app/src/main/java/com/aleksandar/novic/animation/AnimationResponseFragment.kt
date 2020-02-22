package com.aleksandar.novic.animation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.util.Log.e
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.aleksandar.novic.R
import com.aleksandar.novic.data.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_animation_response.*

/*Ovo je posledni ekran u flow-u apliakcije
* I ovaj fragment ima svoje layout i ima svoj viewModel
* SLuzi za slanje rezultata na server i prikazivanje odgovora servera
* Rezultati koje salje ovaj ekran su prikazani pomocu animacija */
class AnimationResponseFragment : Fragment(R.layout.fragment_animation_response) {

    /*Ovo je flag kao true za like i false za dislike i dobijaju se preko bundle-a izmedju fragment-a, ali preko reference pluginom kreiranih klasa*/
    private val likeFlag by lazy { AnimationResponseFragmentArgs.fromBundle(arguments!!).like }
    /*AnimationId koji smo dobili sa prethodnog ekrana kako bismo znali za koju animaciju da posaljemo like ili dislike*/
    private val animationId by lazy { AnimationResponseFragmentArgs.fromBundle(arguments!!).animationId }

    /*Kreiranje odgovarajuceg viewModel-a pomocu kotlinove funkcije*/
    private val viewModel by viewModels<AnimationResponseViewModel> { ViewModelProvider.NewInstanceFactory() }

    /*Callback kojie se okida kada se view kreira*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*Sve do if(likeFlag) linije, kod predstavlja implementaciju blurovanja poslednje analizirane slike sa prethodnog ekrana*/
        val bitmap = Repository.background!!
        val matrix = Matrix()
        matrix.postRotate(90f)
        background.setImageBitmap(
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        )
        blur.setOverlayColor(android.R.color.transparent)
        blur.setDownsampleFactor(1)
        blur.setBlurRadius(25)
        blur.setBlurredView(background)
        blur.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 500 })

        /*U daljem kodu na osnovu odgovorana prethodnog ekrana pozivaju se odgovarajuci metodi u viewModelu*/
        if (likeFlag)
            viewModel.likeAnimation(animationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    e("TAG", it.toString() + " like")
                    like.playAnimation()
                },{
                    e("TAG", it.toString())
                    findNavController().navigateUp()
                })
        else
            viewModel.dislikeAnimation(animationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    e("TAG", it.toString() + " dislike")
                    dislike.playAnimation()
                },{
                    e("TAG", it.toString())
                    findNavController().navigateUp()
                })
    }
}