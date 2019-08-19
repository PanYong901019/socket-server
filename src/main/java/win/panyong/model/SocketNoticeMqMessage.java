package win.panyong.model;


import com.easyond.utils.ObjectUtil;

/**
 * Created by pan on 2019/7/1 7:14 PM
 */
public class SocketNoticeMqMessage {
    private String deviceNo;
    private NettyMessage nettyMessage;

    public String getDeviceNo() {
        return deviceNo;
    }

    public SocketNoticeMqMessage setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
        return this;
    }

    public NettyMessage getNettyMessage() {
        return nettyMessage;
    }

    public SocketNoticeMqMessage setNettyMessage(NettyMessage nettyMessage) {
        this.nettyMessage = nettyMessage;
        return this;
    }

    public String toJsonString() {
        return ObjectUtil.objectToJsonString(this);
    }
}
