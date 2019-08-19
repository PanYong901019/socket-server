package win.panyong.model;

import com.easyond.utils.ObjectUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pan on 2019/6/30 12:32 AM
 */
public class NettyMessage {
    private String msgType;
    private String deviceNo;
    private Map<String, Object> parameter;

    public NettyMessage() {
        this.parameter = new HashMap<>();
    }

    public NettyMessage(String msgType) {
        this.msgType = msgType;
        this.parameter = new HashMap<>();
    }

    public String getMsgType() {
        return msgType;
    }

    public NettyMessage setMsgType(String msgType) {
        this.msgType = msgType;
        return this;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public NettyMessage setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
        return this;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }

    public NettyMessage setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
        return this;
    }

    public NettyMessage setCode(String code) {
        this.parameter.put("code", code);
        return this;
    }

    public NettyMessage setInfo(String info) {
        this.parameter.put("info", info);
        return this;
    }

    public NettyMessage setResult(Object result) {
        this.parameter.put("result", result);
        return this;
    }

    public String toJsonString() {
        return ObjectUtil.objectToJsonString(this) + "\n";
    }
}
