package com.alhussain.zebraprinter.module

import android.content.Context
import android.util.Log
import com.alhussain.zebraprinter.BluetoothPrinterEntity
import com.zebra.sdk.btleComm.BluetoothLeConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.printer.ZebraPrinterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BluetoothPrinter(
    private val printerEntity: BluetoothPrinterEntity,
    private val context: Context,
) : AbstractPrinter() {


    override suspend fun createConnection(): BluetoothPrinter = suspendCoroutine {

        connection = BluetoothLeConnection(printerEntity.mac, context)

        it.resumeWith(Result.success(this))

        Log.i("PrinterConnection", "Bluetooth connection created")
    }
    override suspend fun connect() = suspendCoroutine {
        connection.open()

        it.resumeWith(Result.success(true))
        Log.i("PrinterConnection", "Bluetooth connected")
    }


}