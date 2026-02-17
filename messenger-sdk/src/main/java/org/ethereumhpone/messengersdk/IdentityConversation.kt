package org.ethereumhpone.messengersdk

/**
 * Represents a conversation belonging to an isolated identity.
 */
data class IdentityConversation(
    /** The XMTP conversation ID. */
    val id: String,
    /** The peer's Ethereum address (the other party in the DM). */
    val peerAddress: String,
    /** Conversation creation time in milliseconds since epoch. */
    val createdAtMs: Long
)
