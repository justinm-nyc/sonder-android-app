package com.android.sonder_app.Fragment

import Adapter.UserAdapter
import android.os.Bundle
import android.os.RecoverySystem
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Message
import com.android.sonder_app.Model.MessageList
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatFragment : Fragment() {
    private val TAG = "MyMessage:"
    private lateinit var recyclerView: RecyclerView

    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference

    private lateinit var usersList: ArrayList<MessageList>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        usersList = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.uid)
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList.clear()
                for(snapshot in dataSnapshot.children) {
                    val messageList = snapshot.getValue(MessageList::class.java)
                    usersList.add(messageList!!)
                }
                messageList()
            }

            override fun onCancelled(error: DatabaseError) {}

        })
        return view
    }

    private fun messageList() {
        mUsers = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers.clear()
                for(snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for(messageList in usersList) {
                        if(user!!.getId() == messageList.getId()){
                            mUsers.add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers, true, true, true)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

}
