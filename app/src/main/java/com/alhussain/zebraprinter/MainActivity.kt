package com.alhussain.zebraprinter

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alhussain.zebraprinter.ui.theme.ZebraPrinterTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZebraPrinterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PageContent()
                }
            }
        }
    }
}

@Composable
fun PageContent(viewModel: PrinterViewModel = hiltViewModel()) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val wifiState = viewModel.uiWifiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->

            println("uri: ${uri.toString()}")

            if (uri != null) {
                viewModel.sendFileToPrinter(uri)
            }
        })


    var openDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Wifi Printer")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.fetchWifiPrinters()
                openDialog = true
            }) {
                Text(text = "Select Wifi Printer")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.value.ipWifiPrinter != null) {
                Text(text = (uiState.value.ipWifiPrinter as WifiPrinterEntity).ip)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(modifier = Modifier.weight(1f), onClick = {
                        viewModel.connectToPrinter(uiState.value.ipWifiPrinter!!)
                    }) {
                        Text(text = "Connect")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(modifier = Modifier.weight(1f), onClick = viewModel::testPrinter) {
                        Text(text = "Test")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Status: " + uiState.value.status1)


        }
        Divider(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)

        ) {

            Text(text = "Bluetooth Printer")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.fetchBluetoothPrinters()
                openDialog = true
            }) {
                Text(text = "Select Bluetooth Printer")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.value.bluetoothMac != null) {
                Text(text = (uiState.value.bluetoothMac as BluetoothPrinterEntity).mac)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(modifier = Modifier.weight(1f), onClick = {
                        viewModel.connectToPrinter(uiState.value.bluetoothMac!!)
                    }) {
                        Text(text = "Connect")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(modifier = Modifier.weight(1f), onClick = viewModel::testPrinter) {
                        Text(text = "Test")
                    }
                }

            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Status: " + uiState.value.status2)

            Button(modifier = Modifier, onClick = {
                launcher.launch(arrayOf("application/pdf"))
            }) {
                Text(text = "Pick File")
            }


        }
    }

    if (openDialog) {
        PrintersDialog(wifiState.value, onSelectPrinter = {
            viewModel.onSelectPrinter(it)
            openDialog = false
        }) {
            openDialog = false
        }
    }
}


@Composable
private fun PrintersDialog(
    state: PrinterState,
    onSelectPrinter: (PrinterEntity) -> Unit,
    onDialogDismiss: () -> Unit
) {

    Dialog(onDismissRequest = onDialogDismiss) {
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.7f),
            shape = RoundedCornerShape(8.dp),
        ) {
            if (state.isLoading) {
                LoadingView()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Available printers",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    state.printersEntity.forEach { printer ->
                        Row(modifier = Modifier
                            .clickable {
                                onSelectPrinter.invoke(printer)
                            }
                            .fillMaxWidth()
                            .border(1.dp, color = Color.Blue)
                            .padding(8.dp)
                        ) {
                            Text(text = if (printer is WifiPrinterEntity) printer.ip else if (printer is BluetoothPrinterEntity) printer.mac else "")
                        }
                    }
                }

            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ZebraPrinterTheme {
        PageContent()
    }
}