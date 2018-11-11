package com.example.thanaphoombabparn.firemessage

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1
    //Create Provider from AuthUI from Firebase
    private val signInProvider = listOf(AuthUI.IdpConfig
        .EmailBuilder()
        .setAllowNewAccounts(true)
        .setRequireName(true)
        .build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //AuthUI from Firebase will get tou to log in by email
        account_sign_in.setOnClickListener {
            val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(signInProvider)
                .setLogo(R.drawable.ic_1f525).build()
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            val response =  IdpResponse.fromResultIntent(data)

            if(resultCode == Activity.RESULT_OK){
                val progressDialog = indeterminateProgressDialog("Setting up your account")
                //TODO: Initialize current user in Firestore
                startActivity(intentFor<MainActivity>().newTask().clearTask())
                progressDialog.dismiss()
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                if(response == null) return

                when(response.error?.errorCode){
                    ErrorCodes.NO_NETWORK ->
                        longSnackbar(constraint_layout, "No network")
                    ErrorCodes.UNKNOWN_ERROR ->
                        longSnackbar(constraint_layout, "Unknown Error")
                }
            }
        }
    }
}
