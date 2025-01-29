package com.moxi.lyra.Conversation.Message;

import com.moxi.lyra.Conversation.Conversation;
import com.moxi.lyra.Conversation.ConversationService;
import com.moxi.lyra.User.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.moxi.lyra.Mongo.Message.MessageMongoRepository;
import com.moxi.lyra.Mongo.Message.MongoMessage;
import com.moxi.lyra.User.UserService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
public class MessageService {
    @Autowired
    private MessageMongoRepository messageMongoRepository;
    @Autowired
    private  MessageRepository messageRepository;
    @Autowired
    private UserService userService;
@Autowired
private ConversationService conversationService;
private final int MONGO_MESSAGE_LIMIT = 10;
private final int MESSAGE_RETENTION_TIME_LIMIT = 5;
public void saveMongoMessage(MongoMessage message){
    long messageCount = messageMongoRepository.count();
    if(messageCount >= MONGO_MESSAGE_LIMIT){
        transfertOldMessageToSql();
    }
    messageMongoRepository.save(message);
    }
@Transactional
public Message convertToMysqlMessage(MongoMessage mongoMessage) {
    Message message = new Message();
    message.setContent(mongoMessage.getContent());

    Set<User> users = new HashSet<>();
    User receiver = userService.findByUsername(mongoMessage.getReceiver());
    User sender = userService.findByUsername(mongoMessage.getSender());
    message.setSender(sender);
    users.add(receiver);
    users.add(sender);

    List<Object[]> existingConversations = conversationService.findByParticipants(users);
    Conversation conversation;

    if (existingConversations.isEmpty()) {
        conversation = new Conversation();
        conversation.setParticipants(users);
        conversationService.save(conversation);
    } else {
        Optional<Object[]> matchedConv = existingConversations
                .stream()
                .filter(row -> {
                    Conversation conv = (Conversation) row[0];
                    Long participantsCount = (Long) row[1];
                    return participantsCount == users.size() && conv.getParticipants().containsAll(users);
                })
                .findFirst();

        conversation = matchedConv.map(row -> {
            Conversation conv = (Conversation) row[0];

            Hibernate.initialize(conv.getParticipants());

            return conv;
        }).orElseGet(() -> {
            Conversation newConv = new Conversation();
            newConv.setParticipants(users);
            return newConv;
        });

        if (!conversation.getMessages().contains(message)) {
            conversation.getMessages().add(message);
        }
        conversationService.save(conversation);
    }

    log.debug("Saving message to MySQL: " + message);
    message.setConversation(conversation);
    messageRepository.save(message);
    return message;
}


    @Scheduled(fixedRate = 300000)
    public void scheduledMongoToMysqlTransfert(){
    transfertOldMessageToSql();
    }
public void transfertOldMessageToSql(){
    LocalDateTime retentionTreshold = LocalDateTime.now().minusMinutes(MESSAGE_RETENTION_TIME_LIMIT);
    List<MongoMessage> oldMessages = messageMongoRepository.findByTimestampBefore(retentionTreshold);
    if(!oldMessages.isEmpty()){
        List<Message> messagesToSave = oldMessages.stream()
                .map(this::convertToMysqlMessage).toList();
        log.debug("Saving messages to MySQL...");
        messageRepository.saveAll(messagesToSave);
        messageMongoRepository.deleteAll(oldMessages);
    }

}
public List<MongoMessage> getRecentMessages() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(MESSAGE_RETENTION_TIME_LIMIT);
    return messageMongoRepository.findByTimestampAfter(threshold);
}
public List<Message> getOldMessages() {
    return messageRepository.findAllByOrderByTimestampAsc();
}
}
