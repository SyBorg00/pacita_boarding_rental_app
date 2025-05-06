package com.example.android_finals_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


//============================================================================================================
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val monthlyCalculation: SharedPreferences = applicationContext.getSharedPreferences("monthlyCalculate", Context.MODE_PRIVATE)
        val latePayCheck: SharedPreferences = applicationContext.getSharedPreferences("latePayDateCheck", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        createNotificationChannel()
        val currentUser = auth.currentUser
        manageDueDate(monthlyCalculation, latePayCheck)
//        val dueDateManager = PeriodicWorkRequest.Builder(DueDateManager::class.java, 24, TimeUnit.HOURS).build()
//        WorkManager.getInstance(applicationContext).enqueue(dueDateManager)
        if (currentUser == null) {
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        } else {
            val homepageActivity = Intent(this, HomepageActivity::class.java)
            startActivity(homepageActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun manageDueDate(
        monthlyCalculation: SharedPreferences,
        latePayCheck: SharedPreferences
    ) {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val currDateToString = currentDate.format(formatter)
        db.collection("account").get().addOnSuccessListener { result ->
            if (!result.isEmpty) {
                for (i in result) {
                    val emailID = i.id.trim()
                    db.collection("account").document(emailID).collection("currentContracts")
                        .whereEqualTo("status", "active").get().addOnSuccessListener { result2 ->
                            for (contracts in result2) {
                                val contractID = contracts.id
                                val dueDate = contracts.data["dueDate"].toString()
                                val monthlyPay = contracts.data["monthlyPay"].toString()
                                val billStore = contracts.data["billStore"] as HashMap<String, Any>
                                val outBal =
                                    contracts.data["outstandingBalance"].toString().toDoubleOrNull()
                                        ?: 0.00
                                val dueDateLocal = LocalDate.parse(dueDate, formatter)
                                notify(
                                    dueDateLocal, currentDate, emailID, contractID,
                                    monthlyPay, outBal, billStore, monthlyCalculation)
                                lateDateManagement(
                                    currentDate,
                                    dueDateLocal,
                                    emailID,
                                    contractID,
                                    currDateToString, latePayCheck
                                )

                            }
                        }
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notify(dueDateLocal: LocalDate, currentDate: LocalDate, emailID: String,
        contractID: String, monthlyPay: String, outBal: Double,
        billStore: HashMap<String, Any>, monthlyCalculation: SharedPreferences) {
        val dueMonth = dueDateLocal.month.toString()
        val dueYr = dueDateLocal.year.toString()
        val daysBeforeDueDate = ChronoUnit.DAYS.between(currentDate,dueDateLocal).toInt()
        val hasBeenCalculated = monthlyCalculation.getBoolean("hasBeenCalculated$emailID$contractID", false) //make it only run once per month
        println("$hasBeenCalculated hello")

        if(daysBeforeDueDate <= 7 && !hasBeenCalculated){
            monthlyCalculation.edit().putBoolean("hasBeenCalculated$emailID$contractID", true).apply()
            var total = 0.00
            for(i in billStore.keys){
                val innerMap = billStore[i] as HashMap<*,*>
                innerMap.let {
                    val price = it["price"].toString().toDoubleOrNull()?: 0.00
                    total += price
                }
            }
            println("EYYYOO")
            db.collection("account").document(emailID).collection("currentContracts").document(contractID)
            .update("outstandingBalance", FieldValue.increment(total+monthlyPay.toDouble())).addOnSuccessListener {
                    println("YOOOO")
                    val bill = hashMapOf("initialPayment" to monthlyPay.toDouble(), "oldBalance" to outBal, "others" to billStore, "amountPaid" to 0)
                    db.collection("account").document(emailID).collection("currentContracts").document(contractID)
                        .collection("bill").document("$dueMonth-$dueYr").set(bill).addOnSuccessListener {
                            billStore.clear() //clear the current billStore
                            db.collection("account").document(emailID).collection("currentContracts").
                            document(contractID).update("billStore", billStore)
                            println("EYYYOO2")
                        }

                }
        }
        if(daysBeforeDueDate <= 7){
            if(auth.currentUser != null && auth.currentUser!!.email.toString() == emailID){
                showNotification("Due Date Notification", "Make sure to pay within the due date to avoid penalties",1)
            }

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun lateDateManagement(currentDate: LocalDate, dueDateLocal: LocalDate,
        emailID: String, contractID: String, currDateToString: String, latePayCheck: SharedPreferences) {

        println(currentDate.isAfter(dueDateLocal))
        if(currentDate.isAfter(dueDateLocal)){
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val currMonth = currentDate.month.toString()
            val currYr = currentDate.year.toString()
            println(latePayCheck.getString("lastDateOfChecking$emailID$contractID", "empty"))
            val lastCheckedDate = latePayCheck.getString("lastDateOfChecking$emailID$contractID", null)?. let {LocalDate.parse(it,formatter)}
            val isPastSevenDays = if(lastCheckedDate != null){ ChronoUnit.DAYS.between(currentDate, lastCheckedDate).toInt() >= 7 } else true
            if(isPastSevenDays){
                val data = hashMapOf("description" to "Late Payment", "price" to FieldValue.increment(100), "type" to "penalties")
                val penalties = hashMapOf("latePayment" to data)
                val others = mapOf("others" to penalties)

                db.collection("account").document(emailID).collection("currentContracts").document(contractID)
                    .update("outstandingBalance", FieldValue.increment(100)).addOnSuccessListener {

                        db.collection("account").document(emailID).collection("currentContracts").document(contractID)
                            .collection("bill").document("$currMonth-$currYr").set(others, SetOptions.merge())
                    }
                latePayCheck.edit().putString("lastDateOfChecking$emailID$contractID", currDateToString).apply()
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "notifier_channel"
            val channelName = "Due Date Notification"
            val channelDescription = "Notifications for handling due date reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val channelId = "notifier_channel"
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








