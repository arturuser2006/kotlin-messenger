package com.example.testkotlinapplication.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Message(val senderUid: String, val receiverUid: String, val text: String, val id: String) : Parcelable {
    constructor() : this("", "", "", "")
}