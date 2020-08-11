package Adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Fragment.PostDetailsFragment
import com.android.sonder_app.Model.Post
import com.android.sonder_app.R
import com.bumptech.glide.Glide

class PhotoGridAdapter: RecyclerView.Adapter<PhotoGridAdapter.ViewHolder> {
    private var mContext: Context
    private var mPosts: List<Post>

    constructor(mContext: Context, mPosts: List<Post>) : super() {
        this.mContext = mContext
        this.mPosts = mPosts
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.photos_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPosts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPosts[position]
        Glide.with(mContext).load(post.getPostimage()).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor: SharedPreferences.Editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit()
            editor.putString("postid", post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }
    }

}