package win.panyong.utils;


import win.panyong.NettyServer;

public class AppInitFunction {
    private static volatile AppInitFunction instance = null;

    private AppInitFunction() {
    }

    public static AppInitFunction getInstance() {
        if (instance == null) {
            synchronized (AppInitFunction.class) {
                if (instance == null) {
                    instance = new AppInitFunction();
                }
            }
        }
        return instance;
    }

    public String getTextInfo() throws AppException {
        return "success";
    }

    public void startNettyServer(NettyServer nettyServer) {
        new Thread(() -> {
            try {
                nettyServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
