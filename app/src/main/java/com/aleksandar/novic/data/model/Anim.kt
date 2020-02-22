package com.aleksandar.novic.data.model

/*data class je kotlinova vrsta klase koja u compile time-u generise sve getter i settere i override-uje copy, toString i jos neke metode*/
/*Anim je objekat koji se dobija mapiranjem (parsiranjem) data.json file iz asset folder-a
* data.json je nas template file za kreiranje odgovarajucec json-a koji moze Lottie biblioteka da parsira i prikaze animaciju
* Lottie biblioteka se koristi za prikazivanje animacije u Android-u koje se mogu kreirati u AfterEffects-u*/
data class Anim(
    val v: String,
    val fr: Int,
    val ip: Int,
    val op: Int,
    val w: Int,
    val h: Int,
    val nm: String,
    val ddd: Int,
    /*assets polje menjamo kada dobijemo podatke o nasim animacijama*/
    val assets: List<Asset>,
    /*layers polje menjamo kada dobijemo podatke o nasim animacijama*/
    val layers: List<Layer>,
    val markers: List<String>
)
/*Ostala polja nisu znacajna za nas sistem*/