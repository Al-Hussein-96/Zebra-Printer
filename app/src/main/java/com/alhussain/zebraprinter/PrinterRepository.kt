package com.alhussain.zebraprinter

import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionBuilder
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.comm.internal.ZebraConnector
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLinkOs
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.NetworkDiscoverer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PrinterRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private lateinit var connection: Connection


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

    fun connectToPrinter(ipWifiPrinter: WifiPrinterEntity) = flow {
        try {
            connection = TcpConnection(ipWifiPrinter.ip, ipWifiPrinter.port.toInt())
            emit("Connecting....")
            connection.open()
            emit("Connected....")

            println("Connection status : ${connection.isConnected}")
        } catch (e: Exception) {
            emit("Error while connecting.... ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    fun testPrinter() = CoroutineScope(dispatcher).launch {
        if (connection.isConnected) {
            val zebraPrinter = ZebraPrinterFactory.getInstance(connection)
            zebraPrinter.printConfigurationLabel()
        }
    }
}