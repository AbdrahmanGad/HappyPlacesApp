package com.example.happyplacesapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.R
import com.example.happyplacesapp.adapters.HappyPlaceAdapter
import com.example.happyplacesapp.database.databaseHandler
import com.example.happyplacesapp.models.HappyPlaceModel
import com.example.happyplacesapp.utilities.SwipeToDeleteCallBack
import com.example.happyplacesapp.utilities.SwipeToEditCallBack

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getHappyPlacesListFromLocalDB()

    }

    fun fabAddHappyPlaces(view: android.view.View) {
        var intent = Intent(this, AddHappyPlaceActivity::class.java)
        startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
    }

    @SuppressLint("CutPasteId")
    private fun setUpHappyPlacesRecycle(happyPlaceList: ArrayList<HappyPlaceModel>){
        findViewById<RecyclerView>(R.id.rvHappyPlacesLists).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rvHappyPlacesLists).setHasFixedSize(true)
        val placesAdapter = HappyPlaceAdapter(this, happyPlaceList)
        findViewById<RecyclerView>(R.id.rvHappyPlacesLists).adapter = placesAdapter
        placesAdapter.setOnClickListener(object: HappyPlaceAdapter.OnclickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        // Edit Swipe Handler
        val editSwipeHandler = object : SwipeToEditCallBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = findViewById<RecyclerView>(R.id.rvHappyPlacesLists).adapter as HappyPlaceAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(findViewById(R.id.rvHappyPlacesLists))

        // Delete Swipe Handler
        val deleteSwipeHandler = object : SwipeToDeleteCallBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = findViewById<RecyclerView>(R.id.rvHappyPlacesLists).adapter as HappyPlaceAdapter
                adapter.removeAt(
                    viewHolder.adapterPosition
                )
                getHappyPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(findViewById(R.id.rvHappyPlacesLists))
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = databaseHandler(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.size > 0){
                findViewById<RecyclerView>(R.id.rvHappyPlacesLists).visibility = View.VISIBLE
                findViewById<TextView>(R.id.tvNoRecordsAvailable).visibility = View.GONE
                setUpHappyPlacesRecycle(getHappyPlaceList)
        }else{
            findViewById<RecyclerView>(R.id.rvHappyPlacesLists).visibility = View.GONE
            findViewById<TextView>(R.id.tvNoRecordsAvailable).visibility = View.VISIBLE

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}