package com.android.sonder_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.sonder_app.Model.Story
import com.android.sonder_app.Model.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    private val TAG = "MyMessage:"

    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L

    private lateinit var storiesProgressView: StoriesProgressView
    private lateinit var image: ImageView
    private lateinit var story_photo: ImageView
    private lateinit var story_username: TextView

    private lateinit var r_seen: LinearLayout
    private lateinit var seen_number: TextView
    private lateinit var story_delete: ImageView

    private lateinit var images: ArrayList<String>
    private lateinit var storyIds: ArrayList<String>
    private lateinit var userId: String


    private var onTouchListener = View.OnTouchListener {view: View, motionEvent: MotionEvent ->
        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now: Long = System.currentTimeMillis()
                storiesProgressView.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        return@OnTouchListener false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        r_seen = findViewById(R.id.r_seen)
        seen_number = findViewById(R.id.seen_number)
        story_delete = findViewById(R.id.story_delete)


        storiesProgressView = findViewById(R.id.stories)
        image = findViewById(R.id.image)
        story_photo = findViewById(R.id.story_photo)
        story_username = findViewById(R.id.story_username)

        r_seen.visibility = View.GONE
        story_delete.visibility = View.GONE

        userId = intent.getStringExtra("userid")!!
        if(userId == FirebaseAuth.getInstance().currentUser!!.uid){
            r_seen.visibility = View.VISIBLE
            story_delete.visibility = View.VISIBLE
        }

        getStories(userId)
        userInfo(userId)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener {
            storiesProgressView.reverse()
        }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener {
            storiesProgressView.skip()
        }
        skip.setOnTouchListener(onTouchListener)

        r_seen.setOnClickListener {
            val intent = Intent(this@StoryActivity, FollowersActivity::class.java)
            intent.putExtra("id",userId)
            intent.putExtra("storyid", storyIds[counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }

        story_delete.setOnClickListener {
            val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyIds[counter])
            reference.removeValue()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext, "Deleted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
        }

    }

    fun getStories(userId: String) {
        images = ArrayList()
        storyIds = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Story").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                images.clear()
                storyIds.clear()
                for(snapshot: DataSnapshot in dataSnapshot.children){
                    val story: Story = snapshot.getValue(Story::class.java)!!
                    val timeCurrent = System.currentTimeMillis()
                    if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()){
                        images.add(story.getImageUrl())
                        storyIds.add(story.getStoryId())
                    }
                }
                storiesProgressView.setStoriesCount(images.size)
                storiesProgressView.setStoryDuration(5000L)
                storiesProgressView.setStoriesListener(this@StoryActivity)
                storiesProgressView.startStories(counter)
                Glide.with(applicationContext).load(images[counter]).into(image)

                Log.d(TAG, "counter is $counter")

                addView(storyIds[counter])
                seenNumber(storyIds[counter])

            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun userInfo(userId: String){
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User = dataSnapshot.getValue(User::class.java)!!
                Glide.with(applicationContext).load(user.getImageurl()).into(story_photo)
                story_username.text = user.getUsername()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addView(storyId: String) {
        FirebaseDatabase.getInstance().getReference("Story")
            .child(userId).child(storyId).child("views")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(true)
    }

    fun seenNumber(storyId: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("Story").child(userId).child(storyId).child("views")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                seen_number.text = ""+dataSnapshot.childrenCount
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onComplete() {
        Log.d(TAG, "onComplete called")
        finish()
    }

    override fun onNext() {
        Log.d(TAG, "onNext called")
        Glide.with(applicationContext).load(images[++counter]).into(image)
        addView(storyIds[counter])
        seenNumber(storyIds[counter])
    }

    override fun onPrev() {
        Log.d(TAG, "onPrev called")
        if ((counter - 1) < 0 ){
            return
        }
        Glide.with(applicationContext).load(images[--counter]).into(image)
        seenNumber(storyIds[counter])
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        storiesProgressView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        Log.d(TAG, "onPause called")
        storiesProgressView.pause()
        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "onResume called")
        storiesProgressView.resume()
        super.onResume()
    }

}
