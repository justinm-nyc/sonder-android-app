package com.android.sonder_app

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddStoryActivity : AppCompatActivity() {
    val TAG = "MyMessage:"

    private lateinit var mImageUri: Uri
    private var myUrl: String = ""
    private lateinit var storageTask: StorageTask<UploadTask.TaskSnapshot>
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        storageReference = FirebaseStorage.getInstance().getReference("Story")
        CropImage.activity().setAspectRatio(9,16).start(this)
    }


    private fun getFileExtension(uri: Uri): String? {
        Log.d(TAG, "getFileExtension Called");
        var contentResolver: ContentResolver = contentResolver
        var mime: MimeTypeMap  = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun publishStory() {
//        val progressBar: ProgressBar
//        progressBar = view.findViewById(R.id.progress_circular)
        if(mImageUri != null){
            val imageReference: StorageReference = storageReference.child(System.currentTimeMillis().toString() + "." + getFileExtension(mImageUri))
            storageTask = imageReference.putFile(mImageUri)
            storageTask.continueWithTask{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val myUrl = downloadUri.toString()
                    val myId: String = FirebaseAuth.getInstance().currentUser!!.uid
                    val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Story").child(myId)
                    val storyId: String = reference.push().key!!
                    val timeStart: Long = System.currentTimeMillis()
                    val timeEnd: Long = System.currentTimeMillis() + 86400000
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["imageurl"] = myUrl
                    hashMap["timestart"] = timeStart
                    hashMap["timeend"] = timeEnd
                    hashMap["storyid"] = storyId
                    hashMap["userid"] = myId
                    reference.child(storyId).setValue(hashMap)
                    finish()
//                    progressBar.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{e: Exception ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image Selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data);
            mImageUri = result.uri
            publishStory()
        } else {
            Toast.makeText(this, "something gone wrong!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}
