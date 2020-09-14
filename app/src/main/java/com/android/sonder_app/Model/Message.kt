package com.android.sonder_app.Model

class Message {
    private lateinit var sender: String
    private lateinit var receiver: String
    private lateinit var message: String
    private var isseen: Boolean = false

    constructor(sender: String, receiver: String, message: String, isseen: Boolean) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.isseen = isseen
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


    fun isIsSeen(): Boolean {
        return isseen
    }

    fun setIsSeen(isseen: Boolean) {
        this.isseen = isseen
    }
}