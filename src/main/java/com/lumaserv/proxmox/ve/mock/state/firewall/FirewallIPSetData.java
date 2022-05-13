package com.lumaserv.proxmox.ve.mock.state.firewall;

import com.lumaserv.proxmox.ve.model.firewall.FirewallIPSet;
import org.javawebstack.abstractdata.mapper.annotation.MapperOptions;

import java.util.HashMap;
import java.util.Map;

public class FirewallIPSetData {

    public String name;
    public String comment;
    @MapperOptions(generic = { String.class, FirewallIPSetEntryData.class })
    public Map<String, FirewallIPSetEntryData> entries = new HashMap<>();

    public FirewallIPSet toFirewallIPSet() {
        return new FirewallIPSet()
                .setName(name)
                .setComment(comment);
    }

}
