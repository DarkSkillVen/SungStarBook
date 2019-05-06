package com.sungbin.sungstarbook.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sungbin.sungstarbook.dto.ChattingItem
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.RelativeLayout
import androidx.annotation.NonNull
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.ImageViewerActivity
import java.io.ByteArrayOutputStream


class ChatAdapter(private val list: ArrayList<ChattingItem>?, private val act:Activity) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var layout_L: RelativeLayout = view.findViewById(R.id.leftLayout)
        var layout_R: RelativeLayout = view.findViewById(R.id.rightLayout)
        var profilePic_L: ImageView = view.findViewById(R.id.profile_image_L)
        var profilePic_R: ImageView = view.findViewById(R.id.profile_image)
        var name_L: TextView = view.findViewById(R.id.name_L)
        var name_R: TextView = view.findViewById(R.id.name)
        var time_L: TextView = view.findViewById(R.id.time_L)
        var time_R: TextView = view.findViewById(R.id.time)
        var msg_L: TextView = view.findViewById(R.id.msg_L)
        var msg_R: TextView = view.findViewById(R.id.msg)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.chat_content_view, viewGroup, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull viewholder: ChatViewHolder, position: Int) {
        val name = list!![position].name
        val time = list[position].time
        val type = list[position].type
        val msg = list[position].msg
        val contentUri = list[position].contentUri
        val profilePicUri = list[position].profilePicUri

        val uid = Utils.readData(act, "uid", "null")!!
        val myName = Utils.readData(act, "myName", "null")!!

        if (myName == name) { //오른쪽
            viewholder.msg_R.text = msg
            viewholder.time_R.text = time
            viewholder.name_R.text = name
            Glide.with(act).load(profilePicUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(viewholder.profilePic_R)
            viewholder.profilePic_R.setOnClickListener {
                val image = viewholder.profilePic_R.drawable
                val sendBitmap = (image as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                val intent = Intent(act, ImageViewerActivity::class.java)
                    .putExtra("image", byteArray).putExtra("tag", "profilePic_R")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    act,
                    viewholder.profilePic_R,
                    "profilePic_R"
                )
                if (Build.VERSION.SDK_INT >= 21) {
                    act.startActivity(intent, options.toBundle())
                } else act.startActivity(intent)
            }
        } else { //왼쪽
            viewholder.layout_R.visibility = View.GONE
            viewholder.layout_L.visibility = View.VISIBLE

            viewholder.msg_L.text = msg
            viewholder.time_L.text = time
            viewholder.name_L.text = name
            Glide.with(act).load(profilePicUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(viewholder.profilePic_L)
            viewholder.profilePic_L.setOnClickListener {
                val image = viewholder.profilePic_L.drawable
                val sendBitmap = (image as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                val intent = Intent(act, ImageViewerActivity::class.java)
                    .putExtra("image", byteArray).putExtra("tag", "profilePic_L")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    act,
                    viewholder.profilePic_L,
                    "profilePic_L"
                )
                if (Build.VERSION.SDK_INT >= 21) {
                    act.startActivity(intent, options.toBundle())
                } else act.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun getItem(position: Int): ChattingItem {
        return list!![position]
    }

}
