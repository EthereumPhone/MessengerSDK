package org.ethereumhpone.messengersdk

import android.content.Context
import android.content.pm.PackageManager

object MessengerPermissions {

    const val MESSENGER_PACKAGE = "org.ethereumhpone.messenger"

    const val SEND_MESSAGE_AS_USER =
        "org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER"

    fun hasSendPermission(context: Context): Boolean =
        context.checkSelfPermission(SEND_MESSAGE_AS_USER) == PackageManager.PERMISSION_GRANTED

    fun isMessengerInstalled(context: Context): Boolean =
        try {
            context.packageManager.getPackageInfo(MESSENGER_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
}
