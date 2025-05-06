package com.example.android_finals_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AdminProcessDownpayment: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_process_transaction)
        createNotificationChannel()
        auth = FirebaseAuth.getInstance()


        val requestID = intent.getStringExtra("requestID").toString()
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        val db = Firebase.firestore

        //=====================================================================//
        val userImg = findViewById<ImageView>(R.id.userImage)
        val amountET = findViewById<EditText>(R.id.amountPaidField)
        val uNameTxt = findViewById<TextView>(R.id.userNameText)
        val msgET = findViewById<EditText>(R.id.messageField)
        val submitBtn = findViewById<Button>(R.id.confirmButton)
        val cancelBtn = findViewById<Button>(R.id.cancelButton)
        //---------------------------------------------------------------------//

        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
            for(users in result){
                val photoUrl = users.data["photoUrl"].toString()
                val name = users.data["displayName"].toString()
                Glide.with(this).load(photoUrl).placeholder(R.drawable.profile).into(userImg)
                uNameTxt.text = String.format(Locale.US, "Now processing %s's account balance", name)
            }
        }
        submitBtn.setOnClickListener {
            val currDate = LocalDate.now()
            val amount = amountET.text.toString().trim()
            var message = msgET.text.toString().trim()
            if (amount.isNotEmpty()) {
                val nextDueDate = getDueDate()
                val amountNum = amount.toDouble()
                println(amountNum)
                val updateAccount = mapOf("dueDate" to nextDueDate, "outstandingBalance" to FieldValue.increment(-amount.toDouble()),
                    "status" to "active"
                )
                db.collection("payments")
                    .document("${currDate.month}-${currDate.year}")
                    .set("totalPayments" to FieldValue.increment(amount.toDouble()), SetOptions.merge())

                db.collection("request").document(requestID).update("status", "accepted")

                db.collection("account").document(email).collection("currentContracts").document(apartmentID)
                    .update(updateAccount).addOnSuccessListener {
                        if(auth.currentUser?.email.toString() == email){
                            if(message.isEmpty()){
                                message = "Your rental request has been accepted." +
                                        " Kindly double check your rentals in the profile page"
                            }
                            showNotification("Request Accepted", message, 3)
                        }
                        finish()
                        Toast.makeText(baseContext, "Process successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, AdminHomepageActivity::class.java)
                        startActivity(intent)
                    }
            } else Toast.makeText(baseContext, "Please input the amount field", Toast.LENGTH_SHORT)
                .show()
        }
        cancelBtn.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDueDate(): String{ //function to initialize the tenant's due date (which is 7 days from renting)
        val currentDate = LocalDate.now()
        val dueDate = currentDate.plusDays(30)
        val dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val dueDateString = dueDate.format(dateFormat)
        return dueDateString
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "accept_notifier"
            val channelName = "Accepted User Notification"
            val channelDescription = "Notifications for handling accepted rental requests"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val channelId = "accept_notifier"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}