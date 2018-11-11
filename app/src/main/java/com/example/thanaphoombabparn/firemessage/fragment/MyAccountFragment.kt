package com.example.thanaphoombabparn.firemessage.fragment


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.thanaphoombabparn.firemessage.R
import com.example.thanaphoombabparn.firemessage.SignInActivity
import com.example.thanaphoombabparn.firemessage.glide.GlideApp
import com.example.thanaphoombabparn.firemessage.utility.FirestoreUtil
import com.example.thanaphoombabparn.firemessage.utility.StorageUtil
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class MyAccountFragment : Fragment() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_account, container, false)

        view.apply {
            imageView_profile_picture.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
            }

            btn_save.setOnClickListener {
                if (::selectImageBytes.isInitialized)
                    StorageUtil.uploadProfilePhoto(selectImageBytes) { imagePath ->
                        FirestoreUtil.updateCurrentUser(
                            editText_name.text.toString(),
                            editText_bio.text.toString(),
                            imagePath
                        )
                    }
                else
                    FirestoreUtil.updateCurrentUser(
                        editText_name.text.toString(),
                        editText_bio.text.toString(),
                        null
                    )
            }

            btn_sign_out.setOnClickListener {
                AuthUI.getInstance()
                    .signOut(this@MyAccountFragment.context!!)
                    .addOnCompleteListener {
                        intentFor<SignInActivity>().newTask().clearTask()
                    }
            }
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data !== null && data.data != null
        ) {
            //get path : data.data = Path of image
            val selectedImagePath = data.data
            val selectImageBmp = MediaStore.Images.Media
                .getBitmap(activity?.contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            //Compress to JPEG file
            selectImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectImageBytes = outputStream.toByteArray()

            //TODO: load picture
            GlideApp.with(this)
                .load(selectImageBytes)
                .into(imageView_profile_picture)
            pictureJustChanged = true
        }
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if (this@MyAccountFragment.isVisible) {
                if (user != null) {
                    editText_name.setText(user.name)
                    editText_bio.setText(user.bio)
                    if (!pictureJustChanged && user.profilePicturePath != null) {
                        GlideApp.with(this)
                            .load(StorageUtil.pathToReference((user.profilePicturePath)))
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(imageView_profile_picture)
                    }
                }
            }
        }
    }

}
