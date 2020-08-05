package com.android.sonder_app.Fragment

import Adapter.PostAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Post

import com.android.sonder_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class PostDetailsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postLists: ArrayList<Post>

    private lateinit var postid: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val preferences: SharedPreferences =
            context!!.getSharedPreferences("PREPS", Context.MODE_PRIVATE)
        postid = preferences.getString("postid", "none")!!

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_post_details, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        postLists = ArrayList<Post>()
        postAdapter = PostAdapter(context!!, postLists)
        recyclerView.adapter = postAdapter

        readPost()
        return view
    }

    private fun readPost() {
        var reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("posts").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postLists.clear()
                var post: Post = dataSnapshot.getValue(Post::class.java)!!
                postLists.add(post)
                postAdapter.notifyDataSetChanged()

            }
        })
    }

}
