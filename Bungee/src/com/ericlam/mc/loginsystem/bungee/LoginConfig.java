package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.bungee.dnmc.config.yaml.BungeeConfiguration;
import com.ericlam.mc.bungee.dnmc.config.yaml.Resource;

@Resource(locate = "config.yml")
public class LoginConfig extends BungeeConfiguration {

    public int sessionExpireMins;

    public int timesBeforeFail;


    public int secBeforeFail;


    public int maxAcPerIP;

    public int maxLoginPerIP;

    public String lobby;
}
