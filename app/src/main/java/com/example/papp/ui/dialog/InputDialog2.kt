package com.example.papp.ui.dialog

import android.content.Context
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.internal.TextWatcherAdapter
import com.example.papp.R
import timber.log.Timber

class InputDialog2(private val context: Context, private val title: Spanned, private val text: String) {

    private var dialog: AlertDialog? = null
    private var isExternal = true
    private var inputType = InputType.TYPE_CLASS_TEXT
    private var inputFunc: ((String) -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null
    private var canEditFunc: ((String, String) -> Boolean)? = null
    private var canInputFunc: ((String) -> Boolean)? = null

    fun setInputType(type: Int): InputDialog2 {
        inputType = type
        return this
    }

    fun setOnInputListener(func: ((String) -> Unit)?): InputDialog2 {
        inputFunc = func
        return this
    }

    fun setCanEditFunc(func: (String, String) -> Boolean): InputDialog2 {
        canEditFunc = func
        return this
    }

    fun show() {
        Timber.d(" <= Показать диалог")
        dialog?.dismiss()

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null)

        if (inputType == InputType.TYPE_CLASS_NUMBER)
            view.findViewById<TextView>(R.id.hintView)?.visibility = View.GONE

        val titleView = view.findViewById<TextView>(R.id.titleView)
        val textView = view.findViewById<EditText>(R.id.textView)

        titleView.text = title
        textView.setText(text)
        textView.inputType = inputType
        textView.selectAll()



        dialog = AlertDialog.Builder(context)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ -> inputFunc?.invoke(textView.text.toString()) }
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.cancel() }
            .setOnCancelListener { onCancelListener?.invoke() }
            .create()

        textView.addTextChangedListener(object : TextWatcherAdapter() {
            private var lastText: String = ""
            private var lastSelection: Int = 0

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (!isExternal)
                    return

                lastText = s.toString()
                lastSelection = textView.selectionStart
            }

            override fun afterTextChanged(s: Editable) {
                if (canEditFunc?.invoke(lastText, s.toString()) == false) {
                    isExternal = false

                    textView.setText(lastText)
                    textView.setSelection(lastSelection)

                    isExternal = true

                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = canInputFunc?.invoke(lastText) != false
                } else {
                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = canInputFunc?.invoke(s.toString()) != false
                }
            }
        })

        if (inputType != InputType.TYPE_CLASS_NUMBER)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog?.setOnShowListener {
            textView.requestFocus()
        }

        dialog?.show()
    }
}