package org.ethereumhpone.ipc;

import org.ethereumhpone.ipc.IIdentityMessageCallback;

interface IXmtpIdentityService {
    String createIdentity();
    boolean hasIdentity();
    String getIdentityAddress();
    String getInboxId();
    String sendMessage(String recipientAddress, String body);
    void syncConversations();
    String getConversations();
    String getMessages(String conversationId, long afterNs);
    void registerMessageCallback(IIdentityMessageCallback callback);
    void unregisterMessageCallback(IIdentityMessageCallback callback);
}
