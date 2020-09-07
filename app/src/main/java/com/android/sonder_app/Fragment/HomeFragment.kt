package com.android.sonder_app.Fragment

import Adapter.PostAdapter
import Adapter.StoryAdapter
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.AddStoryActivity
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.Story
import com.android.sonder_app.Model.User

import com.android.sonder_app.R
import com.android.sonder_app.StoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postLists: ArrayList<Post>
    private lateinit var recyclerViewStory: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var storyList: ArrayList<Story>

    private lateinit var followingList: ArrayList<String>
    private lateinit var progressBar: ProgressBar

    private lateinit var flashButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        flashButton = view.findViewById(R.id.flashView)
        progressBar = view.findViewById(R.id.progress_circular)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        recyclerViewStory.setHasFixedSize(true)
        val layoutManager1 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = layoutManager1
        storyList = ArrayList()
        storyAdapter = StoryAdapter(context!!, storyList)
        recyclerViewStory.adapter = storyAdapter

        postLists = ArrayList()
        postAdapter = PostAdapter(context!!,postLists)
        recyclerView.adapter = postAdapter

        flashButton.setOnClickListener {
            val intent = Intent(context, AddStoryActivity::class.java)
            context!!.startActivity(intent)
        }

        checkFollowing()
        return view
    }

    private fun checkFollowing(){
        followingList = ArrayList()
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid).child("following")
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followingList.clear()
                for(snapshot in dataSnapshot.children){
                    followingList.add(snapshot.key!!)
                }
                readPosts()
                readStory()
            }

        })
    }

    fun readPosts(){
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postLists.clear()
                for(snapshot in dataSnapshot.children){
                    val post: Post = snapshot.getValue(Post::class.java)!!
                    for(id:String in followingList){
                        if(post.getPublisher() == id){
                            postLists.add(post)
                        }
                    }

                    if(post.getPublisher() == FirebaseAuth.getInstance().currentUser!!.uid){
                        postLists.add(post)
                    }
                }

                postAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

        })
    }

    fun readStory() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Story")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentTime: Long = System.currentTimeMillis()
                storyList.clear()
                storyList.add(Story("",0,0,"", FirebaseAuth.getInstance().currentUser!!.uid))

                for(id: String in followingList){
                    var countStory = 0
                    var story: Story? = null
                    for(snapshot: DataSnapshot in dataSnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)
                        if(currentTime > story!!.getTimeStart() && currentTime < story.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if(countStory > 0){
                        storyList.add(story!!)
                    }
                }
                storyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}
