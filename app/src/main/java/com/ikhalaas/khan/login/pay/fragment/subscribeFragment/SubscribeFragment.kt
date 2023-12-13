package com.ikhalaas.khan.login.pay.fragment.subscribeFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.ikhalaas.khan.login.pay.R
import com.ikhalaas.khan.login.pay.activity.BillingViewModel
import com.ikhalaas.khan.login.pay.activity.MainViewModel
import com.ikhalaas.khan.login.pay.databinding.FragmentSignInBinding
import com.ikhalaas.khan.login.pay.databinding.FragmentSubscribeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscribeFragment : Fragment() {

    val viewModel: MainViewModel by activityViewModels()
    val billingViewModel: BillingViewModel by activityViewModels()

    private var _binding: FragmentSubscribeBinding? = null
    private val binding get() = _binding!!

    private val productPriceObserver = Observer<String> { price ->
        binding.btnSubscribe.text = getString(R.string.subscribe_text, price)
    }

    private val purchaseResponseObserver = Observer<Pair<Int, Purchase>> { response ->
        when (response.first) {
            BillingClient.BillingResponseCode.OK -> {
                if (response.second.purchaseState == PurchaseState.PURCHASED) {
                    lifecycleScope.launch {
                        billingViewModel.acknowledgeSubscription()
                    }
                }
                goToLinkFragment()
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                //user canceled
            }

            BillingClient.BillingResponseCode.ERROR -> {
                Log.e("BILLING_TEST", "error")
            }

            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                goToLinkFragment()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscribeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingViewModel.subscriptionPrice.observe(viewLifecycleOwner, productPriceObserver)
        billingViewModel.purchaseResponse.observe(viewLifecycleOwner, purchaseResponseObserver)

        binding.btnSubscribe.setOnClickListener {
            viewModel._subscribeClicked.postValue(true)
        }
        binding.btnTest.setOnClickListener {
            goToLinkFragment()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun goToLinkFragment() {
        val action = SubscribeFragmentDirections.actionSubscribeFragmentToLinkFragment()
        findNavController().navigate(action)
    }
}