package com.liligo.reggie.common;


import lombok.Data;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "chat_messages")
public class MyChatMessages {
    @Id
    private ObjectId id; // 唯一标识，映射到MongoDB文档的_id字段

    private String memoryId; // 聊天记忆id

    private String content; // 存储当前聊天记录列表的json字符串
}
