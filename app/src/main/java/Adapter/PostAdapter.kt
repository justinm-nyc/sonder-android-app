package Adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.CommentsActivity
import com.android.sonder_app.Fragment.PostDetailsFragment
import com.android.sonder_app.Fragment.ProfileFragment
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PostAdapter : RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private val TAG = "MyMessage:"
    private var mContext: Context
    private var mPost: List<Post>
    private lateinit var firebaseUser: FirebaseUser

    constructor(mContext: Context, mPost: List<Post>) : super() {
        this.mContext = mContext
        this.mPost = mPost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        var post: Post = mPost[position]

        Glide.with(mContext).load(post.getPostimage()).into(holder.postImage)

        if (post.getDescription() == "") {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }

        publisherInfo(holder.imageProfile, holder.username, holder.publisher, post.getPublisher())

        isLiked(post.getPostid(), holder.like)
        numLikes(holder.likes, post.getPostid())
        getComments(post.getPostid(), holder.comments)
        isSaved(post.getPostid() ,holder.save)

        holder.comment.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid",post.getPostid())
            intent.putExtra("publisherid", post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.save.setOnClickListener {
            Log.d(TAG, "save was clicked")
            if(holder.save.tag == "save"){
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid).child(post.getPostid()).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid).child(post.getPostid()).removeValue()
            }
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid",post.getPostid())
            intent.putExtra("publisherid", post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.imageProfile.setOnClickListener {
            var editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.publisher.setOnClickListener {
            var editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.postImage.setOnClickListener {
            var editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
            editor.putString("postid", post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }

        holder.username.setOnClickListener {
            var editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.like.setOnClickListener {
            if (holder.like.tag == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid())
                    .child(firebaseUser.uid).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid())
                    .child(firebaseUser.uid).removeValue()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: ImageView = itemView.findViewById(R.id.image_profile)
        var postImage: ImageView = itemView.findViewById(R.id.post_image)
        var like: ImageView = itemView.findViewById(R.id.like)
        var comment: ImageView = itemView.findViewById(R.id.comment)
        var save: ImageView = itemView.findViewById(R.id.save)

        var username: TextView = itemView.findViewById(R.id.username)
        var likes: TextView = itemView.findViewById(R.id.likes)
        var comments: TextView = itemView.findViewById(R.id.comments)
        var description: TextView = itemView.findViewById(R.id.description)
        var publisher: TextView = itemView.findViewById(R.id.publisher)
    }

    private fun getComments(postid: String, comments: TextView){
        var reference = FirebaseDatabase.getInstance().reference.child("Comments").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                comments.text = "View All " + dataSnapshot.childrenCount + " Comments"
            }
        })
    }

    private fun isLiked(postid: String, imageView: ImageView) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        var reference = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(firebaseUser.uid).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked)
                    imageView.setTag("liked")
                } else {
                    imageView.setImageResource(R.drawable.ic_like)
                    imageView.setTag("like")
                }
            }
        })
    }

    private fun numLikes(likes: TextView, postid: String) {
        var reference = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                likes.text = dataSnapshot.childrenCount.toString() + " likes"
            }
        })
    }

    private fun publisherInfo(
        image_profile: ImageView,
        username: TextView,
        publisher: TextView,
        userid: String
    ) {
        var reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var user: User = dataSnapshot.getValue(User::class.java)!!
                Glide.with(mContext).load(user.getImageurl()).into(image_profile)
                username.text = user.getUsername()
                publisher.text = user.getUsername()
            }
        })
    }

    fun isSaved(postId: String, imageView: ImageView){
        var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        var reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())

        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(postId).exists()){
                    imageView.setImageResource(R.drawable.ic_save_black)
                    imageView.tag = "saved"
                } else {
                    imageView.setImageResource(R.drawable.ic_save)
                    imageView.tag = "save"

                }
            }
        })
    }
}