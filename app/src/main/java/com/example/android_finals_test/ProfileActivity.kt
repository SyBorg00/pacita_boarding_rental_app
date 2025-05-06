package com.example.android_finals_test

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private val logoUrl = " \"https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5\"\n"
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //getExtra values==============================================================================
        val thisIntent: Intent = intent
        val loggedAsAdmin: Boolean = thisIntent.getBooleanExtra("loggedAsAdmin",false)
        val userEmail: String = thisIntent.getStringExtra("email").toString()
        println(loggedAsAdmin)
        println(userEmail)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        //=============================================================================================

        //xml configuration=============================================================//
        val backBtn = findViewById<Button>(R.id.backButton)
        val editProfileBtn = findViewById<ImageView>(R.id.editProfileImage)
        val userImg = findViewById<ImageView>(R.id.userImage)
        val nameTxt = findViewById<TextView>(R.id.nameText)
        val emailTxt = findViewById<TextView>(R.id.emailText)
        val contactNumTxt = findViewById<TextView>(R.id.contactNumTxt)
        val rentedL = findViewById<LinearLayout>(R.id.rentedList)
        //-----------------------------------------------------------------------------//

        val email = if(!loggedAsAdmin){user?.email.toString()}else{userEmail}
        editProfileBtn.visibility = if(loggedAsAdmin){View.GONE}else{View.VISIBLE}

        //button events=====================================================================
        backBtn.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
        setProfile(userImg, editProfileBtn, rentedL,nameTxt, emailTxt, contactNumTxt, email)
        //===================================================================================
    }

    private fun setProfile(userImg: ImageView, editProfileBtn: ImageView, rentedL: LinearLayout,
    nameTxt: TextView, emailTxt: TextView, contactNumTxt: TextView, userEmail: String) {
        val ardentBook = ResourcesCompat.getFont(this, R.font.ardent_sans_book)

        db.collection("users").whereEqualTo("email", userEmail).get().addOnSuccessListener {
            result->
            for(docs in result){
                val name = docs.data["displayName"].toString()
                val photoURL = docs.data["photoUrl"].toString()
                nameTxt.text = name
                emailTxt.text = userEmail
                Glide.with(this).load(photoURL).placeholder(R.drawable.profile).into(userImg)

                db.collection("account").document(userEmail).get().addOnSuccessListener { account ->
                    val lName = account.data?.get("lastName").toString()
                    val fName = account.data?.get("firstName").toString()
                    val contactNum = account.data?.get("contactNum").toString()
                    val birthDate = account.data?.get("birthDate").toString()
                    val occupation = account.data?.get("occupation").toString()
                    contactNumTxt.text = contactNum
                    editProfileBtn.setOnClickListener { 
                        editProfile(it, photoURL, lName, fName, contactNum, birthDate, occupation, userEmail)
                    }
                    db.collection("account").document(userEmail).collection("currentContracts")
                        .get().addOnSuccessListener { //for tenants with rent
                                secondResult ->
                            if(!secondResult.isEmpty){
                                for(docs2 in secondResult){
                                    val apartmentID = docs2.data["apartmentID"].toString()
                                    val room = docs2.data["room"].toString()
                                    val dueDate = docs2.data["dueDate"].toString()
                                    val outBal = docs2.data["outstandingBalance"].toString()
                                    val status = docs2.data["status"].toString()
                                    val payLater = docs2.data["payForLater"].toString().toBoolean()
                                    db.collection("apartment").document(apartmentID).get().addOnSuccessListener {
                                            result->
                                        val apartmentImg = result.getString("photoURL").toString()
                                        val address = result.getString("address").toString()

                                        val cardV = CardView(this).apply {
                                            layoutParams = LayoutParams(convert(200,context), LayoutParams.WRAP_CONTENT
                                            ).apply {
                                                setMargins(convert(20, context), convert(10, context), convert(20, context),convert(10, context))
                                            }
                                            radius = 16F
                                        }
                                        val infoL = LinearLayout(this).apply {
                                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                                            gravity = Gravity.BOTTOM
                                            orientation = LinearLayout.VERTICAL
                                            setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                                        }

                                        val rentV = TextView(this).apply {
                                            text = String.format("Address: %s", address)
                                            setTextColor(Color.WHITE)
                                            textSize = 18F
                                            maxLines = 1
                                            ellipsize = TextUtils.TruncateAt.END
                                            typeface = ardentBook
                                        }

                                        val roomV = TextView(this).apply{
                                            text = String.format("Room: %s", room)
                                            setTextColor(Color.WHITE)
                                            textSize = 14F
                                            typeface = ardentBook
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

                                        val gradientDrawable = GradientDrawable(
                                            GradientDrawable.Orientation.TOP_BOTTOM,
                                            intArrayOf(
                                                ContextCompat.getColor(this, android.R.color.transparent), // Top color (transparent)
                                                ContextCompat.getColor(this, android.R.color.black) // Bottom color (dark)
                                            )
                                        )
                                        gradientDrawable.cornerRadius = 16f
                                        imgV.foreground = gradientDrawable

                                        Glide.with(this).load(apartmentImg).placeholder(R.drawable.house_icon).into(imgV)

                                        frameL.addView(imgV)
                                        infoL.addView(rentV)

                                        if(room.isNotEmpty()){
                                            infoL.addView(roomV)
                                        }

                                        frameL.addView(infoL)
                                        cardV.addView(frameL)
                                        rentedL.addView(cardV)

                                        if(status != "pending"){
                                            cardV.setOnClickListener {
                                                val intent  = Intent(this, ProfileDetailedViewActivity::class.java)
                                                intent.putExtra("apartmentID", apartmentID)
                                                intent.putExtra("email", userEmail)
                                                intent.putExtra("apartmentPhoto", apartmentImg)
                                                intent.putExtra("address", address)
                                                intent.putExtra("outstandingBalance", outBal)
                                                intent.putExtra("room", room)
                                                intent.putExtra("dueDate", dueDate)
                                                startActivity(intent)
                                            }

                                        }
                                        else{
                                            cardV.setOnClickListener{
                                                processRentalPayment(it, apartmentID, userEmail, apartmentImg
                                                    ,address,room, payLater )
                                            }
                                        }

                                    }
                                }
                            }
                        }

                }

            }
        }
    }

    private fun editProfile(view: View, photoURL: String, lName: String,
    fName: String, contactNum: String, birthDate: String, occupation: String, userEmail: String) {

        //popup xml==============================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_edit_profile, null)
        val pWindow = PopupWindow(pView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------//

        val userImg = pView.findViewById<ImageView>(R.id.userImage)
        val fNameET = pView.findViewById<EditText>(R.id.fnameField)
        val lNameET = pView.findViewById<EditText>(R.id.lnameField)
        val contactNumET = pView.findViewById<EditText>(R.id.contactNumField)
        val birthDateET = pView.findViewById<EditText>(R.id.birthDateField)
        val occupationET = pView.findViewById<EditText>(R.id.occupationField)
        val okBtn = pView.findViewById<Button>(R.id.okButton)

        Glide.with(this).load(photoURL).placeholder(R.drawable.profile).into(userImg)
        fNameET.setText(fName); lNameET.setText(lName)
        contactNumET.setText(contactNum); birthDateET.setText(birthDate)
        occupationET.setText(occupation)

        contactNumET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (isValidContactNumber(input)) {
                    contactNumET.error = null // Clear the error if valid
                } else {
                    contactNumET.error = "Invalid contact number"
                } }
            override fun afterTextChanged(s: Editable?) {}
        })

        okBtn.setOnClickListener {
            val newFName = fNameET.text.toString().trim()
            val newLName = lNameET.text.toString().trim()
            val newContact = contactNumET.text.toString().trim()
            val newDate = birthDateET.text.toString().trim()
            val newOccupation = occupationET.text.toString().trim()

            val data = hashMapOf("lastName" to newLName, "firstName" to newFName, "contactNum" to newContact
            , "birthDate" to newDate, "occupation" to newOccupation)

            if(newFName.isNotEmpty() && newLName.isNotEmpty() && (newContact.isNotEmpty() || contactNumET.error == null)
                && newDate.isNotEmpty() && newOccupation.isNotEmpty()){
                db.collection("account").document(userEmail).set(data, SetOptions.merge())
                pWindow.dismiss()
                it.invalidate()
            }
            else Toast.makeText(baseContext, "Please fill in the necessary fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processRentalPayment(view: View, apartmentID: String, userEmail: String, apartmentImg: String, address: String, room: String, payLater: Boolean) {
        //popup xml==============================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_process_rental_downpayment, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------//

        //xml====================================================//
        val apartImg = pView.findViewById<ImageView>(R.id.apartmentImage)
        val adrTxt = pView.findViewById<TextView>(R.id.addressText)
        val descTxt = pView.findViewById<TextView>(R.id.descriptionText)
        val payBtn = pView.findViewById<Button>(R.id.processDownPaymentButton)
        val backBtn = pView.findViewById<Button>(R.id.backButton)
        //-------------------------------------------------------//

        //initialization=========================================//
        if(payLater){
            descTxt.text = String.format(Locale.US, "You have not paid for the down payment required for this apartment." +
                    " Make sure to pay via GCash or any other payment method")
        }
        else {
            descTxt.text = String.format(Locale.US, "Your rental request is still on pending. Please wait for " +
                    "the administrator's notification for the update")
            payBtn.visibility = View.GONE
        }
        //-------------------------------------------------------//

        payBtn.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("email", userEmail)
            intent.putExtra("apartmentID", apartmentID)
            startActivity(intent)
        }
        backBtn.setOnClickListener {
            pWindow.dismiss()
        }
    }

    private fun convert(num: Int, context: Context): Int {
        return(num * context.resources.displayMetrics.density).toInt()
    }

    private fun isValidContactNumber(contactNum: String):Boolean{
        val regex = "^[+]?[0-9]{11}$".toRegex()
        return regex.matches(contactNum)
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