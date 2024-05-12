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
import java.util.concurrent.ConcurrentHashMap;

import static org.example.ws.WebSocketUtils.ONLINE_USER_SESSIONS;
import static org.example.ws.WebSocketUtils.sendMessageAll;


@Slf4j
@Component
@RestController
@ServerEndpoint("/ws/{username}")
public class OrderWsListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderWsListener.class);
    public static ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
//    @OnOpen
//    public void onOpen(@PathParam("token") String token,Session session){
//        System.out.println("建立会话");
//        clients.put(token,session);
//    }

//    @OnMessage
//    public void onMessage(String message, Session session) throws IOException {
//        log.info("服务端收到客户端：{} ；消息：{}", session.getId(), message);
//        session.getBasicRemote().sendText(message);// 回复消息
//    }
//
//    @OnError
//    public void onError(Throwable e){
//        e.printStackTrace();
//    }
//    @OnClose
//    public void onClose(@PathParam("token") String token){
//        System.out.println("会话关闭");
//        clients.remove(token);
//    }


    @OnOpen
    public void openSession(@PathParam("username") String username, Session session) {
        ONLINE_USER_SESSIONS.put(username, session);
        String message = "欢迎用户[" + username + "] 来到聊天室！";
        logger.info("用户登录："+message);
        sendMessageAll(message);
    }

    @OnMessage
    public void onMessage(@PathParam("username") String username, String message) {
        logger.info("发送消息："+message);
        sendMessageAll("用户[" + username + "] : " + message);
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
