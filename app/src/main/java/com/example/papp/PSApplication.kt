package com.example.papp

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.service.PosprinterService
import com.example.papp.settings.PSettings
import com.example.papp.utils.FileLoggingTree
import com.example.papp.utils.PrintReceiver
import com.example.papp.utils.StorageUtils
import timber.log.Timber
import java.io.File

class PSApplication : Application() {

    companion object {
        lateinit var instance: PSApplication
        lateinit var binder: IMyBinder
        lateinit var settings: PSettings

    }

    private val connection: ServiceConnection = object : ServiceConnection {
        @SuppressLint("TimberArgCount")
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            binder = iBinder as IMyBinder
            Timber.d("binder connected")

        }

        @SuppressLint("TimberArgCount")
        override fun onServiceDisconnected(componentName: ComponentName) {
            Timber.d("disbinder disconected")
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate() {
        super.onCreate()
        instance = this
        val intent = Intent(this, PosprinterService::class.java)
        settings = PSettings(this)
        Timber.plant(FileLoggingTree(this))
        PrintReceiver.registerReceivers(this)
        // данный кода будут использоваться чтобы создать папку не открывая самого приложения(используя как сервис)
        val templatesDirectory = File(settings.templatePath)
        templatesDirectory.mkdirs()
        //
        bindService(intent, connection, BIND_AUTO_CREATE)
        val sizeFree = StorageUtils.getFreeSize()
        val sizeAvailable = StorageUtils.getAvailableSpacePercent()
        Timber.i("Размер логов: ${StorageUtils.getLogsSize()}; свободно: $sizeFree (${sizeAvailable}%)")
        Timber.i("Application запущено")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onTerminate() {
        val sizeFree = StorageUtils.getFreeSize()
        val sizeAvailable = StorageUtils.getAvailableSpacePercent()
        Timber.i("Размер логов: ${StorageUtils.getLogsSize()}; свободно: $sizeFree (${sizeAvailable}%)")
        Timber.i("Application остановлено ${BuildConfig.VERSION_NAME}")
        super.onTerminate()
    }
}