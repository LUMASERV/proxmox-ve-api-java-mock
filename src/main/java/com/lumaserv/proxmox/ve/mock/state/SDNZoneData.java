package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.sdn.SDNZone;

public class SDNZoneData {

    public String name;
    public String nodes;
    public String ipam;
    public String peers;
    public String type;
    public Integer mtu;
    public String dns;
    public String dnsZone;
    public String pending;
    public String state;
    public String reverseDns;

    public SDNZone toSDNZone() {
        if(peers == null)
            peers = "";
        return new SDNZone()
                .setName(name)
                .setPeers(peers)
                .setNodes(nodes)
                .setType(type);
    }

}
