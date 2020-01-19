package com.aleksandar.novic.data.usecase

import com.aleksandar.novic.data.network.RetrofitConfig
import io.reactivex.Single

object SendFeedbackForAnimation {

    fun likeAnimation(id: Int): Single<String> {
        return RetrofitConfig.getAnimationService().likeAnimation(id)
    }

    fun dislikeAnimation(id: Int): Single<String> {
        return RetrofitConfig.getAnimationService().dislikeAnimation(id)
    }
}