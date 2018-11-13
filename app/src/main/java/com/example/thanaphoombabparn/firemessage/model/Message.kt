package com.example.thanaphoombabparn.firemessage.model

import java.util.*

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

interface Message {
    val time: Date
    val senderId:String
    // registration token from messaging whenever message to chat channel
    // notify recipient!!
    // Receiver ID = firebase token from instanceId
    val recipientId: String
    // display name who send a message
    val senderName: String
    val type: String
}