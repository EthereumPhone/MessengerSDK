package org.ethereumhpone.messengersdk

import android.content.Context
import android.os.RemoteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.ethereumhpone.ipc.IXmtpIdentityService

class IdentityClient internal constructor(context: Context) {

    internal val delegate = ServiceBindingDelegate<IXmtpIdentityService>(
        context = context,
        action = "org.ethereumhpone.messenger.action.BIND_IDENTITY",
        asInterface = IXmtpIdentityService.Stub::asInterface
    )

    val connectionState: StateFlow<ConnectionState> get() = delegate.connectionState

    fun bind() = delegate.bind()

    fun unbind() = delegate.unbind()

    suspend fun awaitConnected() = delegate.awaitConnected()

    private fun requireService(): IXmtpIdentityService =
        delegate.service ?: throw SdkException.ServiceNotConnectedException()

    suspend fun createIdentity(): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().createIdentity()
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    suspend fun hasIdentity(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                requireService().hasIdentity()
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    suspend fun getIdentityAddress(): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().identityAddress
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

    suspend fun sendMessage(recipientAddress: String, body: String): String? =
        withContext(Dispatchers.IO) {
            try {
                requireService().sendMessage(recipientAddress, body)
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }
}
