package com.android.sonder_app

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_post.*

class PostActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var imageUrl: String
    private lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>
    private lateinit var storageReference: StorageReference
    private lateinit var description: EditText
    private lateinit var imageAdded: ImageView
    private lateinit var close: ImageView
    private lateinit var post: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        close = findViewById(R.id.close)
        description = findViewById(R.id.description)
        imageAdded = findViewById(R.id.image_added)
        post = findViewById(R.id.post)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        storageReference = FirebaseStorage.getInstance().getReference("posts")
        close.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        post.setOnClickListener{
            uploadImage()
        }

        CropImage.activity().setAspectRatio(1,1).start(this)
    }

    private fun getFileExtension(uri: Uri): String? {
        var contentResolver: ContentResolver = contentResolver
        var mime: MimeTypeMap  = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadImage() {
        progressBar.visibility = View.VISIBLE; // To show the ProgressBar

        if(imageUri != null){
            var referenceFile: StorageReference = storageReference.child(System.currentTimeMillis().toString() + "." + getFileExtension(imageUri))
            uploadTask = referenceFile.putFile(imageUri)

            uploadTask.continueWithTask{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                referenceFile.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    imageUrl = downloadUri.toString()
                    val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("posts")
                    var postid: String = reference.push().key!!
                    var hashMap: HashMap<String, String>  = HashMap<String, String>()
                    hashMap["postid"] = postid
                    hashMap["postimage"] = imageUrl
                    hashMap["description"] = description.text.toString()
                    hashMap["publisher"] = FirebaseAuth.getInstance().currentUser?.uid!!

                    reference.child(postid).setValue(hashMap)
                    progressBar.visibility = View.GONE

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{e: Exception ->
                Toast.makeText(this, ""+e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
        }
    }


override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            var result: CropImage.ActivityResult = CropImage.getActivityResult(data);

            imageUri = result.uri;
            image_added.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Something has gone wrong!", Toast.LENGTH_SHORT ).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}
