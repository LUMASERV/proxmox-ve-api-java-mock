package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallGroupData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallIPSetData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallOptionsData;
import com.lumaserv.proxmox.ve.mock.state.firewall.FirewallRuleData;
import com.lumaserv.proxmox.ve.mock.state.qemu.QemuVMData;
import org.javawebstack.abstractdata.mapper.annotation.MapperOptions;

import java.util.*;

public class MockState {

    public String currentUser = "root@pam";
    @MapperOptions(generic = { String.class, PoolData.class })
    public Map<String, PoolData> pools = new HashMap<>();
    @MapperOptions(generic = { Integer.class, QemuVMData.class })
    public Map<Integer, QemuVMData> qemuVMs = new HashMap<>();
    @MapperOptions(generic = { String.class, NodeData.class })
    public Map<String, NodeData> nodes = new HashMap<>();
    @MapperOptions(generic = TaskData.class)
    public List<TaskData> tasks = new ArrayList<>();
    @MapperOptions(generic = { String.class, SDNZoneData.class })
    public Map<String, SDNZoneData> sdnZones = new HashMap<>();
    @MapperOptions(generic = { String.class, SDNVNetData.class })
    public Map<String, SDNVNetData> sdnVNets = new HashMap<>();
    @MapperOptions(generic = { String.class, StorageData.class })
    public Map<String, StorageData> storages = new HashMap<>();
    @MapperOptions(generic = FirewallRuleData.class)
    public List<FirewallRuleData> firewallRules = new ArrayList<>();
    @MapperOptions(generic = { String.class, FirewallGroupData.class })
    public Map<String, FirewallGroupData> firewallGroups = new HashMap<>();
    @MapperOptions(generic = { String.class, FirewallIPSetData.class })
    public Map<String, FirewallIPSetData> firewallIpSets = new HashMap<>();
    public FirewallOptionsData firewallOptions = new FirewallOptionsData();

    public TaskData createTask(String node, String type, Integer vmId) {
        TaskData taskData = new TaskData();
        taskData.user = currentUser;
        taskData.node = node;
        taskData.type = type;
        taskData.vmId = vmId;
        Random random = new Random();
        StringBuilder sb = new StringBuilder("UPID:").append(node).append(":");
        for(int i=0; i<3; i++) {
            for(int j=0; j<4; j++)
                sb.append(String.format("%02X", random.nextInt(256)));
            sb.append(":");
        }
        sb.append(type).append(":").append(vmId != null ? vmId : "").append(":").append(taskData.user).append(":");
        taskData.upId = sb.toString();
        taskData.status = "running";
        taskData.start = System.currentTimeMillis();
        tasks.add(taskData);
        return taskData;
    }

    public NodeData createNode(String name) {
        NodeData nodeData = new NodeData();
        nodeData.name = name;
        nodes.put(name, nodeData);
        return nodeData;
    }

    public StorageData createStorage(String type, String name) {
        StorageData storageData = new StorageData();
        storageData.name = name;
        storageData.type = type;
        storages.put(name, storageData);
        return storageData;
    }

}
