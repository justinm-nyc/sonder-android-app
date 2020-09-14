package com.android.sonder_app

import Adapter.MessageAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Message
import com.android.sonder_app.Model.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ConversationActivity : AppCompatActivity() {
    val TAG = "MyMessage:"

    private lateinit var profileImage: CircleImageView
    private lateinit var username: TextView

    private lateinit var btnSend: ImageView
    private lateinit var textSend: EditText

    private lateinit var userid: String

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var mMessage: ArrayList<Message>

    private lateinit var recyclerView: RecyclerView

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference

    private lateinit var seenListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager


        profileImage = findViewById(R.id.image_profile)
        username = findViewById(R.id.convo_username)
        btnSend = findViewById(R.id.btn_send)
        textSend = findViewById(R.id.text_send)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        userid = intent.getStringExtra("userid")

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid!!)
        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = snapshot.getValue(User::class.java)!!
                username.text = user.getUsername()
                Glide.with(applicationContext).load(user.getImageurl()).into(profileImage)

                readMessages(firebaseUser.uid, userid, user.getImageurl())
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        seenMessage(userid)

        btnSend.setOnClickListener {
            val msg = textSend.text.toString()
            if (msg != "") {
                sendMessage(firebaseUser.uid, userid, msg)
            }
            textSend.setText("")
        }

    }

    private fun seenMessage(userid: String) {
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children) {
                    val message: Message = snapshot.getValue(Message::class.java)!!
                    if(message.getReceiver() == firebaseUser.uid && message.getSender() == userid) {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["isseen"] = true
                        snapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(sender: String, receiver: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["message"] = message
        hashMap["isseen"] = false

        reference.child("Chats").push().setValue(hashMap)

        val chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.uid).child(userid)
        chatRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    chatRef.child("id").setValue(userid)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val chatRefReverse = FirebaseDatabase.getInstance().getReference("Chatlist").child(userid).child(firebaseUser.uid)
        chatRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    chatRefReverse.child("id").setValue(firebaseUser.uid)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun readMessages(myid: String, userid: String, imageurl: String) {
        mMessage = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mMessage.clear()
                for(snapshot in dataSnapshot.children){
                    val message: Message = snapshot.getValue(Message::class.java)!!
                    if(message.getReceiver() == myid && message.getSender() == userid || message.getReceiver() == userid && message.getSender() == myid) {
                            mMessage.add(message)
                    }
                }
                messageAdapter = MessageAdapter(this@ConversationActivity, mMessage, imageurl)
                recyclerView.adapter = messageAdapter
                Log.w(TAG, mMessage.toString());
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onPause() {
        super.onPause()
        reference.removeEventListener(seenListener)
    }
}
