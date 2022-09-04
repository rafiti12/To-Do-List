package com.example.todolist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "create_date") val createDate: String?,
    @ColumnInfo(name = "complete_date") val completeDate: String?,
    @ColumnInfo(name = "is_done") val isDone: Boolean?,
    @ColumnInfo(name = "notification_on") val notificationOn: Boolean?,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "attachment") val attachment: Boolean?
){
    constructor(title: String, description: String, createDate: String?, completeDate: String?,
                isDone: Boolean?, notificationOn: Boolean?, category: String?, attachment: Boolean?)
                : this(0, title, description, createDate, completeDate, isDone, notificationOn,
                        category, attachment)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY date(complete_date) DESC")
    fun getAll(): List<Task>

    @Query("SELECT * FROM task WHERE is_done = 0 ORDER BY date(complete_date) DESC")
    fun getUnAll(): List<Task>

    @Query("SELECT * FROM task WHERE title LIKE :tit ORDER BY date(complete_date) DESC")
    fun findByName(tit: String): List<Task>

    @Query("SELECT * FROM task WHERE category LIKE :cat ORDER BY date(complete_date) DESC")
    fun findByCat(cat: String): List<Task>

    @Query("SELECT * FROM task WHERE title LIKE :tit and is_done = 0 ORDER BY date(complete_date) DESC")
    fun findUnByName(tit: String): List<Task>

    @Query("SELECT * FROM task WHERE category LIKE :cat and is_done = 0 ORDER BY date(complete_date) DESC")
    fun findUnByCat(cat: String): List<Task>

    @Query("SELECT * FROM task WHERE uid LIKE :id LIMIT 1")
    fun findById(id: Int): Task

    @Query("UPDATE task SET title = :title, description = :description, create_date = :createDate" +
            ", complete_date = :completeDate, is_done = :isDone, notification_on = :notificationOn," +
            " category = :category WHERE uid = :id")
    fun updateById(id: Int, title: String, description: String, createDate: String?, completeDate: String?,
                   isDone: Boolean?, notificationOn: Boolean?, category: String?)

    @Insert
    fun insertAll(vararg tasks: Task)

    @Delete
    fun delete(task: Task)
}

@Database(entities = [Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

class MainActivity : AppCompatActivity() {

    private var idList = mutableListOf<Int>()
    private var titlesList = mutableListOf<String?>()
    private var descList = mutableListOf<String?>()
    private var createDateList = mutableListOf<String?>()
    private var completeDateList = mutableListOf<String?>()
    private var isDoneList = mutableListOf<Boolean?>()
    private var notificationOnList = mutableListOf<Boolean?>()
    private var categoryList = mutableListOf<String?>()
    private var imagesList = mutableListOf<Boolean>()
    private var adapter: RecyclerAdapter? = null
    private var timeBeforeNot = "00:00"

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.setTime).text = "00:00"

        CoroutineScope(Dispatchers.IO).launch {
            postToList()
            runOnUiThread{
                val rvRecyclerView = findViewById<RecyclerView>(R.id.rv_recyclerView)
                rvRecyclerView.layoutManager = LinearLayoutManager(parent)
                adapter = RecyclerAdapter(idList, titlesList, descList, createDateList,
                    completeDateList, isDoneList, notificationOnList, categoryList, imagesList)
                rvRecyclerView.adapter = adapter
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("NotifyDataSetChanged")
    fun addClick(view: View){
        val intent = Intent(this, AddTaskActivity::class.java).apply {
            putExtra("beforeNoti", timeBeforeNot)
        }
        startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateClick(view: View){
        CoroutineScope(Dispatchers.IO).launch{
            clearList()
            val taskDao = connectToDatabase()
            timeBeforeNot = findViewById<EditText>(R.id.setTime).text.toString()
            val cb = findViewById<CheckBox>(R.id.checkBox3).isChecked
            val searchValue = findViewById<EditText>(R.id.searchField).text.toString()
            val spin = findViewById<Spinner>(R.id.chooseField).selectedItem.toString()
            val tasks: List<Task>
            if(searchValue == "" || searchValue == "null"){
                tasks = if(cb){
                    taskDao.getUnAll()
                } else{
                    taskDao.getAll()
                }
            }
            else{
                tasks = if(spin == "Category"){
                    if(cb){
                        taskDao.findUnByCat(searchValue)
                    } else{
                        taskDao.findByCat(searchValue)
                    }
                } else{
                    if(cb){
                        taskDao.findUnByName(searchValue)
                    } else{
                        taskDao.findByName(searchValue)
                    }
                }
            }
            val size = tasks.size
            for(i in 0 until size){
                addToList(tasks[i].uid, tasks[i].title, tasks[i].description, tasks[i].createDate,
                    tasks[i].completeDate, tasks[i].isDone, tasks[i].notificationOn,
                    tasks[i].category, tasks[i].attachment)
            }
            runOnUiThread{
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun clearList(){
        idList.clear()
        titlesList.clear()
        descList.clear()
        createDateList.clear()
        completeDateList.clear()
        isDoneList.clear()
        notificationOnList.clear()
        categoryList.clear()
        imagesList.clear()
    }

    private fun addToList(
        id: Int, title: String?, description: String?, createDate: String?,
        completeDate: String?, isDone: Boolean?, notificationOn: Boolean?,
        category: String?, image: Boolean?
    ){
        idList.add(id)
        titlesList.add(title)
        descList.add(description)
        createDateList.add(createDate)
        completeDateList.add(completeDate)
        isDoneList.add(isDone)
        notificationOnList.add(notificationOn)
        categoryList.add(category)
        imagesList.add(image!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postToList(){
        val taskDao = connectToDatabase()
        val everyTask = taskDao.getAll()
        val size = everyTask.size
        for(i in 0 until size){
            addToList(everyTask[i].uid, everyTask[i].title, everyTask[i].description, everyTask[i].createDate,
                everyTask[i].completeDate, everyTask[i].isDone, everyTask[i].notificationOn,
                everyTask[i].category, everyTask[i].attachment)
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