package com.example.thanaphoombabparn.firemessage.services

import com.example.thanaphoombabparn.firemessage.utility.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import java.lang.NullPointerException

class MyFirebaseInstanceIDService: FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        // Get new token
        val newRegistrationToken = FirebaseInstanceId.getInstance().token
        //If already login. will add token to firestore by check if it exist
        if(FirebaseAuth.getInstance().currentUser != null)
            addTokenToFirestore(newRegistrationToken)
    }

    companion object {
        fun addTokenToFirestore(newRegistrationToken: String?){
            if(newRegistrationToken == null)
                throw NullPointerException("FCM token is null.")

            FirestoreUtil.getFCMRegistrationTokens { tokens ->
                // If already have tokens will return exist token
                if(tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                FirestoreUtil.setFCMRegistrationTokens(tokens)
            }
        }
    }
}