package com.alhussain.zebraprinter.module

import android.util.Log
import com.alhussain.zebraprinter.Connection
import com.alhussain.zebraprinter.PrinterEntity
import com.alhussain.zebraprinter.WifiPrinterEntity
import com.zebra.sdk.comm.TcpConnection
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class TcpPrinter(private val printerEntity: WifiPrinterEntity) : AbstractPrinter() {

    override suspend fun createConnection(): AbstractPrinter = suspendCoroutine {
        connection = TcpConnection(printerEntity.ip, printerEntity.port.toInt())
        Log.i("PrinterConnection", "TCP connection created")
        it.resumeWith(Result.success(this))
    }


    override suspend fun connect() = suspendCoroutine {
        connection.open()
        it.resumeWith(Result.success(true))
        Log.i("PrinterConnection", "Tcp connected")
    }


}