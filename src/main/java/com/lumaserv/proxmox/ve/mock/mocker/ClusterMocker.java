package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.ClusterAPI;
import com.lumaserv.proxmox.ve.mock.state.*;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class ClusterMocker extends Mocker {

    public static void mockClient(ProxMoxVEClient client, MockState state, Consumer<MockState> onChange) {
        ClusterAPI clusterAPI = mockClusterAPI(client, state, onChange);
        when(client.cluster()).thenReturn(clusterAPI);
    }

    public static ClusterAPI mockClusterAPI(ProxMoxVEClient client, MockState state, Consumer<MockState> onChange) {
        ClusterAPI api = mock(ClusterAPI.class);
        try {
            when(api.getClient()).thenReturn(client);
            when(api.nextId()).then(i -> state.qemuVMs.keySet().stream().max(Comparator.comparingInt(a -> a)).orElse(99) + 1);
            when(api.getTasks()).then(i -> state.tasks.stream().map(TaskData::toTask).collect(Collectors.toList()));
            ClusterSDNMocker.mockClusterAPI(api, state, onChange);
            ClusterFirewallMocker.mockClusterAPI(api, state, onChange);
        } catch (ProxMoxVEException ignored) {}
        return api;
    }

}
