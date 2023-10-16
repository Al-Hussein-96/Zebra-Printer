package com.alhussain.zebraprinter.module

import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.ZebraPrinterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class AbstractPrinter {

    protected lateinit var connection: Connection

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun isConnected() = connection.isConnected

    @Throws(ConnectionException::class)
    abstract suspend fun createConnection(): AbstractPrinter

    @Throws(ConnectionException::class)
    abstract suspend fun connect(): Boolean

    @Throws(ConnectionException::class)
    fun testPrinter() {
        coroutineScope.launch {
            val zebraPrinter = ZebraPrinterFactory.getInstance(connection)

            zebraPrinter.printConfigurationLabel()
        }
    }


}