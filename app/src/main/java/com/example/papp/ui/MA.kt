package com.example.papp.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.main_activity.*
import com.example.papp.PSApplication
import com.example.papp.R
import timber.log.Timber
import java.io.File


class MA : AppCompatActivity() {

    private val pSettings by lazy { PSApplication.settings }
    @RequiresApi(Build.VERSION_CODES.R)
    private var requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach{
            val templatesDirectory = File(pSettings.templatePath)
            templatesDirectory.mkdirs()

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            requestStoragePermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        setContentView(R.layout.main_activity)
        setTitle()
        setToolbar()
        setBackStackListener()
        Timber.i("Приложение Main activity запущено")

    }

    override fun onSupportNavigateUp(): Boolean {
        return when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawers()
                true
            }
            else -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
        }
    }

    private fun setBackStackListener() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (slideOffset < 0.5)
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
                else
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }
        })
    }

    private fun setTitle() {
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowCustomEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            val view = LayoutInflater.from(this).inflate(R.layout.view_title, null)
            val textView = view.findViewById<TextView>(R.id.title)
            textView.text = "${getString(R.string.main_title_text)}   "
            supportActionBar!!.customView = view
        }
    }

    private fun setToolbar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        Timber.i("Приложение остановлено")
        super.onDestroy()
    }
}