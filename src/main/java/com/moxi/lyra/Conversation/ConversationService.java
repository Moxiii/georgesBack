package com.moxi.lyra.Conversation;

import com.moxi.lyra.Conversation.Message.MessageService;
import com.moxi.lyra.User.User;
import com.moxi.lyra.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {
@Autowired
private  ConversationRepository conversationRepository;
@Autowired
private UserService userService;
@Autowired
private MessageService messageService;


    public List<Conversation> findByUser(User user) {
        return (List<Conversation>) conversationRepository.findByParticipantsContaining(user);
    }
    }
