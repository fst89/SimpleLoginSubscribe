package com.ikhalaas.khan.login.pay.fragment.linkFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ikhalaas.khan.login.pay.activity.MainViewModel
import com.ikhalaas.khan.login.pay.databinding.FragmentLinkBinding

class LinkFragment : Fragment() {

    val viewModel: MainViewModel by activityViewModels()

    var _binding: FragmentLinkBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOpenWhatsapp.setOnClickListener {
            viewModel.openWhatsappLin(requireContext(), viewModel.whatsAppLink)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}