package com.lumaserv.proxmox.ve.mock.state;

import java.util.HashMap;
import java.util.Map;

public class MockState {

    public Map<String, PoolData> pools = new HashMap<>();
    public Map<Integer, QemuVMData> qemuVMs = new HashMap<>();
    public Map<String, NodeData> nodes = new HashMap<>();

    public NodeData createNode(String name) {
        NodeData nodeData = new NodeData();
        nodeData.name = name;
        nodes.put(name, nodeData);
        return nodeData;
    }

}
