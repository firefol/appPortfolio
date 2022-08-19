package com.example.papp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.papp.R

class PA : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer)
        setToolbar()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setToolbar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}