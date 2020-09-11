package com.android.sonder_app

import Adapter.ViewPagerAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import com.android.sonder_app.Fragment.ChatFragment
import com.android.sonder_app.Fragment.UserFragment
import com.google.android.material.tabs.TabLayout

class MessageActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        backButton = findViewById(R.id.back_button)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        val viewPageAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPageAdapter.addFragment(ChatFragment(), "Chats")
        viewPageAdapter.addFragment(UserFragment(), "Users")
        viewPager.adapter = viewPageAdapter

        tabLayout.setupWithViewPager(viewPager)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
