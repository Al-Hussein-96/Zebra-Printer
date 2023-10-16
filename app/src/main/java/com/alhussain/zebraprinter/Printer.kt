package com.alhussain.zebraprinter

import android.content.Context
import com.alhussain.zebraprinter.Connection
import com.alhussain.zebraprinter.PrinterMonitor
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.ZebraPrinter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class Printer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val connection: Connection
) :
    PrinterMonitor {

    private lateinit var zebraPrinter: ZebraPrinter

    private val coroutineScope = CoroutineScope(dispatcher)

    override val isConnected: MutableSharedFlow<Boolean> = MutableSharedFlow()

    init {
        coroutineScope.launch {
            isConnected.tryEmit(connection.isConnected())
        }
    }


    fun testPrinter() = coroutineScope.launch {
        try {
            val zebraPrinter = connection.getZebraPrinter()

            zebraPrinter.printConfigurationLabel()

            isConnected.tryEmit(true)
        } catch (e: ConnectionException) {
            isConnected.tryEmit(false)
        }
    }


    fun ZebraPrinter.checkCurrentlyConnected() {
        if (this.connection.isConnected) {
            isConnected.tryEmit(true)
        } else {
            isConnected.tryEmit(false)
        }
    }






}