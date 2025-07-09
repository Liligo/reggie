package com.liligo.reggie.controller;

import com.liligo.reggie.assistants.ReggieAssistant;
import com.liligo.reggie.common.MyChatMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/reggieAssistant")
public class ReggieAssistantController {
    @Autowired
    private ReggieAssistant reggieAssistant;

    /**
     * 处理用户输入并返回助手响应
     * @return 助手响应
     */
    @PostMapping(value = "/chat", produces = "text/stream;charset=UTF-8")
    public Flux<String> chat(@RequestBody MyChatMessages myChatMessages) {
        return reggieAssistant.chat(myChatMessages.getMemoryId(), myChatMessages.getContent());
    }
}
