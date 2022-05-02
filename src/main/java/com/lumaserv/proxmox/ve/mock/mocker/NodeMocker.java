package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.NodeData;
import com.lumaserv.proxmox.ve.model.nodes.Node;

import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class NodeMocker extends Mocker {

    public static void mockClient(ProxMoxVEClient client, MockState state) {
        try {
            when(client.getNodes()).then(i -> state.nodes.values().stream().map(NodeMocker::mockNode).collect(Collectors.toList()));
            when(client.nodes(anyString())).then(i -> mockNodeAPI(client, state, i.getArgument(0)));
        } catch (ProxMoxVEException e) {}
    }

    public static NodeAPI mockNodeAPI(ProxMoxVEClient client, MockState state, String name) {
        NodeAPI api = mock(NodeAPI.class);
        when(api.getClient()).thenReturn(client);
        when(api.getNodeName()).thenReturn(name);
        QemuVMMocker.mockNodeAPI(api, state);
        return api;
    }

    public static Node mockNode(NodeData data) {
        return new Node()
                .setName(data.name);
    }

}
