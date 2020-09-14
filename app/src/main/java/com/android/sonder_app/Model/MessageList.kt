package com.android.sonder_app.Model

class MessageList {
    private lateinit var id: String

    constructor(id: String) {
        this.id = id
    }

    constructor()

    fun getId(): String {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

}