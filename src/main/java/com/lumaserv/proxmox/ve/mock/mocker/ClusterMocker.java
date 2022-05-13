package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.ClusterAPI;
import com.lumaserv.proxmox.ve.mock.state.*;
import com.lumaserv.proxmox.ve.model.resource.QemuVMResource;
import com.lumaserv.proxmox.ve.model.resource.Resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
            when(api.getResources()).then(i -> api.getResources(null));
            when(api.getResources(anyString())).then(i -> {
                String type = i.getArgument(0);
                List<Resource> resources = new ArrayList<>();
                if(type == null || type.equals("vm")) {
                    state.qemuVMs.forEach((id, data) -> {
                        PoolData pool = state.pools.values().stream().filter(p -> p.members.stream().anyMatch(m -> m.type.equals("vm") && m.vmId == id)).findFirst().orElse(null);
                        resources.add(new QemuVMResource()
                                .setVmid(id)
                                .setNode(data.node)
                                .setStatus(data.started ? "running" : "stopped")
                                .setName(data.name)
                                .setPool(pool == null ? null : pool.id)
                                .setUptime(data.started ? ((int) ((System.currentTimeMillis() - data.startedAt) / 1000)) : null)
                                .setType("vm")
                                .setId("qemu/" + id)
                        );
                    });
                }
                return resources;
            });
            ClusterSDNMocker.mockClusterAPI(api, state, onChange);
            ClusterFirewallMocker.mockClusterAPI(api, state, onChange);
        } catch (ProxMoxVEException ignored) {}
        return api;
    }

}
