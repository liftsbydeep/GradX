package com.example.gradx

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog



    class Dialog_Box {



    fun Context.showAlertDialog(title: String, message: String) {
        // Inflate the custom layout/view
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_box, null)

        // Find views in the custom layout
        val dtitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dmessage = view.findViewById<TextView>(R.id.dialogMessage)
        val dbtn = view.findViewById<Button>(R.id.dialogButton)

        // Set the text for title and message
        dtitle.text = title
        dmessage.text = message

        // Create the AlertDialog with the custom view
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()

        // Set click listener for the button
        dbtn.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog when button is clicked
        }

        // Show the dialog
        dialog.show()
    }


}
