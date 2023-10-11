package com.alhussain.zebraprinter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrintersUiState(
    val ipWifiPrinter: PrinterEntity? = null,
    val bluetoothMac: PrinterEntity? = null,
    val status1: String = "Disconnected",
    val status2: String = "Disconnected",
    val userMessage: String? = null,
    val isLoading: Boolean = false,
)

data class PrinterState(
    val printersEntity: List<PrinterEntity> = emptyList(),
    val isLoading: Boolean = false,
)


abstract class PrinterEntity(
    val uniqueName: String
)

class WifiPrinterEntity(uniqueName: String, val ip: String, val port: String) :
    PrinterEntity(uniqueName)

class BluetoothPrinterEntity(uniqueName: String, val mac: String) : PrinterEntity(uniqueName)

@HiltViewModel
class PrinterViewModel @Inject constructor(private val printerRepository: PrinterRepository) :
    ViewModel() {


    private val _uiState: MutableStateFlow<PrintersUiState> = MutableStateFlow(
        PrintersUiState()
    )

    val uiState = _uiState.asStateFlow()


    private val _uiWifiState: MutableStateFlow<PrinterState> = MutableStateFlow(
        PrinterState(isLoading = true)
    )
    val uiWifiState = _uiWifiState.asStateFlow()


    fun fetchWifiPrinters() = viewModelScope.launch {
        printerRepository.getWifiZebraPrinters().onStart {
            _uiWifiState.emit(
                PrinterState(
                    isLoading = true,
                )
            )
        }.catch {
            _uiWifiState.emit(
                PrinterState(
                    isLoading = false,
                )
            )
        }.collectLatest {
            _uiWifiState.emit(
                PrinterState(
                    isLoading = false,
                    printersEntity = it.map { printer ->
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

    fun fetchBluetoothPrinters() = viewModelScope.launch {
        printerRepository.getBluetoothZebraPrinters().onStart {
            _uiWifiState.emit(
                PrinterState(
                    isLoading = true,
                )
            )
        }.catch {
            _uiWifiState.emit(
                PrinterState(
                    isLoading = false,
                )
            )
        }.collectLatest {
            _uiWifiState.emit(
                PrinterState(
                    isLoading = false,
                    printersEntity = it.map { printer ->
                        BluetoothPrinterEntity(
                            printer.discoveryDataMap["DEVICE_UNIQUE_ID"].orEmpty(),
                            mac = printer.address,
                        )
                    }
                )
            )
        }
    }

    fun onSelectPrinter(selectedPrinter: PrinterEntity) {

        _uiState.update {
            if (selectedPrinter is WifiPrinterEntity)
                it.copy(ipWifiPrinter = selectedPrinter)
            else {
                it.copy(bluetoothMac = selectedPrinter)
            }
        }
    }

    fun connectToPrinter(printer: PrinterEntity) = viewModelScope.launch {
        printerRepository.connectToPrinter(printer).collectLatest { status ->
            _uiState.update {
                if(printer is WifiPrinterEntity)
                it.copy(status1 = status)
                else{
                    it.copy(status2 = status)
                }
            }
        }
    }

    fun testPrinter() {
        printerRepository.testPrinter()

    }


}