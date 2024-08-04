@file:Suppress("DEPRECATION")

package com.example.qrcodescannerapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QRCodeViewModel(private val viewModelStoreOwner: ViewModelStoreOwner) : ViewModel() {
    private val _scanResult = MutableStateFlow("")
    val scanResult: StateFlow<String> = _scanResult

    fun updateScanResult(result: String) {
        _scanResult.value = result
    }
}

@Suppress("UNCHECKED_CAST")
class QRCodeViewModelFactory(private val viewModelStoreOwner: ViewModelStoreOwner) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QRCodeViewModel(viewModelStoreOwner) as T
    }
}

class MainActivity : ComponentActivity() {
    private val qrCodeViewModel: QRCodeViewModel by viewModels {
        QRCodeViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRCodeScannerApp(qrCodeViewModel)
        }
    }

    @Deprecated("hehe")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                qrCodeViewModel.updateScanResult(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

@Composable
fun QRCodeScannerApp(viewModel: QRCodeViewModel) {
    val scanResult by viewModel.scanResult.collectAsState()
    var qrText by remember { mutableStateOf(TextFieldValue("")) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("QR Code Scanner")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val integrator = IntentIntegrator(context as ComponentActivity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.initiateScan()
        }) {
            Text("Scan QR Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Scan Result: $scanResult")

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = qrText,
            onValueChange = { qrText = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val barcodeEncoder = BarcodeEncoder()
            qrCodeBitmap = barcodeEncoder.encodeBitmap(qrText.text, BarcodeFormat.QR_CODE, 400, 400)
        }) {
            Text("Generate QR Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        qrCodeBitmap?.let {
            AndroidView({ context ->
                ImageView(context).apply {
                    setImageBitmap(it)
                }
            }, modifier = Modifier.size(200.dp))
        }
    }
}
