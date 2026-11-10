package com.example.lab9_mapkit

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class PlaceCardDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val coordinates = String.format(Locale.ROOT, "%.5f, %.5f", args.getDouble("latitude"), args.getDouble("longitude"))
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.getString("title"))
            .setMessage(args.getString("description") + "\n\n" + coordinates)
            .setPositiveButton(R.string.close, null).create()
    }

    companion object {
        fun forPlace(place: Place) = PlaceCardDialog().apply {
            arguments = bundleOf("title" to place.title, "description" to place.description,
                "latitude" to place.latitude, "longitude" to place.longitude)
        }
    }
}
