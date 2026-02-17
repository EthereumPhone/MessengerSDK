package org.ethereumhpone.messengersdk

/**
 * Represents a message in an isolated identity conversation.
 */
data class IdentityMessage(
    /** The XMTP message ID. */
    val id: String,
    /** The sender's XMTP inbox ID. */
    val senderInboxId: String,
    /** The message body text. */
    val body: String,
    /** Message sent time in milliseconds since epoch. */
    val sentAtMs: Long,
    /** Whether this message was sent by the isolated identity (i.e. "me"). */
    val isMe: Boolean
)
