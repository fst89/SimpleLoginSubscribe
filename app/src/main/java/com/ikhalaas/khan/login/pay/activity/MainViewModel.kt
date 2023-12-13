package com.ikhalaas.khan.login.pay.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class MainViewModel : ViewModel() {

    var whatsAppLink = ""
    val REMOTE_LINK_KEY = "wa_link"

    /**
     * Live data
     */
    var _signInClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val signInClicked: LiveData<Boolean> = _signInClicked

    var _signedUser: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val signedUser: LiveData<FirebaseUser?> = _signedUser

    var _subscribeClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val subscribeClicked: LiveData<Boolean> = _subscribeClicked

    fun openWhatsappLin(context: Context, whatsAppLink: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsAppLink))
        context.startActivity(intent)
    }
}