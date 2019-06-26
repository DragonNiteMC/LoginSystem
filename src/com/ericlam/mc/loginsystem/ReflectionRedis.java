package com.ericlam.mc.loginsystem;

import java.lang.reflect.Method;

class ReflectionRedis {
    static RedisData getRedisData(boolean bungee) throws Exception{
        Class<?> mainClass = Class.forName(bungee ? "com.ericlam.mc.bungee.hnmc.main.HyperNiteMC" : "com.hypernite.mc.hnmc.core.main.HyperNiteMC");
        Method getAPIMethod = mainClass.getMethod("getAPI");
        Object apiInstance = getAPIMethod.invoke(null);
        Class<?> apiClass = apiInstance.getClass();
        Method getConfigMethod = apiClass.getMethod(bungee ? "getMainConfig" : "getCoreConfig");
        Object configInstance = getConfigMethod.invoke(apiInstance);
        Class<?> configClass = configInstance.getClass();
        Method getDataBaseMethod = configClass.getMethod(bungee ? "getDatabase" : "getDataBase");
        Object databaseInstsance = getDataBaseMethod.invoke(configInstance);
        Class<?> databaseClass = databaseInstsance.getClass();
        Method getIntMethod = databaseClass.getMethod("getInt",String.class);
        Method getStringMethod = databaseClass.getMethod("getString",String.class);
        Method getBooleanMethod = databaseClass.getMethod("getBoolean",String.class);
        String ip = (String)getStringMethod.invoke(databaseInstsance,"Redis.ip");
        int port = (int)getIntMethod.invoke(databaseInstsance,"Redis.port");
        String password = (String)getStringMethod.invoke(databaseInstsance,"Redis.password");
        int timeout = (int)getIntMethod.invoke(databaseInstsance,"Redis.timeout");
        boolean usePassword = (boolean)getBooleanMethod.invoke(databaseInstsance,"Redis.use-password");
        return new RedisData(ip,port,password,timeout,usePassword);
    }
}
