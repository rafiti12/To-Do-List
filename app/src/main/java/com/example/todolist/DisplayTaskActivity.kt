package com.example.todolist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisplayTaskActivity : AppCompatActivity() {

    private var id = 0
    private var title = ""
    private var details = ""
    private var createDate = ""
    private var completeDate = ""
    private var isDone = false
    private var notification = false
    private var category = ""
    private var attached = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_task)

        id = intent.getIntExtra("id", 0)
        title = intent.getStringExtra("title").toString()
        details = intent.getStringExtra("details").toString()
        createDate = intent.getStringExtra("createDate").toString()
        completeDate = intent.getStringExtra("completeDate").toString()
        isDone = intent.getBooleanExtra("isDone", true)
        notification = intent.getBooleanExtra("notification", true)
        category = intent.getStringExtra("category").toString()
        attached = intent.getBooleanExtra("attachement", true)

        findViewById<TextView>(R.id.taskTitle).text = title
        findViewById<TextView>(R.id.taskDescription).text = details
        findViewById<TextView>(R.id.startDate).text = "Create date: $createDate"
        findViewById<TextView>(R.id.taskCompleteDate).text = completeDate
        findViewById<CheckBox>(R.id.checkBox2).isChecked = isDone
        findViewById<CheckBox>(R.id.checkBox).isChecked = notification
        findViewById<TextView>(R.id.taskCategory).text = category
    }

    fun deleteClick(view: View){
        CoroutineScope(Dispatchers.IO).launch {
            val taskDao = connectToDatabase()
            val task = Task(id, title, details, createDate, completeDate, isDone, notification, category, attached)
            taskDao.delete(task)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun updateClick(view: View){
        CoroutineScope(Dispatchers.IO).launch {
            val taskDao = connectToDatabase()
            val newTitle = findViewById<TextView>(R.id.taskTitle).text.toString()
            val newDetails = findViewById<TextView>(R.id.taskDescription).text.toString()
            val newCreateDate = createDate
            val newCompleteDate = findViewById<TextView>(R.id.taskCompleteDate).text.toString()
            val newIsDone = findViewById<CheckBox>(R.id.checkBox2).isChecked
            val newNotification = findViewById<CheckBox>(R.id.checkBox).isChecked
            val newCategory = findViewById<TextView>(R.id.taskCategory).text.toString()
            taskDao.updateById(id, newTitle, newDetails, newCreateDate, newCompleteDate, newIsDone,
            newNotification, newCategory)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun connectToDatabase(): TaskDao {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
        return db.taskDao()
    }
}
