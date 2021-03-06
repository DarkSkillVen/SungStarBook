package com.sungbin.sungstarbook.view.main_fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.storage.FirebaseStorage
import com.karlgao.materialroundbutton.MaterialButton
import com.makeramen.roundedimageview.RoundedImageView
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.activity.ImageViewerActivity
import com.sungbin.sungstarbook.view.activity.ProfileViewActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


private lateinit var uid: String

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

        view.findViewById<CircleImageView>(R.id.my_profile_image).setOnClickListener {
            val image = view.findViewById<CircleImageView>(R.id.my_profile_image).drawable
            val sendBitmap = (image as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            sendBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val intent = Intent(context, ImageViewerActivity::class.java)
                .putExtra("image", byteArray).putExtra("tag", "profile_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity!!,
                view.findViewById(R.id.my_profile_image),
                "profile_image"
            )
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
            try {
                Glide.with(context!!).load(uri).apply(options)
                    .into(view.findViewById<RoundedImageView>(R.id.my_profile_image))
                //ImageDownload().execute(uri.toString())
            }
            catch (e:Exception){
                /*Utils.toast(
                    context!!,
                    "프로필 사진을 불러오는 도중에 오류가 발생하였습니다.",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.WARNING)*/
            }
        }
        storageRef.downloadUrl.addOnFailureListener { e ->
            Utils.toast(context!!, "프로필 사진을 불러올 수 없습니다.\n\n$e", FancyToast.LENGTH_SHORT, FancyToast.WARNING)
        }

        return view
    }
}

private class ImageDownload : AsyncTask<String, Void, Void>() {
    private var fileName: String? = null
    override fun doInBackground(vararg params: String): Void? {
        val savePath = Environment.getExternalStorageDirectory().absolutePath + "/SungStarBook/ProfileImage/"
        val dir = File(savePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        fileName = "$uid.png"

        val fileUrl = params[0]

        val localPath = "$savePath/$fileName"

        if(File(localPath).exists()){
            return null
        }

        try {
            val imgUrl = URL(fileUrl)
            val conn = imgUrl.openConnection() as HttpURLConnection
            val len = conn.contentLength
            val tmpByte = ByteArray(len)
            val `is` = conn.inputStream
            val file = File(localPath)
            val fos = FileOutputStream(file)
            var read: Int

            while (true) {
                read = `is`.read(tmpByte)
                if (read <= 0) {
                    break
                }
                fos.write(tmpByte, 0, read)
            }

            `is`.close()
            fos.close()
            conn.disconnect()
        } catch (e: Exception) {
            Utils.error(getApplicationContext(), e.toString())
        }

        return null
    }

    override fun onPostExecute(result: Void) {
        return
    }

}