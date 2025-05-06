package com.example.android_finals_test

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes.Margins
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginEnd
import java.sql.Time
import java.time.ZoneId


class ApartmentViewingActivity: AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private var isOccupied: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val monthlyNotify = applicationContext.getSharedPreferences("monthlyNotifyCheck", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_apartment_viewing)
        val thisIntent: Intent = intent
        val loggedAsAdmin = thisIntent.getBooleanExtra("loggedAsAdmin", false)
        val addBtn = findViewById<Button>(R.id.addButton)
        val backBtn = findViewById<Button>(R.id.backButton)
        val apartmentL = findViewById<LinearLayout>(R.id.apartmentList)
        //for admins------------------------------------------------------------//
        val manageL = findViewById<LinearLayout>(R.id.rentalManagementLayout)
        val rentViewBtn = findViewById<ImageView>(R.id.viewRentalsImage)
        val rentReqBtn = findViewById<ImageView>(R.id.viewRentalRequestImage)
        //----------------------------------------------------------------------//

        setList(apartmentL,loggedAsAdmin)
        addBtn.visibility = if(loggedAsAdmin){View.VISIBLE}else{View.GONE}
        manageL.visibility = if(loggedAsAdmin){View.VISIBLE}else{View.GONE}

        addBtn.setOnClickListener {
            val intent = Intent(this, ApartmentCreateActivity::class.java)
            startActivity(intent)
        }
        backBtn.setOnClickListener {
            finish()
        }
        rentViewBtn.setOnClickListener{apartmentL.removeAllViews(); setList(apartmentL, loggedAsAdmin)}
        rentReqBtn.setOnClickListener { apartmentL.removeAllViews(); setRequestList(apartmentL, monthlyNotify)}
    }

    //apartment viewing-------------------------------------------------------------------------------------------//
    private fun setList(apartmentL: LinearLayout, loggedAsAdmin: Boolean) {
        //obtaining data from db==================================================
        db.collection("apartment").get().addOnSuccessListener {
            result->
            for(doc in result){
                val addressID = doc.id
                val address = doc.data["address"].toString()
                val city = doc.data["city"].toString()
                val province = doc.data["province"].toString()
                val photoURL = doc.data["photoURL"].toString()
                val phoneNum = doc.data["phoneNum"].toString()
                val price = doc.data["price"].toString()
                val rooms = doc.data["rooms"] as HashMap<String, Any>
                val isOccupied = doc.data["isOccupied"].toString().toBoolean()
                val type = doc.data["type"].toString()

                //setting up xml elements-------------------------
                val ardentBook = ResourcesCompat.getFont(this, R.font.ardent_sans_book)

                if(!isOccupied || type != "house"){
                    val cardV = CardView(this).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.card_margin_bottom))
                            setPadding(convert(20, context),convert(20, context),convert(20, context),convert(20, context))
                        }
                        setOnClickListener{fullView(it, address, city, province, price, phoneNum, addressID, photoURL, rooms, type, loggedAsAdmin, isOccupied)}
                    }
                    val contentL = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    }
                    val imgCardV = CardView(this).apply {
                        radius = 10F
                        layoutParams = LayoutParams(convert(120, context), convert(120, context)).apply {
                            setMargins(convert(10, context), convert(10, context), convert(10, context), convert(10, context))
                        }
                    }
                    val imgV = ImageView(this).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    Glide.with(this).load(photoURL).placeholder(R.drawable.house_icon).into(imgV)
                    imgCardV.addView(imgV)

                    val detailL= LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    }
                    val adrV = TextView(this).apply {
                        textSize = 20F
                        text = address
                        typeface = ardentBook
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }
                    val space = Space(this).apply {
                        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convert(10,context))
                    }
                    val cityV = TextView(this).apply {
                        textSize = 14F
                        text = city
                        setTextColor(Color.GRAY)
                        typeface = ardentBook}
                    val space2 = Space(this).apply {
                        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convert(5,context))
                    }

                    val provinceV = TextView(this).apply {
                        textSize = 14F
                        text = province
                        setTextColor(Color.GRAY)
                        typeface = ardentBook}

                    val space3 = Space(this).apply {
                        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convert(10,context))
                    }
                    val priceV = TextView(this).apply {
                        textSize = 18F
                        text = String.format(Locale.US, "PHP %.2f", price.toDoubleOrNull()?: 0.00)
                        typeface = ardentBook
                        textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                    }
                    detailL.addView(adrV); detailL.addView(space);detailL.addView(cityV); detailL.addView(space2);
                    detailL.addView(provinceV); detailL.addView(space3); detailL.addView(priceV)
                    contentL.addView(imgCardV); contentL.addView(detailL)
                    cardV.addView(contentL); apartmentL.addView(cardV)
                }
            }
        }
        //=========================================================================
    }

    private fun fullView(view: View, address: String, city: String, province: String, price: String, phoneNum: String, addressID: String,
      photoURL: String, rooms: HashMap<String, Any>, type: String, loggedAsAdmin: Boolean, isOccupied: Boolean) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.activity_full_view_apartment, null)
        val pWindow = PopupWindow(pView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.white))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        //============================================================================================

        //popup window xml================================================
        val logoutBtn = pView.findViewById<Button>(R.id.mainLogoutButton)
        val rentalImg = pView.findViewById<ImageView>(R.id.rentalImage)
        val addressTxt = pView.findViewById<TextView>(R.id.addressText)
        val cityTxt = pView.findViewById<TextView>(R.id.cityText)
        val provinceTxt = pView.findViewById<TextView>(R.id.provinceText)
        val priceTxt = pView.findViewById<TextView>(R.id.priceText)
        val phoneTxt = pView.findViewById<TextView>(R.id.contactNumText)
        val roomL = pView.findViewById<LinearLayout>(R.id.availableRooms)
        val rentBtn = pView.findViewById<Button>(R.id.rentButton)
        val updateBtn = pView.findViewById<Button>(R.id.updateButton)
        val delBtn = pView.findViewById<Button>(R.id.deleteButton)
        val backBtn = pView.findViewById<Button>(R.id.backButton)
        //================================================================

        //Rent button configurations================================================//
        rentBtn.visibility = if(type == "apartment" || loggedAsAdmin){View.GONE}else{View.VISIBLE}
        delBtn.visibility = if(!loggedAsAdmin){View.GONE}else{View.VISIBLE}
        updateBtn.visibility = if(!loggedAsAdmin){View.GONE}else{View.VISIBLE}

        rentBtn.setOnClickListener {
            val intent = Intent(this, RentApartmentActivity::class.java)
            intent.putExtra("addressID", addressID)
            intent.putExtra("address", address)
            intent.putExtra("type", type)
            intent.putExtra("price", price)
            startActivity(intent)
        }
        updateBtn.setOnClickListener {
            val intent = Intent(this, ApartmentUpdateActivity::class.java)
            intent.putExtra("apartmentID", addressID)
            startActivity(intent)
        }
        //--------------------------------------------------------------------------//


        //displays==================================================================//
        Glide.with(this).load(photoURL).placeholder(R.drawable.house_icon).into(rentalImg)
        addressTxt.text = String.format(Locale.US, "%s", address)
        cityTxt.text = String.format(Locale.US, "%s", city)
        provinceTxt.text = String.format(Locale.US, "%s", province)
        priceTxt.text = String.format(Locale.US, "Monthly Price: PHP %.2f", price.toDouble())
        phoneTxt.text = String.format(Locale.US, "Owner's Contact #: %s", phoneNum)
        //--------------------------------------------------------------------------//

        //iterating through the rooms hashmap to get details=========================================

        for(key in rooms.keys){
            println (key)
            val innerMap = rooms[key] as HashMap<*,*>
            val apartmentPhoto = innerMap["photoURL"].toString()
            val amenities = innerMap["amenities"] as HashMap<String, Any>
            val isRoomOccupied = innerMap["isOccupied"].toString().toBoolean()
            val contactNum = innerMap["contactNum"].toString()
            val description = innerMap["description"].toString()
            val hasBalcony = innerMap["hasBalcony"].toString().toBoolean()
            val hasWIFI = innerMap["hasInternet"].toString().toBoolean()
            val roomNum = innerMap["roomNumber"].toString()

            //displays----------------------------------------------------------------------
            if(!isRoomOccupied){
                val cardV = CardView(this).apply {
                    layoutParams = LayoutParams(convert(180, context), LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(convert(5,context),0,convert(5,context),0)
                    };cardElevation = 4F
                    radius = 16F}

                val roomNV = TextView(this).apply {
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                    gravity = Gravity.BOTTOM
                    text = roomNum
                    textSize = 20F
                    setTextColor(Color.WHITE)
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

                Glide.with(this).load(apartmentPhoto).placeholder(R.drawable.room).into(imgV)
                cardV.setOnClickListener {
                    viewIndividualRooms(view, address, addressID, type, price, apartmentPhoto, roomNum, contactNum, description,
                        amenities, hasBalcony, hasWIFI, loggedAsAdmin)
                }
                frameL.addView(imgV);
                frameL.addView(roomNV);
                cardV.addView(frameL);
                roomL.addView(cardV)
            }
        }
        //delete configuration----------------------------------//
        delBtn.setOnClickListener {
            if (type == "apartment"){
                var isStillOccupied = false
                for(key in rooms.keys){
                    val innerMap = rooms[key] as HashMap<*,*>
                    val isRoomOccupied = innerMap["isOccupied"].toString().toBoolean()
                    if(isRoomOccupied){
                        isStillOccupied = true
                        break
                    }
                }
                if(!isStillOccupied){
                    db.collection("apartment").document(addressID).delete().addOnSuccessListener {
                        Toast.makeText(baseContext,"Successfully deleted", Toast.LENGTH_SHORT).show()
                    }
                }else Toast.makeText(baseContext,"Error: Could not delete apartment with occupied tenants", Toast.LENGTH_SHORT).show()
            }
            else{
                if(!isOccupied){
                    db.collection("apartment").document(addressID).delete().addOnSuccessListener {
                        Toast.makeText(baseContext,"Successfully deleted", Toast.LENGTH_SHORT).show()
                    }
                }else Toast.makeText(baseContext,"Error: Could not delete apartment with occupied tenants", Toast.LENGTH_SHORT).show()
            } }
        //------------------------------------------------------//
        //==========================================================================================

        //button events=============================================================================
        backBtn.setOnClickListener {
            pWindow.dismiss()
        }
        logoutBtn.setOnClickListener{
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        //==========================================================================================
    }

    private fun viewIndividualRooms(view: View, address: String, addressID: String, type: String,
        price: String, imgURL: String, roomNum: String, contactNum: String, description: String,
        amenities: HashMap<String, Any>, hasBalcony: Boolean,
        hasWIFI: Boolean, loggedAsAdmin: Boolean) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_view_individual_rooms, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //xml===============================================//
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        val roomImg = pView.findViewById<ImageView>(R.id.roomImage)
        val roomNoTxt = pView.findViewById<TextView>(R.id.roomNoText)
        val descTxt = pView.findViewById<TextView>(R.id.descriptionText)
        val amenityL = pView.findViewById<LinearLayout>(R.id.amenitiesList)
        val delBtn = pView.findViewById<Button>(R.id.deleteButton)
        val rentBtn = pView.findViewById<Button>(R.id.rentButton)
        //==================================================//
        
        //setting displays========================================================================//
        Glide.with(this).load(imgURL).placeholder(R.drawable.house_icon).into(roomImg)
        roomNoTxt.text = roomNum
        descTxt.text = description

        if(amenities.isNotEmpty()){
            for(i in amenities.keys){
                val innerMap = amenities[i] as HashMap<String, Any>
                innerMap.let {
                    val amenityType = it["type"].toString()
                    val desc = it["description"].toString()
                    val qty = it["quantity"].toString()
                    val layout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                    }
                    val descV = TextView(this).apply {
                        text = if(amenityType=="furniture"){" $qty $desc"}else{" $desc"}
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        textSize = 18F
                    }
                    layout.addView(descV); amenityL.addView(layout)
                } } }
        //==========================================================================================
        rentBtn.visibility = if(loggedAsAdmin || type == "house"){View.GONE}else{View.VISIBLE}
        delBtn.visibility = if(!loggedAsAdmin){View.GONE}else{View.VISIBLE}
        //button events=============================================================================
        xImg.setOnClickListener {
            pWindow.dismiss()
        }
        rentBtn.setOnClickListener {
            val intent = Intent(this, RentApartmentActivity::class.java)
            intent.putExtra("addressID", addressID)
            intent.putExtra("address", address)
            intent.putExtra("roomNum", roomNum)
            intent.putExtra("type", type)
            intent.putExtra("price", price)
            startActivity(intent)
        }
        //==========================================================================================
    }
    //-------------------------------------------------------------------------------------------------------------//

    //rental request viewing---------------------------------------------------------------------------------------//
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setRequestList(apartmentL: LinearLayout, monthlyNotify: SharedPreferences){
        val db = Firebase.firestore
        val currDate = LocalDate.now()
        db.collection("request").whereEqualTo("status", "pending").get().addOnSuccessListener { result ->
            for(request in result){
                val requestID = request.id
                val userEmail = request.data["email"].toString()
                val rentalID = request.data["apartmentID"].toString()
                val name = request.data["displayName"].toString()
                val content = request.data["content"].toString()
                val date = request.data["date"] as Timestamp
                val localDate = date.toInstant().atZone(ZoneId.systemDefault())

                db.collection("users").whereEqualTo("email", userEmail).get().addOnSuccessListener { result2 ->
                    for(users in result2){
                        val userImg = users.data["photoUrl"].toString()

                        val cardV = CardView(this).apply {
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.card_margin_bottom))
                                setPadding(convert(20, context),convert(20, context),convert(20, context),convert(20, context))
                            } }
                        val contentL = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        }
                        val imgCardV = CardView(this).apply {
                            radius = 10F
                            layoutParams = LayoutParams(convert(120, context), convert(120, context)).apply {
                                setMargins(convert(10, context), convert(10, context), convert(10, context), convert(10, context))
                            }
                        }

                        val imgV = ImageView(this).apply {
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        Glide.with(this).load(userImg).placeholder(R.drawable.profile).into(imgV)

                        val detailL= LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        }

                        val descV = TextView(this).apply {
                            text = String.format(Locale.US, "Request from $name")
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                                setMargins(0,0,0,convert(10,context))
                            }
                        }

                        val msgV = TextView(this).apply {
                            text = String.format(Locale.US, content)
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        }

                        val space = Space(this).apply {
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, convert(20, context))

                        }

                        val dateV = TextView(this).apply {
                            text = localDate.toString()
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                        }

                        detailL.addView(descV); detailL.addView(msgV); detailL.addView(space); detailL.addView(dateV)
                        imgCardV.addView(imgV); contentL.addView(imgCardV); contentL.addView(detailL)
                        cardV.addView(contentL); apartmentL.addView(cardV)
                        cardV.setOnClickListener {
                            val intent = Intent(this, ViewFullRequestActivity::class.java)
                            intent.putExtra("requestID", requestID)
                            intent.putExtra("email", userEmail)
                            intent.putExtra("apartmentID", rentalID)
                            intent.putExtra("content", content)
                            startActivity(intent)
                        } } } } }
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


