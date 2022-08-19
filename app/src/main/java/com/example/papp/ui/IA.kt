package com.example.papp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_about.*
import com.example.papp.R
import java.text.SimpleDateFormat
import java.util.*

class IA : AppCompatActivity() {

    private val DATE = "dd.MM.yyyy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setToolbar()
        val dt = SimpleDateFormat(DATE).format(Date(getString(R.string.build_date).toLong()))
        versionView.text = "Версия ${getString(R.string.build_version)} от ${dt}"
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