package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.firewall.FirewallIPSet;

import java.util.HashMap;
import java.util.Map;

public class FirewallIPSetData {

    public String name;
    public String comment;
    public Map<String, FirewallIPSetEntryData> entries = new HashMap<>();

    public FirewallIPSet toFirewallIPSet() {
        return new FirewallIPSet()
                .setName(name)
                .setComment(comment);
    }

}
