package com.aleksandar.novic.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Animation(val id: Int, val name: String, val like: Int, val dislike: Int, val lottieFile: String): Parcelable