package win.panyong.controller;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.panyong.model.NettyMessage;
import win.panyong.model.SocketNoticeMqMessage;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class TestController extends BaseController {
    @RequestMapping("/pushConfig")
    String index() throws Exception {
        String deviceNo = getParameter("deviceNo");
        String message = new SocketNoticeMqMessage().setDeviceNo(deviceNo).setNettyMessage(new NettyMessage("PUSH_CONFIG").setDeviceNo(deviceNo).setCode("1").setInfo("success").setResult("Update Config")).toJsonString();
        jmsTemplate.convertAndSend(new ActiveMQQueue("socketNotice"), message);
        rspCode = OK;
        rspInfo = "发送成功";
        return getResultJsonString();
    }
}