package com.android.sonder_app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.android.sonder_app.Model.Post
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import java.io.IOException


class EditPostActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MyMessage:"

    private lateinit var taggedList: HashMap<String, Boolean>
    private lateinit var linkEdit: EditText
    private lateinit var locationEdit: TextView
    private lateinit var descriptionEdit: EditText

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var searchLocationViewEdit: SearchView
    private lateinit var selectLocationViewEdit: RelativeLayout
    private lateinit var selectLocationButton: Button
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var ratingBarEdit: RatingBar
    private lateinit var priceBarEdit: io.techery.properratingbar.ProperRatingBar
    private lateinit var categorySpinnerEdit: Spinner
    private lateinit var subCategorySpinnerEdit: Spinner
    private lateinit var selectedLocationEdit: String
    private lateinit var selectedCategoryEdit: String
    private lateinit var selectedSubCategoryEdit: String
    private lateinit var imageAddedEdit: ImageView
    private lateinit var close: ImageView
    private lateinit var updateButton: TextView
    private lateinit var progressBarEdit: ProgressBar

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)
        close = findViewById(R.id.close)
        linkEdit = findViewById(R.id.link_edit)
        locationEdit = findViewById(R.id.location_edit)
        descriptionEdit = findViewById(R.id.description_edit)
        categorySpinnerEdit = findViewById(R.id.select_category_edit)
        subCategorySpinnerEdit = findViewById(R.id.select_sub_category_edit)
        ratingBarEdit = findViewById(R.id.ratingBar_edit)
        priceBarEdit = findViewById(R.id.priceBar_edit)
        imageAddedEdit = findViewById(R.id.image_added_edit)
        updateButton = findViewById(R.id.update_post)
        progressBarEdit = findViewById(R.id.progress_bar)
        searchLocationViewEdit = findViewById(R.id.search_location)
        selectLocationViewEdit = findViewById(R.id.select_location_view)
        selectLocationButton = findViewById(R.id.select_location_button)
        mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment


        locationEdit.setOnClickListener {
            selectLocationViewEdit.visibility = View.VISIBLE
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this@EditPostActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                val permissions: Array<out String> =
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                ActivityCompat.requestPermissions(this@EditPostActivity, permissions, 44)
            }
        }
        searchLocationViewEdit.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                map.clear()
                val currentLocation = searchLocationViewEdit.query.toString()
                var addressList: List<Address>? = null

                if (currentLocation != null || currentLocation != "") {
                    val geocoder = Geocoder(this@EditPostActivity)
                    try {
                        addressList = geocoder.getFromLocationName(currentLocation, 1)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (addressList != null) {
                        if (addressList.isNotEmpty()) {
                            val address = addressList[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            map.addMarker(latLng.let {
                                MarkerOptions().position(it).title(currentLocation)
                            })
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                            selectLocationButton.visibility = View.VISIBLE

                            selectLocationButton.setOnClickListener {
                                selectedLocationEdit = address.featureName
                                map.clear()
                                locationEdit.text = selectedLocationEdit
                                selectLocationViewEdit.visibility = View.GONE
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


        taggedList = HashMap()
        //FOR TESTING PURPOSES
        taggedList["XTICi9lfBddQHBwhkPg1OCwnXyi1"] = true

        selectedLocationEdit = ""
        selectedCategoryEdit = ""
        selectedSubCategoryEdit = ""

        val categories = ArrayList<String>()
        categories.add("Select Category")
        categories.add("Food")
        categories.add("Outdoor")
        categories.add("Nightlife")
        categories.add("Entertainment")
        categories.add("Shopping")

        val foodSubCat = ArrayList<String>()
        foodSubCat.add("Select Sub Category")
        foodSubCat.add("BURGERS")
        foodSubCat.add("VEGAN")
        foodSubCat.add("PIZZA")
        foodSubCat.add("ROMANTIC")
        foodSubCat.add("FAST FOOD")
        foodSubCat.add("LOCAL")

        val outdoorSubCat = ArrayList<String>()
        outdoorSubCat.add("Select Sub Category")
        outdoorSubCat.add("CAMPING")
        outdoorSubCat.add("CYCLING")
        outdoorSubCat.add("TRECKING")
        outdoorSubCat.add("ROCK CLIMBING")
        outdoorSubCat.add("PARK")

        val nightlifeSubCat = ArrayList<String>()
        nightlifeSubCat.add("Select Sub Category")
        nightlifeSubCat.add("BARS")
        nightlifeSubCat.add("ROOF TOPS")
        nightlifeSubCat.add("NIGHTCLUBS")
        nightlifeSubCat.add("EVENTS")

        val entertainmentSubCat = ArrayList<String>()
        entertainmentSubCat.add("Select Sub Category")
        entertainmentSubCat.add("ATTRACTIONS")
        entertainmentSubCat.add("MUSEUMS")
        entertainmentSubCat.add("MONUMENTS")
        entertainmentSubCat.add("WELLNESS")

        val shoppingSubCat = ArrayList<String>()
        shoppingSubCat.add("Select Sub Category")
        shoppingSubCat.add("WOMEN")
        shoppingSubCat.add("MEN")
        shoppingSubCat.add("CHILDREN")
        shoppingSubCat.add("SPORTS")

        val categoryAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinnerEdit.adapter = categoryAdapter
        categorySpinnerEdit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                selectedCategoryEdit = when (categorySpinnerEdit.selectedItem) {
                    "Select Category" -> {
                        ""
                    }
                    else -> {
                        categorySpinnerEdit.selectedItem.toString()

                    }
                }

            }

        }

        subCategorySpinnerEdit.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSubCategoryEdit = when {
                        subCategorySpinnerEdit.selectedItem != "Select Sub Category" -> {
                            subCategorySpinnerEdit.selectedItem.toString()
                        }
                        else -> {
                            ""
                        }
                    }
                    when (subCategorySpinnerEdit.selectedItem) {
                        "Select Sub Category" -> {
                            selectedSubCategoryEdit = ""
                        }
                        else -> {
                            selectedSubCategoryEdit = subCategorySpinnerEdit.selectedItem.toString()
                        }
                    }
                }

            }

        val intent = intent
        val postId = intent.getStringExtra("postid")
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Posts").child(postId)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post: Post = dataSnapshot.getValue(Post::class.java)!!
                Glide.with(applicationContext).load(post.getPostimage()).into(imageAddedEdit)
                descriptionEdit.setText(post.getDescription())
                locationEdit.text = post.getLocation()
                linkEdit.setText(post.getLink())
                ratingBarEdit.rating = (post.getRating())
                priceBarEdit.rating = (post.getPricing())
                categorySpinnerEdit.setSelection(categories.indexOf(post.getCategory()))

                if (post.getSubCategory() != "") {
                    Log.d(TAG, "subcategory exists")
                    when (categories.indexOf(post.getCategory())) {
                        1 -> {
                            val subCategoryAdapter =
                                ArrayAdapter(applicationContext, R.layout.spinner_item, foodSubCat)
                            subCategorySpinnerEdit.adapter = subCategoryAdapter
                            subCategorySpinnerEdit.setSelection(foodSubCat.indexOf(post.getSubCategory()))
                            subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        2 -> {
                            val subCategoryAdapter = ArrayAdapter(
                                applicationContext,
                                R.layout.spinner_item,
                                outdoorSubCat
                            )
                            subCategorySpinnerEdit.adapter = subCategoryAdapter
                            subCategorySpinnerEdit.setSelection(outdoorSubCat.indexOf(post.getSubCategory()))
                            subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        3 -> {
                            val subCategoryAdapter = ArrayAdapter(
                                applicationContext,
                                R.layout.spinner_item,
                                nightlifeSubCat
                            )
                            subCategorySpinnerEdit.adapter = subCategoryAdapter
                            subCategorySpinnerEdit.setSelection(nightlifeSubCat.indexOf(post.getSubCategory()))
                            subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        4 -> {
                            val subCategoryAdapter = ArrayAdapter(
                                applicationContext,
                                R.layout.spinner_item,
                                entertainmentSubCat
                            )
                            subCategorySpinnerEdit.adapter = subCategoryAdapter
                            subCategorySpinnerEdit.setSelection(entertainmentSubCat.indexOf(post.getSubCategory()))
                            subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        5 -> {
                            val subCategoryAdapter = ArrayAdapter(
                                applicationContext,
                                R.layout.spinner_item,
                                shoppingSubCat
                            )
                            subCategorySpinnerEdit.adapter = subCategoryAdapter
                            subCategorySpinnerEdit.setSelection(shoppingSubCat.indexOf(post.getSubCategory()))
                            subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        }
                    }

                }

            }

        })

        updateButton.setOnClickListener{
            updatePost(postId, locationEdit.text.toString(), taggedList, ratingBarEdit.rating ,priceBarEdit.rating, linkEdit.text.toString(),selectedCategoryEdit, selectedSubCategoryEdit, descriptionEdit.text.toString())
            finish()
        }
    }


    private fun updatePost(postId: String, location: String, tagged: HashMap< String,Boolean>, rating: Float, price: Int, link: String, category: String, subcategory: String, description: String) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Posts").child(postId)
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["location"] = location
        hashMap["tagged"] = tagged
        hashMap["rating"] = rating
        hashMap["pricing"] = price
        hashMap["link"] = link
        hashMap["category"] = category
        hashMap["subcategory"] = subcategory
        hashMap["description"] = description
        reference.updateChildren(hashMap as Map<String, Any>)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }
    }

    private fun getCurrentLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation;
        task.addOnSuccessListener { currentLocation ->
            mapFragment.getMapAsync {
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                val options: MarkerOptions =
                    MarkerOptions().position(latLng).title("Current Location")
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                map.addMarker(options)

                val geocoder = Geocoder(this@EditPostActivity)
                var addressList: List<Address>? = null
                addressList =
                    geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                if (addressList != null) {
                    if (addressList.isNotEmpty()) {
                        val address = addressList[0]
                        selectLocationButton.visibility = View.VISIBLE

                        selectLocationButton.setOnClickListener {
                            selectedLocationEdit = address.adminArea + ", " + address.countryName
                            map.clear()
                            locationEdit.text = selectedLocationEdit
                            selectLocationViewEdit.visibility = View.GONE
                            selectLocationButton.visibility = View.GONE

                        }
                    }
                }

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectLocationViewEdit.visibility = View.VISIBLE
                getCurrentLocation()
            }
        }
    }
}
