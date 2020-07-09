package com.android.sonder_app.Fragment

import Adapter.PostAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.User

import com.android.sonder_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postLists: ArrayList<Post>

    private lateinit var followingList: ArrayList<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view: View = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        var layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        postLists = ArrayList<Post>()
        postAdapter = PostAdapter(context!!,postLists)
        recyclerView.adapter = postAdapter

        checkFollowing()
        return view
    }

    private fun checkFollowing(){
        followingList = ArrayList<String>()
        var reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid).child("following")
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followingList.clear()
                for(snapshot in dataSnapshot.children){
                    followingList.add(snapshot.key!!)
                }
                readPosts()
            }

        })
    }

    fun readPosts(){
        var reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postLists.clear()
                for(snapshot in dataSnapshot.children){
                    var post: Post = snapshot.getValue(Post::class.java)!!
                    for(id:String in followingList){
                        if(post.getPublisher() == id){
                            postLists.add(post)
                        }
                    }
                }

                postAdapter.notifyDataSetChanged()
            }

        })
    }

}
