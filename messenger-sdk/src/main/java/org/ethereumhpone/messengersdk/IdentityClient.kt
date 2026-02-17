package org.ethereumhpone.messengersdk

import android.content.Context
import android.os.RemoteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.ethereumhpone.ipc.IIdentityMessageCallback
import org.ethereumhpone.ipc.IXmtpIdentityService
import org.json.JSONArray

class IdentityClient internal constructor(context: Context) {

    private val _newMessages = MutableSharedFlow<Int>(extraBufferCapacity = 64)

    /** Emits the count of new messages detected after each background sync. */
    val newMessages: SharedFlow<Int> = _newMessages.asSharedFlow()

    private val messageCallback = object : IIdentityMessageCallback.Stub() {
        override fun onNewMessages(messageCount: Int) {
            _newMessages.tryEmit(messageCount)
        }
    }

    internal val delegate = ServiceBindingDelegate<IXmtpIdentityService>(
        context = context,
        action = "org.ethereumhpone.messenger.action.BIND_IDENTITY",
        asInterface = IXmtpIdentityService.Stub::asInterface,
        onConnected = { service ->
            try {
                service.registerMessageCallback(messageCallback)
            } catch (_: RemoteException) { }
        }
    )

    val connectionState: StateFlow<ConnectionState> get() = delegate.connectionState

    fun bind() = delegate.bind()

    fun unbind() {
        try {
            delegate.service?.unregisterMessageCallback(messageCallback)
        } catch (_: RemoteException) { }
        delegate.unbind()
    }

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

    /**
     * Syncs all conversations from the XMTP network for this isolated identity.
     * Call this before [getConversations] or [getMessages] to ensure fresh data.
     */
    suspend fun syncConversations(): Unit =
        withContext(Dispatchers.IO) {
            try {
                requireService().syncConversations()
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    /**
     * Returns the list of conversations for this isolated identity.
     * Call [syncConversations] first to fetch fresh data from the network.
     */
    suspend fun getConversations(): List<IdentityConversation> =
        withContext(Dispatchers.IO) {
            try {
                val json = requireService().conversations ?: return@withContext emptyList()
                parseConversations(json)
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    /**
     * Returns messages for a given conversation.
     * @param conversationId The conversation ID to fetch messages for.
     * @param afterNs Only return messages sent after this timestamp in nanoseconds. Use 0 for all.
     * Call [syncConversations] first to fetch fresh data from the network.
     */
    suspend fun getMessages(conversationId: String, afterNs: Long = 0): List<IdentityMessage> =
        withContext(Dispatchers.IO) {
            try {
                val json = requireService().getMessages(conversationId, afterNs)
                    ?: return@withContext emptyList()
                parseMessages(json)
            } catch (e: RemoteException) {
                throw SdkException.RemoteCallException(e)
            }
        }

    private fun parseConversations(json: String): List<IdentityConversation> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            IdentityConversation(
                id = obj.getString("id"),
                peerAddress = obj.optString("peerAddress", ""),
                createdAtMs = obj.getLong("createdAtMs")
            )
        }
    }

    private fun parseMessages(json: String): List<IdentityMessage> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            IdentityMessage(
                id = obj.getString("id"),
                senderInboxId = obj.optString("senderInboxId", ""),
                body = obj.optString("body", ""),
                sentAtMs = obj.getLong("sentAtMs"),
                isMe = obj.optBoolean("isMe", false)
            )
        }
    }
}
