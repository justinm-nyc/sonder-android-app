package Adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.viewpager.widget.PagerAdapter
import com.android.sonder_app.R
import com.google.firebase.database.collection.LLRBNode

class SliderAdapter: PagerAdapter {
    private val TAG = "MyMessage:"
    private var context: Context
    private lateinit var layoutInflator: LayoutInflater

    constructor(context: Context) : super() {
        Log.d(TAG, "sliderAdapter constructor called")
        this.context = context
    }

    var slide_videos = arrayOf(
        "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/1_discover.mp4?alt=media&token=5653bf20-9c89-4529-aa2b-97513e536acd",
        "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/2_connect_friends.mp4?alt=media&token=9a96b051-a97c-4f27-9415-286de1802595",
        "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/3_plan_your_trip.mp4?alt=media&token=cdbbc36c-3b2d-42d1-9ca4-8da33fe7711a",
        "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/4_share_your_experiences.mp4?alt=media&token=3a6388c8-cc13-48f8-a20b-038de7ce6a84"
    )

    var slide_headings:Array<String> = arrayOf("DISCOVER", "CONNECT WITH FRIENDS", "PLAN YOUR TRIP", "SHARE EXPERIENCES")

    var slide_desc:Array<String> = arrayOf("Get inspired and explore fascination destinations with Travilous",
        "Connect with old friends or make new ones, find travel buddies for your upcoming trips",
        "Create unique itineraries with recommendations from your travel community",
        "Blog, rate, and share your travels all in one place"
    )


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        Log.d(TAG, "isViewFromObject called")
        return view === `object`
    }

    override fun getCount(): Int {
        Log.d(TAG, "getCount called")
        return slide_headings.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {
        Log.d(TAG, "instantiateItem called")
        layoutInflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view: View = layoutInflator.inflate(R.layout.slide_layout, container, false)

        var slideVideoView: VideoView = view.findViewById(R.id.slide_video)
        var slideHeading: TextView = view.findViewById(R.id.slide_heading)
        var slideDescription: TextView = view.findViewById(R.id.slide_description)

        slideVideoView.setVideoURI(Uri.parse(slide_videos[position]))
        slideVideoView.start();
        slideHeading.text = slide_headings[position]
        when (position) {
            0, 3 -> {
                slideHeading.setTextColor(ContextCompat.getColor(this.context, R.color.colorBlue))
            }
            1, 2 -> {
                slideHeading.setTextColor(ContextCompat.getColor(this.context, R.color.colorPurple))

            }
        }
        slideDescription.text = slide_desc[position]

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any){
        Log.d(TAG, "destroyItem called")
        container.removeView(`object` as View)
    }


}