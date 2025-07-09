package com.liligo.reggie.config;

import com.liligo.reggie.common.MyChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class MongoChatMemoryStoreConfig implements ChatMemoryStore {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 构建查询条件：根据 memoryId 查找聊天记录
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);

        // 执行查询，获取封装好的MyChatMessages对象
        MyChatMessages myChatMessages = mongoTemplate.findOne(query, MyChatMessages.class);

        // 处理空值情况，返回空列表
        if (myChatMessages == null) {
            return new LinkedList<>();
        }

        // 将 JSON 字符串反序列化为 ChatMessage 对象列表
        String contentJson = myChatMessages.getContent();
        return ChatMessageDeserializer.messagesFromJson(contentJson);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 构建更新条件
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);

        // 创建更新操作：将消息列表序列化为 JSON 后存入 messages 字段
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(list));

        // 执行更新操作（存在则更新，不存在则创建）
        mongoTemplate.upsert(query, update, MyChatMessages.class);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = Query.query(criteria);
        mongoTemplate.remove(query, MyChatMessages.class);
    }
}

