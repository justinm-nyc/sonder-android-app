package Adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.CommentsActivity
import com.android.sonder_app.FollowersActivity
import com.android.sonder_app.Fragment.ProfileFragment
import com.android.sonder_app.Model.Post
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

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
        val post: Post = mPost[position]

        Glide.with(mContext).load(post.getPostimage()).into(holder.postImage)

        if (post.getLink() != "") {
            holder.readMore.visibility = View.VISIBLE
            holder.readMore.setOnClickListener {
                Log.d(TAG, "readMore was clicked")
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(post.getLink())
                mContext.startActivity(intent)
            }
        }


        if (post.getDescription() == "") {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }

        holder.location.text = post.getLocation()
        holder.ratingBar.rating = post.getRating()
        holder.price.rating = post.getPricing()

        publisherInfo(holder.imageProfile, holder.username, post.getPublisher())

        isLiked(post.getPostid(), holder.like)
        numLikes(holder.likes, post.getPostid())
        getComments(post.getPostid(), holder.comments)
        isSaved(post.getPostid(), holder.save)


        holder.comment.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid", post.getPostid())
            intent.putExtra("publisherid", post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.save.setOnClickListener {
            Log.d(TAG, "save was clicked")
            if (holder.save.tag == "save") {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
                    .child(post.getPostid()).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
                    .child(post.getPostid()).removeValue()
            }
        }

        if(post.getPublisher() == FirebaseAuth.getInstance().currentUser!!.uid) {
            holder.options.setOnClickListener {
                Log.d(TAG, "OPTIONS was clicked")
                val popup = PopupMenu(mContext, holder.options)
                popup.inflate(R.menu.post_menu)
                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        when (item!!.itemId) {
                            R.id.post_edit -> {
                                Toast.makeText(mContext, "Edit clicked", Toast.LENGTH_SHORT)
                                    .show()
                                return true
                            }
                            R.id.post_delete -> {
                                Toast.makeText(mContext, "Delete clicked", Toast.LENGTH_SHORT)
                                    .show()
                                deletePost(post.getPostid())
                                return true
                            }
                        }
                        return false
                    }

                })
                popup.show()

            }
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid", post.getPostid())
            intent.putExtra("publisherid", post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.imageProfile.setOnClickListener {
            val editor: SharedPreferences.Editor =
                mContext.getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }


        holder.username.setOnClickListener {
            val editor: SharedPreferences.Editor =
                mContext.getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.likes.setOnClickListener {
            val intent  = Intent(mContext, FollowersActivity::class.java)
            intent.putExtra("id", post.getPostid())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.like.setOnClickListener {
            if (holder.like.tag == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid())
                    .child(firebaseUser.uid).setValue(true)
                addNotifications(post.getPublisher(), post.getPostid())
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
        var options: ImageView = itemView.findViewById(R.id.options)

        var ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        var price: io.techery.properratingbar.ProperRatingBar = itemView.findViewById(R.id.priceBar)
        var readMore: TextView = itemView.findViewById(R.id.read_more)
        var username: TextView = itemView.findViewById(R.id.username)
        var location: TextView = itemView.findViewById(R.id.location)
        var likes: TextView = itemView.findViewById(R.id.likes)
        var comments: TextView = itemView.findViewById(R.id.comments)
        var description: TextView = itemView.findViewById(R.id.description)
    }

    private fun getComments(postid: String, comments: TextView) {
        val reference = FirebaseDatabase.getInstance().reference.child("Comments").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                comments.text = dataSnapshot.childrenCount.toString()
            }
        })
    }

    private fun isLiked(postid: String, imageView: ImageView) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val reference = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(firebaseUser.uid).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked)
                    imageView.tag = "liked"
                } else {
                    imageView.setImageResource(R.drawable.ic_like)
                    imageView.tag = "like"
                }
            }
        })
    }

    private fun addNotifications(userid: String, postid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid)
        val hashMap: HashMap<String, Any> = HashMap<String, Any>()
        hashMap["userid"] = firebaseUser.uid
        hashMap["text"] = "liked your post"
        hashMap["postid"] = postid
        hashMap["ispost"] = true
        reference.push().setValue(hashMap)
    }

    private fun numLikes(likes: TextView, postid: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                likes.text = dataSnapshot.childrenCount.toString()
            }
        })
    }

    private fun publisherInfo(image_profile: ImageView, username: TextView, userid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User = dataSnapshot.getValue(User::class.java)!!
                Glide.with(mContext).load(user.getImageurl()).into(image_profile)
                username.text = user.getUsername()
            }
        })
    }

    private fun isSaved(postId: String, imageView: ImageView) {
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val reference =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.ic_save_blue)
                    imageView.tag = "saved"
                } else {
                    imageView.setImageResource(R.drawable.ic_save)
                    imageView.tag = "save"

                }
            }
        })
    }

    fun deletePost(postid: String) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postid).removeValue()
        FirebaseDatabase.getInstance().reference.child("Comments").child(postid).removeValue()
        FirebaseDatabase.getInstance().reference.child("Posts").child(postid).removeValue()
        FirebaseDatabase.getInstance().reference.child("Saves").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot1 in dataSnapshot.children){
                    for(snapshot2 in snapshot1.children){
                        if (snapshot2.key == postid) {
                            snapshot2.ref.removeValue()
                        }
                    }
                }
            }
        })
        FirebaseDatabase.getInstance().reference.child("Notifications").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "dataSnapshot is $dataSnapshot")
                for(snapshot1 in dataSnapshot.children){
                    Log.d(TAG, "snapshot1 is $snapshot1")
                    for(snapshot2 in snapshot1.children){
                        Log.d(TAG, "snapshot2 is $snapshot2")
                        if (snapshot2.child("postid").value == postid) {
                            snapshot2.ref.removeValue()
                        }
                    }
                }
            }

        })

    }



}