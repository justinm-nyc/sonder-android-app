package com.android.sonder_app

import Adapter.CommentAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Comment
import com.android.sonder_app.Model.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {
    private val TAG = "MyMessage:"

    private lateinit var addcomment: EditText
    private lateinit var imageProfile: ImageView
    private lateinit var post: TextView
    private lateinit var postid: String
    private lateinit var publisherid: String
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: ArrayList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        var toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Comments"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        var linearLayoutManger:  LinearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManger

        commentList = ArrayList<Comment>()
        commentAdapter = CommentAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        toolbar.setNavigationOnClickListener() {
            finish()
        }

        addcomment = findViewById(R.id.add_comment)
        post = findViewById(R.id.post)
        imageProfile = findViewById(R.id.image_profile)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val intent: Intent = intent
        postid = intent.getStringExtra("postid")
        publisherid = intent.getStringExtra("publisherid")

        post.setOnClickListener {
            if(addcomment.text.toString() == ""){
                Toast.makeText(this@CommentsActivity, "Comment can not be empty", Toast.LENGTH_SHORT).show()
            } else {
                addcomment()
            }
        }

        getImage()
        readComments()
    }

    private fun addcomment(){
        var reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid)
        var hashMap: HashMap<String, String> = HashMap()
        hashMap["comment"] = addcomment.text.toString()
        hashMap["publisher"] = firebaseUser.uid

        reference.push().setValue(hashMap)
        addcomment.setText("")
    }

    private fun getImage(){
        Log.d(TAG, "Started geImage()");
        var reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
        val getImageListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var user: User? = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    Glide.with(applicationContext).load(user.getImageurl()).into(image_profile)
                }
            }
        }
        reference.addValueEventListener(getImageListener)
    }

    fun readComments(){
        var reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid)
        Log.d(TAG, "Started readComments()");
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange in readComments()");
                commentList.clear()
                for(snapshot in dataSnapshot.children){
                    val comment: Comment = snapshot.getValue(Comment::class.java)!!
                        Log.d(TAG, "comment is: " + comment.getComment().toString())
                        commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }

        })
    }
}
