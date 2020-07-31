package com.android.sonder_app.Fragment

import Adapter.NotificationAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Notification
import com.android.sonder_app.Model.User

import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.post_item.*
import java.util.*
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationList: ArrayList<Notification>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_notification, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        notificationList = ArrayList<Notification>()
        notificationAdapter = NotificationAdapter(context!!, notificationList)
        recyclerView.adapter = notificationAdapter
        readNotification()
        return view
    }

    private fun readNotification() {
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.uid)
        reference.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notificationList.clear()
                for (snapshot in dataSnapshot.children) {
                    val notification: Notification = snapshot.getValue(Notification::class.java)!!
                    notificationList.add(notification)
                }

                notificationList.reverse()
                notificationAdapter.notifyDataSetChanged()

            }

        })
    }

}
