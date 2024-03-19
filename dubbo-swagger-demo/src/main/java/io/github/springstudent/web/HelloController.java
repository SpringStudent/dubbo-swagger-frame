package io.github.springstudent.web;

import io.github.springstudent.mqtt.MqttClientService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
