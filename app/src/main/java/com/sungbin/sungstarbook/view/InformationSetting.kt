package com.sungbin.sungstarbook.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
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
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.shashank.sony.fancytoastlib.FancyToast


class InformationSetting : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sungbin.sungstarbook.R.layout.activity_information_setting)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        val uid = intent.getStringExtra("uid")
        Utils.toast(applicationContext, uid)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "업데이트 예정...", Snackbar.LENGTH_LONG)
                .setAction("닫기", null).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        profile_image.setOnClickListener {
            val permissionlistener = object : PermissionListener {
                override fun onPermissionGranted() {
                    TedBottomPicker.with(this@InformationSetting)
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
                                .start(this@InformationSetting)
                        }
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Utils.toast(applicationContext, "권한 사용에 동의 해 주셔야 프로필 사진을 불러올 수 있습니다.\n" +
                            "내부메모리 접근 권한 사용이 거절되어, 프로필 사진이 기본 사진으로 대채됩니다.",
                        FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                }
            }

            TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle("권한 필요")
                .setRationaleMessage("프로필 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "권한 사용을 허용해 주세요.")
                .setDeniedTitle("내부 메모리 접근 권한 필요")
                .setDeniedMessage("프로필 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
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
                Utils.error(this, "프로필 사진을 선택하는 도중에 오류가 발생했습니다.\n$error")
            }
        }
    }

}
