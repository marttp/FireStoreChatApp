package com.example.thanaphoombabparn.firemessage.utility

import com.example.thanaphoombabparn.firemessage.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
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
            .uid?: throw NullPointerException("UID is null.")}")

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

    fun getCurrentUser(onComplete: (User?) -> Unit){
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java))
            }
    }
}