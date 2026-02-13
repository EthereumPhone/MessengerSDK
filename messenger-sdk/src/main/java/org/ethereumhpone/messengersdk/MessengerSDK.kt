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

    companion object {
        @Volatile
        private var instance: MessengerSDK? = null

        fun getInstance(context: Context): MessengerSDK {
            if (!MessengerPermissions.isMessengerInstalled(context)) {
                throw SdkException.MessengerNotInstalledException()
            }
            return instance ?: synchronized(this) {
                instance ?: MessengerSDK(context).also { instance = it }
            }
        }

        @androidx.annotation.VisibleForTesting
        internal fun resetForTesting() {
            instance = null
        }
    }
}
