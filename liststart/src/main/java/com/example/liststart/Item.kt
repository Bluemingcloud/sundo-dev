package com.example.liststart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val title: String,
    val date: String,
    val profileImage: Int,
    val lat: Double,
    val long: Double
) : Parcelable
