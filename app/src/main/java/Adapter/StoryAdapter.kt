package Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.AddStoryActivity
import com.android.sonder_app.Model.Story
import com.android.sonder_app.Model.User
import com.android.sonder_app.R
import com.android.sonder_app.StoryActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class StoryAdapter : RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private val TAG = "MyMessage:"
    private var mContext: Context
    private var mStory: ArrayList<Story>

    constructor(mContext: Context, mStory: ArrayList<Story>) : super() {
        this.mContext = mContext
        this.mStory = mStory
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var storyPhoto: ImageView = itemView.findViewById(R.id.story_photo)
        var storyPhotoSeen: ImageView? = itemView.findViewById(R.id.story_photo_seen)
        var storyPlus: ImageView? = itemView.findViewById(R.id.story_plus)
        var storyUsername: TextView? = itemView.findViewById(R.id.story_username)
        var addStoryText: TextView? = itemView.findViewById(R.id.addstory_text)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        if (i == 0) {
            Log.d(TAG, "i = 0")
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.add_story_item, viewGroup, false)
            return ViewHolder(view)
        } else {
            Log.d(TAG, "i != 0")
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.story_item, viewGroup, false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story: Story = mStory[position]
        userInfo(holder, story.getUserId(), position)

        if (holder.adapterPosition != 0) {
            seenStory(holder, story.getUserId())
        }

        if (holder.adapterPosition == 0) {
            myStory(holder.addStoryText!!, holder.storyPlus!!, false)
        }

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == 0) {
                myStory(holder.addStoryText!!, holder.storyPlus!!, true)
            } else {
                val intent = Intent(mContext, StoryActivity::class.java)
                intent.putExtra("userid", story.getUserId())
                mContext.startActivity(intent)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, pos: Int) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User = dataSnapshot.getValue(User::class.java)!!
                Glide.with(mContext).load(user.getImageurl()).into(viewHolder.storyPhoto)
                if (pos != 0) {
                    Glide.with(mContext).load(user.getImageurl()).into(viewHolder.storyPhotoSeen!!)
                    viewHolder.storyUsername?.text = user.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun myStory(textView: TextView, imageView: ImageView, click: Boolean) {
        val currentUserId: String = FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Story").child(currentUserId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0
                val timeCurrent: Long = System.currentTimeMillis()
                for (snapshot in dataSnapshot.children) {
                    val story: Story = snapshot.getValue(Story::class.java)!!
                    if (timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        count++
                    }
                }
                if (click) {
                    if (count > 0) {
                        val alertDialog: AlertDialog = AlertDialog.Builder(mContext).create()
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEGATIVE,
                            "View story"
                        ) { dialogInterface: DialogInterface, i: Int ->
                            val intent = Intent(mContext, StoryActivity::class.java)
                            intent.putExtra(
                                "userid",
                                FirebaseAuth.getInstance().currentUser!!.uid
                            )
                            mContext.startActivity(intent)
                            dialogInterface.dismiss()
                        }

                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Add story"
                        ) { dialogInterface: DialogInterface, i: Int ->
                            val intent = Intent(mContext, AddStoryActivity::class.java)
                            mContext.startActivity(intent)
                            dialogInterface.dismiss()
                        }
                        alertDialog.show()
                    } else {
                        val intent = Intent(mContext, AddStoryActivity::class.java)
                        mContext.startActivity(intent)
                    }
                } else {
                    if (count > 0) {
                        textView.text = "My Story"
                        imageView.visibility = View.GONE
                    } else {
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun seenStory(viewHolder: ViewHolder, userId: String) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Story").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var i: Int = 0
                for (snapshot in dataSnapshot.children) {
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .exists() &&
                        System.currentTimeMillis() < snapshot.getValue(Story::class.java)!!
                            .getTimeEnd()
                    ) {
                        i++
                    }
                }
                if (i > 0) {
                    viewHolder.storyPhoto.visibility = View.VISIBLE
                    viewHolder.storyPhotoSeen?.visibility = View.GONE
                } else {
                    viewHolder.storyPhoto.visibility = View.GONE
                    viewHolder.storyPhotoSeen?.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}