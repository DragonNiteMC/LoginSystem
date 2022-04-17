package com.ericlam.mc.loginsystem.main;

import com.dragonnite.mc.dnmc.core.config.yaml.Configuration;
import com.dragonnite.mc.dnmc.core.config.yaml.Resource;

import java.util.List;

@Resource(locate = "lobby.yml")
public class LoginConfig extends Configuration {

    List<String> premiumPermissions;
}
