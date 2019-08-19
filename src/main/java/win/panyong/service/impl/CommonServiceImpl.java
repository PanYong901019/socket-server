package win.panyong.service.impl;


import com.alibaba.fastjson.TypeReference;
import com.easyond.utils.ObjectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import win.panyong.model.NettyMessage;
import win.panyong.model.SocketNoticeMqMessage;
import win.panyong.service.BaseService;
import win.panyong.service.CommonService;

@Service(value = "commonService")
public class CommonServiceImpl extends BaseService implements CommonService {

    @JmsListener(destination = "socketNotice")
    public void socketNotice(String message) {
        SocketNoticeMqMessage socketNoticeMqMessage = ObjectUtil.jsonStringToObject(message, new TypeReference<SocketNoticeMqMessage>() {
        });
        String deviceNo = socketNoticeMqMessage.getDeviceNo();
        NettyMessage sendMessage = socketNoticeMqMessage.getNettyMessage();
        String sendContent = sendMessage.toJsonString();
        System.out.println("收到推送指令，正在推送：" + sendContent);
        ByteBuf seneMsg = Unpooled.buffer(sendContent.length());
        seneMsg.writeBytes(sendContent.getBytes());
        SocketChannel nettyDeviceNo = appCache.getNettyDeviceNo(deviceNo);
        if (nettyDeviceNo != null) {
            nettyDeviceNo.writeAndFlush(seneMsg);
        } else {
            System.out.println("设备" + deviceNo + "当前不在线");
        }
    }
}