package org.ethereumhpone.messengersdk

import android.os.RemoteException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.ethereumhpone.ipc.IMessagingService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessagingClientTest {

    private lateinit var client: MessagingClient
    private val mockService: IMessagingService = mockk(relaxed = true)

    @Before
    fun setUp() {
        client = MessagingClient(mockk(relaxed = true))
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `sendMessage throws when not connected`() = runTest {
        client.sendMessage("0xAddress", "Hello")
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `sendGroupMessage throws when not connected`() = runTest {
        client.sendGroupMessage("conv-id", "Hello")
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `isClientReady throws when not connected`() = runTest {
        client.isClientReady()
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `getUserAddress throws when not connected`() = runTest {
        client.getUserAddress()
    }

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `getInboxId throws when not connected`() = runTest {
        client.getInboxId()
    }

    @Test
    fun `sendMessage delegates to AIDL stub when connected`() = runTest {
        every { mockService.sendMessage("0xAddr", "Hi") } returns "msg-123"
        client.delegate.setServiceForTesting(mockService)

        val result = client.sendMessage("0xAddr", "Hi")

        assertEquals("msg-123", result)
        verify { mockService.sendMessage("0xAddr", "Hi") }
    }

    @Test
    fun `sendGroupMessage delegates to AIDL stub when connected`() = runTest {
        every { mockService.sendGroupMessage("conv-1", "Hi") } returns "msg-456"
        client.delegate.setServiceForTesting(mockService)

        val result = client.sendGroupMessage("conv-1", "Hi")

        assertEquals("msg-456", result)
        verify { mockService.sendGroupMessage("conv-1", "Hi") }
    }

    @Test
    fun `isClientReady delegates to AIDL stub when connected`() = runTest {
        every { mockService.isClientReady() } returns true
        client.delegate.setServiceForTesting(mockService)

        assertTrue(client.isClientReady())
    }

    @Test
    fun `getUserAddress delegates to AIDL stub when connected`() = runTest {
        every { mockService.userAddress } returns "0xUser"
        client.delegate.setServiceForTesting(mockService)

        assertEquals("0xUser", client.getUserAddress())
    }

    @Test
    fun `getInboxId delegates to AIDL stub when connected`() = runTest {
        every { mockService.inboxId } returns "inbox-abc"
        client.delegate.setServiceForTesting(mockService)

        assertEquals("inbox-abc", client.getInboxId())
    }

    @Test(expected = SdkException.RemoteCallException::class)
    fun `sendMessage wraps RemoteException`() = runTest {
        every { mockService.sendMessage(any(), any()) } throws RemoteException("fail")
        client.delegate.setServiceForTesting(mockService)

        client.sendMessage("0xAddr", "Hi")
    }

    @Test(expected = SdkException.RemoteCallException::class)
    fun `isClientReady wraps RemoteException`() = runTest {
        every { mockService.isClientReady() } throws RemoteException("fail")
        client.delegate.setServiceForTesting(mockService)

        client.isClientReady()
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
