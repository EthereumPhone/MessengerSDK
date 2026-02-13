package org.ethereumhpone.messengersdk.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.ethereumhpone.messengersdk.MessengerPermissions
import org.ethereumhpone.messengersdk.MessengerSDK
import org.ethereumhpone.messengersdk.SdkException

class MainActivity : ComponentActivity() {

    private var sdk: MessengerSDK? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            sdk = MessengerSDK.getInstance(this)
        } catch (e: SdkException.MessengerNotInstalledException) {
            Toast.makeText(this, "XMTP Messenger not installed", Toast.LENGTH_LONG).show()
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen(
                        onRequestPermissions = { requestPermissions() },
                        onBindMessaging = { sdk?.messaging?.bind() },
                        onSendMessage = { address, body -> sendMessage(address, body) }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        if (!MessengerPermissions.hasSendPermission(this)) {
            requestPermissions(arrayOf(MessengerPermissions.SEND_MESSAGE_AS_USER), 100)
        }
    }

    private fun sendMessage(address: String, body: String) {
        lifecycleScope.launch {
            try {
                val messageId = sdk?.messaging?.sendMessage(address, body)
                Toast.makeText(this@MainActivity, "Sent: $messageId", Toast.LENGTH_SHORT).show()
            } catch (e: SdkException) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sdk?.unbindAll()
    }
}

@Composable
fun SampleScreen(
    onRequestPermissions: () -> Unit,
    onBindMessaging: () -> Unit,
    onSendMessage: (String, String) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MessengerSDK Sample", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = onRequestPermissions) {
            Text("Request Permissions")
        }

        Button(onClick = onBindMessaging) {
            Text("Bind Messaging Service")
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Recipient Address") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSendMessage(address, message) },
            enabled = address.isNotBlank() && message.isNotBlank()
        ) {
            Text("Send Message")
        }
    }
}
