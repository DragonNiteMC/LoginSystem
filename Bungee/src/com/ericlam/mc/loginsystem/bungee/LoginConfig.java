package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.bungee.hnmc.config.Prop;
import com.ericlam.mc.bungee.hnmc.config.yaml.BungeeConfiguration;
import com.ericlam.mc.bungee.hnmc.config.yaml.Resource;

@Resource(locate = "config.yml")
public class LoginConfig extends BungeeConfiguration {

    @Prop(path = "session-expire")
    public int expireMins;

    @Prop(path = "times-failed-kick")
    public int timesBeforeFail;

    @Prop(path = "sec-failed-kick")
    public int secBeforeFail;

    @Prop
    public String lobby;

    @Prop(path = "max-ac-per-ip")
    public int maxAcPerIP;

    @Prop(path = "max-login-per-ip")
    public int maxLoginPerIP;
}
