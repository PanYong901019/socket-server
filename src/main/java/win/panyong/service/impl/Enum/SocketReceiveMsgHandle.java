package win.panyong.service.impl.Enum;


import com.easyond.utils.ObjectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Component;
import win.panyong.model.NettyMessage;

import java.util.Map;

/**
 * Created by pan on 2019/07/04 上午11:00
 */
@Component
public enum SocketReceiveMsgHandle {
    PING_PONG() {
        @Override
        public void doMsgHandle(NettyMessage nettyMessage) {
            Map<String, Object> parameter = nettyMessage.getParameter();
            String request = parameter.get("result").toString();
            if ("ping".equals(request)) {
                NettyMessage sendMessage = new NettyMessage(SocketReceiveMsgHandle.NOMAL.name());
                sendMessage.setCode("1");
                sendMessage.setInfo("success");
                sendMessage.setResult("pong");
                doSendMsg(sendMessage);
            }
        }
    },
    ANSWER() {
        @Override
        public void doMsgHandle(NettyMessage nettyMessage) {

        }
    },
    NOMAL() {
        @Override
        public void doMsgHandle(NettyMessage nettyMessage) {
            doSendAnswer(nettyMessage);

        }
    },
    SIGN_IN() {
        @Override
        public void doMsgHandle(NettyMessage nettyMessage) {
            Map<String, Object> parameter = nettyMessage.getParameter();
            String code = parameter.get("code").toString();
            if ("1".equals(code)) {
                doSendAnswer(nettyMessage);
            } else {

            }
        }
    },
    PUSH_CONFIG() {
        @Override
        public void doMsgHandle(NettyMessage nettyMessage) {
            doSendAnswer(nettyMessage);
        }
    },
    PAY_TRADE() {
        @Override
        public void doMsgHandle(NettyMessage nettyMessage) {
            doSendAnswer(nettyMessage);
            Map<String, Object> parameter = nettyMessage.getParameter();
            String code = parameter.get("code").toString();
            String info = parameter.get("info").toString();
            Map<String, String> result = (Map<String, String>) parameter.get("result");
            String status = result.get("status");
            String orderId = result.get("orderId");
            String orderNo = result.get("orderNo");
        }
    };

    public static SocketReceiveMsgHandle getSocketReceiveMsgHandleByType(String type) {
        for (SocketReceiveMsgHandle nettyReceiveMsgHandle : SocketReceiveMsgHandle.values()) {
            if (nettyReceiveMsgHandle.name().equals(type)) {
                return nettyReceiveMsgHandle;
            }
        }
        return null;
    }

    private static boolean doSendMsg(NettyMessage sendMessage) {
        String sendContent = sendMessage.toJsonString();
        ByteBuf seneMsg = Unpooled.buffer(sendContent.length());
        seneMsg.writeBytes(sendContent.getBytes());
        return true;
    }

    private static boolean doSendAnswer(NettyMessage nettyMessage) {
        NettyMessage sendMessage = new NettyMessage(SocketReceiveMsgHandle.ANSWER.name());
        sendMessage.setCode("1");
        sendMessage.setInfo("success");
        sendMessage.setResult("ok, u say:" + ObjectUtil.objectToJsonString(nettyMessage));
        doSendMsg(sendMessage);
        return true;
    }

    public abstract void doMsgHandle(NettyMessage nettyMessage);
}
