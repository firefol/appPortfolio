package com.example.papp.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.papp.R

class SettingA : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setToolbar()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onResume() {
        super.onResume()
        currentFragment()?.fetch()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setToolbar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun currentFragment() : SettingF? =
        (supportFragmentManager.fragments.find { (it is SettingF) } as? SettingF)
}