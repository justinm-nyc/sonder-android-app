package com.android.sonder_app

import Adapter.SliderAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager

class OnboardingActivity : AppCompatActivity() {
    private lateinit var mSlideViewPager: ViewPager
    private lateinit var mDotLayout: LinearLayout
    private lateinit var sliderAdapter: SliderAdapter

    private lateinit var mBackBtn: Button
    private lateinit var mNextBtn: Button
    private lateinit var mDots: Array<TextView?>

    private var mCurrentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        mSlideViewPager = findViewById(R.id.slideViewPager)
        mDotLayout = findViewById(R.id.dots)
        mBackBtn = findViewById(R.id.prvBtn)
        mNextBtn = findViewById(R.id.nxtBtn)

        sliderAdapter = SliderAdapter(this)
        mSlideViewPager.adapter = sliderAdapter
        addDotsIndicator(0)
        mSlideViewPager.addOnPageChangeListener(viewListener)

        mNextBtn.setOnClickListener{
            mSlideViewPager.setCurrentItem(mCurrentPage + 1)
        }

        mBackBtn.setOnClickListener{
            mSlideViewPager.setCurrentItem(mCurrentPage - 1)
        }
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
            mCurrentPage = position

            if( position == 0){
                mNextBtn.isEnabled = true
                mBackBtn.isEnabled = false
                mBackBtn.visibility = View.INVISIBLE

                mNextBtn.text = resources.getString(R.string.next)

                mNextBtn.setTextColor(resources.getColor(R.color.colorBlue))
                mBackBtn.setTextColor(resources.getColor(R.color.colorBlue))

                mBackBtn.text = ""
            } else if ( position == mDots.size-1) {
                mNextBtn.isEnabled = true
                mBackBtn.isEnabled = true
                mBackBtn.visibility = View.VISIBLE

                mNextBtn.setTextColor(resources.getColor(R.color.colorBlue))
                mBackBtn.setTextColor(resources.getColor(R.color.colorBlue))

                mNextBtn.text = resources.getString(R.string.finish)
                mBackBtn.text =  resources.getString(R.string.back)
            } else {
                mNextBtn.isEnabled = true
                mBackBtn.isEnabled = true
                mBackBtn.visibility = View.VISIBLE

                mNextBtn.text = resources.getString(R.string.next)
                mBackBtn.text =  resources.getString(R.string.back)

                mNextBtn.setTextColor(resources.getColor(R.color.colorPurple))
                mBackBtn.setTextColor(resources.getColor(R.color.colorPurple))
            }
        }
    }


}



