package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.firewall.FirewallRule;

public class FirewallRuleData {

    public int pos;
    public boolean enable;
    public String action;
    public String type;
    public String comment;
    public String dest;
    public String source;
    public String dPort;
    public String sPort;
    public String icmpType;
    public String iface;
    public String macro;
    public String proto;
    public String log;

    public FirewallRule toFirewallRule() {
        return new FirewallRule()
                .setPos(pos)
                .setAction(action)
                .setType(type)
                .setIcmpType(icmpType)
                .setIface(iface)
                .setComment(comment)
                .setProtocol(proto)
                .setMacro(macro)
                .setSourcePort(sPort)
                .setDestinationPort(dPort)
                .setSource(source)
                .setDestination(dest)
                .setEnable(enable ? 1 : 0)
                .setLog(log);
    }

}
