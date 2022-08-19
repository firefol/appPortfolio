package com.example.papp.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.android.synthetic.main.drawer_menu_fragment.*
import com.example.papp.R
import timber.log.Timber


class DMF : Fragment() {

    private var editLauncher:ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }
        return inflater.inflate(R.layout.drawer_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val viewAdapter = ViewAdapter()
        viewAdapter += ViewAdapter.DataItem(
            id = R.id.settingsButton,
            name = "Общие настройки",
            data = ViewAdapter.ButtonData { onSettingsClicked() }
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.settingPrinterButton,
            name = "Настройки принтера",
            data = ViewAdapter.ButtonData { onSettingsPrinterClicked() }
        )

        viewAdapter += ViewAdapter.DataItem(
            id = R.id.typeSettingsButton,
            name = "О программе",
            data = ViewAdapter.ButtonData { onBarcodeInfoClicked() }
        )

        optionsView.adapter = viewAdapter
        exitButton.setOnClickListener { onExitClicked() }

    }

    private fun onBarcodeInfoClicked() {
        Timber.d("Нажато: 'О приложении'")
        editLauncher?.launch(Intent(requireContext(), IA::class.java))
    }

    private fun onSettingsClicked() {
        Timber.d("Нажато: 'Общие настройки'")

        editLauncher?.launch(Intent(requireContext(), SettingA::class.java))
    }

    private fun onSettingsPrinterClicked() {
        Timber.d("Нажато: 'Настройки принтера'")

        editLauncher?.launch(Intent(requireContext(), PSettingA::class.java))
    }


    private fun onExitClicked() {
        Timber.d("Нажато: 'Выход'")
        activity?.finish()
    }
}