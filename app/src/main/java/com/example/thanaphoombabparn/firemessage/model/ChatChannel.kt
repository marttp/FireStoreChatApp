package com.example.thanaphoombabparn.firemessage.model

data class ChatChannel (val userIds: MutableList<String>){
    constructor() : this(mutableListOf())
}