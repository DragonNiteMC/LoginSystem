package com.ericlam.mc.loginsystem.main;

import com.hypernite.mc.hnmc.core.config.Prop;
import com.hypernite.mc.hnmc.core.config.yaml.Configuration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;

import java.util.List;

@Resource(locate = "lobby.yml")
public class LoginConfig extends Configuration {

    @Prop(path = "premium-permissions")
    List<String> playersCommand;
}
