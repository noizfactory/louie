/*
 * MessageManager.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

import java.util.*;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythm.pb.data.Data;
import com.rhythm.pb.jms.JmsProtos.ContentPB;
import com.rhythm.pb.jms.JmsProtos.MessageBPB;

/**
 *
 * @author cjohnson
 */
public class MessageManager {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 61616;
    
    private final String host;
    private final int port;
    private boolean failover = true;
    
    private final Map<String, List<MessageProcessor>> messageProcessors 
            = Collections.synchronizedMap(new HashMap<String, List<MessageProcessor>>());
    
    private final List<ManagedListener> listeners = new ArrayList<ManagedListener>();
    
    private MessageManager(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public static MessageManager initializeMessageManager(String host) {
        return new MessageManager(host,DEFAULT_PORT);
    }
    
    public static MessageManager initializeMessageManager(String host,int port) {
        return new MessageManager(host,port);
    }
    
    public void enableFailover(boolean enable) {
        failover = enable;
    }
    
    private String getBrokerUrl() {
        if (failover) {
            return "failover:(tcp://" + host + ":"+port+")";
        } else {
            return "tcp://" + host + ":"+port;
        }
    }
    
    public void listenToTopic(String topicName) {
        listeners.add(new ManagedTopicListener(topicName));
    }
    
    public void listenToTopic(String topicName,Collection<MessageType> messageTypes) {
        listeners.add(new ManagedTopicListener(topicName,messageTypes));
    }
    
    public void listenToQueue(String queueName) {
        listeners.add(new ManagedQueueListener(queueName));
    }
    
    public void addMessageHandler(MessageHandler mh) {
        for(MessageProcessor processor : mh.getMessageProcessors() ) {
            addMessageProcessor(processor.getType(), processor);
        }
    }
    
    private void addMessageProcessor(String type, MessageProcessor messageProcessor) {
        List<MessageProcessor> messageProcessorList = messageProcessors.get(type);
        if (messageProcessorList == null) {
            messageProcessorList = new ArrayList<MessageProcessor>();
        }
        messageProcessorList.add(messageProcessor);
        messageProcessors.put(type, messageProcessorList);
    }
   
    private void processMessage(MessageAction action, String type, Data data) {
        List<MessageProcessor> messageProcessorList = messageProcessors.get(type);
        if (messageProcessorList != null) {
            for (MessageProcessor mp : messageProcessorList) {
                try {
                    mp.execute(action, data);
                } catch (Exception ex) {
                    LOGGER.error("Error processing message", ex);
                }
            }
        }
    }
    
    private void processBytesMessage(BytesMessage message) {
        try {
            byte[] by = new byte[(int) message.getBodyLength()];
            message.readBytes(by);

            MessageBPB messageBPB = MessageBPB.parseFrom(by);

            MessageInfo msgInfo = new MessageInfo(messageBPB);
            MessageContext.setMessage(msgInfo);
            
            while (msgInfo.hasMoreContent()) {
                ContentPB content = msgInfo.getNextContent();
                Data data = Data.newPBData(content.getContent().toByteArray());
                String type = content.getType();
                processMessage(msgInfo.getAction(), type, data);
            }
        } catch (Exception e) {
            LOGGER.error("Error processBytesMessage", e);
        } finally {
            MessageContext.clearMessage();
        }
    }
    
    public void shutdown() {
        for (ManagedListener listener : listeners) {
            listener.stop();
        }
        listeners.clear();
        messageProcessors.clear();
    }
    
    // Message Listeners
    
    private abstract class ManagedListener extends AbstractMessageListener {
        protected final String destName;

        public ManagedListener(String destName) {
            this.destName = destName;
            super.connect();
        }

        public ManagedListener(String destName, Collection<MessageType> messageTypes) {
            this.destName = destName;
            super.connect(messageTypes);
        }

        public ManagedListener(String destName, String messageSelector) {
            this.destName = destName;
            super.connect(messageSelector);
        }

        @Override
        public String getBrokerUrl() {
            return MessageManager.this.getBrokerUrl();
        }
        
        @Override
        public void onMessage(Message msg) {
            try {
                if (msg instanceof BytesMessage) {
                    processBytesMessage((BytesMessage) msg);
                } else {
                    LOGGER.error("Unsupported message type: {}", msg.getClass().getName());
                }
                msg.acknowledge();
            } catch (Exception e) {
            }
        }
    }

    private class ManagedQueueListener extends ManagedListener {
        public ManagedQueueListener(String destName) {
            super(destName);
        }

        public ManagedQueueListener(String destName, Collection<MessageType> messageTypes) {
            super(destName,messageTypes);
        }

        public ManagedQueueListener(String destName, String messageSelector) {
            super(destName,messageSelector);
        }
        
        @Override
        public Destination getDestination(Session session) throws Exception {
            return session.createQueue(destName);
        }
    }
    
    private class ManagedTopicListener extends ManagedListener {
        public ManagedTopicListener(String destName) {
            super(destName);
        }

        public ManagedTopicListener(String destName, Collection<MessageType> messageTypes) {
            super(destName,messageTypes);
        }

        public ManagedTopicListener(String destName, String messageSelector) {
            super(destName,messageSelector);
        }
        
        @Override
        public Destination getDestination(Session session) throws Exception {
            return session.createTopic(destName);
        }
    }
}
