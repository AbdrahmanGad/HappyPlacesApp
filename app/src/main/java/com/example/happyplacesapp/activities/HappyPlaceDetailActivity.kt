package com.example.happyplacesapp.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.net.toUri
import com.example.happyplacesapp.R
import com.example.happyplacesapp.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)

        var happyPlaceDetailModel : HappyPlaceModel? = null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (happyPlaceDetailModel != null){
            findViewById<ImageView>(R.id.iv_place_image_happy_place_details).setImageURI(happyPlaceDetailModel.image.toUri())
            findViewById<TextView>(R.id.tv_description_happy_place_details).text = (happyPlaceDetailModel.description)
            findViewById<TextView>(R.id.tv_location_happy_place_details).text = (happyPlaceDetailModel.location)
        }
    }
}