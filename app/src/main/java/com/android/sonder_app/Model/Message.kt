package com.android.sonder_app.Model

class Message {
    private lateinit var sender: String
    private lateinit var receiver: String
    private lateinit var message: String

    constructor(sender: String, receiver: String, message: String) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
    }

    constructor()

    fun getSender(): String {
        return sender
    }

    fun setSender(sender: String) {
        this.sender = sender
    }

    fun getReceiver(): String {
        return receiver
    }

    fun setReceiver(receiver: String) {
        this.receiver = receiver
    }

    fun getMessage(): String {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

}