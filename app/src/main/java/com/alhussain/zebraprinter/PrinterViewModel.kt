package com.alhussain.zebraprinter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrintersUiState(
    val ipWifiPrinter: WifiPrinterEntity? = null,
    val bluetoothMac: BluetoothPrinterEntity? = null,
    val status: String = "Disconnected",
    val userMessage: String? = null,
    val isLoading: Boolean = false,
)

data class WifiPrinterState(
    val wifiPrinterList: List<WifiPrinterEntity> = emptyList(),
    val isLoading: Boolean = false,
)


abstract class PrinterEntity(
    val uniqueName: String
)

class WifiPrinterEntity(uniqueName: String, val ip: String, val port: String) :
    PrinterEntity(uniqueName)

class BluetoothPrinterEntity(uniqueName: String, val mac: String) : PrinterEntity(uniqueName)

class PrinterViewModel(private val printerRepository: PrinterRepository = PrinterRepository()) :
    ViewModel() {


    private val _uiState: MutableStateFlow<PrintersUiState> = MutableStateFlow(
        PrintersUiState()
    )

    val uiState = _uiState.asStateFlow()


    private val _uiWifiState: MutableStateFlow<WifiPrinterState> = MutableStateFlow(
        WifiPrinterState(isLoading = true)
    )
    val uiWifiState = _uiWifiState.asStateFlow()


    fun fetchPrinter() = viewModelScope.launch {
        printerRepository.getWifiZebraPrinters().catch {
            _uiWifiState.emit(
                WifiPrinterState(
                    isLoading = false,
                )
            )
        }.collectLatest {
            _uiWifiState.emit(
                WifiPrinterState(
                    isLoading = false,
                    wifiPrinterList = it.map { printer ->
                        WifiPrinterEntity(
                            printer.discoveryDataMap["DEVICE_UNIQUE_ID"].orEmpty(),
                            ip = printer.address,
                            port = printer.discoveryDataMap["PORT_NUMBER"].orEmpty()
                        )
                    }
                )
            )
        }
    }

    fun onSelectPrinter(ipPrinter: WifiPrinterEntity) {
        _uiState.update {
            it.copy(ipWifiPrinter = ipPrinter)
        }
    }

    fun connectToPrinter(ipWifiPrinter: WifiPrinterEntity) = viewModelScope.launch {
        printerRepository.connectToPrinter(ipWifiPrinter).collectLatest { status ->
            _uiState.update {
                it.copy(status = status)
            }
        }
    }

    fun testPrinter() {
            printerRepository.testPrinter()

    }


}