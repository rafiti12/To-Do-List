package com.example.todolist

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private var calendar: Calendar? = null
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null

    private val pickImage = 100
    private var attached = false
    private lateinit var bitmap: Bitmap
    private var timeBeforeNot = "0"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")
        val formatted = current.format(formatter).toString()
        findViewById<TextView>(R.id.taskCompleteDate).text = formatted
        timeBeforeNot = intent.getStringExtra("beforeNoti").toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addOnClick(view: View){
        val cont = this
        CoroutineScope(Dispatchers.IO).launch {
            val taskDao = connectToDatabase()
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")
            val formatted = current.format(formatter).toString()

            val title = findViewById<TextView>(R.id.taskTitle).text.toString()
            val desc = findViewById<TextView>(R.id.taskDescription).text.toString()
            val completeDate = findViewById<TextView>(R.id.taskCompleteDate).text.toString()
            val notificationOn = findViewById<CheckBox>(R.id.checkBox).isChecked
            val category = findViewById<TextView>(R.id.taskCategory).text.toString()

            if(attached){
                val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
                val myDir = File("$root/saved_images")
                myDir.mkdirs()
                val fname = "$title.jpg"
                val file = File(myDir, fname)
                if (file.exists()) file.delete()
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.flush()
                    out.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                MediaScannerConnection.scanFile(
                    cont, arrayOf(file.toString()), null
                ) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                }
            }

            if(notificationOn){
                createNotificationChannel()
                calendar = Calendar.getInstance()
                calendar?.set(Calendar.HOUR_OF_DAY, 1)
                calendar?.set(Calendar.MINUTE, 30)
                calendar?.set(Calendar.SECOND, 0)
                calendar?.set(Calendar.MILLISECOND, 0)
                setAlarm(title, completeDate)
            }

            val task = Task(title, desc, formatted, completeDate,
                false, notificationOn, category, attached)
            taskDao.insertAll(task)

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            val image = data?.data
            val source = ImageDecoder.createSource(this.contentResolver, image!!)
            bitmap = ImageDecoder.decodeBitmap(source)
            attached = true
        }
    }

    fun attachOnClick(view: View){
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)
    }

    private fun connectToDatabase(): TaskDao {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
        return db.taskDao()
    }

    @SuppressLint("UnspecifiedImmutableFlag", "SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAlarm(title: String?, compDate: String) {
        val dateMillis: Date?
        val offsetMillis: Long
        try{
            dateMillis = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(compDate)
            val timeList = timeBeforeNot.split(":")
            offsetMillis = timeList[0].toLong() * 1000 * 60 * 60 + timeList[1].toLong() * 1000 * 60
        }catch(e: Exception){
            System.out.println("XDDDDDDD")
            System.out.println(timeBeforeNot)
            System.out.println("XDDDDDDD")
            return
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("title", title)
        }
        System.out.println("@@@@@@@@@@@@@@@@@@@@")
        System.out.println(offsetMillis)
        System.out.println("@@@@@@@@@@@@@@@@@@@@")
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager!!.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dateMillis.time - offsetMillis, pendingIntent)
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        if (alarmManager == null) {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        }
        alarmManager!!.cancel(pendingIntent)
        Toast.makeText(this, "Alarm Cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "foxandroidReminderChannel"
            val description = "Channel For Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("foxandroid", name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}