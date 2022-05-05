package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.ClusterAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.SDNZoneData;
import com.lumaserv.proxmox.ve.mock.state.TaskData;
import com.lumaserv.proxmox.ve.request.sdn.SDNVNetGetRequest;
import com.lumaserv.proxmox.ve.request.sdn.SDNZoneGetRequest;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

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
                TaskData reloadAllTask = state.createTask(state.nodes.keySet().stream().findFirst().get(), "reloadnetworkall", null);
                for(String node : state.nodes.keySet()) {
                    TaskData task = state.createTask(node, "srvreload", null);
                    task.finish();
                }
                reloadAllTask.finish();
                return reloadAllTask.upId;
            });
            when(api.getTasks()).then(i -> state.tasks.stream().map(TaskData::toTask).collect(Collectors.toList()));
            when(api.getSDNZones()).then(i -> api.getSDNZones(new SDNZoneGetRequest()));
            when(api.getSDNZones(any(SDNZoneGetRequest.class))).then(i -> {
                SDNZoneGetRequest request = i.getArgument(0);
                Stream<SDNZoneData> stream = state.sdnZones.stream();
                return stream.collect(Collectors.toList());
            });
            when(api.getSDNZone(anyString())).then(i -> api.getSDNZone(i.getArgument(0), new SDNZoneGetRequest()));
            when(api.getSDNZone(anyString(), any(SDNZoneGetRequest.class))).then(i -> {
                String name = i.getArgument(0);
                SDNZoneGetRequest request = i.getArgument(1);
                SDNZoneData data = state.sdnZones.stream().filter(z -> z.name.equals(name)).findFirst().orElse(null);
                if(data == null)
                    throwError(404, "Not Found");
                return data.toSDNZone();
            });
            when(api.getSDNVNets()).then(i -> api.getSDNVNets(new SDNVNetGetRequest()));
            when(api.getSDNVNets(any(SDNVNetGetRequest.class))).then(i -> {
                SDNVNetGetRequest request = i.getArgument(0);
                return state.sdnZones.stream().map(SDNZoneData::toSDNZone).collect(Collectors.toList());
            });
        } catch (ProxMoxVEException ignored) {}
        return api;
    }

}
