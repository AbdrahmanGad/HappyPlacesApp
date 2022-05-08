package com.example.happyplacesapp.activities

import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.net.toUri
import com.example.happyplacesapp.R
import com.example.happyplacesapp.database.databaseHandler
import com.example.happyplacesapp.models.HappyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener, MultiplePermissionsListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mHappyPlaceDetails : HappyPlaceModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)
        supportActionBar?.title = "Add a Happy Place"

//        setSupportActionBar(findViewById(R.id.toolbarAddHappyPlaceActivity))
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        findViewById<FloatingActionButton>(R.id.toolbarAddHappyPlaceActivity).setOnClickListener {
//            onBackPressed()
//        }

        if (!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.google_maps_api_key))
        }


        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(
                MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()

        }
        updateDateInView()

        if (mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            findViewById<EditText>(R.id.et_title).setText(mHappyPlaceDetails!!.title)
            findViewById<EditText>(R.id.et_description).setText(mHappyPlaceDetails!!.description)
            findViewById<EditText>(R.id.et_date).setText(mHappyPlaceDetails!!.date)
            findViewById<EditText>(R.id.et_location).setText(mHappyPlaceDetails!!.location)
            //fmLatitude = mHappyPlaceDetails!!.latitude
            //mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            findViewById<ImageView>(R.id.iv_place_image).setImageURI(saveImageToInternalStorage)

            findViewById<Button>(R.id.btn_save).text = "UPDATE"
        }

        findViewById<EditText>(R.id.et_date).setOnClickListener (this)
        findViewById<TextView>(R.id.tv_add_image).setOnClickListener(this)
        findViewById<Button>(R.id.btn_save).setOnClickListener(this)
        findViewById<EditText>(R.id.et_location).setOnClickListener (this)


    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pickerDialogItems = arrayOf("Select photo from Gallery", "Capture photo by camera")
                pictureDialog.setItems(pickerDialogItems){
                    dialog, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.et_location ->{
                try {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            R.id.btn_save ->{
                when{
                    findViewById<EditText>(R.id.et_title).text.isNullOrEmpty() ->{
                        Toast.makeText(this, "please enter title", Toast.LENGTH_SHORT).show()
                    }
                    findViewById<EditText>(R.id.et_description).text.isNullOrEmpty() ->{
                        Toast.makeText(this, "please enter description", Toast.LENGTH_SHORT).show()
                    }
                    findViewById<EditText>(R.id.et_location).text.isNullOrEmpty() ->{
                        Toast.makeText(this, "please enter location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(this, "please select image", Toast.LENGTH_SHORT).show()
                    }else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            findViewById<EditText>(R.id.et_title).text.toString(),
                            saveImageToInternalStorage.toString(),
                            findViewById<EditText>(R.id.et_description).text.toString(),
                            findViewById<EditText>(R.id.et_date).text.toString(),
                            findViewById<EditText>(R.id.et_location).text.toString()
                        )
                        val dbHandler = databaseHandler(this)
                        if (mHappyPlaceDetails == null){
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                            if (addHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                            if (updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(
                report: MultiplePermissionsReport?)
            {if(report!!.areAllPermissionsGranted()){
                val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(galleryIntent, Camera)
            }}
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest> , token: PermissionToken )
            {
                showRationalDialogForPermission()
            }
        }).onSameThread().check();
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(
                report: MultiplePermissionsReport?)
            {if(report!!.areAllPermissionsGranted()){
                val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, Gallery)
            }}
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest> , token: PermissionToken )
            {
                showRationalDialogForPermission()
            }
        }).onSameThread()   .check();
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == Gallery){
                if (data != null){
                val contentURI =    data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)

                        Log.e("Saved image", "path :: $saveImageToInternalStorage")

                        findViewById<ImageView>(R.id.iv_place_image).setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }else if (requestCode == Camera){
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)

                Log.e("Saved image", "path :: $saveImageToInternalStorage")


                findViewById<ImageView>(R.id.iv_place_image)!!.setImageBitmap(thumbnail)
            }else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                findViewById<EditText>(R.id.et_location).setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("Cancelled", "Cancelled")
            Toast.makeText(this, "falid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this).setMessage(""+
                "It looks like you turned off permission required" +
                " for this feature. It can be enabled under" +
                " the Application Settings")
            .setPositiveButton("GO TO SETTINGS"){
                _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView(){
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        findViewById<EditText>(R.id.et_date).setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri? {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val Gallery = 1
        private const val Camera = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }

    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
        TODO("Not yet implemented")
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
        TODO("Not yet implemented")
    }
}