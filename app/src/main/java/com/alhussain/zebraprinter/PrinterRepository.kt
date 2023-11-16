package com.alhussain.zebraprinter

import android.content.Context
import android.net.Uri
import com.alhussain.zebraprinter.module.AbstractPrinter
import com.alhussain.zebraprinter.module.PrinterFactory
import com.zebra.sdk.btleComm.BluetoothLeDiscoverer
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.NetworkDiscoverer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrinterRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val factory: PrinterFactory
) {

    private lateinit var abstractPrinter: AbstractPrinter


    fun getWifiZebraPrinters() = callbackFlow {
        NetworkDiscoverer.findPrinters(object : DiscoveryHandler {
            val discoveredPrinters: MutableList<DiscoveredPrinter> = mutableListOf()
            override fun foundPrinter(printer: DiscoveredPrinter) {
                println(printer)

                discoveredPrinters.add(printer)
            }

            override fun discoveryFinished() {
                for (printer in discoveredPrinters) {
                    println(printer)
                }
                trySend(discoveredPrinters)
            }

            override fun discoveryError(message: String) {
                println("An error occurred during discovery : $message")
            }
        })
        awaitClose()
    }


    fun getBluetoothZebraPrinters() = callbackFlow {
        BluetoothLeDiscoverer.findPrinters(context, object : DiscoveryHandler {
            val discoveredPrinters: MutableList<DiscoveredPrinter> = mutableListOf()
            override fun foundPrinter(printer: DiscoveredPrinter) {
                println(printer)

                discoveredPrinters.add(printer)
            }

            override fun discoveryFinished() {
                for (printer in discoveredPrinters) {
                    println(printer)
                }
                trySend(discoveredPrinters)
            }

            override fun discoveryError(message: String) {
                println("An error occurred during discovery : $message")
            }
        })
        awaitClose()
    }

    suspend fun connectToPrinterWithSuspend(printer: PrinterEntity) {

        abstractPrinter = factory.createPrinter(printer)

        abstractPrinter.createConnection().connect()
    }


    fun connectToPrinter(printer: PrinterEntity) = flow {

        abstractPrinter = factory.createPrinter(printer)

        emit("Connecting....")
        try {
            val isConnected = abstractPrinter.createConnection().connect()


            emit(if (isConnected) "Connected...." else "Disconnected")
        } catch (e: ConnectionException) {
            emit("Error....${e.message}")
        }

    }.flowOn(Dispatchers.IO)


    fun testPrinter() = CoroutineScope(dispatcher).launch {

        if (abstractPrinter.isConnected()) {
            abstractPrinter.testPrinter()
        }
    }

    fun isConnected(): Boolean = abstractPrinter.isConnected()


    fun sendFileToPrinter(filePath: Uri) =
        abstractPrinter.sendFile(context.contentResolver.openInputStream(filePath))


}