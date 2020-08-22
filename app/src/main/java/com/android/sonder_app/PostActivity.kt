package com.android.sonder_app

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_post.*
import java.io.IOException

class PostActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MyMessage:"

    private lateinit var imageUri: Uri
    private lateinit var imageUrl: String
    private lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>
    private lateinit var storageReference: StorageReference
    private lateinit var taggedList: HashMap<String, Boolean>
    private lateinit var link: EditText
    private lateinit var location: TextView
    private lateinit var description: EditText

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var searchLocationView: SearchView
    private lateinit var selectLocationView: RelativeLayout
    private lateinit var selectLocationButton: Button

    private lateinit var ratingBar: RatingBar
    private lateinit var priceBar: RatingBar
    private lateinit var categorySpinner: Spinner
    private lateinit var subCategorySpinner: Spinner
    private lateinit var selectedLocation: String
    private lateinit var selectedCategory: String
    private lateinit var selectedSubCategory: String
    private lateinit var imageAdded: ImageView
    private lateinit var close: ImageView
    private lateinit var doneButton: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        close = findViewById(R.id.close)
        link = findViewById(R.id.link)
        location = findViewById(R.id.location)
        description = findViewById(R.id.description)
        categorySpinner = findViewById(R.id.select_category)
        subCategorySpinner = findViewById(R.id.select_sub_category)
        ratingBar = findViewById(R.id.ratingBar)
        priceBar = findViewById(R.id.priceBar)
        imageAdded = findViewById(R.id.image_added)
        doneButton = findViewById(R.id.post)
        progressBar = findViewById(R.id.progress_bar)
        searchLocationView = findViewById(R.id.search_location)
        selectLocationView = findViewById(R.id.select_location_view)
        selectLocationButton = findViewById(R.id.select_location_button)
        mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        location.setOnClickListener {
            selectLocationView.visibility = View.VISIBLE
        }


        searchLocationView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                map.clear()
                val currentLocation = searchLocationView.query.toString()
                var addressList: List<Address>? = null

                if(currentLocation != null || currentLocation != ""){
                    val geocoder = Geocoder(this@PostActivity)
                    try {
                        addressList = geocoder.getFromLocationName(currentLocation, 1)
                    } catch (e: IOException){
                        e.printStackTrace()
                    }

                    if (addressList != null) {
                        if (addressList.isNotEmpty()){
                            val address = addressList[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            map.addMarker(latLng.let { MarkerOptions().position(it).title(currentLocation) })
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                            selectLocationButton.visibility = View.VISIBLE

                            selectLocationButton.setOnClickListener {
                                selectedLocation = address.featureName
                                location.text = selectedLocation
                                selectLocationView.visibility = View.GONE
                                selectLocationButton.visibility = View.GONE
                            }
                        } else {
                            Toast.makeText(
                                baseContext, "We could not find that location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        mapFragment.getMapAsync(this)


        //TODO: SET THE UP BUTTON SO THAT WHEN IT IS CLICKED, THE EDIT PHOTO ACTIVITY IS WHAT IS SHOWN
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taggedList = HashMap()
        //FOR TESTING PURPOSES
        taggedList["XTICi9lfBddQHBwhkPg1OCwnXyi1"] = true

        selectedLocation = ""
        selectedCategory = ""
        selectedSubCategory = ""

        val categories = ArrayList<String>()
        categories.add("Select Category")
        categories.add("Food")
        categories.add("Outdoor")
        categories.add("Nightlife")
        categories.add("Entertainment")
        categories.add("Shopping")

        val subCategories = ArrayList<String>()
        subCategories.add("Select Sub Category")
        subCategories.add("BARS")
        subCategories.add("ROOF TOPS")
        subCategories.add("NIGHTCLUBS")
        subCategories.add("EVENTS")

        val categoryAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = when {
                    categorySpinner.selectedItem != "Select Category" -> {
                        categorySpinner.selectedItem.toString()
                    }
                    else -> {
                        ""
                    }
                }
            }

        }

        val subCategoryAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, subCategories)
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subCategorySpinner.adapter = subCategoryAdapter
        subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedSubCategory = when {
                    subCategorySpinner.selectedItem != "Select Sub Category" -> {
                        subCategorySpinner.selectedItem.toString()
                    }
                    else -> {
                        ""
                    }
                }
            }

        }

        storageReference = FirebaseStorage.getInstance().getReference("Posts")
        close.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        doneButton.setOnClickListener {
            if (selectedCategory == "") {
                Toast.makeText(
                    baseContext, "Select a category",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (location.text.toString() == "") {
                Toast.makeText(
                    baseContext, "Select a location",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (location.text.toString() != "" && selectedCategory != "") {
                uploadImage()
            }
        }

        CropImage.activity().setAspectRatio(1, 1).start(this)
    }


    private fun getFileExtension(uri: Uri): String? {
        val contentResolver: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadImage() {
        progressBar.visibility = View.VISIBLE // To show the ProgressBar

        if (imageUri != null) {
            val referenceFile: StorageReference = storageReference.child(
                System.currentTimeMillis().toString() + "." + getFileExtension(imageUri)
            )
            uploadTask = referenceFile.putFile(imageUri)

            uploadTask.continueWithTask { task ->
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
                    val reference: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Posts")
                    val postid: String = reference.push().key!!
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["postid"] = postid
                    hashMap["postimage"] = imageUrl
                    hashMap["location"] = selectedLocation
                    hashMap["tagged"] = taggedList
                    hashMap["rating"] = ratingBar.rating
                    hashMap["pricing"] = priceBar.rating
                    hashMap["link"] = link.text.toString()
                    hashMap["category"] = selectedCategory
                    hashMap["subcategory"] = selectedSubCategory
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
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val result: CropImage.ActivityResult = CropImage.getActivityResult(data)

                imageUri = result.uri
                image_added.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Something has gone wrong!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }
    }


}
