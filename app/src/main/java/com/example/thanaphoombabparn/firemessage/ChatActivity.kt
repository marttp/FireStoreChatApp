package com.example.thanaphoombabparn.firemessage

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import com.example.thanaphoombabparn.firemessage.model.ImageMessage
import com.example.thanaphoombabparn.firemessage.model.MessageType
import com.example.thanaphoombabparn.firemessage.model.TextMessage
import com.example.thanaphoombabparn.firemessage.utility.FirestoreUtil
import com.example.thanaphoombabparn.firemessage.utility.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

private const val RC_SELECT_IMAGE = 2

class ChatActivity : AppCompatActivity() {

    private lateinit var currentChannelId: String

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messageSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->

            currentChannelId = channelId

            messagesListenerRegistration =
                    FirestoreUtil.addChatMessageListener(channelId, this, this::updateRecyclerView)

            imageView_send.setOnClickListener {
                if (editText_message.text.toString().isNotEmpty()) {
                    val messageToSend =
                        TextMessage(
                            editText_message.text.toString(),
                            Calendar.getInstance().time,
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            MessageType.TEXT
                        )
                    editText_message.setText("")
                    FirestoreUtil.sendMessage(messageToSend, channelId)
                }
            }

            fab_send_image.setOnClickListener {
                //TODO: Send image messages
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val selectedImagePath = data.data
            val selectImageBmp = MediaStore.Images.Media
                .getBitmap(contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            //Compress to JPEG file
            selectImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectImageBytes) { imagePath ->
                val messageToSend =
                    ImageMessage(
                        imagePath,
                        Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid
                    )

                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
            }
        }
    }

    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messageSection = Section(messages)
                    this.add(messageSection)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItem() = messageSection.update(messages)

        if (shouldInitRecyclerView)
            init()
        else
            updateItem()

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)
    }
}
