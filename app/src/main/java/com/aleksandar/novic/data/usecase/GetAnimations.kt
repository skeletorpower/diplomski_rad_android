package com.aleksandar.novic.data.usecase

import android.util.Log.e
import com.aleksandar.novic.MyApplication
import com.aleksandar.novic.data.model.*
import com.aleksandar.novic.data.network.AnimationsService
import com.aleksandar.novic.data.network.ApiMockService
import com.aleksandar.novic.data.network.RetrofitConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import kotlin.random.Random

object GetAnimations {

    private val gson = Gson()

    private val animationsService: AnimationsService = RetrofitConfig.getAnimationService()

    fun get(): Single<List<Animation>>{
        return animationsService.getAnimations().map {
            return@map it.map { animationData ->
                animationDataToAnimation(animationData) }
        }
    }

    private fun animationDataToAnimation(animationData: AnimationData): Animation{
        return Animation(animationData.id, animationData.name, animationData.likes, animationData.dislikes, getJsonAnimation(animationData.keyframes, animationData.time))
    }

    private fun getJsonAnimation(keyFrames: List<KeyFrame>, time: Int): String{
        val image = getRandomImage()
        var anim: Anim = gson.fromJson(MyApplication.INSTANCE.assets.open("data.json").bufferedReader().readText(), object : TypeToken<Anim>(){}.type)

        val asset: Asset
        anim.assets.first().let {
            asset = it.copy(p=image)
        }
        val layer: Layer
        anim.layers.first().let {
            layer = it.copy(ks = it.ks.copy(position = it.ks.position.copy(positionKeyFrames = keyFrameToPositionKeyFrame(keyFrames))), op = 30 * time)
        }
        anim = anim.copy(assets = listOf(asset), layers = listOf(layer), op = 30 * time)
        return gson.toJsonTree(anim).toString()
    }

    private fun getRandomImage(): String{
        val random = Random.Default
        val number = random.nextInt(1,8)
        return images[number] ?: ""
    }

    private fun keyFrameToPositionKeyFrame(keyFrames: List<KeyFrame>): List<PositionKeyFrame>{

        if (keyFrames.size < 2) {
            return listOf(PositionKeyFrame(ControlPoint(0.167, 0.167), ControlPoint(0.833, 0.833), keyFrames[0].key_frame, listOf(178, keyFrames[0].value.toInt(),0), listOf(0.0,0.0,0.0), listOf(0.0,0.0,0.0)))
        }

        val listOfPositionKeyFrame = mutableListOf<PositionKeyFrame>()

        keyFrames.forEachIndexed { index, keyFrame ->
            val keyFrameTime = keyFrame.key_frame
            val values = listOf(178, keyFrame.value.toInt(),0)
            var firstControlPoint = ControlPoint(0.167,0.167)
            var secondControlPoint =  ControlPoint(0.833,0.833)
            when(keyFrame.interpolator){
                "linear" -> {
                    firstControlPoint = ControlPoint(0.167, 0.167)
                    secondControlPoint = ControlPoint(0.833, 0.833)
                }
                "easy_ease" -> {
                    if (index >= 1){
                        firstControlPoint = ControlPoint(0.333,0.0)
                        val previousKeyFrame = listOfPositionKeyFrame[index-1]
                        listOfPositionKeyFrame[index-1] = previousKeyFrame.copy(secondControlPoint = ControlPoint(0.667,1.0))
                        secondControlPoint = ControlPoint(0.833,0.833)
                    }
                    if (index < 1){
                        firstControlPoint = ControlPoint(0.333,0.0)
                        secondControlPoint = ControlPoint(0.833,0.833)
                    }
                    if (index > keyFrames.size-2){
                        firstControlPoint = ControlPoint(0.167,0.167)
                        val previousKeyFrame = listOfPositionKeyFrame[index-1]
                        listOfPositionKeyFrame[index-1] = previousKeyFrame.copy(secondControlPoint = ControlPoint(0.667,1.0))
                        secondControlPoint = ControlPoint(0.833,0.833)
                    }
                }
                "easy_ease_out"->{
                    firstControlPoint = ControlPoint(0.333,0.0)
                    secondControlPoint = ControlPoint(0.833,0.833)
                }
                "easy_ease_in"-> {
                    secondControlPoint = ControlPoint(0.833,0.833)
                    firstControlPoint = ControlPoint(0.167, 0.167)
                    if (index >= 1){
                        val previousKeyFrame = listOfPositionKeyFrame[index-1]
                        listOfPositionKeyFrame[index-1] = previousKeyFrame.copy(secondControlPoint = ControlPoint(0.667,1.0))
                    }
                }
            }
            listOfPositionKeyFrame.add(PositionKeyFrame(secondControlPoint, firstControlPoint, keyFrameTime, values, listOf(0.0,0.0,0.0), listOf(0.0,0.0,0.0)))
        }

        return listOfPositionKeyFrame
    }

    private val images = hashMapOf(1 to "baseball.png",
        2 to "basketball.png",
        3 to "beachball.png",
        4 to "football.png",
        5 to "tenis.png",
        6 to "valleyball.png",
        7 to "waterpolo.png")
}