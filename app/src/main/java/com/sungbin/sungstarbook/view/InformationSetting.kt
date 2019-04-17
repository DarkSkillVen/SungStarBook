package com.sungbin.sungstarbook.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.sungbin.sungstarbook.utils.Utils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.activity_information_setting.*
import kotlinx.android.synthetic.main.content_information_setting.*


class InformationSetting : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sungbin.sungstarbook.R.layout.activity_information_setting)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "업데이트 예정...", Snackbar.LENGTH_LONG)
                .setAction("닫기", null).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        profile_image.setOnClickListener {

            TedBottomPicker.with(this)
                .setImageProvider { imageView, imageUri ->
                    val options = RequestOptions().centerCrop()
                    Glide.with(baseContext).load(imageUri).apply(options).into(imageView)
                }
                .show {
                    CropImage.activity(it)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                        .setAutoZoomEnabled(true)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .start(this)
                }
        }

    }

    @SuppressLint("MissingSuperCall")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                Glide.with(this).load(resultUri).into(profile_image)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Utils.error(this, "프사를 선택하는 도중에 오류가 발생했습니다.\n$error")
            }
        }
    }

}
