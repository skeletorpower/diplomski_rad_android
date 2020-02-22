package com.aleksandar.novic.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*Animation je model koji koristimo funkcionisanje sistema
Ovo je ujesno i najvazniji model
* lottie file je json kao String koji posle prosledjujemo lottie biblioteci, a njega dobijamo iz Anim objekta nakon izvrsenih izmena
* Ovaj model implementira Parcelable interface koji daje mogucnost da se ovaj objekat ubaci kao value sa svojim kljucem u bundle za komunikaciju izmedju fragmenata
* Anotacija @Parcelize sluzi da kaze kompajleru da nam izgenerise potrebnu implementaciju Parcelable interface-a*/
@Parcelize
data class Animation(val id: Int, val name: String, val like: Int, val dislike: Int, val lottieFile: String): Parcelable