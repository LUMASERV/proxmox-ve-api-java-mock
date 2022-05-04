package com.lumaserv.proxmox.ve.mock.state;

import java.util.*;

public class MockState {

    public String currentUser = "root@pam";
    public Map<String, PoolData> pools = new HashMap<>();
    public Map<Integer, QemuVMData> qemuVMs = new HashMap<>();
    public Map<String, NodeData> nodes = new HashMap<>();
    public List<TaskData> tasks = new ArrayList<>();

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

}
