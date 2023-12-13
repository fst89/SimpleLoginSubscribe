package com.ikhalaas.khan.login.pay.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.android.billingclient.api.ProductDetails
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ikhalaas.khan.login.pay.R

class MainActivity : AppCompatActivity() {

    val viewModel: MainViewModel by viewModels()
    val billingViewModel: BillingViewModel by viewModels()

    val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    /**
     * click observers:
     */

    private val signInClickObserver = Observer<Boolean> { signInClicked ->
        if (signInClicked) {
            signInLauncher.launch(signInIntent)
            viewModel._signInClicked.postValue(false)
        }
    }

    private val subscribeClickObserver = Observer<Boolean> { subscribeClicked ->
        if (subscribeClicked) {
            billingViewModel.launchBillingFlow(this)
            viewModel._subscribeClicked.postValue(false)
        }
    }

    private val billingClientStateObserver = Observer<Boolean> { isConnected ->
        if (isConnected) {
            billingViewModel.queryProductDetails()
        }
    }

    private val productDetailsObserver = Observer<List<ProductDetails>> { productList ->
        if (productList.isNotEmpty()) {
            productList.find { it.productId == billingViewModel.SUBSCRIBE_ID }?.let {
                billingViewModel.subscriptionToken = it.subscriptionOfferDetails?.get(0)?.offerToken.toString()
                Log.v(
                    billingViewModel.BILLING_TEST,
                    "query price: ${it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice}"
                )
                billingViewModel._subscriptionPrice.postValue(
                    it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                )
            }
        }
    }

    /**
     * end click observers
     */

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    // Choose authentication providers
    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    // Create and launch sign-in intent
    val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        setClickObservers()

        billingViewModel.billingClientConnected.observe(this, billingClientStateObserver)
        billingViewModel.productDetailsList.observe(this, productDetailsObserver)
        billingViewModel.initBilling(this)

        fetchRemoteConfig()

        viewModel._signedUser.postValue(FirebaseAuth.getInstance().currentUser)

    }

    private fun setClickObservers() {
        viewModel.signInClicked.observe(this, signInClickObserver)
        viewModel.subscribeClicked.observe(this, subscribeClickObserver)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            viewModel._signedUser.postValue(user)
        } else {
            viewModel._signedUser.postValue(null)
        }
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.whatsAppLink = remoteConfig.getString(viewModel.REMOTE_LINK_KEY)
                    Log.v("REMOTE_CONFIG_TEST", "fetchAndActivate: ${viewModel.whatsAppLink}")
                }
            }
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains(viewModel.REMOTE_LINK_KEY)) {
                    remoteConfig.activate().addOnCompleteListener {
                        viewModel.whatsAppLink = remoteConfig.getString(viewModel.REMOTE_LINK_KEY)
                        Log.v("REMOTE_CONFIG_TEST", "addOnConfigUpdateListener: ${viewModel.whatsAppLink}")
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("REMOTE_CONFIG_ERROR", error.message.toString())
            }
        })
    }
}