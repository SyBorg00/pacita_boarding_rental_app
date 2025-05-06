package com.example.android_finals_test

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.util.Locale

class AdminViewStatisticsActivity: AppCompatActivity() {
    var totalRent: Int = 0
    var totalRooms: Int = 0
    var totalUnoccupiedRooms: Int = 0

    private val db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_statistics)

        val userCountTxt = findViewById<TextView>(R.id.userCountText)
        val totalRentTxt = findViewById<TextView>(R.id.totalRent)
        val totalRoom = findViewById<TextView>(R.id.totalRooms)
        val totalEarnings = findViewById<TextView>(R.id.totalPayments)
        val backBtn = findViewById<Button>(R.id.backButton)

        db.collection("users").get().addOnSuccessListener {
            users ->
            userCountTxt.text = users.size().toString()
        }

        db.collection("apartment").get().addOnSuccessListener {
            result ->
            for(i in result){
                val type = i.data["type"].toString()
                val isOccupied = i.data["isOccupied"].toString().toBoolean()
                val rooms = i.data["rooms"] as HashMap<String, Any>

                if(type == "house" && isOccupied){
                    totalRent += 1
                    totalRentTxt.text = String.format("%s/%s", totalRent.toString(), result.size())
                }
                else{
                    for(j in rooms.keys){
                        val innerMap = rooms[j] as HashMap<*,*>
                        innerMap.let {
                            val isRoomOccupied = it["isOccupied"].toString().toBoolean()

                            if(isRoomOccupied){
                                totalRooms += 1
                            }
                        }

                    }
                }
                totalRoom.text = String.format("%s/%s", totalRooms.toString(), rooms.size)
            }
        }
        val localDate = LocalDate.now()
        val currMonth = localDate.month.toString(); val currYr = localDate.year.toString()
        db.collection("payments").document("$currMonth-$currYr").get().addOnSuccessListener { result ->
            val totalPayment = result["second"].toString().toDoubleOrNull()?: 0.00
            totalEarnings.text = String.format(Locale.US, "PHP %.2f", totalPayment)
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, AdminHomepageActivity::class.java)
            startActivity(intent)
        }

    }
}