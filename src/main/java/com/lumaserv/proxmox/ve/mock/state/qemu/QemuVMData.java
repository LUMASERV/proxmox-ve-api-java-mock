package com.lumaserv.proxmox.ve.mock.state.qemu;

import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallIPSetData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallOptionsData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallRuleData;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QemuVMData {

    public int id;
    public String node;
    public Integer acpi;
    public String agent;
    public String arch;
    public String args;
    public String audio;
    public Integer autostart;
    public Integer balloon;
    public String bios;
    public String bootOrder;
    public String bootDisk;
    public String ciCustom;
    public String ciPassword;
    public String ciType;
    public String ciUser;
    public Integer cores;
    public String cpu;
    public Double cpuLimit;
    public Integer cpuUnits;
    public String description;
    public String efiDisk;
    public Integer freeze;
    public String hookScript;
    public String hotplug;
    public String hugepages;
    public String interVMSharedMemory;
    public Integer keepHugepages;
    public String keyboard;
    public Integer kvm;
    public Integer localTime;
    public String lock;
    public String machine;
    public Integer memory;
    public Double migrateDowntime;
    public Integer migrateSpeed;
    public String name;
    public String nameserver;
    public Integer numa;
    public String startOnBoot;
    public String osType;
    public Integer protection;
    public Integer allowReboot;
    public String randomNumberGenerator;
    public String scsiHw;
    public String searchDomain;
    public Integer shares;
    public String smbios1;
    public Integer sockets;
    public String spiceEnhancements;
    public String sshKeys;
    public String startDate;
    public String startup;
    public Integer tablet;
    public String tags;
    public Integer timeDriftFix;
    public Integer enableTemplate;
    public Integer vcpus;
    public String vga;
    public String vmGenId;
    public String vmStateStorage;
    public String watchdog;
    public final Map<Integer, String> hostPci = new HashMap();
    public final Map<Integer, String> ide = new HashMap();
    public final Map<Integer, IPConfigData> ipConfigs = new HashMap();
    public final Map<Integer, NetworkData> networks = new HashMap();
    public final Map<String, DiskData> disks = new HashMap();
    public final Map<Integer, String> serial = new HashMap();
    public final Map<Integer, String> unused = new HashMap();
    public final Map<Integer, String> usb = new HashMap();
    public final Map<Integer, String> virtio = new HashMap();
    public FirewallOptionsData firewallOptions = new FirewallOptionsData();
    public List<FirewallRuleData> firewallRules = new ArrayList<>();
    public Map<String, FirewallIPSetData> firewallIpSets = new HashMap<>();

    public boolean started = false;
    public long startedAt = 0;

}