package com.android.sonder_app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.android.sonder_app.Fragment.HomeFragment
import com.android.sonder_app.Fragment.NotificationFragment
import com.android.sonder_app.Fragment.ProfileFragment
import com.android.sonder_app.Fragment.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    val TAG = "MyMessage:"

    private lateinit var bottomNavigationView: BottomNavigationView
    private var selectFragment: Fragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home ->
                selectFragment = HomeFragment();
            R.id.nav_search ->
                selectFragment = SearchFragment();
            R.id.nav_add ->
                startActivity(Intent(this@MainActivity, PostActivity::class.java))
            R.id.nav_heart ->
                selectFragment = NotificationFragment();
            R.id.nav_profile -> {
                val sharedPreference = getSharedPreferences("PREPS", MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putString("profileid", FirebaseAuth.getInstance().currentUser?.uid)
                editor.apply()
                selectFragment = ProfileFragment();
            }
        }

        if(selectFragment != null){
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                selectFragment!!
            ).commit()
        }

        return true
    }
}
