package win.panyong;


import com.alibaba.fastjson.TypeReference;
import com.easyond.utils.ObjectUtil;
import com.easyond.utils.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import win.panyong.model.NettyMessage;
import win.panyong.service.impl.Enum.NettyReceiveMsgHandle;
import win.panyong.utils.AppCache;
import win.panyong.utils.AppException;

import java.util.concurrent.TimeUnit;

public class NettyServer {
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int port;

    @Autowired
    private AppCache appCache;

    public NettyServer() {
    }

    public NettyServer(int port) {
        this.port = port;
    }

    protected static void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void start() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap().group(bossGroup, workerGroup).option(ChannelOption.SO_BACKLOG, 1024).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                class SocketHandler extends ChannelInboundHandlerAdapter {
                    private int lossConnectCount = 0;

                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                        if (evt instanceof IdleStateEvent) {
                            IdleStateEvent e = (IdleStateEvent) evt;
                            switch (e.state()) {
                                case READER_IDLE:
                                    String deviceNo = appCache.getNettyClientIdDeviceNo(ctx.channel().id().asLongText());
                                    lossConnectCount++;
                                    if (lossConnectCount < 2) {
                                        System.out.println(deviceNo + "这个机器已经失联" + lossConnectCount + "次了,再给一次机会");
                                    } else {
                                        System.out.println(deviceNo + "这个机器已经失联" + lossConnectCount + "次了，给它关掉");
                                        ctx.channel().close();
                                        lossConnectCount = 0;
                                    }
                                    break;
                                case WRITER_IDLE:
                                    break;
                                case ALL_IDLE:
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            super.userEventTriggered(ctx, evt);
                        }
                    }

                    @Override
                    public void channelRegistered(ChannelHandlerContext ctx) {
                        String clientId = ctx.channel().id().asLongText();
                        appCache.addNettyClient(clientId, (SocketChannel) ctx.channel());
                        System.out.println("client connect: " + clientId);
                    }

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) {
                        String clientId = ctx.channel().id().asLongText();
                        appCache.removeNettyClient(clientId);
                        String deviceNo = appCache.getNettyClientIdDeviceNo(clientId);
                        if (!StringUtil.invalid(deviceNo)) {
                            appCache.removeNettyClientIdDeviceNo(clientId);
                            appCache.removeNettyDeviceNo(deviceNo);
                            System.out.println("client disconnect: " + clientId + "---" + deviceNo);
                        } else {
                            System.out.println("client disconnect: " + clientId);
                        }
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        try {
                            String message = msg.toString();
                            NettyMessage nettyMessage = ObjectUtil.jsonStringToObject(message, new TypeReference<NettyMessage>() {
                            });

                            if (NettyReceiveMsgHandle.PING_PONG.name().equals(nettyMessage.getMsgType())) {
                                System.out.println("接受到ping：" + nettyMessage.getDeviceNo());
                            } else {
                                System.out.println("接受到消息：" + message);
                            }
                            if (!NettyReceiveMsgHandle.SIGN_IN.name().equals(nettyMessage.getMsgType()) && !NettyReceiveMsgHandle.ANSWER.name().equals(nettyMessage.getMsgType()) && !NettyReceiveMsgHandle.PING_PONG.name().equals(nettyMessage.getMsgType())) {
                                String deviceNo = appCache.getNettyClientIdDeviceNoList().get(ctx.channel().id().asLongText());
                                if (StringUtil.invalid(deviceNo) || !deviceNo.equals(nettyMessage.getDeviceNo())) {
                                    throw new AppException("invalid msg");
                                }
                            }
                            NettyReceiveMsgHandle.getNettyReceiveMsgHandleByType(nettyMessage.getMsgType()).doMsgHandle(appCache, ctx, nettyMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            NettyMessage sendMessage = new NettyMessage(NettyReceiveMsgHandle.NOMAL.name());
                            sendMessage.setCode("0");
                            sendMessage.setInfo("invalid msg");
                            String sendContent = sendMessage.toJsonString();
                            ByteBuf seneMsg = Unpooled.buffer(sendContent.length());
                            seneMsg.writeBytes(sendContent.getBytes());
                            ctx.writeAndFlush(seneMsg);
                        }

                    }

                }
                pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                pipeline.addLast(new LineBasedFrameDecoder(102400));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new SocketHandler());
            }
        });

        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        channelFuture.channel().closeFuture().sync();
        System.out.println("server start");
    }
}


