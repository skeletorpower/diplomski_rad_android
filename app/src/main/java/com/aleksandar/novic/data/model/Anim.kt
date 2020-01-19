package com.aleksandar.novic.data.model

data class Anim(
    val v: String,
    val fr: Int,
    val ip: Int,
    val op: Int,
    val w: Int,
    val h: Int,
    val nm: String,
    val ddd: Int,
    val assets: List<Asset>,
    val layers: List<Layer>,
    val markers: List<String>
)