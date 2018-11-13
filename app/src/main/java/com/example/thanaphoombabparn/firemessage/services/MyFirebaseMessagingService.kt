package com.example.thanaphoombabparn.firemessage.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // implements cloud messaging.
        // Go to Manifest and implement meta-data
        if (remoteMessage != null) {
            if(remoteMessage.notification != null){
                //TODO: Show notification if we're not online
                Log.d("FCM",remoteMessage.data.toString())
            }
        }
    }
}