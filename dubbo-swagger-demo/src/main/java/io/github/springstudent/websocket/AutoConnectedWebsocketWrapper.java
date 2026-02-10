package io.github.springstudent.websocket;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zhouning
 * @date 2022/05/19 9:39
 */
//@Component
public class AutoConnectedWebsocketWrapper implements InitializingBean {

    private BlockWebsocketConnectionManager webSocketConnectionManager;

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private boolean reconnectFlag = true;

    private final Object lock = new Object();

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            Long start = System.currentTimeMillis();
            while (true) {
                synchronized (lock) {
                    try {
                        if (reconnectFlag) {
                            System.out.println("####start connect websocket");
                            String result = HttpRequest.post("http://172.16.1.72:9021/bvcsp/v1/auth/login")
                                    .body("{\n" +
                                            "\"username\":\"test\",\n" +
                                            "\"password\":\"test\"\n" +
                                            "}").timeout(5000).execute().body();
                            webSocketConnectionManager = new BlockWebsocketConnectionManager(
                                    new StandardWebSocketClient(),
                                    new WebSocketHandlerImpl(new ArrayList<>()),
                                    "ws://172.16.1.72:9021/bvcsp/v1/ws/event/alarm?token=" + JSONObject.parseObject(result).getJSONObject("data").get("token"));
                            //等待连接成功
                            webSocketConnectionManager.start();
                            if (webSocketConnectionManager.isConnected()) {
                                reconnectFlag = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                        //如果超过30分钟token会过期，也要进行重连
                        if (System.currentTimeMillis() - start >= 30 * 60 * 1000) {
                            //超过30分钟断开重连,解决由于token过期不接收数据BUG
                            if (webSocketConnectionManager != null) {
                                webSocketConnectionManager.closeConnection();
                            }
                            start = System.currentTimeMillis();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    class WebSocketHandlerImpl implements WebSocketHandler {

        private ScheduledFuture<?> scheduledFuture;

        private List<String> puids;

        public WebSocketHandlerImpl(List<String> puids) {
            this.puids = puids;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            session.sendMessage(new TextMessage("{\"badd\":true,\"puid\":[\"PU_264F708E\"]}"));
            System.out.println("####发送订阅参数");
            scheduledFuture = scheduledExecutor.scheduleWithFixedDelay(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + "###发送心跳包");
                    session.sendMessage(new TextMessage(""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 20000, 20000, TimeUnit.MILLISECONDS);
            System.out.println("### connect ok");
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            //TODO 接收gps数据并处理
            System.out.println("####" + message.getPayload().toString());
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            System.out.println("### connection closed");
            scheduledFuture.cancel(true);
            synchronized (lock) {
                reconnectFlag = true;
            }
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }


}
