# MessengerSDK

Android library for communicating with the XMTP Messenger app via AIDL IPC. Wraps service binding, threading, and permissions into a clean coroutine-based Kotlin API.

## Installation

**Step 1.** Add the JitPack repository to your project's `settings.gradle.kts` (inside the `dependencyResolutionManagement` block):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

> If your project uses a root `build.gradle.kts` with `allprojects { repositories { ... } }` instead, add the maven line there.

**Step 2.** Add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.EthereumPhone:MessengerSDK:0.1.0")
}
```

## Quick Start

```kotlin
// Get the SDK instance (throws if Messenger is not installed)
val sdk = MessengerSDK.getInstance(context)

// Bind to the messaging service
sdk.messaging.bind()
sdk.messaging.awaitConnected()

// Send a message as the logged-in user
val messageId = sdk.messaging.sendMessage("0xRecipientAddress", "Hello!")

// Clean up
sdk.unbindAll()
```

## Permissions

The SDK requires permissions granted by the XMTP Messenger app:

| Permission | Purpose |
|---|---|
| `SEND_MESSAGE_AS_USER` | Send messages as the Messenger's logged-in user |
| `GENERATE_XMTP_IDENTITY` | Create an isolated XMTP identity and send from it |

Check and request permissions:

```kotlin
if (!MessengerPermissions.hasSendPermission(context)) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(MessengerPermissions.SEND_MESSAGE_AS_USER),
        REQUEST_CODE
    )
}
```

## API

### MessengerSDK

| Method | Description |
|---|---|
| `getInstance(context)` | Get singleton instance (throws if Messenger not installed) |
| `messaging` | `MessagingClient` for sending as the logged-in user |
| `identity` | `IdentityClient` for isolated identity operations |
| `unbindAll()` | Unbind all services |

### MessagingClient

| Method | Description |
|---|---|
| `bind()` / `unbind()` | Bind/unbind the messaging service |
| `awaitConnected()` | Suspend until connected |
| `connectionState` | `StateFlow<ConnectionState>` |
| `sendMessage(address, body)` | Send a DM, returns message ID |
| `sendGroupMessage(conversationId, body)` | Send to a conversation, returns message ID |
| `isClientReady()` | Check if XMTP client is ready |
| `getUserAddress()` | Get the logged-in user's address |
| `getInboxId()` | Get the logged-in user's inbox ID |

### IdentityClient

| Method | Description |
|---|---|
| `bind()` / `unbind()` | Bind/unbind the identity service |
| `awaitConnected()` | Suspend until connected |
| `connectionState` | `StateFlow<ConnectionState>` |
| `createIdentity()` | Create or retrieve an isolated identity |
| `hasIdentity()` | Check if an identity exists |
| `getIdentityAddress()` | Get the isolated identity's address |
| `getInboxId()` | Get the isolated identity's inbox ID |
| `sendMessage(address, body)` | Send from the isolated identity |

### Error Handling

All SDK exceptions extend `SdkException`:

- `MessengerNotInstalledException` - Messenger app not found
- `PermissionNotGrantedException` - Required permission not granted
- `ServiceNotConnectedException` - Service not bound (call `bind()` first)
- `RemoteCallException` - IPC call failed

## Requirements

- Android API 28+
- XMTP Messenger app installed on the device
