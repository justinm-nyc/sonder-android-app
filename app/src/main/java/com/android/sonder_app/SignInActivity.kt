package com.android.sonder_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class SignInActivity : AppCompatActivity() {
    val TAG = "MyMessage:"

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var txt_signup: TextView
    private lateinit var signin: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        txt_signup = findViewById(R.id.txt_signup)
        signin = findViewById(R.id.signin)
        progressBar = findViewById(R.id.progress_bar)

        auth = FirebaseAuth.getInstance()

        txt_signup.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        signin.setOnClickListener {
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE; // To show the ProgressBar

            val strEmail = email.text.toString()
            val strPassword = password.text.toString()

            if(TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                Toast.makeText(this@SignInActivity, "All Fields are Required!", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(strEmail, strPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmailAndPassword:success");
                            // Sign in success, update UI with the signed-in user's information )
                            val firebaseUser: FirebaseUser? = auth.currentUser
                            val userid: String = firebaseUser?.uid.toString()
                            reference = FirebaseDatabase.getInstance().reference.child("Users")
                                .child(userid)

                            val menuListener = object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    progressBar.visibility = View.GONE;
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    progressBar.visibility = View.GONE;
                                    val intent =
                                        Intent(this@SignInActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            reference.addValueEventListener(menuListener)

                        } else {
                            Log.d(TAG, "signInWithEmailAndPassword:failure", task.exception);
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext, "Authentication Failed!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }
            }
        }
    }
}




