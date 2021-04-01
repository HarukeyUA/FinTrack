package com.harukeyua.fintrack.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.*
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.databinding.TransactionsSyncFragmentBinding
import com.harukeyua.fintrack.utils.MONOBANK_KEY_PREF
import com.harukeyua.fintrack.utils.MONOBANK_NAME_PREF
import com.harukeyua.fintrack.utils.getThemedColor
import com.harukeyua.fintrack.viewmodels.TransactionSyncViewModel
import com.harukeyua.fintrack.workers.SyncFailures
import com.harukeyua.fintrack.workers.MonoSyncWorker
import com.harukeyua.fintrack.workers.MonoSyncWorker.Companion.RESULT_KEY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionsSyncFragment : Fragment() {

    private var _binding: TransactionsSyncFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionSyncViewModel by viewModels()

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
        binding.monoStartSyncButton.setOnClickListener {
            val workRequest = OneTimeWorkRequest.Builder(MonoSyncWorker::class.java)
            val data = Data.Builder()
            data.putBoolean("RETRY", false)
            workRequest.setInputData(data.build())
            WorkManager.getInstance(requireContext())
                .enqueueUniqueWork(SYNC_WORKER_ID, ExistingWorkPolicy.KEEP, workRequest.build())
        }
        observe()
    }

    private fun observe() {
        viewModel.lastSyncInfo.observe(viewLifecycleOwner) { syncInfo ->
            if (syncInfo.isEmpty())
                binding.lastSyncStatusText.text =
                    getString(R.string.last_successful_sync_never_text)
            else {
                val statusMessage =
                    if (syncInfo.first().isSuccess)
                        R.string.last_successful_sync_text
                    else
                        R.string.last_failed_sync_text
                val statusTextColor =
                    if (syncInfo.first().isSuccess)
                        requireContext().getThemedColor(R.attr.colorPrimary)
                    else
                        requireContext().getThemedColor(R.attr.colorError)
                binding.lastSyncStatusText.text =
                    getString(statusMessage, syncInfo.first().syncDateTime)
                binding.lastSyncStatusText.setTextColor(statusTextColor)
            }
        }

        WorkManager.getInstance(requireContext()).getWorkInfosForUniqueWorkLiveData(SYNC_WORKER_ID)
            .observe(viewLifecycleOwner) { work ->
                if (work.isNotEmpty()) {
                    when (work.first().state) {
                        WorkInfo.State.ENQUEUED -> binding.monoStartSyncButton.isEnabled = false
                        WorkInfo.State.RUNNING -> binding.monoStartSyncButton.isEnabled = false
                        WorkInfo.State.FAILED -> {
                            binding.monoStartSyncButton.isEnabled = true
                            when (work.first().outputData.getInt(RESULT_KEY, 0)) {
                                SyncFailures.CONNECTION_ERROR.code -> showToast(R.string.mono_account_error)
                                SyncFailures.RATE_LIMIT.code -> showToast(R.string.mono_account_error_429)
                                SyncFailures.AUTH_ERROR.code -> showToast(R.string.mono_account_error_403)
                                else -> showToast(R.string.mono_account_error)
                            }
                        }
                        else -> binding.monoStartSyncButton.isEnabled = true
                    }
                } else {
                    binding.monoStartSyncButton.isEnabled = true
                }
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
                syncStatusImage.setImageResource(R.drawable.ic_cloud_off)
                monoKeyStatus.text = getString(R.string.missing_label)
                monoKeyStatus.setTextColor(requireContext().getThemedColor(R.attr.colorError))
            } else {
                syncStatusImage.setImageResource(R.drawable.ic_cloud_ok)
                monoKeyStatus.text = getString(R.string.token_ok_label)
                monoKeyStatus.setTextColor(requireContext().getThemedColor(R.attr.colorPrimary))
            }
        }
    }

    private fun showToast(stringResource: Int) {
        Toast.makeText(requireContext(), stringResource, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val SYNC_WORKER_ID = "SYNC_SINGLE_WORKER"
    }

}