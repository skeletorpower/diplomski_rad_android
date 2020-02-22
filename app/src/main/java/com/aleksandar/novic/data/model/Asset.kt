package com.aleksandar.novic.data.model

/*Ovo je model koji Anim model ima u sebi listu sa ovim tipom*/
data class Asset(
    val id: String,
    val w: Int,
    val h: Int,
    val u: String,
    /*Samo se naziv slike menja*/
    val p: String,
    val e: Int
)