package com.lumaserv.proxmox.ve.mock.state.firewall;

import com.lumaserv.proxmox.ve.model.firewall.FirewallOptions;
import com.lumaserv.proxmox.ve.model.firewall.FirewallRule;

public class FirewallOptionsData {

    public boolean dhcp;
    public boolean enable;
    public boolean ipFilter;
    public String logLevelIn;
    public String logLevelOut;
    public boolean macFilter;
    public boolean ndp;
    public FirewallRule.Action policyIn = FirewallRule.Action.ACCEPT;
    public FirewallRule.Action policyOut = FirewallRule.Action.ACCEPT;
    public boolean routerAdvertisement;

    public FirewallOptions toFirewallOptions() {
        return new FirewallOptions()
                .setDhcp(dhcp ? 1 : 0)
                .setEnable(enable ? 1 : 0)
                .setIpFilter(ipFilter ? 1 : 0)
                .setLogLevelIn(logLevelIn)
                .setLogLevelOut(logLevelOut)
                .setMacFilter(macFilter ? 1 : 0)
                .setNdp(ndp ? 1 : 0)
                .setPolicyIn(policyIn)
                .setPolicyOut(policyOut)
                .setRouterAdvertisement(routerAdvertisement ? 1 : 0);
    }
    
}
