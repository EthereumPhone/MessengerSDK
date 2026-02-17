package org.ethereumhpone.messengersdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IInterface
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

internal class ServiceBindingDelegate<T : IInterface>(
    private val context: Context,
    private val action: String,
    private val asInterface: (IBinder) -> T,
    private val onConnected: ((T) -> Unit)? = null
) {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    @Volatile
    var service: T? = null
        private set

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val svc = asInterface(binder)
            service = svc
            _connectionState.value = ConnectionState.CONNECTED
            onConnected?.invoke(svc)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    fun bind() {
        if (_connectionState.value != ConnectionState.DISCONNECTED) return
        _connectionState.value = ConnectionState.CONNECTING
        val intent = Intent(action).apply {
            setPackage(MessengerPermissions.MESSENGER_PACKAGE)
        }
        val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    fun unbind() {
        if (_connectionState.value == ConnectionState.DISCONNECTED) return
        try {
            context.unbindService(serviceConnection)
        } catch (_: IllegalArgumentException) {
            // Service was not bound
        }
        service = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    suspend fun awaitConnected() {
        connectionState.first { it == ConnectionState.CONNECTED }
    }

    @VisibleForTesting
    internal fun setServiceForTesting(testService: T?) {
        service = testService
        _connectionState.value = if (testService != null) {
            ConnectionState.CONNECTED
        } else {
            ConnectionState.DISCONNECTED
        }
    }
}
