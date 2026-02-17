package org.ethereumhpone.messengersdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives explicit broadcasts from Messenger when new XMTP messages arrive
 * for this app's isolated identity.
 *
 * Delegates to the [MessengerSDK.NewMessageWakeupHandler] registered by the
 * consuming app (typically in `Application.onCreate()`). If no handler is set,
 * the broadcast is silently dropped.
 *
 * Registered in the SDK's AndroidManifest.xml so it is automatically merged
 * into every consuming app's manifest.
 */
class XmtpNewMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val count = intent.getIntExtra("message_count", 0)
        if (count <= 0) return

        MessengerSDK.wakeupHandler?.onWakeup(context.applicationContext, count)
    }
}
