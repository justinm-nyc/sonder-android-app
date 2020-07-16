package Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.MainActivity
import com.android.sonder_app.Model.Comment
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private val TAG = "MyMessage:"

    private var mContext: Context
    private var mComment: ArrayList<Comment>
    private lateinit var firebaseUser: FirebaseUser

    constructor(mContext: Context, mComment: ArrayList<Comment>) : super() {
        this.mContext = mContext
        this.mComment = mComment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val comment: Comment = mComment[i]
        holder.comment!!.text = comment.getComment()

        getUserInfo(holder.imageProfile!!, holder.username!!, comment.getPublisher())

        holder.comment!!.setOnClickListener{
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra("publisherid",comment.getPublisher())
            mContext.startActivity(intent)
        }

        holder.imageProfile!!.setOnClickListener{
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra("publisherid", comment.getPublisher())
            mContext.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var comment: TextView? = itemView.findViewById(R.id.comment)
        var username: TextView? = itemView.findViewById(R.id.username)
        var imageProfile: ImageView? = itemView.findViewById(R.id.image_profile)
    }

    fun getUserInfo(imageView: ImageView, username: TextView, publisherid: String){
        Log.d(TAG, "getUserInfo called");
        var reference = FirebaseDatabase.getInstance().reference.child("Users").child(publisherid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var user: User? = dataSnapshot.getValue(User::class.java)
                Glide.with(mContext).load(user?.getImageurl()).into(imageView)
                username.text = user?.getUsername()
            }
        })
    }
}

