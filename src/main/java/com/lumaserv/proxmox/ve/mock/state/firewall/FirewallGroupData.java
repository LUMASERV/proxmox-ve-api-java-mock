package com.lumaserv.proxmox.ve.mock.state.firewall;

import com.lumaserv.proxmox.ve.model.firewall.FirewallGroup;
import org.javawebstack.abstractdata.mapper.annotation.MapperOptions;

import java.util.ArrayList;
import java.util.List;

public class FirewallGroupData {

    public String name;
    public String comment;
    @MapperOptions(generic = FirewallRuleData.class)
    public List<FirewallRuleData> rules = new ArrayList<>();

    public FirewallGroup toFirewallGroup() {
        return new FirewallGroup()
                .setName(name)
                .setComment(comment);
    }

}
