package com.sungbin.sungstarbook.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sungbin.sungstarbook.dto.ChattingItem
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.RelativeLayout
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kakao.auth.StringSet.msg
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils


class ChatAdapter(private val list: ArrayList<ChattingItem>?, private val ctx: Context) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var itemClick: ItemClick? = null
    private var lastName:String? = null

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

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    fun setItemClick(itemClick: ItemClick) {
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.chat_content_activity, viewGroup, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull viewholder: ChatViewHolder, position: Int) {
        val name = list!![position].name
        val time = list[position].time
        val type = list[position].type
        val msg = list[position].msg
        val contentUri = list[position].contentUri
        val profilePicUri = list[position].profilePicUri

        val uid = Utils.readData(ctx, "uid", "null")!!
        val myName = Utils.readData(ctx, "myName", "null")!!

        if (myName == name) { //오른쪽
            viewholder.msg_R.text = msg
            viewholder.time_R.text = time
            viewholder.name_R.text = name
            Glide.with(ctx).load(profilePicUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(viewholder.profilePic_R)
        } else { //왼쪽
            viewholder.layout_R.visibility = View.GONE
            viewholder.layout_L.visibility = View.VISIBLE

            viewholder.msg_L.text = msg
            viewholder.time_L.text = time
            viewholder.name_L.text = name
            Glide.with(ctx).load(profilePicUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(viewholder.profilePic_L)
        }

        /*viewholder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(v, position);
                }
            }
        });*/
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun getItem(position: Int): ChattingItem {
        return list!![position]
    }

}
