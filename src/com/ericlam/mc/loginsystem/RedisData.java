package com.ericlam.mc.loginsystem;

public class RedisData {
    private String ip;
    private int port;
    private String password;
    private int timeout;
    private boolean usePassword;

    public RedisData(String ip, int port, String password, int timeout, boolean usePassword) {
        this.ip = ip;
        this.port = port;
        this.password = password;
        this.timeout = timeout;
        this.usePassword = usePassword;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isUsePassword() {
        return usePassword;
    }
}
