package com.alhussain.zebraprinter

import com.alhussain.zebraprinter.PrinterEntity
import com.zebra.sdk.comm.Connection

abstract class ConnectionFactory {
    abstract fun createConnection(printerEntity: PrinterEntity) : Connection
}