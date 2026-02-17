package org.ethereumhpone.messengersdk

import android.content.Context

class MessengerSDK private constructor(context: Context) {

    private val appContext: Context = context.applicationContext

    val messaging: MessagingClient = MessagingClient(appContext)
    val identity: IdentityClient = IdentityClient(appContext)

    fun unbindAll() {
        messaging.unbind()
        identity.unbind()
    }

    /**
     * Callback invoked when a new-XMTP-message broadcast is received.
     * This fires even when the app was not running — Android creates the
     * Application first, so the handler set in `Application.onCreate()`
     * is available by the time `onReceive` runs.
     */
    fun interface NewMessageWakeupHandler {
        fun onWakeup(context: Context, messageCount: Int)
    }

    companion object {
        @Volatile
        private var instance: MessengerSDK? = null

        @Volatile
        internal var wakeupHandler: NewMessageWakeupHandler? = null
            private set

        fun getInstance(context: Context): MessengerSDK {
            if (!MessengerPermissions.isMessengerInstalled(context)) {
                throw SdkException.MessengerNotInstalledException()
            }
            return instance ?: synchronized(this) {
                instance ?: MessengerSDK(context).also { instance = it }
            }
        }

        /**
         * Registers a handler that is called when Messenger detects new XMTP
         * messages for this app's isolated identity while the app is not bound
         * to the identity service.
         *
         * Call this from [android.app.Application.onCreate] so the handler is
         * available before the broadcast receiver fires.
         */
        fun setNewMessageWakeupHandler(handler: NewMessageWakeupHandler?) {
            wakeupHandler = handler
        }

        @androidx.annotation.VisibleForTesting
        internal fun resetForTesting() {
            instance = null
            wakeupHandler = null
        }
    }
}
