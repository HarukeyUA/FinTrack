package com.harukeyua.fintrack.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.databinding.TransactionsSyncFragmentBinding
import com.harukeyua.fintrack.utils.MONOBANK_KEY_PREF
import com.harukeyua.fintrack.utils.MONOBANK_NAME_PREF
import com.harukeyua.fintrack.utils.getThemedColor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionsSyncFragment : Fragment() {

    private var _binding: TransactionsSyncFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TransactionsSyncFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateUserInfo()
        binding.setKeyButton.setOnClickListener {
            AddKeyBottomSheet { updateUserInfo() }.show(childFragmentManager, "AddKeyBottomSheet")
        }
    }

    private fun updateUserInfo() {
        val isKeyMissing =
            encryptedSharedPreferences.getString(MONOBANK_KEY_PREF, "").isNullOrEmpty()
        val userName = encryptedSharedPreferences.getString(
            MONOBANK_NAME_PREF,
            getString(R.string.unknown_label)
        )
        with(binding) {
            accountHolderName.text = getString(R.string.mono_account_holder, userName)
            if (isKeyMissing) {
                monoKeyStatus.text = getString(R.string.missing_label)
                monoKeyStatus.setTextColor(requireContext().getThemedColor(R.attr.errorTextColor))
                syncStatusImage.setImageResource(R.drawable.ic_cloud_off)
            } else {
                syncStatusImage.setImageResource(R.drawable.ic_cloud_ok)
                monoKeyStatus.text = getString(R.string.token_ok_label)
                monoKeyStatus.setTextColor(requireContext().getThemedColor(R.attr.colorPrimary))
            }
        }
    }

}