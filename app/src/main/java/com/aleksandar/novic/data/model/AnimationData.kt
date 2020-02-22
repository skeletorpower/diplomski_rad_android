package com.aleksandar.novic.data.model

/*Ovo su dva modela koji sluze da mapiraju json koji dobijemo sa servera*/
data class AnimationData(
    val id: Int,
    val name: String,
    val time: Int,
    val likes: Int,
    val dislikes: Int,
    val keyframes: List<KeyFrame>
)

data class KeyFrame(
    val key_frame: Int,
    val value: Float,
    val interpolator: String
)