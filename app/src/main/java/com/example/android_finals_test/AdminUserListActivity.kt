package com.example.android_finals_test

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Locale

class AdminUserListActivity: AppCompatActivity() {
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        val backBtn = findViewById<Button>(R.id.backButton)
        val userL = findViewById<LinearLayout>(R.id.userList)
        backBtn.setOnClickListener {
            finish()
        }
        setUserList(userL)
    }

    private fun setUserList(userL: LinearLayout) {
        db.collection("users").get().addOnSuccessListener { result ->
            for (user in result){
                //database values------------------------------//
                val uname = user.data["displayName"].toString()
                val email = user.data["email"].toString()
                val photoURL = user.data["photoUrl"].toString()
                println(uname)
                //---------------------------------------------//

                //programmatically created xml elements==============================================================================//
                val cardV = CardView(this).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                        .apply { setMargins(30,30,30,30) }
                    radius = 10F
                }
                //information configuration
                val mainL = LinearLayout(this).apply{
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setPadding(10) }
                val contentL = LinearLayout(this).apply{
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }

                val nameV = TextView(this).apply {
                    text = uname }
                val emailV = TextView(this).apply {
                    text = email }
                val imageV = ImageView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(resources.getDimensionPixelOffset(R.dimen.imageView_width),
                        resources.getDimensionPixelOffset(R.dimen.imageView_height)) }
                Glide.with(this).load(photoURL).placeholder(R.drawable.profile).into(imageV)

                cardV.setOnClickListener{
                    viewProfileDetails(it, email, uname, photoURL)

                }
                //=================================================================================================================//
                contentL.addView(nameV); contentL.addView(emailV)
                mainL.addView(imageV); mainL.addView(contentL); cardV.addView(mainL)
                userL.addView(cardV)
            }
        }
    }
    private fun viewProfileDetails(view: View, email: String, uname: String, photoURL: String) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.activity_profile, null)
        val pWindow = PopupWindow(pView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================

        //xml===============================================================================
        val backBtn = pView.findViewById<Button>(R.id.backButton)
        val userImg = pView.findViewById<ImageView>(R.id.userImage)
        val nameTxt = pView.findViewById<TextView>(R.id.nameText)
        val emailTxt = pView.findViewById<TextView>(R.id.emailText)
        val contactNumTxt = pView.findViewById<TextView>(R.id.contactNumTxt)

        val rentedL = pView.findViewById<LinearLayout>(R.id.rentedList)
        //------------------------------------------------------------------------------

        //
        nameTxt.text = uname
        emailTxt.text = email
        Glide.with(this).load(photoURL).placeholder(R.drawable.profile).into(userImg)

        backBtn.setOnClickListener { pWindow.dismiss() }

        db.collection("account").document(email).collection("currentContracts").whereEqualTo("status", "active")
            .get().addOnSuccessListener { result ->
            for(contracts in result){
                val apartmentID = contracts.data["apartmentID"].toString()
                val room = contracts.data["room"].toString()
                val dueDate = contracts.data["dueDate"].toString()
                val outBal = contracts.data["outstandingBalance"].toString()
                val status = contracts.data["status"].toString()
                db.collection("apartment").document(apartmentID).get().addOnSuccessListener {
                        result2 ->
                    val apartmentImg = result2.getString("photoURL").toString()
                    val address = result2.getString("address").toString()

                    val cardV = CardView(this).apply {
                        layoutParams = LayoutParams(convert(200,context), LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(convert(20, context), convert(10, context), convert(20, context),convert(10, context))
                        }
                        radius = 16F
                    }

                    val frameL = FrameLayout(this).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT).apply {
                        }
                    }
                    val imgV = ImageView(this).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    Glide.with(this).load(apartmentImg).placeholder(R.drawable.house_icon).into(imgV)

                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(
                            ContextCompat.getColor(this, android.R.color.transparent), // Top color (transparent)
                            ContextCompat.getColor(this, android.R.color.black) // Bottom color (dark)
                        )
                    )
                    gradientDrawable.cornerRadius = 16f
                    imgV.foreground = gradientDrawable

                    val infoL = LinearLayout(this).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        gravity = Gravity.BOTTOM
                        orientation = LinearLayout.VERTICAL
                        setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                    }

                    val rentV = TextView(this).apply {
                        text = if(room.isNotEmpty() || room != ""){"$address @Room: $room"}else{address}
                    }


                    cardV.setOnClickListener {
                        if(status == "pending"){

                        }
                        else{
                            val intent = Intent(this, AdminEditTenantRentalActivity::class.java)
                            intent.putExtra("apartmentID", apartmentID)
                            intent.putExtra("email", email)
                            intent.putExtra("dueDate", dueDate)
                            intent.putExtra("apartmentImage", apartmentImg)
                            intent.putExtra("outstandingBalance", outBal)
                            intent.putExtra("address", address)
                            startActivity(intent)

                        }
                    }
                    frameL.addView(imgV)
                    infoL.addView(rentV); frameL.addView(infoL)
                    cardV.addView(frameL)
                    rentedL.addView(cardV)

                }
            }
        }
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

    private fun convert(num: Int, context: Context): Int {
        return(num * context.resources.displayMetrics.density).toInt()
    }
}

