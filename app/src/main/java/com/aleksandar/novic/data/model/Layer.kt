package com.aleksandar.novic.data.model

/*Ovo je model koji Anim model ima u sebi listu sa ovim tipom*/
data class Layer(
    val ddd: Int,
    val ind: Int,
    val ty: Int,
    val nm: String,
    val cl: String,
    val refId: String,
    val sr: Int,
    val ks: Ks,
    val ao: Int,
    val ip: Int,
    val op: Int,
    val st: Int,
    val bm: Int
)