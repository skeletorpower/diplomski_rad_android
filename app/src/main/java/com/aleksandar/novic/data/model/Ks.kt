package com.aleksandar.novic.data.model

import com.google.gson.annotations.SerializedName

data class Ks (
    val o: O,
    val r: R,
    @field:SerializedName("p") val position: Position,
    val a: A,
    val s: S
)

data class O(
    val a: Int,
    val k: Int,
    val ix: Int
)

data class R(
    val a: Int,
    val k: Double,
    val ix: Int
)

data class Position(
    val a: Int,
    @field:SerializedName("k") val positionKeyFrames: List<PositionKeyFrame>,
    val ix: Int
)

data class PositionKeyFrame(
    @field:SerializedName("i") val secondControlPoint: ControlPoint,
    @field:SerializedName("o") val firstControlPoint: ControlPoint,
    @field:SerializedName("t") val keyFrame: Int,
    @field:SerializedName("s") val values: List<Int>,
    val to: List<Double>,
    val ti: List<Double>
)

data class ControlPoint(
    val x: Double,
    val y: Double
)

data class A(
    val a: Int,
    val k: List<Double>,
    val ix: Int
)

data class S(
    val a: Int,
    val k: List<Double>,
    val ix: Int
)