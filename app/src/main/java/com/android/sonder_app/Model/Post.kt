package com.android.sonder_app.Model

class Post {
    private lateinit var postid: String
    private lateinit var postimage: String
    private lateinit var location: String
    private lateinit var link: String
    private lateinit var category: String
    private lateinit var subcategory: String
    private lateinit var description: String
    private lateinit var publisher: String
    private lateinit var tagged: HashMap<String, Boolean>
    private var rating: Float = 0F
    private var pricing: Int = 0

    constructor(postid: String, postimage: String, description: String, location: String, link: String, category: String, subcategory: String, rating: Float, pricing: Int, tagged: HashMap<String, Boolean>, publisher: String) {
        this.postid = postid
        this.postimage = postimage
        this.description = description
        this.location = location
        this.link = link
        this.category = category
        this.subcategory = subcategory
        this.pricing = pricing
        this.rating = rating
        this.publisher = publisher
        this.tagged = tagged
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

    fun getLink(): String {
        return this.link
    }

    fun setLink(link: String) {
        this.link = link
    }

    fun getLocation(): String {
        return this.location
    }

    fun setLocation(location: String) {
        this.location = location
    }

    fun getCategory(): String {
        return this.category
    }

    fun setCategory(category: String) {
        this.category = category
    }


    fun getSubCategory(): String {
        return this.subcategory
    }

    fun setSubCategory(subcategory: String) {
        this.subcategory = subcategory
    }

    fun getPricing(): Int {
        return this.pricing
    }

    fun setPricing(pricing: Int) {
        this.pricing = pricing
    }

    fun getRating(): Float {
        return this.rating
    }

    fun setRating(rating: Float) {
        this.rating = rating
    }

    fun getTagged(): HashMap<String, Boolean> {
        return this.tagged
    }

    fun setTagged(tagged: HashMap<String, Boolean>) {
        this.tagged = tagged
    }

}