package com.example.papp.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_setting.*
import com.example.papp.ui.dialog.ErrorPopup
import com.example.papp.ui.dialog.WarningPopup
import com.example.papp.PSApplication
import com.example.papp.R
import com.example.papp.models.SettingViewModel


class SettingF : Fragment() {

    private lateinit var viewModel: SettingViewModel
    private val printerSettings by lazy { PSApplication.settings }

    private val viewAdapter: ViewAdapter?
        get() = settingsView?.adapter as? ViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewAdapter = ViewAdapter()

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.infoVersion,
            name = "Версия приложения",
            data = ViewAdapter.TextEditData(
                value = "Atol Printer service" + requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).versionName
            ),
            isReadOnly = true
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.infoLogsSize,
            name = "Размер логов",
            data = ViewAdapter.TextEditData(value = "N/A"),
            isReadOnly = true
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.settingsDataLoggingSize,
            name = "Логирование",
            data = ViewAdapter.SeekEditData(
                description = "Максимальный размер логов, в мегабайтах",
                range = 20..100,
                value = printerSettings.loggingSize
            ),
            isReadOnly = false
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.diagnosticsOverallSize,
            name = "Общий размер",
            data = ViewAdapter.TextEditData(value = "", isHorizontal = true),
            isReadOnly = true
        )

        viewAdapter += ViewAdapter.FractionItem(
            id = R.id.diagnosticsFreeSize
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.infoCleanLogs,
            name = "Очистить логи",
            data = ViewAdapter.ButtonData {
                clearLogs()
            }
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.diagnosticsExportDb,
            name = "Экспорт логов и настроек",
            data = ViewAdapter.ButtonData {
                exportDiag()
            }
        )

        settingsView.adapter = viewAdapter

        viewModel.dataLive.observe(viewLifecycleOwner, Observer { updateDiagnostics(it) })
        viewModel.fetchError.observe(viewLifecycleOwner, Observer { it.getValue { showError(it) } })
        viewModel.fetchInfo.observe(viewLifecycleOwner, Observer { it.getValue { showInfo(it) } })

        viewModel.fetch(requireContext())

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun clearLogs() {
        viewModel.clearLogs(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun exportDiag() {
        viewModel.exportDiag(requireContext())
    }

    private fun updateDiagnostics(data: SettingViewModel.Diag) {
        viewAdapter?.editItemById(R.id.infoVersion) {
            (it as? ViewAdapter.DataItem)?.data = ViewAdapter.TextEditData(value = data.version)
        }

        viewAdapter?.editItemById(R.id.infoLogsSize) {
            (it as? ViewAdapter.DataItem)?.data =
                ViewAdapter.TextEditData(value = data.logSize)
        }

        /*viewAdapter?.editItemById(R.id.infoFreeSize) {
            (it as? ViewAdapter.DiagnosticsItem)?.text = "${data.freeSize} (${data.freeSizePercent.format(0) }%)"
            (it as? ViewAdapter.DiagnosticsItem)?.state = getFreeSizeState(data.freeSizePercent)
        }*/

        viewAdapter?.editItemById(R.id.diagnosticsOverallSize) {
            (it as? ViewAdapter.DataItem)?.data =
                ViewAdapter.TextEditData(value = data.totalSize, isHorizontal = true)
        }

        viewAdapter?.editItemById(R.id.diagnosticsFreeSize) {
            (it as? ViewAdapter.FractionItem)?.apply {
                fractionPercent = 100.0F - data.freeSizePercent.toFloat()
                firstText = "Занято - ${data.overalSize}"
                secondText = "Свободно - ${data.freeSize}"
            }
        }

    }


    private fun showError(e: Exception?) {
        val err = ErrorPopup(requireContext())
        err.text = e?.localizedMessage ?: e?.message ?: "Error"
        err.show()
    }

    private fun showInfo(text: String?) {
        if (text != null) {
            val w = WarningPopup(requireContext())
            w.text = text
            w.show()
        }
    }

    override fun onPause() {
        super.onPause()
        printerSettings.loggingSize = ((viewAdapter?.getItemById(R.id.settingsDataLoggingSize) as? ViewAdapter.DataItem)?.data as? ViewAdapter.SeekEditData)?.value ?: 100
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun fetch() {
        viewModel.fetch(requireContext())
    }
    fun Double.format(digits: Int) = "%.${digits}f".format(this)
}