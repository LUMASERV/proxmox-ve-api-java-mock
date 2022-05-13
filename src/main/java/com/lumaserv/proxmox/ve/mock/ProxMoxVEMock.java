package com.lumaserv.proxmox.ve.mock;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.mock.mocker.ClusterMocker;
import com.lumaserv.proxmox.ve.mock.mocker.NodeMocker;
import com.lumaserv.proxmox.ve.mock.mocker.PoolMocker;
import com.lumaserv.proxmox.ve.mock.state.MockState;

import java.util.function.Consumer;

import static org.mockito.Mockito.mock;

public class ProxMoxVEMock {

    public static ProxMoxVEClient create() {
        MockState state = new MockState();
        state.createNode("node01");
        return create(state);
    }

    public static ProxMoxVEClient create(MockState state) {
        return create(state, s -> {});
    }

    public static ProxMoxVEClient create(MockState state, Consumer<MockState> onChange) {
        ProxMoxVEClient client = mock(ProxMoxVEClient.class);
        PoolMocker.mockClient(client, state, onChange);
        NodeMocker.mockClient(client, state, onChange);
        ClusterMocker.mockClient(client, state, onChange);
        return client;
    }

}
