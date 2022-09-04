package com.example.todolist

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecyclerAdapter(private var idList: List<Int>, private var titlesList: List<String?>, private var detailsList: List<String?>,
                      private var createDateList: List<String?>, private var completeDateList: List<String?>,
                      private var isDoneList: List<Boolean?>, private var notificationList: List<Boolean?>,
                      private var categoryList: List<String?>, private var imagesList: MutableList<Boolean>
) :
RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val itemTitle: TextView = itemView.findViewById(R.id.tv_title)
        val itemDetail: TextView = itemView.findViewById(R.id.tv_description)
        val itemPicture: ImageView = itemView.findViewById(R.id.iv_image)
        var id = 0
        var title = ""
        var details = ""
        var createDate = ""
        var completeDate = ""
        var isDone = false
        var notification = false
        var category = ""
        var attached = false

        init {
            itemView.setOnClickListener{
                val intent = Intent(itemView.context, DisplayTaskActivity::class.java).apply {
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("details", details)
                    putExtra("createDate", createDate)
                    putExtra("completeDate", completeDate)
                    putExtra("isDone", isDone)
                    putExtra("notification", notification)
                    putExtra("category", category)
                    putExtra("attachement", attached)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return titlesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTitle.text = titlesList[position]
        holder.itemDetail.text = categoryList[position]
        holder.id = idList[position]
        holder.title = titlesList[position].toString()
        holder.details = detailsList[position].toString()
        holder.createDate = createDateList[position].toString()
        holder.completeDate = completeDateList[position].toString()
        holder.isDone = isDoneList[position]!!
        holder.notification = notificationList[position]!!
        holder.category = categoryList[position].toString()
        holder.attached = imagesList[position]

        if(holder.attached){
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString()
            val myDir = File("$root/saved_images")
            myDir.mkdirs()
            val fname = titlesList[position].toString() + ".jpg"
            val file = File(myDir, fname)
            if (file.exists()){
                val bm = BitmapFactory.decodeFile("$root/saved_images/$fname")
                holder.itemPicture.setImageBitmap(Bitmap.createScaledBitmap(bm, 150, 150, false))
            }
        }
    }
}