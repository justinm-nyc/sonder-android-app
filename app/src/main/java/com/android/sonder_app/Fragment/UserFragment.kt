package com.android.sonder_app.Fragment

import Adapter.UserAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserFragment : Fragment() {
    private val TAG = "MyMessage:"
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>

    private lateinit var searchUsers: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        recyclerView = view.findViewById(R.id.chat_user_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
        readUsers()
        searchUsers = view.findViewById(R.id.search_users)
        searchUsers.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }

        })
        return view
    }

    private fun searchUsers(s: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val query: Query =
            FirebaseDatabase.getInstance().getReference("Users").orderByChild("username").startAt(s)
                .endAt(s + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (searchUsers.text.toString() != "") {
                    mUsers.clear()
                    for (snapshot in dataSnapshot.children) {
                        val user: User = snapshot.getValue(User::class.java)!!
                        if (!user.getId().equals(firebaseUser.uid, true)) {
                            mUsers.add(user)
                        }
                    }

                    userAdapter = UserAdapter(context!!, mUsers, false, true, false)
                    recyclerView.adapter = userAdapter
                } else {
                    readUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers.clear()
                for (snapshot in dataSnapshot.children) {
                    val user: User = snapshot.getValue(User::class.java)!!
                    if (user.getId() != firebaseUser!!.uid) {
                        mUsers.add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers, false, true, false)
                recyclerView.adapter = userAdapter
            }
        })

    }

}
