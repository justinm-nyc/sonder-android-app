package com.android.sonder_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class StartActivity : AppCompatActivity() {
    private val TAG = "MyMessage:"

    private lateinit var signin: Button
    private lateinit var signup: Button
    private var firebaseUser: FirebaseUser? = null

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //redirect if user is already logged in
        if(firebaseUser != null){
            Log.d(TAG, "Already signed in");
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        signin = findViewById(R.id.signin)
        signup = findViewById(R.id.signup)

        signin.setOnClickListener{
            val intent = Intent(this@StartActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        signup.setOnClickListener{
            val intent = Intent(this@StartActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
