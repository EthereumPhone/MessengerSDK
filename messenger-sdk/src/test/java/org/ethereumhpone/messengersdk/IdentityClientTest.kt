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

    // --- syncConversations ---

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `syncConversations throws when not connected`() = runTest {
        client.syncConversations()
    }

    @Test
    fun `syncConversations delegates to AIDL stub when connected`() = runTest {
        client.delegate.setServiceForTesting(mockService)

        client.syncConversations()

        verify { mockService.syncConversations() }
    }

    @Test(expected = SdkException.RemoteCallException::class)
    fun `syncConversations wraps RemoteException`() = runTest {
        every { mockService.syncConversations() } throws RemoteException("fail")
        client.delegate.setServiceForTesting(mockService)

        client.syncConversations()
    }

    // --- getConversations ---

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `getConversations throws when not connected`() = runTest {
        client.getConversations()
    }

    @Test
    fun `getConversations returns parsed list when connected`() = runTest {
        val json = """[{"id":"conv-1","peerAddress":"0xPeer","createdAtMs":1000}]"""
        every { mockService.conversations } returns json
        client.delegate.setServiceForTesting(mockService)

        val result = client.getConversations()

        assertEquals(1, result.size)
        assertEquals("conv-1", result[0].id)
        assertEquals("0xPeer", result[0].peerAddress)
        assertEquals(1000L, result[0].createdAtMs)
    }

    @Test
    fun `getConversations returns empty list when service returns null`() = runTest {
        every { mockService.conversations } returns null
        client.delegate.setServiceForTesting(mockService)

        val result = client.getConversations()

        assertTrue(result.isEmpty())
    }

    // --- getMessages ---

    @Test(expected = SdkException.ServiceNotConnectedException::class)
    fun `getMessages throws when not connected`() = runTest {
        client.getMessages("conv-1")
    }

    @Test
    fun `getMessages returns parsed list when connected`() = runTest {
        val json = """[{"id":"msg-1","senderInboxId":"inbox-a","body":"Hello","sentAtMs":2000,"isMe":false}]"""
        every { mockService.getMessages("conv-1", 0) } returns json
        client.delegate.setServiceForTesting(mockService)

        val result = client.getMessages("conv-1")

        assertEquals(1, result.size)
        assertEquals("msg-1", result[0].id)
        assertEquals("inbox-a", result[0].senderInboxId)
        assertEquals("Hello", result[0].body)
        assertEquals(2000L, result[0].sentAtMs)
        assertFalse(result[0].isMe)
    }

    @Test
    fun `getMessages returns empty list when service returns null`() = runTest {
        every { mockService.getMessages("conv-1", 0) } returns null
        client.delegate.setServiceForTesting(mockService)

        val result = client.getMessages("conv-1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMessages passes afterNs parameter`() = runTest {
        every { mockService.getMessages("conv-1", 5000000000) } returns "[]"
        client.delegate.setServiceForTesting(mockService)

        client.getMessages("conv-1", afterNs = 5000000000)

        verify { mockService.getMessages("conv-1", 5000000000) }
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
