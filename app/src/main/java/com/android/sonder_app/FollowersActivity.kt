package com.android.sonder_app

import Adapter.UserAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.User
import com.google.firebase.database.*

class FollowersActivity : AppCompatActivity() {

    private lateinit var id: String
    private lateinit var title: String
    private lateinit var idList: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)

        var intent: Intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList<String>()

        when (title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
        }

    }

    private fun getFollowers() {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Follow").child(id).child("followers")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getFollowing() {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Follow").child(id).child("following")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getLikes() {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Likes").child(id)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showUsers() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val user: User = snapshot.getValue(User::class.java)!!
                    for (id: String in idList) {
                        if (user.getId() == id) {
                            userList.add(user)
                        }
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}
