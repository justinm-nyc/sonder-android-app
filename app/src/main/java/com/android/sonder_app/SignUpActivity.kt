package com.android.sonder_app

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignUpActivity : AppCompatActivity() {
    val TAG = "MyMessage:"

    private lateinit var fullname: EditText
    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var txt_signin: TextView
    private lateinit var signup: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fullname = findViewById(R.id.fullname)
        email = findViewById(R.id.email)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        txt_signin = findViewById(R.id.txt_signin)
        signup = findViewById(R.id.signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        txt_signin.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        signup.setOnClickListener {
            progressBar = ProgressBar(
                this@SignUpActivity,
                null,
                android.R.attr.progressBarStyleHorizontal
            )
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE; // To show the ProgressBar

            val strUsername = username.text.toString()
            val strFullName = fullname.text.toString()
            val strEmail = email.text.toString()
            val strPassword = password.text.toString()

            if(TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strFullName) || TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                Toast.makeText(this@SignUpActivity, "All Fields are Required!", Toast.LENGTH_SHORT).show()
            } else if (strPassword.length < 6) {
                Toast.makeText(this@SignUpActivity, "Password must have 6 characters!", Toast.LENGTH_SHORT).show()
            } else {
               signUp(strUsername, strFullName, strEmail, strPassword)
            }
        }

    }

    public fun signUp(username: String, fullname: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success");
                    // Sign in success, update UI with the signed-in user's information )
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val userid: String = firebaseUser?.uid.toString()
                    reference = FirebaseDatabase.getInstance().reference.child("Users").child(userid)

                    var hashMap : HashMap<String, Any> = HashMap<String, Any> ()
                    hashMap["id"] = userid
                    hashMap["username"] = username.toLowerCase()
                    hashMap["fullname"] = fullname
                    hashMap["bio"] = ""
                    hashMap["imageurl"] = "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/placeholder.png?alt=media&token=407c5679-8e25-4cd5-adab-cdf85153b3c1"
                    reference.setValue(hashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            progressBar.visibility = View.INVISIBLE
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }

                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "You are not registered with this email and password!",
                        Toast.LENGTH_SHORT).show()
                }

            }
    }
}

