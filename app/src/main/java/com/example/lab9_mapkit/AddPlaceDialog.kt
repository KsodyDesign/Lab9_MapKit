package com.example.lab9_mapkit

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddPlaceDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input = EditText(requireContext()).apply {
            id = R.id.placeNameInput
            hint = getString(R.string.place_name)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine()
            filters = arrayOf(android.text.InputFilter.LengthFilter(80))
            setText(savedInstanceState?.getString("title"))
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.new_place).setView(input)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.add, null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    input.error = getString(R.string.empty_name)
                } else {
                    parentFragmentManager.setFragmentResult(RESULT,
                        bundleOf("title" to name, "latitude" to requireArguments().getDouble("latitude"),
                            "longitude" to requireArguments().getDouble("longitude")))
                    dismiss()
                }
            }
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("title", dialog?.findViewById<EditText>(R.id.placeNameInput)?.text?.toString())
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val RESULT = "add_place"
        fun at(latitude: Double, longitude: Double) = AddPlaceDialog().apply {
            arguments = bundleOf("latitude" to latitude, "longitude" to longitude)
        }
    }
}
