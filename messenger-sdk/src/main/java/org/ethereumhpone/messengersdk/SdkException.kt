package org.ethereumhpone.messengersdk

sealed class SdkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class MessengerNotInstalledException :
        SdkException("XMTP Messenger app is not installed on this device")

    class PermissionNotGrantedException(permission: String) :
        SdkException("Permission not granted: $permission")

    class ServiceNotConnectedException :
        SdkException("Service is not connected. Call bind() first and wait for CONNECTED state.")

    class RemoteCallException(cause: Throwable) :
        SdkException("Remote service call failed: ${cause.message}", cause)
}
