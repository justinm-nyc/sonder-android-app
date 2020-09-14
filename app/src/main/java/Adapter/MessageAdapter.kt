package Adapter

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.Message
import com.android.sonder_app.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    val TAG = "MyMessage:"

    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1

    private var mContext: Context
    private var mMessage: List<Message>
    private var imageurl: String
    private lateinit var firebaseUser: FirebaseUser

    constructor(mContext: Context, mMessage: List<Message>, imageurl: String) : super() {
        this.mContext = mContext
        this.mMessage = mMessage
        this.imageurl = imageurl
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return return if(viewType == MSG_TYPE_RIGHT) {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false)
            ViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message = mMessage[position]
        holder.showMessage.text = message.getMessage()
        Glide.with(mContext).load(imageurl).into(holder.imageProfile)
        if(position == mMessage.size -1){
            if(message.isIsSeen()){
                holder.textSeen.text = "Seen"
            } else {
                holder.textSeen.text = "Delivered"
            }
        } else {
            holder.textSeen.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return mMessage.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var showMessage: TextView = itemView.findViewById(R.id.show_message)
        var imageProfile: CircleImageView = itemView.findViewById(R.id.image_profile)
        var textSeen: TextView = itemView.findViewById(R.id.text_seen)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        if(mMessage[position].getSender() == firebaseUser.uid) {
            return MSG_TYPE_RIGHT
        } else {
            return MSG_TYPE_LEFT
        }
    }

}