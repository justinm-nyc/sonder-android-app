package com.android.sonder_app.Model

class Story {
    private lateinit var imageUrl: String
    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private lateinit var storyId: String
    private lateinit var userId: String

    constructor(imageUrl: String, timeStart: Long, timeEnd: Long, storyId: String, userId: String) {
        this.imageUrl = imageUrl
        this.timeStart = timeStart
        this.timeEnd = timeEnd
        this.storyId = storyId
        this.userId = userId
    }

    constructor()

    fun getImageUrl(): String {
        return imageUrl
    }

    fun setImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    fun getTimeStart(): Long {
        return timeStart
    }

    fun setTimeStart(timeStart: Long) {
        this.timeStart = timeStart
    }

    fun getTimeEnd(): Long {
        return timeEnd
    }

    fun setTimeEnd(timeEnd: Long) {
        this.timeEnd = timeEnd
    }

    fun getStoryId(): String {
        return storyId
    }

    fun setStoryId(storyId: String) {
        this.storyId = storyId
    }

    fun getUserId(): String {
        return userId
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }



}