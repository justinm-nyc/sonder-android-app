package com.android.sonder_app.Model

import android.content.Context

class Notification {
    private lateinit var userid: String
    private lateinit var text: String
    private lateinit var postid: String
    private var ispost: Boolean = true

    constructor(userid: String, text: String, postid: String, ispost: Boolean) {
        this.userid = userid
        this.text = text
        this.postid = postid
        this.ispost = ispost
    }

    constructor(){}

    fun  getUserId(): String {
        return this.userid
    }

    fun setUserId(userid: String) {
        this.userid = userid
    }

    fun  getText(): String {
        return this.text
    }

    fun setText(text: String) {
        this.text = text
    }

    fun  getPostId(): String {
        return this.postid
    }

    fun setPostId(postid: String) {
        this.postid = postid
    }

    fun isIsPost(): Boolean {
        return this.ispost
    }

    fun setIsPost(ispost: Boolean) {
        this.ispost = ispost
    }





}