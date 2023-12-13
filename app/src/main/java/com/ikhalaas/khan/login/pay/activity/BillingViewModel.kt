package com.ikhalaas.khan.login.pay.activity

import android.app.Activity
import android.content.Context
import android.service.voice.VoiceInteractionSession.ActivityId
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillingViewModel : ViewModel() {
    val BILLING_TEST = "BILLING_TEST"

    val SUBSCRIBE_ID = "dinaar_subscription"

    var _billingClientConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    val billingClientConnected: LiveData<Boolean> = _billingClientConnected

    var _productDetailsList: MutableLiveData<List<ProductDetails>> = MutableLiveData()
    val productDetailsList: LiveData<List<ProductDetails>> = _productDetailsList

    var _subscriptionPrice: MutableLiveData<String> = MutableLiveData("")
    val subscriptionPrice: LiveData<String> = _subscriptionPrice

    var _purchaseResponse: MutableLiveData<Pair<Int, Purchase>> = MutableLiveData()
    val purchaseResponse: LiveData<Pair<Int, Purchase>> = _purchaseResponse

    var billingClient: BillingClient? = null

    var subscriptionToken: String = ""

    val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.v(
                BILLING_TEST,
                "PurchasesUpdatedListener: \n billingResult: ${billingResult.responseCode} \n purchases: ${purchases?.size}"
            )
            purchases?.let {
                if (it.isNotEmpty())
                    _purchaseResponse.postValue(Pair(billingResult.responseCode, it[0]))
            }
        }

    val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SUBSCRIBE_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

    fun initBilling(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.e(BILLING_TEST, "billing disconnected")
                _billingClientConnected.postValue(false)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.v(BILLING_TEST, "billing setup finished, result: ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingClientConnected.postValue(true)
                }
            }
        })
    }

    fun queryProductDetails() {
        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                             productDetailsList ->
            Log.v(BILLING_TEST, "queryProductDetails, response: ${billingResult.responseCode}")
            Log.v(BILLING_TEST, "queryProductDetails, list size: ${productDetailsList.size}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                _productDetailsList.postValue(productDetailsList)
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        productDetailsList.value?.let {
            if (it.isNotEmpty()) {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(it[0])
                        .setOfferToken(subscriptionToken)
                        .build()
                )
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }

    suspend fun acknowledgeSubscription() {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(subscriptionToken)
        val acknPurchaseResult = withContext(Dispatchers.IO) {
            billingClient?.acknowledgePurchase(
                acknowledgePurchaseParams.build(),
                object : AcknowledgePurchaseResponseListener {
                    override fun onAcknowledgePurchaseResponse(p0: BillingResult) {

                    }
                })
        }
    }
}