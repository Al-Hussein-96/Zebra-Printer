package com.alhussain.zebraprinter

import com.alhussain.zebraprinter.BluetoothPrinterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface PrinterMonitor {

    val isConnected: MutableSharedFlow<Boolean>
}