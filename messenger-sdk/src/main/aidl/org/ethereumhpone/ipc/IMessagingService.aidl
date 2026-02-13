package org.ethereumhpone.ipc;

interface IMessagingService {
    boolean isClientReady();
    String getUserAddress();
    String getInboxId();
    String sendMessage(String recipientAddress, String body);
    String sendGroupMessage(String conversationId, String body);
}
