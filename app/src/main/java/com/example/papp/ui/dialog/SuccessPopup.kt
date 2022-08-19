package com.example.papp.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.papp.R

class SuccessPopup(private val context: Context) {

        private val view: View = LayoutInflater.from(context).inflate(R.layout.popup_success, null)

        var text: String
            set(value) {
                view.findViewById<TextView>(R.id.successView).text = value
            }
            get() {
                return view.findViewById<TextView>(R.id.successView).text.toString()
            }

        init {
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        fun show() {
            val toast = Toast.makeText(context, null, Toast.LENGTH_SHORT)

            toast.view = view
            toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            toast.show()
        }
}