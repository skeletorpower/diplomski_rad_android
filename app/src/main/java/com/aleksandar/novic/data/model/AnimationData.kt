package com.aleksandar.novic.data.model

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