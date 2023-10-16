package com.alhussain.zebraprinter

import android.content.Context
import com.zebra.sdk.btleComm.BluetoothLeConnection
import com.zebra.sdk.comm.Connection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BluetoothLeConnectionPrinterMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : ConnectionFactory() {

    private lateinit var connection: BluetoothLeConnection

    private val coroutineScope: CoroutineScope = CoroutineScope(dispatcher)


    fun connect(bluetoothPrinterEntity: BluetoothPrinterEntity) = flow {
        coroutineScope.launch {
            connection = BluetoothLeConnection(bluetoothPrinterEntity.mac, context)
            emit(false)
            connection.open()
            emit(true)
        }
    }.flowOn(dispatcher)

//    override fun isConnected(): Boolean = connection.isConnected
//
//
//    @Throws(ConnectionException::class)
//    override fun getZebraPrinter(): ZebraPrinter {
//        return ZebraPrinterFactory.getInstance(connection)
//    }

    override fun createConnection(printerEntity: PrinterEntity): Connection {
        if (printerEntity is BluetoothPrinterEntity) {
            return BluetoothLeConnection(printerEntity.mac, context)
        }
        throw Exception("Error create connection")
    }

}