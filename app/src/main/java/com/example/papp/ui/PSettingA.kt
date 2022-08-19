package com.example.papp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.papp.R

class PSettingA : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_settings)
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