package Adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.ConversationActivity
import com.android.sonder_app.Fragment.ProfileFragment
import com.android.sonder_app.MainActivity
import com.android.sonder_app.MessageActivity
import com.android.sonder_app.Model.Message
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter : RecyclerView.Adapter<UserAdapter.ViewHolder> {
    val TAG = "MyMessage:"

    private var mContext: Context
    private var mUser: List<User>
    private var isFragments: Boolean = false
    var isChat: Boolean = false
    var isConversation: Boolean = false
    var theLastMessage: String? = null
    private lateinit var firebaseUser: FirebaseUser

    constructor(
        mContext: Context,
        mUser: List<User>,
        isFragments: Boolean,
        isChat: Boolean,
        isConversation: Boolean
    ) : super() {
        this.mContext = mContext
        this.mUser = mUser
        this.isFragments = isFragments
        this.isChat = isChat
        this.isConversation = isConversation
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }


    private fun addNotifications(userid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid)
        val hashMap: HashMap<String, Any> = HashMap<String, Any>()
        hashMap["userid"] = firebaseUser.uid
        hashMap["text"] = "started following you"
        hashMap["postid"] = ""
        hashMap["ispost"] = false
        reference.push().setValue(hashMap)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val user: User = mUser[position]
        holder.username?.text = user.getUsername()
        holder.fullname?.text = user.getFullname()
        Glide.with(mContext).load(user.getImageurl()).into(holder.imageProfile!!)

        //If this is not a chat then this will e used in search, followers, and follow components
        if (!isChat) {
            holder.btnFollow?.visibility = View.VISIBLE
            isFollowing(user.getId(), holder.btnFollow!!)

            if (user.getId() == firebaseUser.uid) {
                holder.btnFollow!!.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (isFragments) {
                    val editor: SharedPreferences.Editor =
                        mContext.getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit()
                    editor.putString("profileid", user.getId())
                    editor.apply()

                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                } else {
                    val intent: Intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("publisherid", user.getId())
                    mContext.startActivity(intent)
                }
            }

            holder.btnFollow?.setOnClickListener {
                Log.w(TAG, "Follow Button pressed");
                if (holder.btnFollow!!.text.toString() == "follow") {
                    Log.w(TAG, "holder.btn_follow!!.text.toString() == follow");
                    FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                        .child("following").child(user.getId()).setValue(true)
                    FirebaseDatabase.getInstance().reference.child("Follow").child(user.getId())
                        .child("followers").child(firebaseUser.uid).setValue(true)
                    addNotifications(user.getId())
                } else {
                    Log.w(TAG, "holder.btn_follow!!.text.toString() IS NOT follow")
                    FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                        .child("following").child(user.getId()).removeValue()
                    FirebaseDatabase.getInstance().reference.child("Follow").child(user.getId())
                        .child("followers").child(firebaseUser.uid).removeValue()
                }
            }
        } else {
            if(isConversation) {
                holder.lastMessage!!.visibility  = View.VISIBLE
                lastMessage(user.getId(), holder.lastMessage!!)
            } else {
                holder.lastMessage!!.visibility  = View.GONE
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(mContext, ConversationActivity::class.java)
                intent.putExtra("userid", user.getId())
                mContext.startActivity(intent)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView? = null
        var fullname: TextView? = null
        var lastMessage: TextView? = null
        var imageProfile: CircleImageView? = null
        var btnFollow: Button? = null

        init {
            username = itemView.findViewById(R.id.username)
            fullname = itemView.findViewById(R.id.fullname)
            lastMessage = itemView.findViewById(R.id.last_msg)
            imageProfile = itemView.findViewById(R.id.image_profile)
            btnFollow = itemView.findViewById(R.id.btn_follow)
            btnFollow = itemView.findViewById(R.id.btn_follow)
        }
    }

    private fun isFollowing(userid: String, button: Button) {
        val reference =
            FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid)
                .child("following")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(userid).exists()) {
                    button.text = "following"
                } else {
                    button.text = "follow"
                }
            }
        })
    }

    private fun lastMessage(userId: String, lastMsg: TextView) {
        theLastMessage = "default"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message!!.getReceiver() == firebaseUser!!.uid && message.getSender() == userId || message.getReceiver() == userId && message.getSender() == firebaseUser.uid) {
                        theLastMessage = message.getMessage()
                    }
                }
                when (theLastMessage) {
                    "default" -> {
                        lastMsg.text = "No Message"
                    }
                    else -> {
                        lastMsg.text = theLastMessage
                    }
                }
                theLastMessage = "default"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


}