package com.alhussain.zebraprinter.module

import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.device.ProgressMonitor
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.util.internal.FileUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.InputStream

abstract class AbstractPrinter {

    protected lateinit var connection: Connection

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun isConnected(): Boolean{
        var ok = connection.isConnected

        try {
            connection.write(byteArrayOf(1))
        }catch (e: ConnectionException){
            ok = false
        }
        return ok
    }

    @Throws(ConnectionException::class)
    abstract suspend fun createConnection(): AbstractPrinter

    @Throws(ConnectionException::class)
    abstract suspend fun connect(): Boolean


    @Throws(ConnectionException::class)
    fun sendFile(filePath: InputStream?) = callbackFlow {
        if(!::connection.isInitialized){
            throw ConnectionException("printer has not connected yet")
        }

        if (filePath != null) {
            val fileSize: Int = filePath.available()

            val progressMonitor = ProgressMonitor { t1, t2 ->
                println("t1 $t1 --> t2 $t2")
                trySend(t1)
            }

            FileUtilities.sendFileContentsInChunks(
                connection,
                progressMonitor,
                filePath,
                fileSize,
            )

        }


        awaitClose()
    }.flowOn(Dispatchers.IO)

    @Throws(ConnectionException::class)
    fun testPrinter() {
        coroutineScope.launch {
            val zebraPrinter = ZebraPrinterFactory.getInstance(connection)
            zebraPrinter.printConfigurationLabel()
        }
    }


}