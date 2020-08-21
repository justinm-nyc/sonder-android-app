package Adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Fragment.PostDetailsFragment
import com.android.sonder_app.Fragment.ProfileFragment
import com.android.sonder_app.Model.Notification
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private val TAG = "MyMessage:"
    private var mContext: Context
    private var mNotification: List<Notification>

    constructor(mContext: Context, mNotification: List<Notification>) : super() {
        this.mContext = mContext
        this.mNotification = mNotification
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: ImageView = itemView.findViewById(R.id.image_profile)
        val postImage: ImageView = itemView.findViewById(R.id.image_post)
        val username: TextView = itemView.findViewById(R.id.username)
        val text: TextView = itemView.findViewById(R.id.comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification: Notification = mNotification[position]
        holder.text.text = notification.getText()

        getUserInfo(holder.imageProfile, holder.username, notification.getUserId())

        if(notification.isIsPost()) {
            Log.d(TAG, "IS POST")
            Log.d(TAG, "post id is " + notification.getPostId())
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.getPostId())

        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if(notification.isIsPost()) {
                var editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
                editor.putString("postid", notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()

            } else {
                var editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
                editor.putString("profileid", notification.getUserId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()

            }
        }
    }

    private fun getUserInfo(imageView: ImageView, username: TextView, publisherid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = snapshot.getValue(User::class.java)!!
                Glide.with(mContext).load(user.getImageurl()).into(imageView)
                username.text = user.getUsername()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getPostImage(imageView: ImageView, postid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post: Post = snapshot.getValue(Post::class.java)!!
                Glide.with(mContext).load(post.getPostimage()).into(imageView)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}