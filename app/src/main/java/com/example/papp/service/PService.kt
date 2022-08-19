package com.example.papp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.annotation.RequiresApi
import net.posprinter.posprinterface.ProcessData
import net.posprinter.posprinterface.UiExecute
import net.posprinter.utils.DataForSendToPrinterPos80
import net.posprinter.utils.DataForSendToPrinterTSC
import net.posprinter.utils.PosPrinterDev
import com.example.papp.ui.dialog.WarningPopup
import com.example.papp.PSApplication
import com.example.papp.R
import com.example.papp.ui.dialog.SuccessPopup
import ru.atol.xmlparser.XmlParser
import timber.log.Timber
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern


class PService: Service() {

    private val settings by lazy { PSApplication.settings }
    private var portType: PosPrinterDev.PortType? = null
    private var usbAddress: String? = null
    private val broadcast: BroadcastReceiver = object: BroadcastReceiver() {
        lateinit var values: String
        lateinit var map: Map<String,String>
        lateinit var name: String
        lateinit var ages: String
        lateinit var bvalue: String


        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun onReceive(context: Context, intent: Intent) {
            // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
            values = intent.getStringExtra(MAP_VALUES_EXTRA).toString()
            name = intent.getStringExtra(NAME_VALUE_EXTRA).toString()
            ages = intent.getStringExtra(AGE_VALUE_EXTRA).toString()
            bvalue = intent.getStringExtra(BARCODE_VALUE_EXTRA).toString()
            map = values.split(",").associate {
                val (key, value) = it.split("=")
                key to value
            }
            val values = arrayListOf(name,ages,bvalue)
            handler!!.postDelayed(Runnable {
                showSuccessSnackbar(context,name+ages+bvalue)
                if (settings.connectionType == 0) {
                val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                if (bluetoothManager.adapter.isEnabled) {
                    val bondedDevices = bluetoothManager.adapter.bondedDevices
                    if (bondedDevices.size >0) {
                        bondedDevices.forEach {
                            if (it.address == settings.device) {
                                if (!connect) {
                                PSApplication.binder.connectBtPort(
                                    it.address.toString(),
                                    object : UiExecute {
                                        override fun onsucess() {
                                            Timber.i(getString(R.string.con_success))
                                            connect = true
                                            PSApplication.binder.write(
                                                DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(
                                                    0x1f
                                                ),
                                                object : UiExecute {
                                                    override fun onsucess() {
                                                        PSApplication.binder.acceptdatafromprinter(
                                                            object : UiExecute {
                                                                override fun onsucess() {

                                                                }

                                                                override fun onfailed() {
                                                                    Timber.i(getString(R.string.con_has_discon))
                                                                }
                                                            })
                                                    }

                                                    override fun onfailed() {

                                                    }
                                                })
                                        }

                                        override fun onfailed() {
                                            Timber.i(getString(R.string.con_failed))
                                        }
                                    })
                            }
                        }
                        }
                        if (settings.template != "") {
                            try {
                                if (File(settings.templatePath + settings.template).exists()) {
                                    PSApplication.binder.writeDataByYouself(object :
                                        UiExecute {
                                        override fun onsucess() {

                                        }

                                        override fun onfailed() {

                                        }
                                    }, ProcessData {
                                        val list = java.util.ArrayList<ByteArray>()
                                        XmlParser.parseAndPrint(
                                            settings.templatePath,
                                            settings.template,
                                            values,
                                            list
                                        )
                                        list.add(DataForSendToPrinterTSC.print(1))
                                        return@ProcessData list
                                    })
                                } else {
                                    Timber.d("Выбранный шаблон отсутствует в папке с шаблонами!")
                                    showSnackbar(context,"Выбранный шаблон отсутствует в папке с шаблонами!")
                                }
                            } catch (e: Exception) {
                                Timber.d(e)
                            }
                        } else {
                            Timber.d("Не выбран шаблон!")
                            showSnackbar(context,"Не выбран шаблон!")
                        }
                    } else {
                        Timber.d("Broadcast: Нет подключенных устройств!")
                        showSnackbar(context,"Нет подключенных устройств!")
                    }
                } else {
                    Timber.d("Broadcast: Bluetooth не включен!")
                    showSnackbar(context,"Bluetooth не включен!")
                }
            } else {
                if (!connect) {
                        usbAddress = settings.device
                        if (usbAddress == null || usbAddress == "" || containsOnlySpaces(usbAddress)) {
                            showSnackbar(context, getString(R.string.usbselect))
                            Timber.i(getString(R.string.usbselect))
                        } else {
                            PSApplication.binder.connectUsbPort(
                                context,
                                usbAddress,
                                object : UiExecute {
                                    override fun onsucess() {
                                        connect = true
                                        showSnackbar(context, getString(R.string.con_success))
                                        Timber.i(getString(R.string.con_success))
                                        setPortType(PosPrinterDev.PortType.USB)
                                    }

                                    override fun onfailed() {
                                        connect = false
                                        showSnackbar(context, getString(R.string.con_failed))
                                        Timber.i(getString(R.string.con_failed))
                                    }
                                })
                        }
                    }
                    if (settings.template != "") {
                        try {
                            if (File(settings.templatePath + settings.template).exists()) {
                                PSApplication.binder.writeDataByYouself(object :
                                    UiExecute {
                                    override fun onsucess() {

                                    }

                                    override fun onfailed() {

                                    }
                                }, ProcessData {
                                    val list = java.util.ArrayList<ByteArray>()
                                    XmlParser.parseAndPrint(
                                        settings.templatePath,
                                        settings.template,
                                        values,
                                        list
                                    )
                                    list.add(DataForSendToPrinterTSC.print(1))
                                    return@ProcessData list
                                })
                            } else {
                                Timber.d("Выбранный шаблон отсутствует в папке с шаблонами!")
                                showSnackbar(context,"Выбранный шаблон отсутствует в папке с шаблонами!")
                            }
                        } catch (e: Exception) {
                            Timber.d(e)
                        }
                    } else {
                        Timber.d("Не выбран шаблон!")
                        showSnackbar(context,"Не выбран шаблон!")
                    }
            }
                                           }, 0)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Timber.d(" <= Сервис печати: создание...")
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification: Notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(R.id.service, notification)
        }
        val filter = IntentFilter()
        filter.addAction(PRINT_ACTION)
        registerReceiver(broadcast, filter)
        Timber.d(" <= Сервис печати: успешно создан")
    }

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d(" <= Сервис печати: запуск $intent, flags: $flags, startId: $startId")
        mIsServiceRunning = true
        handlerThread = HandlerThread("MyLocationThread")
        handlerThread!!.isDaemon = true
        handlerThread!!.start()
        handler =  Handler(handlerThread!!.looper)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Timber.d(" <= Сервис печати: остановка...")
        try {
            connect = false
            mIsServiceRunning = false
            unregisterReceiver(broadcast)
            handlerThread!!.quit()
            super.onDestroy()
        } catch (e: Exception) {
            Timber.e(e, "onDestroy error")
        }
        Timber.d(" <= Сервис печати: успешно остановлен")
    }

    companion object {

        const val PRINT_ACTION = "com.example.papp.action.PRINT"
        const val MAP_VALUES_EXTRA = "MAP_VALUES_EXTRA"
        const val NAME_VALUE_EXTRA = "NAME_VALUE_EXTRA"
        const val BARCODE_VALUE_EXTRA = "BARCODE_VALUE_EXTRA"
        const val AGE_VALUE_EXTRA = "AGE_VALUE_EXTRA"
        private var connect = false
        private var mIsServiceRunning = false
        private var supressStartTill: Long = 0
        fun setNeedSupressStart() {
            supressStartTill = System.currentTimeMillis() + 1000
        }

        fun needSupressStart(): Boolean {
            return supressStartTill > System.currentTimeMillis()
        }

        val isScanServiceRunning: Boolean
            get() {
                //Timber.d("mIsServiceRunning = %s", mIsServiceRunning)
                return mIsServiceRunning
            }
    }
    private fun showSnackbar(context:Context, showstring: String) {
        //Snackbar.make(container, showstring, Snackbar.LENGTH_LONG).show()
        val w = WarningPopup(context)
        w.text = showstring
        w.show()
    }
    private fun showSuccessSnackbar(context:Context, showstring: String) {
        //Snackbar.make(container, showstring, Snackbar.LENGTH_LONG).show()
        val w = SuccessPopup(context)
        w.text = showstring
        w.show()
    }

    @JvmName("setPortType1")
    private fun setPortType(portType: PosPrinterDev.PortType) {
        this.portType = portType
    }

    fun containsOnlySpaces(line: String?): Boolean {
        val pattern: Pattern = Pattern.compile("\\s+")
        val matcher: Matcher = pattern.matcher(line)
        return matcher.matches()
    }
}