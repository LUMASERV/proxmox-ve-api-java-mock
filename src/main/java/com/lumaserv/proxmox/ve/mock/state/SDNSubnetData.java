package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.model.sdn.SDNSubnet;

public class SDNSubnetData {

    public String cidr;
    public String gateway;
    public String type;

    public SDNSubnet toSDNSubnet() {
        return new SDNSubnet()
                .setCidr(cidr)
                .setType(type)
                .setGateway(gateway);
    }

}
