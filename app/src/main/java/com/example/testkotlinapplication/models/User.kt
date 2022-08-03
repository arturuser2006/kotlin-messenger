package com.example.testkotlinapplication.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val name: String, val password: String, val email: String, val uid: String, val profilePhoto: String) : Parcelable{
    constructor() : this("", "", "", "", "")
}