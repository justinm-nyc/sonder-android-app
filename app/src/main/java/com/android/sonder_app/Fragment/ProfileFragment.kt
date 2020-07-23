package com.android.sonder_app.Fragment

import Adapter.MyPhotoAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.User

import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.post_item.*
import kotlinx.android.synthetic.main.post_item.image_profile
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private val TAG = "MyMessage:"

    private lateinit var imageProfile: ImageView
    private lateinit var options: ImageView
    private lateinit var posts: TextView
    private lateinit var followers: TextView
    private lateinit var following: TextView
    private lateinit var fullname: TextView
    private lateinit var bio: TextView
    private lateinit var username: TextView
    private lateinit var editProfile: Button
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var profileid: String
    private lateinit var myPhotos: ImageButton
    private lateinit var savedPhotos: ImageButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var myPhotoAdapter: MyPhotoAdapter
    private lateinit var postList: ArrayList<Post>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val prefs: SharedPreferences = context!!.getSharedPreferences("PREPS", Context.MODE_PRIVATE)
        profileid = prefs.getString("profileid", "none")!!

        Log.d(TAG, "profileid is $profileid");

        imageProfile = view.findViewById(R.id.image_profile)
        options = view.findViewById(R.id.options)
        posts = view.findViewById(R.id.posts)
        followers = view.findViewById(R.id.followers)
        following = view.findViewById(R.id.following)
        fullname = view.findViewById(R.id.fullname)
        bio = view.findViewById(R.id.bio)
        username = view.findViewById(R.id.username)
        myPhotos = view.findViewById(R.id.my_photos)
        savedPhotos = view.findViewById(R.id.saved_photos)
        editProfile = view.findViewById(R.id.edit_profile)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = linearLayoutManager
        postList = ArrayList<Post>()
        myPhotoAdapter = MyPhotoAdapter(context!!, postList)
        recyclerView.adapter = myPhotoAdapter

        userInfo()
        getFollowers()
        getNumPosts()
        myPhotos()

        if (profileid == firebaseUser.uid) {
            editProfile.text = "Edit Profile"
        } else {
            checkFollow()
            savedPhotos.visibility = View.GONE
        }

        editProfile.setOnClickListener {
            var btn: String = editProfile.text.toString()
            if (btn == "Edit Profile") {
                //go to edit profile
            } else if (btn == "follow") {
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                    .child("following").child(profileid).setValue(true);
                FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                    .child("followers").child(firebaseUser.uid).setValue(true);
            } else if (btn == "following") {
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                    .child("following").child(profileid).removeValue()
                FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                    .child("followers").child(firebaseUser.uid).removeValue()
            }
        }

        return view
    }

    fun userInfo() {
        var reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(profileid)
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (context == null) {
                    return
                }
                var user: User? = dataSnapshot.getValue(User::class.java)
                Glide.with(context!!).load(user?.getImageurl()).into(image_profile)
                username.text = user?.getUsername()
                fullname.text = user?.getFullname()
                bio.text = user?.getBio()

            }

        })

    }

    fun checkFollow() {
        var reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.uid)
                .child("following")
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
                    editProfile.text = "following"
                } else {
                    edit_profile.text = "follow"
                }
            }

        })
    }

    fun getFollowers() {
        var reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                .child("following")
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                following.text = dataSnapshot.childrenCount.toString()
            }
        })

        var reference1: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                .child("followers")
        reference1.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followers.text = dataSnapshot.childrenCount.toString()
            }

        })

    }

    fun getNumPosts() {
        var reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var i: Int = 0
                for (snapshot in dataSnapshot.children) {
                    var post: Post = snapshot.getValue(Post::class.java)!!
                    if (post.getPublisher() == profileid) {
                        i++
                    }
                }
                posts.text = i.toString()
            }
        })
    }

    fun myPhotos() {
        var reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    var post: Post = snapshot.getValue(Post::class.java)!!
                    if (post.getPublisher() == profileid) {
                        postList.add(post)
                    }
                }
                postList.reverse()
                myPhotoAdapter.notifyDataSetChanged()
            }
        })
    }
}