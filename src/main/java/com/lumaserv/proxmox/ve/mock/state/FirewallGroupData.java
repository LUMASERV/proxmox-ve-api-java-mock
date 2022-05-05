package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.firewall.FirewallGroup;

import java.util.ArrayList;
import java.util.List;

public class FirewallGroupData {

    public String name;
    public String comment;
    public List<FirewallRuleData> rules = new ArrayList<>();

    public FirewallGroup toFirewallGroup() {
        return new FirewallGroup()
                .setName(name)
                .setComment(comment);
    }

}
