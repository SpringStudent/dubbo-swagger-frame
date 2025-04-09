package io.github.springstudent.web;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author 周宁
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String index() {
        return "hello world";
    }

    //    @Resource
//    private MqttClientService mqttClientService;
//
//    @GetMapping("/publish")
//    public void publish(@RequestParam String data) throws MqttException {
//        mqttClientService.publish("amp/eletricDevice/reply",data);
//    }
    @Autowired
    private DeepSeekClient deepSeekClient;

    // sse 流式返回
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatCompletionResponse> chat(String prompt) {
        return deepSeekClient.chatFluxCompletion(prompt);
    }

}
