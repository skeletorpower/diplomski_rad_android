package com.aleksandar.novic.data.network

import com.aleksandar.novic.MyApplication
import com.aleksandar.novic.data.model.AnimationData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import java.lang.Exception

object ApiMockService: AnimationsService{

    override fun getAnimations(): Single<List<AnimationData>> = Single.create { emitter ->
        val jsonStringFromAssets = MyApplication.INSTANCE.assets.open("animations.json").bufferedReader().readText()
        val listOfAnimations: List<AnimationData> = Gson().fromJson(jsonStringFromAssets, object : TypeToken<List<AnimationData>>(){}.type)
        try {
            Thread.sleep(3000)
        }catch (e: Exception){

        }
        emitter.onSuccess(listOfAnimations)
    }

    override fun likeAnimation(id: Int): Single<String> = Single.create { e ->
        e.onSuccess("success")
    }

    override fun dislikeAnimation(id: Int): Single<String> = Single.create { e ->
        e.onSuccess("success")
    }
}