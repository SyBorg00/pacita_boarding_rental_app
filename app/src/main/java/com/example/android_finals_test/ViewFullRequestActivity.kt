package com.example.android_finals_test

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.work.ListenableWorker.Result.Success
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ViewFullRequestActivity: AppCompatActivity() {
    private var paymentProofURI = ""
    private var idURI = ""
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_request)

        createNotificationChannel()
        auth = FirebaseAuth.getInstance()

        //initialization===============================================//
        val requestID = intent.getStringExtra("requestID").toString()
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        val room = intent.getStringExtra("room").toString()
        val db = Firebase.firestore
        //-------------------------------------------------------------//

        //xml==========================================================//
        val userImg = findViewById<ImageView>(R.id.userImage)
        val nameTxt = findViewById<TextView>(R.id.usernameText)
        val emailTxt = findViewById<TextView>(R.id.emailText)
        val apartmentImg = findViewById<ImageView>(R.id.apartmentImage)
        val adrTxt = findViewById<TextView>(R.id.addressText)
        val typeTxt = findViewById<TextView>(R.id.typeText)
        val roomTxt = findViewById<TextView>(R.id.roomText)
        val idImg = findViewById<ImageView>(R.id.IDImage)
        val payImg = findViewById<ImageView>(R.id.paymentImage)
        val acceptBtn = findViewById<Button>(R.id.acceptButton)
        val removeBtn = findViewById<Button>(R.id.removeButton)
        val backBtn = findViewById<Button>(R.id.backButton)
        //-------------------------------------------------------------//

        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
            for(i in result){
                val name = i.data["displayName"].toString()
                val photoURL = i.data["photoUrl"].toString()

                nameTxt.text = name
                Glide.with(this).load(photoURL).placeholder(R.drawable.profile).into(userImg)
                emailTxt.text = email

                db.collection("apartment").document(apartmentID).get().addOnSuccessListener { apartment ->
                    apartment.let {
                        val address = it["address"].toString()
                        val type = it["type"].toString()
                        val apartmentPhoto = it["photoURL"].toString()
                        adrTxt.text = address; typeTxt.text = type; roomTxt.text = room
                        Glide.with(this).load(apartmentPhoto).placeholder(R.drawable.house_icon).into(apartmentImg)
                    }
                    db.collection("account").document(email).collection("currentContracts").document(apartmentID).get().addOnSuccessListener { account ->
                        account.let {
                            idURI = it["IDCard"].toString().trim()
                            paymentProofURI = it["paymentProof"].toString().trim()
                            Glide.with(this).load(idURI).into(idImg)
                            Glide.with(this).load(paymentProofURI).into(payImg)
                            idImg.setOnClickListener {
                                fullImageView(it, idURI)
                            }
                            payImg.setOnClickListener {
                                fullImageView(it, paymentProofURI)
                            }
                        } } } } }

        acceptBtn.setOnClickListener {
            if(paymentProofURI != "" || paymentProofURI.isNotEmpty()){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Accept User Rental Request?")
                    .setMessage("Are you sure to accept this user? Review the documents first for clarity")
                    .setPositiveButton("OK") { dialog, _ ->
                        val intent = Intent(this, AdminProcessDownpayment::class.java)
                        intent.putExtra("requestID",requestID)
                        intent.putExtra("email", email)
                        intent.putExtra("apartmentID", apartmentID)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()


            }
            else Toast.makeText(baseContext, "User has still not sent their down payment proof yet",Toast.LENGTH_SHORT).show()
        }

        removeBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Remove User Rental Request?")
                .setMessage("Are you sure to remove this user?")
                .setPositiveButton("OK") { dialog, _ ->
                    db.collection("account").document(email).delete()
                    if(auth.currentUser?.email.toString() == email){
                        showNotification("Request Rejected", "Your rental request on ${adrTxt.text} has been rejected." +
                                " Kindly contact the land owner or admin for more details", 3)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun fullImageView(view: View, uri: String) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_full_image, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.MATCH_PARENT,true)
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================
        val img = pView.findViewById<ImageView>(R.id.receiptImage)
        Glide.with(this).load(uri).into(img)
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

    private fun convert(num: Int, context: Context): Int {
        return(num * context.resources.displayMetrics.density).toInt()
    }

    private fun dimBehind(popupWindow: PopupWindow) {
        val container = popupWindow.contentView.rootView
        val context = popupWindow.contentView.context
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = (container.layoutParams as WindowManager.LayoutParams).apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        windowManager.updateViewLayout(container, layoutParams)
    }


}