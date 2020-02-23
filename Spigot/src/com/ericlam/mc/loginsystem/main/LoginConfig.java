package com.ericlam.mc.loginsystem.main;

import com.hypernite.mc.hnmc.core.config.yaml.Configuration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;

import java.util.List;

@Resource(locate = "lobby.yml")
public class LoginConfig extends Configuration {

    List<String> premiumPermissions;
}
