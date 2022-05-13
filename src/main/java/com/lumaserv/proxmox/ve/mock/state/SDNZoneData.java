package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.sdn.SDNZone;
import org.javawebstack.abstractdata.mapper.annotation.MapperOptions;

import java.util.ArrayList;
import java.util.List;

public class SDNZoneData {

    public String name;
    public String type;
    @MapperOptions(generic = String.class)
    public List<String> peers = new ArrayList<>();
    @MapperOptions(generic = String.class)
    public List<String> nodes = new ArrayList<>();
    public String ipam;
    public Integer mtu;
    public String dns;
    public String dnsZone;
    public String pending;
    public String state;
    public String reverseDns;

    public SDNZone toSDNZone() {
        return new SDNZone()
                .setName(name)
                .setType(type)
                .setPeers(String.join(",", peers))
                .setNodes(String.join(",", nodes));
    }

}
