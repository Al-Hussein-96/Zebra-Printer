package com.alhussain.zebraprinter.module

import android.content.Context
import com.alhussain.zebraprinter.BluetoothPrinterEntity
import com.alhussain.zebraprinter.Printer
import com.alhussain.zebraprinter.PrinterEntity
import com.alhussain.zebraprinter.WifiPrinterEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrinterFactory @Inject constructor(@ApplicationContext private val context: Context) {
    fun createPrinter(printerEntity: PrinterEntity): AbstractPrinter {

        return when (printerEntity) {
            is WifiPrinterEntity -> {
                TcpPrinter(printerEntity)
            }

            is BluetoothPrinterEntity -> {
                BluetoothPrinter(printerEntity, context)
            }

            else -> {
                throw Exception("Error not found")
            }

        }


    }

}