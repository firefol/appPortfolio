package com.example.papp.ui.dialog

import android.content.Context
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.example.papp.R

class ErrorPopup(private val context: Context) {

    private val view: View = LayoutInflater.from(context).inflate(R.layout.popup_error, null)

    var text: String
        set(value) {
            view.findViewById<TextView>(R.id.errorView).text = value
        }
        get() {
            return view.findViewById<TextView>(R.id.errorView).text.toString()
        }

    init {
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun show() {
        val toast = Toast.makeText(context, null, Toast.LENGTH_LONG)
        toast.view = view
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        toast.show()
    }
}
