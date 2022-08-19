package com.example.papp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.appsearch.AppSearchResult
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_printer_settings.*
import net.posprinter.utils.PosPrinterDev
import com.example.papp.ui.dialog.WarningPopup
import com.example.papp.PSApplication
import com.example.papp.R
import com.example.papp.ui.dialog.SuccessPopup
import timber.log.Timber
import java.io.File

class PSettingF: Fragment() {

    private val pSettings by lazy { PSApplication.settings }
    private var pos = pSettings.connectionType
    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppSearchResult.RESULT_OK) {
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
    private var tList = onSelectTemplateClicked(pSettings.templatePath)
    private lateinit var blManager:BluetoothAdapter
    private var dialogView: View? = null
    private var blArrayAdapter: ArrayAdapter<String>? = null
    private var usbArrayAdapter: ArrayAdapter<String>? = null
    private var listViewBl: ListView? = null
    private var listViewU: ListView? = null
    private val deviceListBonded = ArrayList<String>()
    private var dialog: AlertDialog? = null
    private lateinit var mac:String
    private var dialogViewU: View? = null
    private var textViewU: TextView? = null
    private var uList: List<String>? = null
    private var uDev = ""
    private val viewAdapter: ViewAdapter?
        get() = typeSettingsView?.adapter as? ViewAdapter
    private var templatesFilesAdapter: ArrayAdapter<String>? = null
    private var templatesListView: ListView? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_printer_settings, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewAdapter = ViewAdapter()


        viewAdapter += ViewAdapter.DataItem(
            id = R.id.settingsConectionType,
            name = "Тип подключения",
            data = ViewAdapter.EnumEditData(
                variants = requireContext().resources.getStringArray(R.array.array_connection_types),
                value = pSettings.connectionType
            )
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.infoCleanLogs,
            name = "Принтер",
            data = ViewAdapter.ButtonData {
                onSelectPrinterClicked()
            }
        )

            /*viewAdapter += ViewAdapter.DataItem(
            id = R.id.settingsPrinterConectionType,
            name = "Выбор шаблона 1С для печати",
            data = ViewAdapter.EnumEditData(
                variants = qwe,
                value =
            )
        )*/

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.selectedTemplateVariant,
            name = "Шаблон 1С",
            data = ViewAdapter.TextEditData(value = pSettings.template),
            isReadOnly = true
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.selectTemplateVariant,
            name = "Выбор шаблона 1С",
            data = ViewAdapter.ButtonData {
                val inflater = LayoutInflater.from(activity)
                dialogView = inflater.inflate(R.layout.templates_list, null)
                //onSelectTemplateClicked(dir)
                templatesFilesAdapter = activity?.let {
                    ArrayAdapter<String>(
                        it,
                        android.R.layout.simple_list_item_1,
                        tList
                    )
                }
                templatesListView = dialogView!!.findViewById<View>(R.id.listView1) as ListView
                templatesListView!!.adapter = templatesFilesAdapter
                templatesDialog()
                dialog =
                    AlertDialog.Builder(activity).setTitle("Список шаблонов").setView(dialogView)
                        .create()
                dialog?.show()

            }
        )

        typeSettingsView.adapter = viewAdapter

    }

     fun templatesDialog (){
         templatesListView!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, arg, _ ->
                try {
                    pSettings.template = tList[arg]
                    //mac = msg.substring(msg.length - 17)
                    //msg.substring(0, msg.length - 18)
                    templatesListView!!.setSelection(arg)
                    viewAdapter?.editItemById(R.id.selectedTemplateVariant) {
                        (it as? ViewAdapter.DataItem)?.data =
                            ViewAdapter.TextEditData(value = pSettings.template)
                    }
                    dialog?.cancel()
                } catch (e: Exception) {
                    Timber.e(e)
                    e.printStackTrace()
                }
            }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun onSelectPrinterClicked() {
        Timber.d("Нажато: 'Выбор принтера'")
        pos = ((viewAdapter?.getItemById(R.id.settingsConectionType) as? ViewAdapter.DataItem)?.data as? ViewAdapter.EnumEditData)?.value ?: 0
        when (pos) {
            0 -> {
                Timber.d("Нажато: 'Выбор принтера' для подключения по Bluetooth")
                setBluetooth()
            }
            1 -> {
                Timber.d("Нажато: 'Выбор принтера' для подключения по USB")
                setUSB()
            }
        }
    }

    private fun onSelectTemplateClicked(dir: String): Array<String> {
        // тут будет выбор шаблона 1С для печати
        val file1 = File(dir)
        val files = ArrayList<File>()
        for (file in file1.listFiles()) {
            if(file.extension == "xml") files.add(file)
        }
        val array = arrayOfNulls<String>(files.size)
        for (i in files.indices){
            array[i]= files[i].toString().replace(dir,"")
        }
        //val strings: Array<String> = files.stream().toArray { _Dummy_.__Array__() }
        return array as Array<String>
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun setBluetooth() {
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.blManager =  bluetoothManager.adapter
        if (!this.blManager.isEnabled) {
            //open bluetooth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
            }
        } else {
            showBluetoothList()
        }
    }

    @SuppressLint("InflateParams")
    private fun showBluetoothList() {
        if (!blManager.isDiscovering) {
            blManager.startDiscovery()
        }
        val inflater = LayoutInflater.from(activity)
        dialogView = inflater.inflate(R.layout.printer_list, null)
        blArrayAdapter = activity?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, deviceListBonded) }
        listViewBl = dialogView!!.findViewById<View>(R.id.listView1) as ListView
        listViewBl!!.adapter = blArrayAdapter
        dialog = AlertDialog.Builder(activity).setTitle("Bluetooth").setView(dialogView).create()
        setDlistener()
        findAvalibleDevice()
    }

    @SuppressLint("TimberArgCount", "BinaryOperationInTimber")
    private fun setDlistener() {
        listViewBl!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, arg, _ ->
                try {
                    if (blManager.isDiscovering) {
                        blManager.cancelDiscovery()
                    }
                    val msg = deviceListBonded[arg]
                    mac = msg.substring(msg.length - 17)
                    val name = msg.substring(0, msg.length - 18)
                    if (name.contains("XP-P323") || name.contains("XP-323")) {
                        Timber.i("Нажато: 'Данный принтер не поддерживает многострочность в шаблонах!")
                        showWarningSnackbar("Данный принтер не поддерживает многострочность!")
                    }
                    listViewBl!!.setSelection(arg)
                    dialog?.cancel()
                    pSettings.device = mac
                    Timber.i("TAG", "mac=$mac")
                } catch (e: Exception) {
                    Timber.e(e)
                    e.printStackTrace()
                }
            }
    }

    private fun findAvalibleDevice() {
        val device = blManager.bondedDevices
        deviceListBonded.clear()
        if (blManager.isDiscovering) {
            blArrayAdapter!!.notifyDataSetChanged()
        }
        if (device.size > 0) {
            val it: Iterator<BluetoothDevice> = device.iterator()
            while (it.hasNext()) {
                val btd = it.next()
                deviceListBonded.add(
                    """
                        ${btd.name}
                        ${btd.address}
                        """.trimIndent()
                )
                blArrayAdapter!!.notifyDataSetChanged()
                dialog?.show()
            }
        } else {
            //deviceListBonded.add("Не может быть сопоставлено для использования Bluetooth")
            //bluetoothArrayAdapter!!.notifyDataSetChanged()
            showWarningSnackbar("Нет устройств подключенных по Bluetooth!")
            Timber.i("Нет устройств подключенных по Bluetooth!")
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun setUSB() {
        val inflater = LayoutInflater.from(activity)
        dialogViewU = inflater.inflate(R.layout.usb_link, null)
        textViewU = dialogViewU!!.findViewById<View>(R.id.textView1) as TextView
        listViewU = dialogViewU!!.findViewById<View>(R.id.listView1) as ListView
        uList = PosPrinterDev.GetUsbPathNames(activity)
        if (uList == null) {
            showWarningSnackbar("Нет устройств подключенных по USB!")
            Timber.i("Нет устройств подключенных по USB!")
        } else {
            textViewU!!.text = getString(R.string.usb_pre_connected) + uList!!.size
            usbArrayAdapter =
                activity?.let {
                    ArrayAdapter<String>(
                        it,
                        android.R.layout.simple_list_item_1,
                        uList!!
                    )
                }
            listViewU!!.adapter = usbArrayAdapter
            val dialog: AlertDialog = AlertDialog.Builder(activity)
                .setView(dialogViewU).create()
            dialog.show()
            setUsbListener(dialog)
        }
    }

    @SuppressLint("TimberArgCount")
    fun setUsbListener(dialog: AlertDialog) {
        listViewU!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, i, _ ->
                uDev = uList!![i]
                pSettings.device = uDev
                dialog.cancel()
                Timber.i("usbDev: ", uDev)
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

    override fun onPause() {
        super.onPause()
        pSettings.connectionType = ((viewAdapter?.getItemById(R.id.settingsConectionType) as? ViewAdapter.DataItem)?.data as? ViewAdapter.EnumEditData)?.value ?: 0
        //printerSettings.printerTemplate = viewAdapter?.getEnumValue(R.id.settingsPrinterConectionType) ?: 0
    }

}