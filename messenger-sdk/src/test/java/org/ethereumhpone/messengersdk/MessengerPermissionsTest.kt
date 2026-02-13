package org.ethereumhpone.messengersdk

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class MessengerPermissionsTest {

    private val mockContext: Context = mockk(relaxed = true)

    @Test
    fun `MESSENGER_PACKAGE is correct`() {
        assertEquals("org.ethereumhpone.messenger", MessengerPermissions.MESSENGER_PACKAGE)
    }

    @Test
    fun `SEND_MESSAGE_AS_USER constant is correct`() {
        assertEquals(
            "org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER",
            MessengerPermissions.SEND_MESSAGE_AS_USER
        )
    }

    @Test
    fun `GENERATE_XMTP_IDENTITY constant is correct`() {
        assertEquals(
            "org.ethereumhpone.messenger.permission.GENERATE_XMTP_IDENTITY",
            MessengerPermissions.GENERATE_XMTP_IDENTITY
        )
    }

    @Test
    fun `hasSendPermission returns true when granted`() {
        every {
            mockContext.checkSelfPermission(MessengerPermissions.SEND_MESSAGE_AS_USER)
        } returns PackageManager.PERMISSION_GRANTED

        assertTrue(MessengerPermissions.hasSendPermission(mockContext))
    }

    @Test
    fun `hasSendPermission returns false when denied`() {
        every {
            mockContext.checkSelfPermission(MessengerPermissions.SEND_MESSAGE_AS_USER)
        } returns PackageManager.PERMISSION_DENIED

        assertFalse(MessengerPermissions.hasSendPermission(mockContext))
    }

    @Test
    fun `hasIdentityPermission returns true when granted`() {
        every {
            mockContext.checkSelfPermission(MessengerPermissions.GENERATE_XMTP_IDENTITY)
        } returns PackageManager.PERMISSION_GRANTED

        assertTrue(MessengerPermissions.hasIdentityPermission(mockContext))
    }

    @Test
    fun `hasIdentityPermission returns false when denied`() {
        every {
            mockContext.checkSelfPermission(MessengerPermissions.GENERATE_XMTP_IDENTITY)
        } returns PackageManager.PERMISSION_DENIED

        assertFalse(MessengerPermissions.hasIdentityPermission(mockContext))
    }

    @Test
    fun `isMessengerInstalled returns true when package found`() {
        every {
            mockContext.packageManager.getPackageInfo(MessengerPermissions.MESSENGER_PACKAGE, 0)
        } returns PackageInfo()

        assertTrue(MessengerPermissions.isMessengerInstalled(mockContext))
    }

    @Test
    fun `isMessengerInstalled returns false when package not found`() {
        every {
            mockContext.packageManager.getPackageInfo(MessengerPermissions.MESSENGER_PACKAGE, 0)
        } throws PackageManager.NameNotFoundException()

        assertFalse(MessengerPermissions.isMessengerInstalled(mockContext))
    }
}
