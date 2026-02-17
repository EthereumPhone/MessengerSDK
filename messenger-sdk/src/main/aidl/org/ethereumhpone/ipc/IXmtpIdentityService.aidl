package org.ethereumhpone.ipc;

interface IXmtpIdentityService {
    String createIdentity();
    boolean hasIdentity();
    String getIdentityAddress();
    String getInboxId();
    String sendMessage(String recipientAddress, String body);
    void syncConversations();
    String getConversations();
    String getMessages(String conversationId, long afterNs);
}
