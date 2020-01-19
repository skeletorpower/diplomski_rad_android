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

class AnimationResponseFragment : Fragment(R.layout.fragment_animation_response) {

    private val likeFlag by lazy { AnimationResponseFragmentArgs.fromBundle(arguments!!).like }
    private val animationId by lazy { AnimationResponseFragmentArgs.fromBundle(arguments!!).animationId }

    private val viewModel by viewModels<AnimationResponseViewModel> { ViewModelProvider.NewInstanceFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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