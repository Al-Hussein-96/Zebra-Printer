package com.alhussain.zebraprinter

import com.zebra.sdk.printer.ZebraPrinter

interface Connection {
    fun isConnected(): Boolean
    abstract fun getZebraPrinter(): ZebraPrinter


}