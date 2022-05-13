package com.lumaserv.proxmox.ve.mock.state.qemu;

import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallIPSetData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallOptionsData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallRuleData;
import org.javawebstack.abstractdata.mapper.annotation.MapperOptions;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QemuVMData {

    public int id;
    public String node;

    public String name;
    public String agent;
    public String arch;
    public String bootOrder;
    public String bootDisk;
    public CICustom ciCustom;
    public String ciPassword;
    public String ciType;
    public String ciUser;
    public Integer cores;
    public String cpu;
    public String description;
    public String lock;
    public String machine;
    public Integer memory;
    public String nameserver;
    public String osType;
    public String searchDomain;
    public String sshKeys;
    public String startup;
    @MapperOptions(generic = { Integer.class, IPConfigData.class })
    public final Map<Integer, IPConfigData> ipConfigs = new HashMap();
    @MapperOptions(generic = { Integer.class, NetworkData.class })
    public final Map<Integer, NetworkData> networks = new HashMap();
    @MapperOptions(generic = { String.class, DiskData.class })
    public final Map<String, DiskData> disks = new HashMap();
    public FirewallOptionsData firewallOptions = new FirewallOptionsData();
    @MapperOptions(generic = FirewallRuleData.class)
    public List<FirewallRuleData> firewallRules = new ArrayList<>();
    @MapperOptions(generic = { String.class, FirewallIPSetData.class })
    public Map<String, FirewallIPSetData> firewallIpSets = new HashMap<>();

    public boolean started = false;
    public long startedAt = 0;

    public String findUnused() {
        int n = 0;
        while (disks.containsKey("unused" + n))
            n++;
        return "unused" + n;
    }

}
