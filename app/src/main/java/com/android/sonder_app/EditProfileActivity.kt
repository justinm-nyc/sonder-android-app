package com.android.sonder_app

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.android.sonder_app.Model.User
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.rengwuxian.materialedittext.MaterialEditText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class EditProfileActivity : AppCompatActivity() {

    private lateinit var close: ImageView
    private lateinit var bio: MaterialEditText
    private lateinit var fullname: MaterialEditText
    private lateinit var username: MaterialEditText
    private lateinit var imageProfile: ImageView
    private lateinit var tvChange: TextView
    private lateinit var save: TextView
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var mImageUri: Uri
    private lateinit var uploadtask:  StorageTask<UploadTask.TaskSnapshot>
    private lateinit var storageRef: StorageReference
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        close = findViewById(R.id.close)
        imageProfile = findViewById(R.id.image_profile)
        fullname = findViewById(R.id.fullname)
        username = findViewById(R.id.username)
        bio = findViewById(R.id.bio)
        save = findViewById(R.id.save)
        tvChange = findViewById(R.id.tv_change)
        progressBar = findViewById(R.id.progress_bar)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance().getReference("uploads")
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User = dataSnapshot.getValue(User::class.java)!!
                fullname.setText(user.getFullname())
                username.setText(user.getUsername())
                bio.setText(user.getBio())
                Glide.with(applicationContext).load(user.getImageurl()).into(imageProfile)
            }
        })

        close.setOnClickListener {
            finish()
        }

        tvChange.setOnClickListener {
            CropImage.activity().setAspectRatio(1,1)
                .setCropShape(CropImageView.CropShape.OVAL).start(this@EditProfileActivity)
        }

        imageProfile.setOnClickListener {
            CropImage.activity().setAspectRatio(1,1)
                .setCropShape(CropImageView.CropShape.OVAL).start(this@EditProfileActivity)
        }

        save.setOnClickListener{
            updateProfile(fullname.text.toString(),username.text.toString(), bio.text.toString())
            finish()
        }
    }

    private fun updateProfile(fullname: String, username: String, bio: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
        val hashMap: HashMap<String, String> = HashMap<String, String>()
        hashMap["fullname"] = fullname
        hashMap["username"] = username
        hashMap["bio"] = bio
        reference.updateChildren(hashMap as Map<String, Any>)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver: ContentResolver = contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadImage() {
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE

        if(mImageUri != null) {
            val fileReference: StorageReference = storageRef.child(System.currentTimeMillis().toString()+"."+getFileExtension(mImageUri))
            uploadtask = fileReference.putFile(mImageUri)
            uploadtask.continueWithTask{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val myUrl = downloadUri.toString()
                    val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid())

                    val hashMap: HashMap<String, String> = HashMap<String, String>()
                    hashMap["imageurl"] = myUrl
                    reference.updateChildren(hashMap as Map<String, Any>)
                    progressBar.visibility = View.GONE

                } else {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{e: Exception ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            var result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            mImageUri = result.uri
            uploadImage()
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show()
        }

    }

}
