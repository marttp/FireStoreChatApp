package com.example.thanaphoombabparn.firemessage.utility

import android.content.Context
import android.util.Log
import com.example.thanaphoombabparn.firemessage.model.*
import com.example.thanaphoombabparn.firemessage.recyclerView.item.ImageMessageItem
import com.example.thanaphoombabparn.firemessage.recyclerView.item.PersonItem
import com.example.thanaphoombabparn.firemessage.recyclerView.item.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import java.lang.NullPointerException

object FirestoreUtil {

    //Create instance and get it in singleton pattern by lazy
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    // Firestore have reference to own document in collection
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth
            // Check current user id in auth and get instance from them
            .getInstance()
            //Check UID.If it null.Throw Error to NullPointerException
            .currentUser?.uid?: throw NullPointerException("UID is null.")}")

    private val chatChannelCollectionReference =
        firestoreInstance.collection("chatChannels")


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit){
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if(!documentSnapshot.exists()){
                val newUser = User(
                    FirebaseAuth.getInstance()
                        .currentUser?.displayName?: "","",null)
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else {
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name:String = "", bio: String = "", profilePicturePath: String? = null){
        val userFieldMap = mutableMapOf<String, Any>()
        if(name.isNotBlank())
            userFieldMap["name"] = name
        if (bio.isNotBlank())
            userFieldMap["bio"] = bio
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit){
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

    fun addUserListener(context: Context, onListen: (List<Item>) -> Unit) : ListenerRegistration {
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    Log.e("FIRESTORE", "Users listener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    // Get the person who registration in app not only me
                    if(it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                }
                onListen(items)
            }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(otherUserId: String,
                               onComplete: (channelId:String) -> Unit){
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if(it.exists()){
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }
                // Get current user from mobile
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
                //Create new channel with other user. The other user will get from parameter of this method
                val newChannel = chatChannelCollectionReference.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                currentUserDocRef.collection("engagedChatChannels").document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }

    fun addChatMessageListener(channelId: String, context: Context,
                               onListen: (List<Item>) -> Unit) : ListenerRegistration {
        return chatChannelCollectionReference.document(channelId)
            .collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    Log.e("FIRESTORE", "ChatMessageListener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if(it["type"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                    else {
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                    }
                     return@forEach
                }
                onListen(items)
            }
    }

    fun sendMessage(message: Message, channelId: String){
        chatChannelCollectionReference.document(channelId)
            .collection("messages")
            .add(message)
    }
}