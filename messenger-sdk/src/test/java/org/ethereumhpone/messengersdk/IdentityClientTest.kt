package org.ethereumhpone.messengersdk

import android.os.RemoteException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.ethereumhpone.ipc.IXmtpIdentityService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IdentityClientTest {

    private lateinit var client: IdentityClient
    private val mockService: IXmtpIdentityService = mockk(relaxed = true)

    @Before
    fun setUp() {
        client = IdentityClient(mockk(relaxed = true))
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `createIdentity throws when not connected`() = runTest {
        client.createIdentity()
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `hasIdentity throws when not connected`() = runTest {
        client.hasIdentity()
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `getIdentityAddress throws when not connected`() = runTest {
        client.getIdentityAddress()
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `getInboxId throws when not connected`() = runTest {
        client.getInboxId()
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `sendMessage throws when not connected`() = runTest {
        client.sendMessage("0xAddr", "Hello")
    }

    @Test
    fun `createIdentity delegates to AIDL stub when connected`() = runTest {
        every { mockService.createIdentity() } returns "0xNewIdentity"
        client.delegate.setServiceForTesting(mockService)

        val result = client.createIdentity()

        assertEquals("0xNewIdentity", result)
        verify { mockService.createIdentity() }
    }

    @Test
    fun `hasIdentity delegates to AIDL stub when connected`() = runTest {
        every { mockService.hasIdentity() } returns true
        client.delegate.setServiceForTesting(mockService)

        assertTrue(client.hasIdentity())
    }

    @Test
    fun `getIdentityAddress delegates to AIDL stub when connected`() = runTest {
        every { mockService.identityAddress } returns "0xIdentity"
        client.delegate.setServiceForTesting(mockService)

        assertEquals("0xIdentity", client.getIdentityAddress())
    }

    @Test
    fun `getInboxId delegates to AIDL stub when connected`() = runTest {
        every { mockService.inboxId } returns "inbox-xyz"
        client.delegate.setServiceForTesting(mockService)

        assertEquals("inbox-xyz", client.getInboxId())
    }

    @Test
    fun `sendMessage delegates to AIDL stub when connected`() = runTest {
        every { mockService.sendMessage("0xRecipient", "Body") } returns "msg-789"
        client.delegate.setServiceForTesting(mockService)

        val result = client.sendMessage("0xRecipient", "Body")

        assertEquals("msg-789", result)
        verify { mockService.sendMessage("0xRecipient", "Body") }
    }

    @Test(expected = SdkException.RemoteCallException::class)
    fun `createIdentity wraps RemoteException`() = runTest {
        every { mockService.createIdentity() } throws RemoteException("fail")
        client.delegate.setServiceForTesting(mockService)

        client.createIdentity()
    }

    @Test(expected = SdkException.RemoteCallException::class)
    fun `sendMessage wraps RemoteException`() = runTest {
        every { mockService.sendMessage(any(), any()) } throws RemoteException("fail")
        client.delegate.setServiceForTesting(mockService)

        client.sendMessage("0xAddr", "Hi")
    }

    @Test
    fun `connectionState is CONNECTED after setServiceForTesting`() {
        client.delegate.setServiceForTesting(mockService)
        assertEquals(ConnectionState.CONNECTED, client.connectionState.value)
    }

    @Test
    fun `connectionState is DISCONNECTED initially`() {
        assertEquals(ConnectionState.DISCONNECTED, client.connectionState.value)
    }
}
