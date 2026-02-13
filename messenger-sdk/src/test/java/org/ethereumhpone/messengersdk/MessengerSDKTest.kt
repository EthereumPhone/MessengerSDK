package org.ethereumhpone.messengersdk

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessengerSDKTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockAppContext: Context = mockk(relaxed = true)
    private val mockPackageManager: PackageManager = mockk()

    @Before
    fun setUp() {
        MessengerSDK.resetForTesting()
        every { mockContext.applicationContext } returns mockAppContext
        every { mockAppContext.applicationContext } returns mockAppContext
        every { mockContext.packageManager } returns mockPackageManager
        every { mockAppContext.packageManager } returns mockPackageManager
    }

    @After
    fun tearDown() {
        MessengerSDK.resetForTesting()
    }

    @Test(expected = SdkException.MessengerNotInstalledException::class)
    fun `getInstance throws when Messenger not installed`() {
        every {
            mockContext.packageManager.getPackageInfo(MessengerPermissions.MESSENGER_PACKAGE, 0)
        } throws PackageManager.NameNotFoundException()

        MessengerSDK.getInstance(mockContext)
    }

    @Test
    fun `getInstance returns SDK when Messenger is installed`() {
        every {
            mockContext.packageManager.getPackageInfo(MessengerPermissions.MESSENGER_PACKAGE, 0)
        } returns PackageInfo()

        val sdk = MessengerSDK.getInstance(mockContext)

        assertNotNull(sdk)
        assertNotNull(sdk.messaging)
        assertNotNull(sdk.identity)
    }

    @Test
    fun `getInstance returns same instance (singleton)`() {
        every {
            mockContext.packageManager.getPackageInfo(MessengerPermissions.MESSENGER_PACKAGE, 0)
        } returns PackageInfo()

        val sdk1 = MessengerSDK.getInstance(mockContext)
        val sdk2 = MessengerSDK.getInstance(mockContext)

        assertSame(sdk1, sdk2)
    }
}
