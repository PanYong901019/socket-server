package win.panyong.service.impl.Enum;


import com.alibaba.fastjson.JSONObject;
import com.easyond.utils.ObjectUtil;
import com.easyond.utils.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import org.springframework.stereotype.Component;
import win.panyong.model.NettyMessage;
import win.panyong.utils.AppCache;
import win.panyong.utils.AppException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pan on 2018/11/1 上午11:04
 */
@Component
public enum NettyReceiveMsgHandle {
    PING_PONG() {
        @Override
        public void doMsgHandle(AppCache appCache, ChannelHandlerContext ctx, NettyMessage nettyMessage) {
            Map<String, Object> parameter = nettyMessage.getParameter();
            String request = parameter.get("result").toString();
            if ("ping".equals(request)) {
                NettyMessage sendMessage = new NettyMessage(NettyReceiveMsgHandle.PING_PONG.name());
                sendMessage.setCode("1");
                sendMessage.setInfo("success");
                sendMessage.setResult("pong");
                doSendMsg(ctx, sendMessage);
            }
        }
    },
    ANSWER() {
        @Override
        public void doMsgHandle(AppCache appCache, ChannelHandlerContext ctx, NettyMessage nettyMessage) {
            System.out.println("client say:" + ObjectUtil.objectToJsonString(nettyMessage));
        }
    },
    NOMAL() {
        @Override
        public void doMsgHandle(AppCache appCache, ChannelHandlerContext ctx, NettyMessage nettyMessage) {
            doSendAnswer(ctx, nettyMessage);
        }
    },
    SIGN_IN() {
        private String token = "a1B2c3D4";

        @Override
        public void doMsgHandle(AppCache appCache, ChannelHandlerContext ctx, NettyMessage nettyMessage) {
            try {
                Map<String, Object> parameter = nettyMessage.getParameter();
                String signature = ((JSONObject) parameter.get("result")).get("signature").toString();
                LinkedHashMap<String, String> paramsMap = new LinkedHashMap<String, String>() {{
                    put("deviceNo", ((JSONObject) parameter.get("result")).get("deviceNo").toString());
                    put("authorizeCode", ((JSONObject) parameter.get("result")).get("authorizeCode").toString());
                    put("timestamp", ((JSONObject) parameter.get("result")).get("timestamp").toString());
                }};
                if (StringUtil.veryifySign(token, "SHA-1", paramsMap, "&", true, signature)) {
                    NettyMessage sendMessage = new NettyMessage(NettyReceiveMsgHandle.SIGN_IN.name());
                    sendMessage.setCode("1");
                    sendMessage.setInfo("client signin success");
                    doSendMsg(ctx, sendMessage);
                    appCache.addNettyDeviceNo(paramsMap.get("deviceNo"), (SocketChannel) ctx.channel());
                    appCache.addNettyClientIdDeviceNo(ctx.channel().id().asLongText(), paramsMap.get("deviceNo"));
                    System.out.println("client signin : " + paramsMap.get("deviceNo"));
                } else {
                    throw new AppException("signin error");
                }
            } catch (Exception e) {
                NettyMessage sendMessage = new NettyMessage(NettyReceiveMsgHandle.NOMAL.name());
                sendMessage.setCode("0");
                sendMessage.setInfo("signin error");
                doSendMsg(ctx, sendMessage);
            }
        }
    };

    public static NettyReceiveMsgHandle getNettyReceiveMsgHandleByType(String type) {
        for (NettyReceiveMsgHandle nettyReceiveMsgHandle : NettyReceiveMsgHandle.values()) {
            if (nettyReceiveMsgHandle.name().equals(type)) {
                return nettyReceiveMsgHandle;
            }
        }
        return null;
    }

    private static boolean doSendAnswer(ChannelHandlerContext ctx, NettyMessage nettyMessage) {
        NettyMessage sendMessage = new NettyMessage(SocketReceiveMsgHandle.ANSWER.name());
        sendMessage.setCode("1");
        sendMessage.setInfo("success");
        sendMessage.setResult("ok, u say:" + ObjectUtil.objectToJsonString(nettyMessage));
        doSendMsg(ctx, sendMessage);
        return true;
    }

    private static boolean doSendMsg(ChannelHandlerContext ctx, NettyMessage sendMessage) {
        String sendContent = sendMessage.toJsonString();
        ByteBuf seneMsg = Unpooled.buffer(sendContent.length());
        seneMsg.writeBytes(sendContent.getBytes());
        ctx.writeAndFlush(seneMsg);
        return true;
    }

    public abstract void doMsgHandle(AppCache appCache, ChannelHandlerContext ctx, NettyMessage nettyMessage);
}
