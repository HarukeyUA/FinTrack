package com.harukeyua.fintrack.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.*
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.databinding.TransactionsSyncFragmentBinding
import com.harukeyua.fintrack.utils.MONOBANK_KEY_PREF
import com.harukeyua.fintrack.utils.MONOBANK_NAME_PREF
import com.harukeyua.fintrack.utils.PREFS_NAME
import com.harukeyua.fintrack.utils.getThemedColor
import com.harukeyua.fintrack.viewmodels.TransactionSyncViewModel
import com.harukeyua.fintrack.workers.MonoSyncWorker
import com.harukeyua.fintrack.workers.MonoSyncWorker.Companion.RESULT_KEY
import com.harukeyua.fintrack.workers.MonoSyncWorker.Companion.RETRY_DATA_KEY
import com.harukeyua.fintrack.workers.SyncFailures
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class TransactionsSyncFragment : Fragment() {

    private var _binding: TransactionsSyncFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionSyncViewModel by viewModels()

    private lateinit var workManager: WorkManager

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TransactionsSyncFragmentBinding.inflate(inflater, container, false)
        workManager = WorkManager.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateUserInfo()
        binding.setKeyButton.setOnClickListener {
            AddKeyBottomSheet { updateUserInfo() }.show(childFragmentManager, "AddKeyBottomSheet")
        }
        binding.monoStartSyncButton.setOnClickListener {
            val workRequest = OneTimeWorkRequest.Builder(MonoSyncWorker::class.java)
                .setInputData(workDataOf(RETRY_DATA_KEY to false)).build()
            workManager.enqueueUniqueWork(
                SYNC_WORKER_ID,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            observeWorkState(workRequest.id)
        }
        binding.monoPeriodicSyncCheck.isChecked =
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(PERIODIC_SYNC_PREF_KEY, false)

        binding.monoPeriodicSyncCheck.setOnCheckedChangeListener { _, isChecked ->
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit(commit = true) {
                    putBoolean(PERIODIC_SYNC_PREF_KEY, isChecked)
                }
            if (isChecked) {
                val workRequest = PeriodicWorkRequestBuilder<MonoSyncWorker>(12, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED).build()
                    )
                    .addTag("sync")
                    .setInputData(workDataOf(RETRY_DATA_KEY to true))
                    .build()
                workManager.enqueueUniquePeriodicWork(
                    PERIODIC_SYNC_WORKER_ID,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } else {
                workManager.cancelUniqueWork(PERIODIC_SYNC_WORKER_ID)
            }
        }

        binding.tokenRemoveButton.setOnClickListener {
            viewModel.removeMonoAccounts()
            // Disable periodic sync
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit(commit = true) {
                    putBoolean(PERIODIC_SYNC_PREF_KEY, false)
                }
            workManager.cancelUniqueWork(PERIODIC_SYNC_WORKER_ID)
            // Remove personal information
            encryptedSharedPreferences.edit(commit = true) {
                putString(MONOBANK_NAME_PREF, "")
                putString(MONOBANK_KEY_PREF, "")
            }
            updateUserInfo()
        }
        observe()
    }

    private fun observe() {
        viewModel.lastSyncInfo.observe(viewLifecycleOwner) { syncInfo ->
            if (syncInfo.isEmpty()) {
                binding.lastSyncStatusText.text =
                    getString(R.string.last_successful_sync_never_text)
                binding.lastSyncStatusText.setTextColor(requireContext().getThemedColor(R.attr.colorOnSurface))
            } else {
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
    }

    private fun updateUserInfo() {
        val isKeyMissing =
            encryptedSharedPreferences.getString(MONOBANK_KEY_PREF, "").isNullOrEmpty()
        val userName = encryptedSharedPreferences.getString(
            MONOBANK_NAME_PREF,
            getString(R.string.unknown_label)
        )
        with(binding) {
            accountHolderName.text = getString(
                R.string.mono_account_holder,
                if (userName.isNullOrEmpty()) getString(R.string.unknown_label) else userName
            )
            if (isKeyMissing) {
                syncStatusImage.setImageResource(R.drawable.ic_cloud_off)
                monoKeyStatus.text = getString(R.string.missing_label)
                monoKeyStatus.setTextColor(requireContext().getThemedColor(R.attr.colorError))
                binding.monoPeriodicSyncCheck.isEnabled = false
                binding.monoStartSyncButton.isEnabled = false
            } else {
                syncStatusImage.setImageResource(R.drawable.ic_cloud_ok)
                monoKeyStatus.text = getString(R.string.token_ok_label)
                monoKeyStatus.setTextColor(requireContext().getThemedColor(R.attr.colorPrimary))
                binding.monoPeriodicSyncCheck.isEnabled = true
                binding.monoStartSyncButton.isEnabled = true
            }
        }
    }

    private fun observeWorkState(uuid: UUID) {
        workManager.getWorkInfoByIdLiveData(uuid)
            .observe(viewLifecycleOwner) { work ->
                if (work != null) {
                    when (work.state) {
                        WorkInfo.State.ENQUEUED -> binding.monoStartSyncButton.isEnabled = false
                        WorkInfo.State.RUNNING -> binding.monoStartSyncButton.isEnabled = false
                        WorkInfo.State.FAILED -> {
                            binding.monoStartSyncButton.isEnabled = true
                            when (work.outputData.getInt(RESULT_KEY, 0)) {
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

    private fun showToast(stringResource: Int) {
        Toast.makeText(requireContext(), stringResource, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val SYNC_WORKER_ID = "SYNC_SINGLE_WORKER"
        const val PERIODIC_SYNC_WORKER_ID = "PERIODIC_SYNC"
        const val PERIODIC_SYNC_PREF_KEY = "PERIODIC_SYNC_PREF"
    }

}