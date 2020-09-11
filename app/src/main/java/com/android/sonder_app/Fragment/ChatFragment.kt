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

    private lateinit var usersList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser()!!

        usersList = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList.clear()
                for(snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(Message::class.java)
                    if(message!!.getSender() == firebaseUser.uid) {
                        usersList.add(message.getReceiver())
                    }
                    if(message!!.getReceiver() == firebaseUser.uid) {
                        usersList.add(message.getSender())
                    }
                }
                readMessages()
            }

        })

        return view
    }

    private fun readMessages() {
        mUsers = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers.clear()

                for(snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)

                    //Display 1 user from chats
                    for(id in usersList){
                        if(user!!.getId() == id){
                            if(mUsers.size != 0) {
                                Log.w(TAG, mUsers.size.toString());
                                for(i in 0 until mUsers.size) {
                                    val user1 = mUsers[i]
                                    if(user.getId() != user1.getId()) {
                                        mUsers.add(user)
                                    }
                                }
                            } else {
                                //ONLY ADD IF IS NOT ALREADY IN mUSERS
                                mUsers.add(user)
                            }
                        }
                    }
                }
                Log.w(TAG, mUsers.size.toString());
                userAdapter = UserAdapter(context!!, mUsers, false, true)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }
}
