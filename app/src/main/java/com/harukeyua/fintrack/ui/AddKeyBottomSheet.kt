/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harukeyua.fintrack.ui

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.api.ClientInfo
import com.harukeyua.fintrack.databinding.AddKeyDialogBinding
import com.harukeyua.fintrack.utils.MONOBANK_KEY_PREF
import com.harukeyua.fintrack.utils.MONOBANK_NAME_PREF
import com.harukeyua.fintrack.utils.Resource
import com.harukeyua.fintrack.viewmodels.AddKeyBottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddKeyBottomSheet(private val onDismissAction: () -> Unit) : BottomSheetDialogFragment() {

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences
    private val viewModel: AddKeyBottomSheetViewModel by viewModels()

    private lateinit var binding: AddKeyDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddKeyDialogBinding.inflate(inflater, container, false)

        setupListeners()

        observe()

        return binding.root
    }

    private fun setupListeners() {
        binding.addKeyButton.setOnClickListener {
            val keyText = binding.apiKeyText.editText?.text?.toString() ?: ""
            viewModel.verifyKey(keyText)
        }
    }

    private fun observe() {
        viewModel.incorrectKeySizeError.observe(viewLifecycleOwner) { size ->
            size.getContentIfNotHandled()?.let {
                if (it == 0) {
                    binding.apiKeyText.error = getString(R.string.required_field_label)
                } else {
                    binding.apiKeyText.error = getString(R.string.key_length_error)
                }
            }
        }

        viewModel.clientInfoStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.ApiError -> showApiError(status)
                is Resource.ExceptionError -> showIOError(status)
                Resource.Loading -> {
                    binding.apiKeyText.error = null
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is Resource.Success -> onSuccess(status)
            }
        }
    }

    private fun onSuccess(status: Resource.Success<ClientInfo>) {
        binding.progressIndicator.visibility = View.INVISIBLE
        encryptedSharedPreferences.edit(commit = true) {
            putString(
                MONOBANK_KEY_PREF,
                viewModel.apiKey
            )
            putString(
                MONOBANK_NAME_PREF,
                status.data.name
            )
        }
        Toast.makeText(
            requireContext(),
            getString(R.string.success),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    private fun showIOError(status: Resource.ExceptionError) {
        binding.progressIndicator.visibility = View.INVISIBLE
        Toast.makeText(
            requireContext(),
            getString(R.string.io_error_text, status.e),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showApiError(status: Resource.ApiError) {
        binding.progressIndicator.visibility = View.INVISIBLE
        val errorText = when (status.errorCode) {
            403 -> getString(R.string.mono_account_error_403)
            429 -> getString(R.string.mono_account_error_429)
            else -> getString(R.string.mono_account_error)
        }
        Toast.makeText(
            requireContext(),
            errorText,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissAction()
    }
}