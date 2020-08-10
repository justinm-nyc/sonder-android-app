package com.android.sonder_app.Model

class Post {
    private lateinit var postid: String
    private lateinit var postimage: String
    private lateinit var description: String
    private lateinit var publisher: String

    constructor(postid: String, postimage: String, description: String, publisher: String) {
        this.postid = postid
        this.postimage = postimage
        this.description = description
        this.publisher = publisher
    }

    constructor(){}

    fun getPostid(): String {
        return postid
    }

    fun setPostid(postid: String) {
        this.postid = postid
    }

    fun getPostimage(): String {
        return postimage
    }

    fun setPostimage(postimage: String) {
        this.postimage = postimage
    }

    fun getDescription(): String {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun getPublisher(): String {
        return this.publisher
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }

}