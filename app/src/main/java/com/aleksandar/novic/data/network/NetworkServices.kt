package com.aleksandar.novic.data.network

import com.aleksandar.novic.data.model.AnimationData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/*Ovo je service ( java interface) koji definise http metode naseg API-a
* Mora da se definisi kada se koristi najpopularnija Http biblioteka za Android - Retrofit*/
interface AnimationsService{

    @GET("/animations")
    fun getAnimations(): Single<List<AnimationData>>

    @POST("/animations/{id}/incrementLike/")
    fun likeAnimation(@Path("id") id: Int): Single<String>

    @POST("/animations/{id}/incrementDislike/")
    fun dislikeAnimation(@Path("id") id: Int): Single<String>
}