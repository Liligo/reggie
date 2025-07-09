package com.liligo.reggie.assistants;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;


@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
//        chatModel = "qwenChatModel",
        streamingChatModel = "qwenStreamingChatModel",
        chatMemoryProvider = "reggieChatMemoryProvider",
        contentRetriever = "reggieContentRetriever"
)
public interface ReggieAssistant {
    @SystemMessage(fromResource = "reggie_sys_prompt_template.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
