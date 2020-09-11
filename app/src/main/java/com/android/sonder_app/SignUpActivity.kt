package com.android.sonder_app

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.HashMap


class SignUpActivity : AppCompatActivity() {
    val TAG = "MyMessage:"

    private lateinit var fullname: EditText
    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var txtsignin: TextView
    private lateinit var showPasswordButton: ImageView
    private lateinit var showPasswordButton2: ImageView
    private lateinit var checkbox: CheckBox
    private lateinit var signup: Button
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

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
        showPasswordButton = findViewById(R.id.show_pass_btn)
        showPasswordButton2 = findViewById(R.id.show_pass_btn_2)
        confirmPassword = findViewById(R.id.confirm_password)
        checkbox = findViewById(R.id.checkbox_agree)
        txtsignin = findViewById(R.id.txt_signin)
        signup = findViewById(R.id.signup)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        toolbar = findViewById(R.id.toolbar)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    StartActivity::class.java
                )
            )
        }

        txtsignin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        showPasswordButton.setOnClickListener {
            if (password.transformationMethod == PasswordTransformationMethod.getInstance()) {
                showPasswordButton.setImageResource(R.drawable.ic_visible_eye)

                //Show Password
                password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                showPasswordButton.setImageResource(R.drawable.ic_visible_off_eye)

                //Hide Password
                password.transformationMethod = PasswordTransformationMethod.getInstance()

            }

        }

        showPasswordButton2.setOnClickListener {
            if (confirmPassword.transformationMethod == PasswordTransformationMethod.getInstance()) {
                showPasswordButton2.setImageResource(R.drawable.ic_visible_eye)

                //Show Password
                confirmPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                showPasswordButton2.setImageResource(R.drawable.ic_visible_off_eye)

                //Hide Password
                confirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            }

        }

        signup.setOnClickListener {

            progressBar.visibility = View.VISIBLE; // To show the ProgressBar

            val strUsername = username.text.toString()
            val strFullName = fullname.text.toString()
            val strEmail = email.text.toString()
            val strPassword = password.text.toString()
            val strConfirmPassword = confirmPassword.text.toString()



            if (TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strFullName) || TextUtils.isEmpty(
                    strEmail
                ) || TextUtils.isEmpty(strPassword) || TextUtils.isEmpty(strConfirmPassword)
            ) {
                Toast.makeText(this@SignUpActivity, "All Fields are Required!", Toast.LENGTH_SHORT)
                    .show()
                progressBar.visibility = View.GONE
            } else if (strConfirmPassword != strPassword) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Password fields must be the same!",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } else if (strPassword.length < 6) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Password must have 6 or more characters!",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } else if (!checkbox.isChecked) {
                Toast.makeText(
                    this@SignUpActivity,
                    "You must agree to the terms and conditions.",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } else {
                signUp(strUsername, strFullName, strEmail, strPassword)
            }
        }

    }

    private fun signUp(username: String, fullname: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    // Sign in success, update UI with the signed-in user's information )
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val userid: String = firebaseUser?.uid.toString()
                    reference =
                        FirebaseDatabase.getInstance().reference.child("Users").child(userid)

                    var hashMap: HashMap<String, Any> = HashMap()
                    hashMap["id"] = userid
                    hashMap["username"] = username.toLowerCase(Locale.ROOT)
                    hashMap["fullname"] = fullname
                    hashMap["bio"] = ""
                    hashMap["imageurl"] =
                        "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/placeholder.png?alt=media&token=407c5679-8e25-4cd5-adab-cdf85153b3c1"
                    reference.setValue(hashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressBar.visibility = View.GONE
                            val intent = Intent(this, OnboardingActivity::class.java)
                            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }

                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "You are not registered with this email and password!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }
}

