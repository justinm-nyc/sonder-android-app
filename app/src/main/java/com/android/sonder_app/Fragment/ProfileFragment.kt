package com.android.sonder_app.Fragment

import Adapter.MyPhotoAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.EditProfileActivity
import com.android.sonder_app.FollowersActivity
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.post_item.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private val TAG = "MyMessage:"

    private lateinit var imageProfile: ImageView
    private lateinit var options: ImageView
    private lateinit var posts: TextView
    private lateinit var followers: TextView
    private lateinit var followersButton: LinearLayout
    private lateinit var following: TextView
    private lateinit var followingButton: LinearLayout
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

    private lateinit var mySaves: ArrayList<String>
    private lateinit var recyclerView_saves: RecyclerView
    private lateinit var myPhotoAdapter_saves: MyPhotoAdapter
    private lateinit var postListSaves: ArrayList<Post>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val prefs: SharedPreferences = context!!.getSharedPreferences("PREPS", Context.MODE_PRIVATE)
        profileid = prefs.getString("profileid", "none")!!

        Log.d(TAG, "profileid is $profileid")
        imageProfile = view.findViewById(R.id.image_profile)
        options = view.findViewById(R.id.options)
        posts = view.findViewById(R.id.posts)
        followers = view.findViewById(R.id.followers)
        followersButton = view.findViewById(R.id.followersButton)
        following = view.findViewById(R.id.following)
        followingButton = view.findViewById(R.id.followingButton)
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


        recyclerView_saves = view.findViewById(R.id.recycler_view_saves)
        recyclerView_saves.setHasFixedSize(true)
        val linearLayoutManagerSaves: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerView_saves.layoutManager = linearLayoutManagerSaves
        postListSaves = ArrayList<Post>()
        myPhotoAdapter_saves = MyPhotoAdapter(context!!, postListSaves)
        recyclerView_saves.adapter = myPhotoAdapter_saves

        recyclerView.visibility = View.VISIBLE
        recyclerView_saves.visibility = View.GONE

        userInfo()
        getFollowers()
        getNumPosts()
        myPhotos()
        mysaves()

        if (profileid == firebaseUser.uid) {
            editProfile.text = "Edit Profile"
        } else {
            checkFollow()
            savedPhotos.visibility = View.GONE
        }

        followersButton.setOnClickListener(){
            val intent: Intent = Intent(context, FollowersActivity::class.java)
            intent.putExtra("id",profileid)
            intent.putExtra("title","followers")
            startActivity(intent)
        }

        followingButton.setOnClickListener(){
            val intent: Intent = Intent(context, FollowersActivity::class.java)
            intent.putExtra("id",profileid)
            intent.putExtra("title","following")
            startActivity(intent)
        }

        editProfile.setOnClickListener {
            val btn: String = editProfile.text.toString()
            when (btn) {
                "Edit Profile" -> {
                    val intent = Intent(context, EditProfileActivity::class.java)
                    startActivity(intent)
                }
                "follow" -> {
                    FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                        .child("following").child(profileid).setValue(true)
                    FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                        .child("followers").child(firebaseUser.uid).setValue(true)

                    addNotifications()

                }
                "following" -> {
                    FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                        .child("following").child(profileid).removeValue()
                    FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                        .child("followers").child(firebaseUser.uid).removeValue()
                }
            }
        }

        myPhotos.setOnClickListener {
            Log.d(TAG, "myPhotos clicked")
            recyclerView.visibility = View.VISIBLE
            recyclerView_saves.visibility = View.GONE
        }

        savedPhotos.setOnClickListener {
            Log.d(TAG, "savedPhotos clicked")
            recyclerView.visibility = View.GONE
            recyclerView_saves.visibility = View.VISIBLE

            Log.d(TAG, "recyclerView visibility is " + recyclerView.visibility)
        }
        return view
    }

    private fun userInfo() {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(profileid)
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (context == null) {
                    return
                }
                val user: User? = dataSnapshot.getValue(User::class.java)
                Glide.with(context!!).load(user?.getImageurl()).into(image_profile)
                username.text = user?.getUsername()
                fullname.text = user?.getFullname()
                bio.text = user?.getBio()

            }

        })

    }

    private fun addNotifications(){
        var reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid)
        val hashMap: HashMap<String, Any> = HashMap<String, Any>()
        hashMap["userid"] = firebaseUser.uid
        hashMap["text"] = "Started following you "
        hashMap["postid"] = ""
        hashMap["ispost"] = false
        reference.push().setValue(hashMap)
    }

    private fun checkFollow() {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                .child("following")
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
                    editProfile.text = "following"
                } else {
                    editProfile.text = "follow"
                }
            }

        })
    }

    private fun getFollowers() {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                .child("following")
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                following.text = dataSnapshot.childrenCount.toString()
            }
        })

        val reference1: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Follow").child(profileid)
                .child("followers")
        reference1.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followers.text = dataSnapshot.childrenCount.toString()
            }

        })

    }

    private fun getNumPosts() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

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

    private fun myPhotos() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val post: Post = snapshot.getValue(Post::class.java)!!
                    if (post.getPublisher() == profileid) {
                        postList.add(post)
                    }
                }
                postList.reverse()
                myPhotoAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun mysaves() {
        Log.d(TAG, "mysaves() called")
        mySaves = ArrayList<String>()
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    mySaves.add(snapshot.key!!)
                }
                readSaves()
            }
        })
    }

    fun readSaves() {
        Log.d(TAG, "readSaves() was clicked")
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val post: Post = snapshot.getValue(Post::class.java)!!
                    for(id: String in mySaves){
                        if(post.getPostid() == id){
                            postListSaves.add(post)
                        }
                    }
                }
                myPhotoAdapter_saves.notifyDataSetChanged()
            }
        })
    }


}