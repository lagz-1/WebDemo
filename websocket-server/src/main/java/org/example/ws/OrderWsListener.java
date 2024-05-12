package org.example.ws;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.ws.WebSocketUtils.*;


@Slf4j
@Component
@RestController
@ServerEndpoint("/ws/{username}")
public class OrderWsListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderWsListener.class);
    public static ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> BUTTON_STATES = new ConcurrentHashMap<>();


    @OnOpen
    public void openSession(@PathParam("username") String username, Session session) {
        ONLINE_USER_SESSIONS.put(username, session);
        String message = "欢迎用户[" + username + "] 来到聊天室！";
        logger.info("用户登录："+message);
        sendMessageAll(message);
    }

    @OnMessage
    public void onMessage(@PathParam("username") String username,String message) {
//        logger.info("发送消息："+message);
//        sendMessageAll("用户[" + username + "] : " + message);


        String[] parts = message.split(":");
        String buttonId="",action="";
            if (parts.length == 2) {
                buttonId = parts[0];
                action = parts[1];
                // 根据buttonId和action进行处理
                // ...
            }

        if ("ButtonClicked".equals(action)) {
            boolean isSelected = BUTTON_STATES.getOrDefault(buttonId, false);
            BUTTON_STATES.put(buttonId, !isSelected);
            broadcastButtonState(buttonId, !isSelected);
        } else if ("ButtonCreated".equals(action)) {
            BUTTON_STATES.putIfAbsent(buttonId, false);
        }
    }

    @OnClose
    public void onClose(@PathParam("username") String username, Session session) {
        //当前的Session 移除
        ONLINE_USER_SESSIONS.remove(username);
        //并且通知其他人当前用户已经离开聊天室了
        sendMessageAll("用户[" + username + "] 已经离开聊天室了！");
        try {
            session.close();
        } catch (IOException e) {
            logger.error("onClose error",e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            session.close();
        } catch (IOException e) {
            logger.error("onError excepiton",e);
        }
        logger.info("Throwable msg "+throwable.getMessage());
    }




}
