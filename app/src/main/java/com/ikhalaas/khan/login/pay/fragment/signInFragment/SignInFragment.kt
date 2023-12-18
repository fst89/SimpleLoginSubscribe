package com.ikhalaas.khan.login.pay.fragment.signInFragment

import android.app.Notification.Action
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.ikhalaas.khan.login.pay.R
import com.ikhalaas.khan.login.pay.activity.MainViewModel
import com.ikhalaas.khan.login.pay.databinding.FragmentSignInBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SignInFragment : Fragment() {

    val viewModel: MainViewModel by activityViewModels()

    var _binding: FragmentSignInBinding? = null
    val binding get() = _binding!!

    private val userObserver = Observer<FirebaseUser?> { user ->
        if (user != null) {
            binding.btnSignIn.visibility = View.GONE
            binding.userExistsGroup.visibility = View.VISIBLE
            binding.userName.text = user.displayName
        } else {
            binding.btnSignIn.visibility = View.VISIBLE
            binding.userExistsGroup.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignIn.setOnClickListener {
            viewModel._signInClicked.postValue(true)
        }

        binding.btnContinue.setOnClickListener {
            goToSubscribeFragment()
        }

        binding.btnTest.setOnClickListener {
            goToSubscribeFragment()
        }

        binding.privacyPolicy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)))
            startActivity(intent)
        }

        viewModel.signedUser.observe(viewLifecycleOwner, userObserver)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun goToSubscribeFragment() {
        val action = SignInFragmentDirections.actionSignInFragmentToSubscribeFragment()
        findNavController().navigate(action)
    }
}