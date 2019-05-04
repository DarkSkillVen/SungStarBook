package com.sungbin.sungstarbook.view.main_fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.karlgao.materialroundbutton.MaterialButton
import com.sungbin.sungstarbook.view.ProfileViewActivity
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.utils.Utils
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.makeramen.roundedimageview.RoundedDrawable.drawableToBitmap
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.view.ImageViewerActivity
import java.io.ByteArrayOutputStream


private lateinit var uid:String

@SuppressLint("InflateParams")
class Friends : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        uid = Utils.readData(context!!, "uid", "null")!!

        val view = inflater.inflate(R.layout.fragment_friends, null)

        view.findViewById<MaterialButton>(R.id.see_my_profile).setOnClickListener {
            startActivity(
                Intent(context, ProfileViewActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        view.findViewById<RoundedImageView>(R.id.my_profile_image).setOnClickListener {
            val image = drawableToBitmap((view.findViewById<RoundedImageView>(R.id.my_profile_image)).drawable)
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val intent = Intent(context, ImageViewerActivity::class.java)
                .putExtra("image", byteArray).putExtra("tag", "profile_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity!!,
                view.findViewById(R.id.my_profile_image),
                "profile_image")
            if (Build.VERSION.SDK_INT >= 21) {
                startActivity(intent, options.toBundle())
            } else startActivity(intent)
        }

        val options = RequestOptions()
            .skipMemoryCache(true)
            .centerInside()
            .placeholder(R.drawable.profile_image_preview)
            .transform(CircleCrop())

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://sungstarbook-6f4ce.appspot.com/")
            .child("Profile_Image/$uid/Profile.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context!!).load(uri).apply(options).into(view.findViewById<RoundedImageView>(R.id.my_profile_image)) }
        storageRef.downloadUrl.addOnFailureListener { e ->
            Utils.toast(context!!, "프로필 사진을 불러올 수 없습니다.\n\n$e", FancyToast.LENGTH_SHORT, FancyToast.WARNING) }

        return view
    }
}