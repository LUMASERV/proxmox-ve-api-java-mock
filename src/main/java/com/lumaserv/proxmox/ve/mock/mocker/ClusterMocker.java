package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.ClusterAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;

import java.util.Comparator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterMocker extends Mocker {

    public static void mockClient(ProxMoxVEClient client, MockState state) {
        ClusterAPI clusterAPI = mockClusterAPI(client, state);
        when(client.cluster()).thenReturn(clusterAPI);
    }

    public static ClusterAPI mockClusterAPI(ProxMoxVEClient client, MockState state) {
        ClusterAPI api = mock(ClusterAPI.class);
        try {
            when(api.getClient()).thenReturn(client);
            when(api.nextId()).then(i -> state.qemuVMs.keySet().stream().max(Comparator.comparingInt(a -> a)).orElse(99) + 1);
            when(api.applySDN()).then(i -> {
                return ""; // TODO
            });
        } catch (ProxMoxVEException ignored) {}
        return api;
    }

}
