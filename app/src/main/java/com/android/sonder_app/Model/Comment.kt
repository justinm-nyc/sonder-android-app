package com.android.sonder_app.Model

class Comment {
    private lateinit var comment: String
    private lateinit var publisher: String

    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    constructor(){}

    fun getComment(): String {
        return comment
    }

    fun setComment(comment: String) {
        this.comment = comment
    }

    fun getPublisher(): String {
        return publisher
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }
}