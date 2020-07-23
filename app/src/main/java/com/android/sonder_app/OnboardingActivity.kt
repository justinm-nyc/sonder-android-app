package com.android.sonder_app

import Adapter.SliderAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager

class OnboardingActivity : AppCompatActivity() {
    private lateinit var mSlideViewPager: ViewPager
    private lateinit var mDotLayout: LinearLayout
    private lateinit var sliderAdapter: SliderAdapter

    private lateinit var mDots: Array<TextView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        mSlideViewPager = findViewById(R.id.slideViewPager)
        mDotLayout = findViewById(R.id.dots)
        sliderAdapter = SliderAdapter(this)
        mSlideViewPager.adapter = sliderAdapter
        addDotsIndicator(0)
        mSlideViewPager.addOnPageChangeListener(viewListener)

    }

    fun addDotsIndicator(position: Int) {
        mDots = arrayOfNulls<TextView>(4)
        mDotLayout.removeAllViews()

        for (i in mDots.indices) run {
            mDots[i] = TextView(this)
            mDots[i]?.text = Html.fromHtml("&#8226;")
            mDots[i]?.textSize = 35F
            mDots[i]?.setTextColor(resources.getColor(R.color.colorGray))

            mDotLayout.addView(mDots[i])
        }

        if (mDots.size > 0) {
            when (position) {
                0, 3 -> {
                    mDots[position]?.setTextColor(resources.getColor(R.color.colorBlue))
                }
                1, 2 -> {
                    mDots[position]?.setTextColor(resources.getColor(R.color.colorPurple))
                }
            }

        }
    }


    private val viewListener = object :
        ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            addDotsIndicator(position)
        }
    }


}



