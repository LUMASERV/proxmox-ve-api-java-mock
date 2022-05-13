package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.ClusterAPI;
import com.lumaserv.proxmox.ve.mock.state.*;
import com.lumaserv.proxmox.ve.request.sdn.*;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class ClusterSDNMocker extends Mocker {

    public static void mockClusterAPI(ClusterAPI api, MockState state, Consumer<MockState> onChange) {
        try {
            /*
            Apply
             */
            when(api.applySDN()).then(i -> {
                TaskData reloadAllTask = state.createTask(state.nodes.keySet().stream().findFirst().get(), "reloadnetworkall", null);
                for(String node : state.nodes.keySet()) {
                    TaskData task = state.createTask(node, "srvreload", null);
                    task.finish();
                }
                reloadAllTask.finish();
                onChange.accept(state);
                return reloadAllTask.upId;
            });

            /*
            Zones
             */
            doAnswer(i -> {
                SDNZoneCreateRequest request = i.getArgument(0);
                verifyRequiredParam("name", request.getName());
                verifyRequiredParam("type", request.getType());
                if(state.sdnZones.containsKey(request.getName()))
                    throwError(409, "Name is already used");
                SDNZoneData sdnZone = new SDNZoneData();
                sdnZone.name = request.getName();
                sdnZone.type = request.getType();
                if(request.getNodes() != null && request.getNodes().length() > 0)
                    sdnZone.nodes.addAll(Arrays.asList(request.getNodes().split(",")));
                if(request.getPeers() != null && request.getPeers().length() > 0)
                    sdnZone.peers.addAll(Arrays.asList(request.getPeers().split(",")));
                state.sdnZones.put(sdnZone.name, sdnZone);
                onChange.accept(state);
                return null;
            }).when(api).createSDNZone(any(SDNZoneCreateRequest.class));
            doAnswer(i -> {
                String zoneName = i.getArgument(0);
                SDNZoneUpdateRequest request = i.getArgument(1);
                SDNZoneData zoneData = state.sdnZones.get(zoneName);
                if(zoneData == null)
                    throwError(404, "Not Found");
                if(request.getNodes() != null) {
                    zoneData.nodes.clear();
                    if(request.getNodes().length() > 0)
                        zoneData.nodes.addAll(Arrays.asList(request.getNodes().split(",")));
                }
                if(request.getPeers() != null) {
                    zoneData.peers.clear();
                    if(request.getPeers().length() > 0)
                        zoneData.peers.addAll(Arrays.asList(request.getPeers().split(",")));
                }
                if(request.getDelete() != null) {
                    for(String d : request.getDelete().split(",")) {
                        switch (d) {
                            case "nodes":
                                zoneData.nodes.clear();
                                break;
                            case "peers":
                                zoneData.peers.clear();
                                break;
                            default:
                                onChange.accept(state);
                                throwError(400, "Cannot delete '" + d + "'");
                        }
                    }
                }
                onChange.accept(state);
                return null;
            }).when(api).updateSDNZone(anyString(), any(SDNZoneUpdateRequest.class));
            when(api.getSDNZones()).then(i -> api.getSDNZones(new SDNZoneGetRequest()));
            when(api.getSDNZones(any(SDNZoneGetRequest.class))).then(i -> {
                SDNZoneGetRequest request = i.getArgument(0);
                Stream<SDNZoneData> stream = state.sdnZones.values().stream();
                return stream.collect(Collectors.toList());
            });
            when(api.getSDNZone(anyString())).then(i -> api.getSDNZone(i.getArgument(0), new SDNZoneGetRequest()));
            when(api.getSDNZone(anyString(), any(SDNZoneGetRequest.class))).then(i -> {
                String name = i.getArgument(0);
                SDNZoneGetRequest request = i.getArgument(1);
                SDNZoneData data = state.sdnZones.get(name);
                if(data == null)
                    throwError(404, "Not Found");
                return data.toSDNZone();
            });
            doAnswer(i -> {
                String name = i.getArgument(0);
                if(!state.sdnZones.containsKey(name))
                    throwError(404, "Not Found");
                if(state.sdnVNets.values().stream().anyMatch(v -> v.zone.equals(name)))
                    throwError(409, "Zone has Vnet's");
                state.sdnZones.remove(name);
                onChange.accept(state);
                return null;
            }).when(api).deleteSDNZone(anyString());

            /*
            VNet
             */
            doAnswer(i -> {
                SDNVNetCreateRequest request = i.getArgument(0);
                verifyRequiredParam("name", request.getName());
                verifyRequiredParam("type", request.getType());
                if(state.sdnVNets.containsKey(request.getName()))
                    throwError(409, "Name is already used");
                SDNZoneData zone = state.sdnZones.get(request.getZone());
                if(zone == null)
                    throwError(404, "Zone not found");
                SDNVNetData sdnVNet = new SDNVNetData();
                sdnVNet.type = request.getType();
                sdnVNet.name = request.getName();
                sdnVNet.zone = request.getZone();
                state.sdnVNets.put(sdnVNet.name, sdnVNet);
                onChange.accept(state);
                return null;
            }).when(api).createSDNVNet(any(SDNVNetCreateRequest.class));
            doAnswer(i -> {
                String vNetName = i.getArgument(0);
                SDNVNetUpdateRequest request = i.getArgument(1);
                SDNVNetData vNetData = state.sdnVNets.get(vNetName);
                if(vNetData == null)
                    throwError(404, "Not Found");
                if(request.getVlanAware() != null)
                    vNetData.vlanAware = request.getVlanAware() > 0;
                if(request.getAlias() != null)
                    vNetData.alias = request.getAlias();
                if(request.getZone() != null) {
                    if(!state.sdnZones.containsKey(request.getZone()))
                        throwError(400, "Zone doesn't exist");
                    vNetData.zone = request.getZone();
                }
                if(request.getDelete() != null) {
                    for(String d : request.getDelete().split(",")) {
                        switch (d) {
                            case "alias":
                                vNetData.alias = null;
                                break;
                            case "vlanaware":
                                vNetData.vlanAware = false;
                                break;
                            default:
                                onChange.accept(state);
                                throwError(400, "Cannot delete '" + d + "'");
                        }
                    }
                }
                onChange.accept(state);
                return null;
            }).when(api).updateSDNVNet(anyString(), any(SDNVNetUpdateRequest.class));
            when(api.getSDNVNets()).then(i -> api.getSDNVNets(new SDNVNetGetRequest()));
            when(api.getSDNVNets(any(SDNVNetGetRequest.class))).then(i -> {
                SDNVNetGetRequest request = i.getArgument(0);
                return state.sdnZones.values().stream().map(SDNZoneData::toSDNZone).collect(Collectors.toList());
            });
            doAnswer(i -> {
                String name = i.getArgument(0);
                if(!state.sdnVNets.containsKey(name))
                    throwError(404, "Not Found");
                if(state.sdnVNets.get(name).subnets.size() > 0)
                    throwError(409, "Vnet not empty");
                state.sdnVNets.remove(name);
                onChange.accept(state);
                return null;
            }).when(api).deleteSDNVNet(anyString());

            /*
            Subnets
             */
            doAnswer(i -> {
                String vNetName = i.getArgument(0);
                SDNSubnetCreateRequest request = i.getArgument(1);
                SDNVNetData vNetData = state.sdnVNets.get(vNetName);
                if(vNetData == null)
                    throwError(404, "Not Found");
                if(vNetData.subnets.stream().anyMatch(s -> s.cidr.equals(request.getCidr())))
                    throwError(409, "Subnet already exists");
                SDNSubnetData subnetData = new SDNSubnetData();
                subnetData.cidr = request.getCidr();
                subnetData.gateway = request.getGateway();
                subnetData.type = request.getType();
                vNetData.subnets.add(subnetData);
                onChange.accept(state);
                return null;
            }).when(api).createSDNVNetSubnet(anyString(), any(SDNSubnetCreateRequest.class));
            doAnswer(i -> {
                String vNetName = i.getArgument(0);
                String cidr = i.getArgument(1);
                SDNSubnetUpdateRequest request = i.getArgument(2);
                SDNVNetData vNetData = state.sdnVNets.get(vNetName);
                if(vNetData == null)
                    throwError(404, "Not Found");
                SDNSubnetData subnetData = vNetData.subnets.stream().filter(s -> s.cidr.equals(cidr)).findFirst().orElse(null);
                if(subnetData == null)
                    throwError(404, "Not Found");
                if(request.getGateway() != null)
                    subnetData.gateway = request.getGateway();
                if(request.getVnet() != null) {
                    SDNVNetData newVNetData = state.sdnVNets.get(request.getVnet());
                    if(newVNetData == null)
                        throwError(400, "Vnet doesn't exist");
                    vNetData.subnets.remove(subnetData);
                    newVNetData.subnets.add(subnetData);
                }
                if(request.getDelete() != null) {
                    for(String d : request.getDelete().split(",")) {
                        switch (d) {
                            case "gateway":
                                subnetData.gateway = null;
                                break;
                            default:
                                onChange.accept(state);
                                throwError(400, "Cannot delete '" + d + "'");
                        }
                    }
                }
                onChange.accept(state);
                return null;
            }).when(api).updateSDNVNetSubnet(anyString(), anyString(), any(SDNSubnetUpdateRequest.class));
            when(api.getSDNVNetSubnets(anyString())).then(i -> api.getSDNVNetSubnets(i.getArgument(0), new SDNSubnetGetRequest()));
            when(api.getSDNVNetSubnets(anyString(), any(SDNSubnetGetRequest.class))).then(i -> {
                String vNetName = i.getArgument(0);
                SDNSubnetGetRequest request = i.getArgument(1);
                SDNVNetData vNetData = state.sdnVNets.get(vNetName);
                if(vNetData == null)
                    throwError(404, "Not Found");
                return vNetData.subnets.stream().map(SDNSubnetData::toSDNSubnet).collect(Collectors.toList());
            });
            doAnswer(i -> {
                String vNetName = i.getArgument(0);
                String cidr = i.getArgument(1);
                SDNVNetData vNetData = state.sdnVNets.get(vNetName);
                if(vNetData == null)
                    throwError(404, "Not Found");
                SDNSubnetData subnetData = vNetData.subnets.stream().filter(s -> s.cidr.equals(cidr)).findFirst().orElse(null);
                if(subnetData == null)
                    throwError(404, "Not Found");
                vNetData.subnets.remove(subnetData);
                onChange.accept(state);
                return null;
            }).when(api).deleteSDNVNetSubnet(anyString(), anyString());
        } catch (ProxMoxVEException ignored) {}
    }

}
