package com.example.android_finals_test

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ApartmentUpdateActivity: AppCompatActivity(){
    private val db = Firebase.firestore
    private lateinit var roomImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartment_set)
        //xml elements=========================================================================
        val apartmentImg = findViewById<ImageView>(R.id.apartmentImage)
        val addressET = findViewById<EditText>(R.id.addressField)
        val cityET = findViewById<EditText>(R.id.cityField)
        val provinceET = findViewById<EditText>(R.id.provinceField)
        val priceET = findViewById<EditText>(R.id.priceField)
        val ownerNameET = findViewById<EditText>(R.id.ownerNameField)
        val contactET = findViewById<EditText>(R.id.contactNumField)
        val descET = findViewById<EditText>(R.id.descriptionField)
        val apartmentTypeL = findViewById<LinearLayout>(R.id.apartmentTypeLayout)
        val setBtn = findViewById<Button>(R.id.setButton)
        val contentL = findViewById<LinearLayout>(R.id.mainContentList) //to display created rooms
        val addApartment = findViewById<Button>(R.id.addApartmentButton)
        val cancelBtn = findViewById<Button>(R.id.backButton)
        //======================================================================================

        //text watchers=========================================================================//
        contactET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (isValidContactNumber(input)) {
                    contactET.error = null // Clear the error if valid
                } else {
                    contactET.error = "Invalid contact number"
                } }
            override fun afterTextChanged(s: Editable?) {}
        })

        priceET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) return

                val input = s.toString()
                if (!isValidDecimal(input)) {
                    priceET.error = "Only up to 2 decimal places allowed"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) return
                val input = s.toString()
                if (!isValidDecimal(input)) {
                    // Remove extra characters
                    val trimmedInput = input.substring(0, input.length - 1)
                    priceET.setText(trimmedInput)
                    priceET.setSelection(trimmedInput.length) // Reset cursor position
                }
            }
            private fun isValidDecimal(input: String): Boolean {
                return input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))
            }
        })
        //--------------------------------------------------------------------------------------//

        //initialization-----------------------------//
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        apartmentTypeL.visibility = View.GONE
        db.collection("apartment").document(apartmentID).get().addOnSuccessListener { result ->
            result?.let { it ->
                val address = it["address"].toString()
                val city = it["city"].toString()
                val province = it["province"].toString()
                val price = it["price"].toString().toDoubleOrNull()?: 0.00
                val ownerName = it["ownerName"].toString()
                val phoneNum = it["phoneNum"].toString()
                val desc = it["description"].toString()
                val roomData = it.get("rooms") as HashMap<String, HashMap<String, Any>>
                val type = it["type"].toString()

                addressET.setText(address); cityET.setText(city); provinceET.setText(province)
                priceET.setText(String.format("%.2f", price))
                ownerNameET.setText(ownerName)
                contactET.setText(phoneNum)
                descET.setText(desc)
                addApartment.text = String.format("Update Apartment")

                for(key in roomData.keys){
                    val roomNum = roomData[key]
                    roomNum?.let {
                        val roomNo = it["roomNumber"].toString()
                        val isOccupied = it["isOccupied"].toString().toBoolean()
                        val amenityData = it["amenities"] as HashMap<String, HashMap<String, Any>>

                        val cardV = CardView(this).apply {
                            layoutParams = LayoutParams(convert(120,context), LayoutParams.MATCH_PARENT
                            ).apply {
                                setMargins(convert(10, context), 0, convert(10, context), 0)
                            }
                        }
                        val infoL = LinearLayout(this).apply {
                            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                            gravity = Gravity.CENTER_HORIZONTAL
                            orientation = LinearLayout.VERTICAL
                            setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                        }
                        val img = ImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                convert(90, context), convert(90, context))
                        }
                        Glide.with(this).load("").placeholder(R.drawable.room).into(img)
                        val roomNV = TextView(this).apply{ text =
                            if(isOccupied){
                                String.format("$roomNo (Occupied)")
                            }else roomNo
                            typeface = Typeface.DEFAULT_BOLD}

                        val delBtn = TextView(this).apply { text = String.format("Remove")
                            typeface = Typeface.DEFAULT_BOLD; textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                            setOnClickListener{
                                if(!isOccupied){
                                    contentL.removeView(cardV)
                                    roomData.remove(roomNo)
                                }
                                else Toast.makeText(baseContext, "Cannot remove an occupied room", Toast.LENGTH_SHORT).show()
                            }
                        }

                        cardV.setOnClickListener {//when clicking to edit information on the newly created CardV
                            editExistingRoom(it, roomNV, roomData, amenityData, type) }

                        infoL.addView(img); infoL.addView(roomNV); infoL.addView(delBtn)
                        cardV.addView(infoL)
                        contentL.addView(cardV)
                    }
                }
                //button clicking events----------------------//
                cancelBtn.setOnClickListener {
                    finish()
                }
                setBtn.setOnClickListener {
                    addRoom(it, contentL, roomData, type)
                }
                addApartment.setOnClickListener {
                    val newImgURL = ""
                    val newAddress = addressET.text.toString().trim()
                    val newCity = cityET.text.toString().trim()
                    val newProvince = provinceET.text.toString().trim()
                    val newPrice = priceET.text.toString().trim()
                    val newOwnerName = ownerNameET.text.toString().trim()
                    val newPhoneNum = contactET.text.toString().trim()
                    val newDescription = descET.text.toString().trim()
                    if(newAddress.isNotEmpty() && newCity.isNotEmpty() && newProvince.isNotEmpty() && newPrice.isNotEmpty() && newPhoneNum.isNotEmpty() && roomData.isNotEmpty()){
                        val priceNum = newPrice.toFloatOrNull()?: 0.00f
                        db.collection("apartment").document(apartmentID).update("photoURL", newImgURL)
                        db.collection("apartment").document(apartmentID).update("address", newAddress)
                        db.collection("apartment").document(apartmentID).update("city", newCity)
                        db.collection("apartment").document(apartmentID).update("province", newProvince)
                        db.collection("apartment").document(apartmentID).update("price", priceNum)
                        db.collection("apartment").document(apartmentID).update("ownerName", newOwnerName)
                        db.collection("apartment").document(apartmentID).update("phoneNum", newPhoneNum)
                        db.collection("apartment").document(apartmentID).update("description", newDescription)
                        db.collection("apartment").document(apartmentID).update("rooms", roomData)
                        finish()
                        val intent = Intent(this, ApartmentViewingActivity::class.java)
                        intent.putExtra("loggedAsAdmin", true)
                        startActivity(intent)
                    }
                    else Toast.makeText(baseContext, "Please do not leave the data as empty", Toast.LENGTH_SHORT).show()
                }
                //--------------------------------------------//
            } } }

    private fun addRoom(view: View, contentL: LinearLayout, roomData: HashMap<String, HashMap<String, Any>>, chosenType: String) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_add_room, null)
        val pWindow = PopupWindow(
            pView,
            convert(400, context = this),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================

        //popup xml===============================================================================================
        val roomImg = pView.findViewById<ImageView>(R.id.addRoomImage)
        val labelTxt = pView.findViewById<TextView>(R.id.typeLabel)
        val roomNumET = pView.findViewById<EditText>(R.id.roomNumField)
        val amenityBtn = pView.findViewById<Button>(R.id.manageAmenitiesButton)
        val amenityL = pView.findViewById<LinearLayout>(R.id.amenitiesList)

        val descET = pView.findViewById<EditText>(R.id.descriptionField)
        val addBtn = pView.findViewById<Button>(R.id.addRoomButton)
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        addBtn.text = "Add Room"
        //=========================================================================================================

        //hashMaps===============================================//
        val amenityData = HashMap<String, HashMap<String, Any>>()
        //=======================================================//

        //initialization=====================
        if (chosenType == "apartment") {
            roomNumET.hint = "Room Number"
            labelTxt.text = "Room No: "
        } else {
            roomNumET.hint = "Room Name"
            labelTxt.text = "Type: "
        }
        //===================================

        //button events============================================================================================
        amenityBtn.setOnClickListener { setAmenities(view, amenityData, amenityL) }
        addBtn.setOnClickListener {
            val imageURL = ""
            val roomNo = roomNumET.text.toString()

            val desc = descET.text.toString()
            if (roomNo != "") {
                if (chosenType == "apartment") {
                    roomData[roomNo] = hashMapOf(
                        "photoURL" to imageURL, "roomNumber" to roomNo, "amenities" to amenityData,
                        "description" to desc, "isOccupied" to false
                    )
                } else {
                    roomData[roomNo] = hashMapOf(
                        "photoURL" to imageURL,
                        "roomNumber" to roomNo,
                        "amenities" to amenityData,
                        "description" to desc
                    )
                }

                val cardV = CardView(this).apply {
                    layoutParams = LayoutParams(
                        convert(120, context), LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(convert(10, context), 0, convert(10, context), 0)
                    }
                }
                val infoL = LinearLayout(this).apply {
                    layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    gravity = Gravity.CENTER_HORIZONTAL
                    orientation = LinearLayout.VERTICAL
                    setPadding(
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y),
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y)
                    )
                }

                val img = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        convert(90, context), convert(90, context)
                    )
                }
                Glide.with(this).load("").placeholder(R.drawable.room).into(img)
                val roomNV =
                    TextView(this).apply {
                        text = roomNo; typeface = Typeface.DEFAULT_BOLD }

                val delBtn = TextView(this).apply {
                    text = "Remove"; typeface = Typeface.DEFAULT_BOLD; textAlignment =
                    TextView.TEXT_ALIGNMENT_TEXT_END
                    setOnClickListener {
                        contentL.removeView(cardV)
                        roomData.remove(roomNo)
                    }
                }

                cardV.setOnClickListener {//when clicking to edit information on the newly created CardV
                    editExistingRoom(view, roomNV, roomData, amenityData, chosenType)
                }

                infoL.addView(img); infoL.addView(roomNV); infoL.addView(delBtn)
                cardV.addView(infoL)
                contentL.addView(cardV)
                pWindow.dismiss()
            } else {
                Toast.makeText(baseContext, "Please fill the necessary details", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        xImg.setOnClickListener {
            pWindow.dismiss()
        }
    }

    private fun editExistingRoom(view: View, roomNV: TextView, roomData: HashMap<String, HashMap<String, Any>>,
                                 amenityData: HashMap<String, HashMap<String, Any>>, chosenType: String) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_add_room, null)
        val pWindow = PopupWindow(
            pView,
            convert(400, context = this),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================

        //popup xml===============================================================================================
        val roomImg = pView.findViewById<ImageView>(R.id.addRoomImage)
        val roomNumET = pView.findViewById<EditText>(R.id.roomNumField)
        val amenityL = pView.findViewById<LinearLayout>(R.id.amenitiesList)
        val descET = pView.findViewById<EditText>(R.id.descriptionField)
        val updateBtn = pView.findViewById<Button>(R.id.addRoomButton)
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        updateBtn.text = "Update Room"
        //=========================================================================================================

        //others=================================================================
        val currRoom = roomNV.text.toString()
        val roomNo: String;
        val checkB: Boolean;
        val checkI: Boolean;
        val desc: String  //data holders
        val chosenRoom = roomData[currRoom]
        chosenRoom.let { //load existing data
            roomNo = it?.get("roomNumber")?.toString() ?: ""
            checkB = it?.get("hasBalcony")?.toString().toBoolean()
            checkI = it?.get("hasInternet")?.toString().toBoolean()
            desc = it?.get("description")?.toString() ?: ""
        }
        //=======================================================================

        //set current data to the fields============================
        roomNumET.setText(roomNo)
        descET.setText(desc)
        //==========================================================

        //initialization----------------------//
        if (chosenType == "apartment") {
            roomNumET.hint = "Room Name"
        } else {
            roomNumET.hint = "Room No."
        }
        //-----------------------------------//

        //loading amenities------------------//
        if (amenityData.isNotEmpty()) {
            for (key in amenityData.keys) {
                val innerMap = amenityData[key]
                innerMap?.let {
                    val description = it["description"].toString()
                    val qty = it["quantity"].toString().toIntOrNull() ?: 0

                    val cardV = CardView(this).apply {
                        layoutParams = LayoutParams(
                            convert(120, context), LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(convert(5, context), 0, convert(5, context), 0)
                        }
                        cardElevation = 4F
                    }
                    val infoL = LinearLayout(this).apply {
                        layoutParams =
                            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        gravity = Gravity.CENTER_HORIZONTAL
                        orientation = LinearLayout.VERTICAL
                        setPadding(
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y),
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y)
                        )
                    }
                    val imgV = ImageView(this).apply {
                        layoutParams =
                            LinearLayout.LayoutParams(convert(90, context), convert(90, context))
                    }
                    Glide.with(this).load("").placeholder(R.drawable.img).into(imgV)
                    val descV = TextView(this).apply {
                        text = description
                    }
                    val removeBtn = TextView(this).apply {
                        text = "Remove"
                        setOnClickListener {
                            amenityL.removeView(cardV)
                            amenityData.remove(description)
                        }
                    }
                    cardV.setOnClickListener {
                        editAmenities(view, amenityData, description, descV)
                    }
                    infoL.addView(imgV); infoL.addView(descV); infoL.addView(removeBtn)
                    cardV.addView(infoL); amenityL.addView(cardV)
                }
            }
        }
    }

    private fun addHouseRoom(view: View, contentL: LinearLayout, roomData: HashMap<String, HashMap<String, Any>>, chosenType: String){
        //popup xml==============================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_add_house_room, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------//

        //popup xml==============================================//
        roomImg = pView.findViewById(R.id.addRoomImage)
        val roomTypeSpin = pView.findViewById<Spinner>(R.id.roomTypeField)
        val amenityBtn = pView.findViewById<Button>(R.id.manageAmenitiesButton)
        val amenityL = pView.findViewById<LinearLayout>(R.id.amenitiesList)
        val descET = pView.findViewById<EditText>(R.id.descriptionField)
        val addBtn = pView.findViewById<Button>(R.id.addRoomButton)
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        addBtn.text = "Add Room"
        //-------------------------------------------------------//

        //hashMaps===============================================//
        val amenityData = HashMap<String, HashMap<String, Any>>()
        val options = listOf("Kitchen", "Bedroom", "Dining Room", "Laundry Room", "Basement", "Bathroom")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomTypeSpin.adapter = adapter
        //-------------------------------------------------------//

        //button events==========================================//
        amenityBtn.setOnClickListener { setAmenities(view, amenityData, amenityL) }

        addBtn.setOnClickListener {
            val imageURL = ""
            val roomType = roomTypeSpin.selectedItem.toString().trim()
            val desc = descET.text.toString()
            if(roomType.isNotEmpty()){
                if(!roomData.containsKey(roomType)){ //check if a specific room type is already in the hashmap
                    roomData[roomType] = hashMapOf("photoURL" to imageURL,"roomNumber" to roomType, "amenities" to amenityData, "description" to desc)
                    val cardV = CardView(this).apply {
                        layoutParams = LayoutParams(convert(120,context), LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.card_margin_bottom))
                        }
                    }
                    val infoL = LinearLayout(this).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        gravity = Gravity.CENTER_HORIZONTAL
                        orientation = LinearLayout.VERTICAL
                        setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x), resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                    }
                    val img = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            convert(90, context), convert(90, context))
                    }
                    Glide.with(this).load("").placeholder(R.drawable.room).into(img)
                    val roomNV = TextView(this).apply{ text = roomType; typeface = Typeface.DEFAULT_BOLD}

                    val delBtn = TextView(this).apply { text = "Remove"; typeface = Typeface.DEFAULT_BOLD; textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                        setOnClickListener{ contentL.removeView(cardV)
                            roomData.remove(roomType)} }

                    cardV.setOnClickListener {//when clicking to edit information on the newly created CardV
                        editExistingHouseRoom(view, roomNV, roomData, amenityData, chosenType) }

                    infoL.addView(img); infoL.addView(roomNV); infoL.addView(delBtn)
                    cardV.addView(infoL)
                    contentL.addView(cardV)
                    pWindow.dismiss()
                }
                else Toast.makeText(baseContext,"Room already exist",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(baseContext,"Please fill the necessary details",Toast.LENGTH_SHORT).show()
            }
        }
        xImg.setOnClickListener {
            pWindow.dismiss()
        }
        //-------------------------------------------------------//
    }

    private fun editExistingHouseRoom(view: View, roomNV: TextView, roomData: HashMap<String, HashMap<String, Any>>,
                                      amenityData: HashMap<String, HashMap<String, Any>>, chosenType: String) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_add_house_room, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================

        //popup xml===============================================================================================
        val roomImg = pView.findViewById<ImageView>(R.id.addRoomImage)
        val roomTypeSpin = pView.findViewById<Spinner>(R.id.roomTypeField)
        val amenityL = pView.findViewById<LinearLayout>(R.id.amenitiesList)
        val descET = pView.findViewById<EditText>(R.id.descriptionField)
        val updateBtn = pView.findViewById<Button>(R.id.addRoomButton)
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        updateBtn.text = "Update Room"
        //=========================================================================================================
        val options = listOf("Kitchen", "Bedroom", "Dining Room", "Laundry Room", "Basement", "Bathroom")
        val position = options.indexOf(roomNV.toString())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomTypeSpin.adapter = adapter
        roomTypeSpin.setSelection(position)

        //others=================================================================
        val currRoom = roomNV.text.toString()
        val chosenRoom = roomData[currRoom] as HashMap<*,*>
        val oldDesc = chosenRoom["description"].toString()
        //=======================================================================

        //set current data to the fields============================
        descET.setText(oldDesc)
        //==========================================================

        //loading amenities------------------//
        if(amenityData.isNotEmpty()){
            for (key in amenityData.keys){
                val innerMap = amenityData[key]
                innerMap?.let{
                    val description = it["description"].toString()
                    val cardV = CardView(this).apply {
                        layoutParams = LayoutParams(convert(120, context), LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(convert(5,context),0,convert(5,context),0)
                        }
                        cardElevation = 4F
                    }
                    val infoL = LinearLayout(this).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        gravity = Gravity.CENTER_HORIZONTAL
                        orientation = LinearLayout.VERTICAL
                        setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y),
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                            resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                    }
                    val imgV = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(convert(90, context), convert(90,context))
                    }
                    Glide.with(this).load(R.drawable.furniture_icon).into(imgV)
                    val descV = TextView(this).apply {
                        text = description
                    }
                    val removeBtn = TextView(this).apply {
                        text = "Remove"
                        setOnClickListener{
                            amenityL.removeView(cardV)
                            amenityData.remove(description)
                        }
                    }
                    cardV.setOnClickListener {
                        editAmenities(view, amenityData, description, descV)
                    }
                    infoL.addView(imgV); infoL.addView(descV); infoL.addView(removeBtn)
                    cardV.addView(infoL); amenityL.addView(cardV)
                }
            }
        }
        //-----------------------------------//

        //button events=============================================
        updateBtn.setOnClickListener {
            val imageURL = ""
            val newRoomType = roomTypeSpin.selectedItem.toString()
            val newDesc = descET.text.toString().trim()
            roomData.remove(currRoom)
            roomData[newRoomType] = hashMapOf("photoURL" to imageURL,"roomNumber" to newRoomType, "amenities" to amenityData, "description" to newDesc)
            roomNV.text = newRoomType
        }
        xImg.setOnClickListener {
            pWindow.dismiss()
        }
        //==========================================================
    }

    //setting the amenities of the created room========================================================================
    private fun setAmenities(
        view: View, amenityData: HashMap<String, HashMap<String, Any>>, amenityL: LinearLayout) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_set_amenities_2, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================

        //xml elements=============================================================================//
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        val furnitureCheck = pView.findViewById<CheckBox>(R.id.furnitureCheckbox)
        val othersCheck = pView.findViewById<CheckBox>(R.id.othersCheckbox)
        val calculateCheck = pView.findViewById<CheckBox>(R.id.calculateCheckbox)
        val descET = pView.findViewById<EditText>(R.id.descriptionField)
        val qtyET = pView.findViewById<EditText>(R.id.quantityField)
        val qtyL = pView.findViewById<LinearLayout>(R.id.quantityLayout)
        val priceL = pView.findViewById<LinearLayout>(R.id.priceLayout)
        val priceET = pView.findViewById<EditText>(R.id.priceField)
        val okBtn = pView.findViewById<Button>(R.id.okButton)
        //=========================================================================================//

        //initialize----------------------------------//
        furnitureCheck.isChecked = true
        calculateCheck.visibility = View.GONE
        //--------------------------------------------//

        //events===================================================================================//
        xImg.setOnClickListener {
            pWindow.dismiss()
        }
        furnitureCheck.setOnClickListener {
            othersCheck.isChecked = false
            qtyL.visibility = View.VISIBLE
            calculateCheck.visibility = View.GONE
            priceL.visibility = View.VISIBLE
        }

        othersCheck.setOnClickListener {
            furnitureCheck.isChecked = false
            qtyL.visibility = View.GONE
            calculateCheck.visibility = View.VISIBLE
            priceL.visibility = View.GONE
        }
        calculateCheck.setOnClickListener {
            if(calculateCheck.isChecked){
                priceL.visibility = View.VISIBLE
            }
            else{
                qtyL.visibility = View.GONE
                priceL.visibility = View.GONE
            }
        }
        okBtn.setOnClickListener {
            val description = descET.text.toString().trim()
            val quantityNum: Int
            val priceNum: Double
            val ardent = ResourcesCompat.getFont(this, R.font.ardent_sans_light)
            if(description.isNotEmpty()){
                if(furnitureCheck.isChecked){
                    val quantity = qtyET.text.toString().trim()
                    val price = priceET.text.toString().trim()

                    if(quantity.isNotEmpty() && price.isNotEmpty()){
                        quantityNum = quantity.toInt()
                        priceNum = price.toDouble()
                        amenityData[description] = hashMapOf("description" to description, "type" to "furniture",
                            "quantity" to quantityNum, "price" to priceNum)
                    }
                    else Toast.makeText(baseContext, "Please fill in the quantity and price",Toast.LENGTH_SHORT).show()
                }
                else {
                    if(calculateCheck.isChecked){
                        val price = priceET.text.toString().trim()
                        if(price.isNotEmpty()){
                            priceNum = price.toDouble()
                            amenityData[description] = hashMapOf("description" to description, "type" to "othersWithPrice",
                                "price" to priceNum)
                        }
                        else Toast.makeText(baseContext, "Please fill in the price",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        amenityData[description] = hashMapOf("description" to description, "type" to "others")
                    }
                }
                val cardV = CardView(this).apply {
                    layoutParams = LayoutParams(convert(120, context), LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(convert(5,context),0,convert(5,context),0)
                    }
                    cardElevation = 4F
                }
                val infoL = LinearLayout(this).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    gravity = Gravity.CENTER_HORIZONTAL
                    orientation = LinearLayout.VERTICAL
                    setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y),
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                        resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                }
                val imgV = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(convert(90, context), convert(90,context))
                }
                Glide.with(this).load(R.drawable.furniture_icon).into(imgV)

                val descV = TextView(this).apply {
                    text = description
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 1
                    typeface = ardent
                }
                val removeBtn = TextView(this).apply {
                    text = "Remove"
                    setOnClickListener{
                        amenityL.removeView(cardV)
                        amenityData.remove(description)
                    } }

                cardV.setOnClickListener {
                    editAmenities(view, amenityData, description, descV)
                }
                infoL.addView(imgV); infoL.addView(descV); infoL.addView(removeBtn)
                cardV.addView(infoL); amenityL.addView(cardV)
                pWindow.dismiss()
            }
            else Toast.makeText(baseContext, "Please fill in the necessary details",Toast.LENGTH_SHORT).show()
        }
        //=========================================================================================//
    }
    //=================================================================================================================
    private fun editAmenities(view: View, amenityData: HashMap<String, HashMap<String, Any>>,
                              description: String, descV: TextView) {

        //popup window==============================================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_set_amenities_2, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------------------------//

        //xml elements=============================================================================//
        val xImg = pView.findViewById<ImageView>(R.id.xImage)
        val furnitureCheck = pView.findViewById<CheckBox>(R.id.furnitureCheckbox)
        val othersCheck = pView.findViewById<CheckBox>(R.id.othersCheckbox)
        val calculateCheck = pView.findViewById<CheckBox>(R.id.calculateCheckbox)
        val descET = pView.findViewById<EditText>(R.id.descriptionField)
        val qtyET = pView.findViewById<EditText>(R.id.quantityField)
        val qtyL = pView.findViewById<LinearLayout>(R.id.quantityLayout)
        val priceL = pView.findViewById<LinearLayout>(R.id.priceLayout)
        val priceET = pView.findViewById<EditText>(R.id.priceField)
        val okBtn = pView.findViewById<Button>(R.id.okButton)
        //=========================================================================================//

        //initialize----------------------------------//
        val innerMap = amenityData[description] as HashMap<*,*>

        val oldDescription = innerMap["description"].toString()
        val oldType = innerMap["type"].toString()
        val oldQty = innerMap["quantity"].toString()
        val oldPrice = innerMap["price"].toString()
        furnitureCheck.visibility = View.GONE; othersCheck.visibility = View.GONE; calculateCheck.visibility = View.GONE
        descET.setText(oldDescription)
        qtyET.setText(oldQty)
        priceET.setText(oldPrice)

        if(oldType == "othersWithPrice"){
            qtyET.visibility = View.GONE
        }
        else if(oldType == "others"){
            qtyET.visibility = View.GONE; priceET.visibility = View.GONE
        }
        //--------------------------------------------//

        //events===================================================================================//
        xImg.setOnClickListener { pWindow.dismiss() }

        okBtn.setOnClickListener {
            val newDescription = descET.text.toString().trim()

            if(newDescription.isNotEmpty()){
                if(oldType == "furniture"){
                    val newQty = qtyET.text.toString().trim()
                    val newPrice = priceET.text.toString().trim()
                    if(newQty.isNotEmpty() && newPrice.isNotEmpty()){
                        descV.text = newDescription
                        amenityData.remove(oldDescription)
                        amenityData[newDescription] = hashMapOf("description" to newDescription, "type" to "furniture",
                            "quantity" to newQty.toDouble(), "price" to newPrice.toDouble())
                    }
                    else Toast.makeText(baseContext, "Please fill in the empty fields",Toast.LENGTH_SHORT).show()
                }
                else if(oldType == "othersWithPrice"){
                    val newPrice = priceET.text.toString().trim()
                    if(newPrice.isNotEmpty()){
                        descV.text = newDescription
                        amenityData.remove(oldDescription)
                        amenityData[newDescription] = hashMapOf("description" to newDescription, "type" to "othersWithPrice",
                            "price" to newPrice.toDouble())
                    }
                    else Toast.makeText(baseContext, "Please fill in the empty fields",Toast.LENGTH_SHORT).show()
                }
                else{
                    amenityData.remove(oldDescription)
                    descV.text = newDescription
                    amenityData[newDescription] = hashMapOf("description" to newDescription, "type" to "others")
                }
                pWindow.dismiss()
            }
            else Toast.makeText(baseContext, "Please fill in the necessary details",Toast.LENGTH_SHORT).show()
        }
        //=========================================================================================//
    }


    //customs================================================================================
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

    private fun isValidContactNumber(contactNum: String):Boolean{
        val regex = "^[+]?[0-9]{10,15}$".toRegex()
        return regex.matches(contactNum)
    }
    //====================================================================================

}