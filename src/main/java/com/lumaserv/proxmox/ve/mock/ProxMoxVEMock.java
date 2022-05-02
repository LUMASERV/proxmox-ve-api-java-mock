package com.lumaserv.proxmox.ve.mock;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.mock.mocker.PoolMocker;
import com.lumaserv.proxmox.ve.mock.state.MockState;

import static org.mockito.Mockito.*;

public class ProxMoxVEMock {

    public static ProxMoxVEClient create() {
        return create(new MockState());
    }

    public static ProxMoxVEClient create(MockState state) {
        ProxMoxVEClient client = mock(ProxMoxVEClient.class);
        PoolMocker.mockClient(client, state);
        return client;
    }

}