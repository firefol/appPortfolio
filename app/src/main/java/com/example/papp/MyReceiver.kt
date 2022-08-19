package com.example.papp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.ListView

class MyReceiver(
    deviceList_found: ArrayList<String>,
    adapter2: ArrayAdapter<String>?,
    lv2: ListView
) : BroadcastReceiver() {

    private var deviceList_found = ArrayList<String>()
    private var adapter: ArrayAdapter<String>? = null
    private var listView: ListView? = null

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
        if (BluetoothDevice.ACTION_FOUND == action) {
            //search for new device
            val btd = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            //the device din't boned
            if (btd!!.bondState != BluetoothDevice.BOND_BONDED) {
                if (!deviceList_found.contains(
                        """
                    ${btd!!.name}
                    ${btd!!.address}
                    """.trimIndent()
                    )
                ) {
                    deviceList_found.add(
                        """
                    ${btd!!.name}
                    ${btd!!.address}
                    """.trimIndent()
                    )
                    try {
                        adapter!!.notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
            //search result
            if (listView!!.count == 0) {
                deviceList_found.add(context.getString(R.string.none_ble_device))
                try {
                    adapter!!.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}