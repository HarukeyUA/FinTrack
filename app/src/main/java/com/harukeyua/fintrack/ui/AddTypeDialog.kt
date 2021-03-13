package com.harukeyua.fintrack.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.harukeyua.fintrack.R

class AddTypeDialog(private val listener: (name: String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.add_type_dialog, null))
                .setPositiveButton("ADD") { _, _ ->
                    val name = dialog?.findViewById<TextInputLayout>(R.id.type_name_text)
                        ?.editText?.text.toString()
                    if (name.isNotEmpty())
                        listener(name)
                }
                .setNegativeButton("CANCEL") { _, _ ->
                    dialog?.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException()
    }
}