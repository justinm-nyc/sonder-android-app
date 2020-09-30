package com.android.sonder_app

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class StartActivity : AppCompatActivity() {
    private val TAG = "MyMessage:"
    private val RC_SIGN_IN: Int = 100
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var signin: Button
    private lateinit var signup: Button
    private lateinit var googlesignin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private var firebaseUser: FirebaseUser? = null

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //redirect if user is already logged in
        if (firebaseUser != null) {
            Log.d(TAG, "Already signed in");
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

//      Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        signin = findViewById(R.id.signin)
        signup = findViewById(R.id.signup)
        googlesignin = findViewById(R.id.googleconnect)

        signin.setOnClickListener {
            val intent = Intent(this@StartActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        signup.setOnClickListener {
            val intent = Intent(this@StartActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        googlesignin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val userid: String = firebaseUser?.uid.toString()
                    reference =
                        FirebaseDatabase.getInstance().reference.child("Users").child(userid)
                    reference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(TAG, "$snapshot")
                            // Add user to database if not already a user
                            if (snapshot.value == null) {
                                Log.d(TAG, "user not in db")
                                addUserToDB(userid)
                            } else {
                                Log.d(TAG, "user is in db")
                                //Start the MainActivity if user is already in database
                                val intent = Intent(this@StartActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@StartActivity, "Authentication Failed.", Toast.LENGTH_SHORT)
                        .show()

                }

            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@StartActivity,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addUserToDB(userid: String) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["id"] = userid
        hashMap["username"] = ""
        hashMap["fullname"] = ""
        hashMap["bio"] = ""
        hashMap["imageurl"] =
            "https://firebasestorage.googleapis.com/v0/b/sonderapp-43ab9.appspot.com/o/placeholder.png?alt=media&token=407c5679-8e25-4cd5-adab-cdf85153b3c1"
        reference.setValue(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

    }
}
