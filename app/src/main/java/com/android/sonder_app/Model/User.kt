package com.android.sonder_app.Model

class User {
    private lateinit var id: String
    private lateinit var username: String
    private lateinit var fullname: String
    private lateinit var imageurl: String
    private lateinit var bio: String

    constructor(id: String, username: String, fullname: String, imageurl: String, bio: String) {
        this.id = id
        this.username = username
        this.fullname = fullname
        this.imageurl = imageurl
        this.bio = bio
    }

    constructor(){}


    fun getId(): String {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getUsername(): String {
        return this.username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getFullname(): String {
        return this.fullname
    }

    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun getImageurl(): String {
        return imageurl
    }

    fun setImageurl(imageurl: String) {
        this.imageurl = imageurl
    }
    fun getBio(): String {
        return bio
    }

    fun setBio(bio: String) {
        this.bio = bio
    }
}
