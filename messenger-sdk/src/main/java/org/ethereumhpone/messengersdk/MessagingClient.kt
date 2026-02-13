package org.ethereumhpone.messengersdk

import android.content.Context
import android.os.RemoteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.ethereumhpone.ipc.IMessagingService

class MessagingClient internal constructor(context: Context) {

    internal val delegate = ServiceBindingDelegate<IMessagingService>(
        context = context,
        action = "org.ethereumhpone.messenger.action.BIND_MESSAGING",
        asInterface = IMessagingService.Stub::asInterface
    )

    val connectionState: StateFlow<ConnectionState> get() = delegate.connectionState

    fun bind() = delegate.bind()

    fun unbind() = delegate.unbind()

    suspend fun awaitConnected() = delegate.awaitConnected()

    private fun requireService(): IMessagingService =
        delegate.service ?: throw SdkException.ServiceNotConnectedException()

    suspend fun sendMessage(recipientAddress: String, body: String): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().sendMessage(recipientAddress, body)
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    suspend fun sendGroupMessage(conversationId: String, body: String): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().sendGroupMessage(conversationId, body)
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    suspend fun isClientReady(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                requireService().isClientReady()
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    suspend fun getUserAddress(): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().userAddress
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    suspend fun getInboxId(): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().inboxId
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }
}
