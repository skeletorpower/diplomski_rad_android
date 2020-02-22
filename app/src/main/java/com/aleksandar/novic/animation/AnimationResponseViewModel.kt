package com.aleksandar.novic.animation

import androidx.lifecycle.ViewModel
import com.aleksandar.novic.data.usecase.SendFeedbackForAnimation
import io.reactivex.Single

/*ViewModel za Slanje odgovora*/
class AnimationResponseViewModel : ViewModel() {

    /*SendFeedbackForAnimation je opet kotlinom object (singleton) koji implementira Api pozive*/
    fun likeAnimation(id: Int): Single<String> {
        return SendFeedbackForAnimation.likeAnimation(id)
    }

    fun dislikeAnimation(id: Int): Single<String> {
        return SendFeedbackForAnimation.dislikeAnimation(id)
    }
}