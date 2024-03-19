package io.github.springstudent.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author zhouning
 * @date 2023/12/26 14:47
 */
//@Component
public class MqttClientService implements InitializingBean, DisposableBean {

    private MqttClient mqttClient;

    @Override
    public void destroy() throws Exception {
        mqttClient.disconnect();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String topic = "TOPIC_TEST";
        String broker = "tcp://172.16.1.72:1883";
        String clientId = "clientId";
        MemoryPersistence persistence = new MemoryPersistence();
        mqttClient = new MqttClient(broker, clientId, persistence);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("###messageArrived" + topic + ":" + message);

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    System.out.println("###deliveryComplete" + token.getResponse().getPayload());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName("maodan");
        connOpts.setPassword("111111".toCharArray());
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
        mqttClient.subscribe(topic);
        mqttClient.subscribe("/exit");
    }

    public void publish(String topic,String data) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(MqttQoS.AT_LEAST_ONCE.value());
        mqttMessage.setRetained(false);
        mqttMessage.setPayload(data.getBytes(StandardCharsets.UTF_8));
        mqttClient.publish(topic,mqttMessage);
    }
}
