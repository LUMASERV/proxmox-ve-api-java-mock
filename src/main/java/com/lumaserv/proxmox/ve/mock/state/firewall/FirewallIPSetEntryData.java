package com.lumaserv.proxmox.ve.mock.state.firewall;

import com.lumaserv.proxmox.ve.model.firewall.FirewallIPSetEntry;

public class FirewallIPSetEntryData {

    public String cidr;
    public String comment;
    public boolean noMatch;

    public FirewallIPSetEntry toFirewallIPSetEntry() {
        return new FirewallIPSetEntry()
                .setCidr(cidr)
                .setComment(comment)
                .setNoMatch(noMatch);
    }

}
