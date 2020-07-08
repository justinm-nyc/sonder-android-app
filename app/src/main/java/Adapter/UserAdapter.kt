package Adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Fragment.ProfileFragment
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

class UserAdapter: RecyclerView.Adapter<UserAdapter.ViewHolder> {
    val TAG = "MyMessage:"

    private var mContext: Context
    private var mUser: List<User>

    private lateinit var firebaseUser: FirebaseUser

    constructor(mContext: Context, mUser: List<User>) : super() {
        this.mContext = mContext
        this.mUser = mUser
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val user: User = mUser[position]
        holder.btn_follow?.visibility = View.VISIBLE
        holder.username?.text = user.getUsername()
        holder.fullname?.text =  user.getFullname()
        Glide.with(mContext).load(user.getImageurl()).into(holder.image_profile!!)

        isFollowing(user.getId(), holder.btn_follow!!)

        if (user.getId() == firebaseUser.uid) {
            holder.btn_follow!!.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit()
            editor.putString("profileid", user.getId())
            editor.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.btn_follow?.setOnClickListener {
            Log.w(TAG, "Follow Button pressed");
            if(holder.btn_follow!!.text.toString() == "follow"){
                Log.w(TAG, "holder.btn_follow!!.text.toString() == follow");
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("following").child(user.getId()).setValue(true);
                FirebaseDatabase.getInstance().reference.child("Follow").child(user.getId()).child("followers").child(firebaseUser.uid).setValue(true);
            } else {
                Log.w(TAG, "holder.btn_follow!!.text.toString() IS NOT follow");
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("following").child(user.getId()).removeValue();
                FirebaseDatabase.getInstance().reference.child("Follow").child(user.getId()).child("followers").child(firebaseUser.uid).removeValue();
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var username: TextView? = null
        var fullname: TextView? = null
        var image_profile: CircleImageView? = null
        var btn_follow: Button? = null

        init{
            username = itemView.findViewById(R.id.username)
            fullname = itemView.findViewById(R.id.fullname)
            image_profile = itemView.findViewById(R.id.image_profile)
            btn_follow = itemView.findViewById(R.id.btn_follow)
        }
    }

    fun isFollowing(userid: String, button: Button){
        var reference = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("following")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(userid).exists()) {
                    button.text = "following"
                }else {
                    button.text = "follow"
                }
            }
        })
    }



}