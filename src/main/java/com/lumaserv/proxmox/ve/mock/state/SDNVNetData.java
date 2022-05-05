package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.sdn.SDNVNet;

public class SDNVNetData {

    public String name;
    public Boolean vlanAware;
    public String zone;
    public Integer tag;
    public String type;
    public String alias;

    public SDNVNet toSDNVNet() {
        return new SDNVNet()
                .setName(name)
                .setType(type)
                .setTag(tag)
                .setVlanAware((vlanAware != null && vlanAware) ? 1 : 0)
                .setZone(zone)
                .setAlias(alias);
    }
    
}
