package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.PoolData;
import com.lumaserv.proxmox.ve.model.pools.Pool;
import com.lumaserv.proxmox.ve.request.pools.PoolCreateRequest;
import com.lumaserv.proxmox.ve.request.pools.PoolUpdateRequest;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class PoolMocker extends Mocker {

    public static void mockClient(ProxMoxVEClient client, MockState state, Consumer<MockState> onChange) {
        try {
            when(client.getPools()).then(i -> state.pools.values().stream().map(PoolMocker::mockPool).collect(Collectors.toList()));
            when(client.getPool(anyString())).then(i -> {
                String id = i.getArgument(0);
                if(!state.pools.containsKey(id))
                    throwError(404, "Not Found");
                return mockPool(state.pools.get(id));
            });
            doAnswer(i -> {
                client.createPool(new PoolCreateRequest().setId(i.getArgument(0)));
                return null;
            }).when(client).createPool(anyString());
            doAnswer(i -> {
                client.createPool(new PoolCreateRequest().setId(i.getArgument(0)).setComment(i.getArgument(1)));
                return null;
            }).when(client).createPool(anyString(), anyString());
            doAnswer(i -> {
                PoolCreateRequest request = i.getArgument(0);
                verifyRequiredParam("poolid", request.getId());
                if(state.pools.containsKey(request.getId()))
                    throwError(409, "Pool with id '" + request.getId() + "' already exists");
                PoolData data = new PoolData();
                data.id = request.getId();
                data.comment = request.getComment();
                state.pools.put(data.id, data);
                onChange.accept(state);
                return null;
            }).when(client).createPool(any(PoolCreateRequest.class));
            doAnswer(i -> {
                String id = i.getArgument(0);
                PoolUpdateRequest request = i.getArgument(1);
                if(!state.pools.containsKey(id))
                    throwError(404, "Not Found");
                PoolData poolData = state.pools.get(id);
                if(request.getComment() != null)
                    poolData.comment = request.getComment();
                if(request.getDelete() != null && request.getDelete() == 1) {
                    if(request.getVMs() != null && request.getVMs().length() > 0) {
                        Integer[] vmids = Stream.of(request.getVMs().split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                        for(int vmid : vmids) {
                            if(poolData.members.stream().noneMatch(m -> m.type.equals("vm") && m.vmId == vmid)) {
                                onChange.accept(state);
                                throwError(409, "VM not in pool");
                            }
                        }
                        for(int vmid : vmids) {
                            PoolData.Member member = poolData.members.stream().filter(m -> m.type.equals("vm") && m.vmId == vmid).findFirst().get();
                            poolData.members.remove(member);
                        }
                    }
                    if(request.getStorages() != null && request.getStorages().length() > 0) {
                        String[] storageIds = request.getStorages().split(",");
                        for(String storageId : storageIds) {
                            if(poolData.members.stream().noneMatch(m -> m.type.equals("storage") && m.name.equals(storageId))) {
                                onChange.accept(state);
                                throwError(409, "Storage not in pool");
                            }
                        }
                        for(String storageId : storageIds) {
                            PoolData.Member member = poolData.members.stream().filter(m -> m.type.equals("storage") && m.name.equals(storageId)).findFirst().get();
                            poolData.members.remove(member);
                        }
                    }
                } else {
                    if(request.getVMs() != null && request.getVMs().length() > 0) {
                        Integer[] vmids = Stream.of(request.getVMs().split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                        for(int vmid : vmids) {
                            if(state.pools.values().stream().anyMatch(p -> p.members.stream().anyMatch(m -> m.type.equals("vm") && m.vmId == vmid))) {
                                onChange.accept(state);
                                throwError(409, "VM already in a pool");
                            }
                        }
                        for(int vmid : vmids) {
                            PoolData.Member member = new PoolData.Member();
                            member.type = "vm";
                            member.name = "qemu/" + vmid;
                            member.vmId = vmid;
                            poolData.members.add(member);
                        }
                    }
                    if(request.getStorages() != null && request.getStorages().length() > 0) {
                        String[] storageIds = request.getStorages().split(",");
                        for(String storageId : storageIds) {
                            if(state.pools.values().stream().anyMatch(p -> p.members.stream().anyMatch(m -> m.type.equals("storage") && m.name.equals(storageId)))) {
                                onChange.accept(state);
                                throwError(409, "Storage already in pool");
                            }
                        }
                        for(String storageId : storageIds) {
                            PoolData.Member member = new PoolData.Member();
                            member.type = "storage";
                            member.name = storageId;
                            poolData.members.add(member);
                        }
                    }
                }
                onChange.accept(state);
                return null;
            }).when(client).updatePool(anyString(), any(PoolUpdateRequest.class));
            doAnswer(i -> {
                String id = i.getArgument(0);
                if(!state.pools.containsKey(id))
                    throwError(404, "Not Found");
                state.pools.remove(id);
                onChange.accept(state);
                return null;
            }).when(client).deletePool(anyString());
        } catch (ProxMoxVEException ignored) {}
    }

    public static Pool mockPool(PoolData data) {
        return new Pool()
                .setId(data.id)
                .setComment(data.comment)
                .setMembers(data.members.stream().map(PoolMocker::mockPoolMember).toArray(Pool.Member[]::new));
    }

    public static Pool.Member mockPoolMember(PoolData.Member data) {
        return new Pool.Member()
                .setType(data.type)
                .setVmId(data.vmId)
                .setName(data.name)
                .setNode(data.node);
    }

}
