package com.example.papp.ui


import android.app.appsearch.AppSearchResult.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import net.posprinter.posprinterface.UiExecute
import net.posprinter.utils.DataForSendToPrinterPos80
import net.posprinter.utils.PosPrinterDev.PortType
import com.example.papp.ui.dialog.WarningPopup
import com.example.papp.PSApplication
import com.example.papp.R
import com.example.papp.service.PService
import com.example.papp.ui.dialog.SuccessPopup
import timber.log.Timber
import java.util.regex.Matcher
import java.util.regex.Pattern


class MMF : Fragment(),View.OnClickListener {

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Timber.d("Bluetooth request canceled")
        } else {
            Timber.d("Bluetooth request accept")
        }
    }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { _ ->
            }
        }
    companion object{
        var isConnect = false
    }

    private val printerSettings by lazy { PSApplication.settings }
    private lateinit var showET: EditText
    private lateinit var buttonConnect: Button
    private lateinit var container: CoordinatorLayout
    private var portType: PortType? = null
    private var usbAddress: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonP: Button = view.findViewById(R.id.buttonXP323B)
        showET = view.findViewById(R.id.selected_device)
        if (printerSettings.device != "") {
            showET.setText(printerSettings.device)
        }
        if (PService.isScanServiceRunning) {
            Timber.d("Сервис уже запущен")
        } else {
            val intentBroadcast = Intent(requireContext(), PService::class.java)
            ContextCompat.startForegroundService(requireContext(), intentBroadcast)
        }
        val buttonSelect: Button = view.findViewById((R.id.selectbutton))
        buttonConnect = view.findViewById(R.id.connbutton)
        if (isConnect) buttonConnect.text = getString(R.string.con_success)
        container = view.findViewById(R.id.drawerLayout)
        val buttonDisconnect: Button = view.findViewById(R.id.discbutton)
        val buttonSend: Button = view.findViewById(R.id.broadcast_send)
        buttonP.setOnClickListener(this)
        buttonSelect.setOnClickListener(this)
        buttonConnect.setOnClickListener(this)
        buttonSend.setOnClickListener(this)
        buttonDisconnect.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        showET.setText(printerSettings.device)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(p0: View?) {
        val id = p0!!.id
        if (id == R.id.buttonXP323B) {
            Timber.d("Нажато: 'Работа с устройством'")
            if (isConnect) {
                val intent = Intent(context, PA::class.java)
                startActivity(intent)
            } else {
                Timber.i("устройство не подключено")
                showWarningSnackbar("уйстройство не подключено")
            }
        }
        if (id == R.id.connbutton) {
            when (printerSettings.connectionType) {
                0 -> {
                    Timber.d("Нажато: 'Подключение' для подключения по Bluetooth")
                    showET.setText(printerSettings.device)
                    connectBluetooth()
                }
                1 -> {
                    Timber.d("Нажато: 'Подключение' для подключения по USB")
                    showET.setText(printerSettings.device)
                    connectUSB()
                }
            }
        }
        if (id == R.id.discbutton) {
            Timber.d("Нажато: 'Отключение'")
            val binder = PSApplication.binder
            if (isConnect) {
                binder.disconnectCurrentPort(object : UiExecute {
                    override fun onsucess() {
                        showSuccessSnackbar(getString(R.string.toast_discon_success))
                        //showET.setText("")
                        isConnect = false
                        buttonConnect.text = getString(R.string.connect)
                        Timber.i(getString(R.string.toast_discon_success))
                    }

                    override fun onfailed() {
                        showSuccessSnackbar(getString(R.string.toast_discon_failed))
                        Timber.i(getString(R.string.toast_discon_failed))
                    }
                })
            } else {
                showWarningSnackbar(getString(R.string.toast_present_con))
                Timber.i(getString(R.string.toast_present_con))
                buttonConnect.text = getString(R.string.connect)
            }
        }
        if (id == R.id.broadcast_send) {
            val intent = Intent()
            val map = mapOf(
                "Имя" to "Александр", "Возраст" to "21",
                "ШК" to "127468126747")
            val mapString = map.toString()
            intent.action = PService.PRINT_ACTION
            intent.putExtra(PService.MAP_VALUES_EXTRA,mapString)
            intent.putExtra(PService.NAME_VALUE_EXTRA, "печенье Шлёпа")
            intent.putExtra(PService.AGE_VALUE_EXTRA, "21")
            intent.putExtra(PService.BARCODE_VALUE_EXTRA, "127468126747")
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            activity?.sendBroadcast(intent)
        }
    }



    private fun connectBluetooth() {
        val binder = PSApplication.binder
        val bleAdrress = showET.text.toString()
        if (bleAdrress == "") {
            showWarningSnackbar(getString(R.string.bleselect))
            Timber.i(getString(R.string.bleselect))
        } else {
            if (!isConnect) {
            binder.connectBtPort(bleAdrress, object : UiExecute {
                override fun onsucess() {
                    isConnect = true
                    showSuccessSnackbar(getString(R.string.con_success))
                    Timber.i(getString(R.string.con_success))
                    buttonConnect.text = getString(R.string.con_success)
                    binder.write(
                        DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(0x1f),
                        object : UiExecute {
                            override fun onsucess() {
                                binder.acceptdatafromprinter(object : UiExecute {
                                    override fun onsucess() {

                                    }

                                    override fun onfailed() {
                                        //isConnect = false
                                        //showWarningSnackbar(getString(R.string.con_has_discon))
                                        //Timber.i(getString(R.string.con_has_discon))
                                        //buttonConnect.text = getString(R.string.connect)
                                    }
                                })
                            }

                            override fun onfailed() {

                            }
                        })
                }

                override fun onfailed() {
                    isConnect = false
                    showWarningSnackbar(getString(R.string.con_failed))
                    Timber.i(getString(R.string.con_failed))
                }
            })
        }
        }
    }

    private fun connectUSB() {
        usbAddress = showET.text.toString()
        if (usbAddress == null || usbAddress == "" || containsOnlySpaces(usbAddress)) {
            showWarningSnackbar(getString(R.string.usbselect))
            Timber.i(getString(R.string.usbselect))
        } else {
            PSApplication.binder.connectUsbPort(
               context,
                usbAddress,
                object : UiExecute {
                    override fun onsucess() {
                        isConnect = true
                        showSuccessSnackbar(getString(R.string.con_success))
                        Timber.i(getString(R.string.con_success))
                        buttonConnect.text = getString(R.string.con_success)
                        setPortType(PortType.USB)
                    }

                    override fun onfailed() {
                        isConnect = false
                        showWarningSnackbar(getString(R.string.con_failed))
                        Timber.i(getString(R.string.con_failed))
                        //buttonConnect.text = getString(R.string.con_failed)
                    }
                })
        }
    }

    private fun showSuccessSnackbar(showstring: String) {
        //Snackbar.make(container, showstring, Snackbar.LENGTH_LONG).show()
        val w = SuccessPopup(requireContext())
        w.text = showstring
        w.show()
    }

    private fun showWarningSnackbar(showstring: String) {
        //Snackbar.make(container, showstring, Snackbar.LENGTH_LONG).show()
        val w = WarningPopup(requireContext())
        w.text = showstring
        w.show()
    }

    @JvmName("setPortType1")
    private fun setPortType(portType: PortType) {
        this.portType = portType
    }

    fun containsOnlySpaces(line: String?): Boolean {
        val pattern: Pattern = Pattern.compile("\\s+")
        val matcher: Matcher = pattern.matcher(line)
        return matcher.matches()
    }
}