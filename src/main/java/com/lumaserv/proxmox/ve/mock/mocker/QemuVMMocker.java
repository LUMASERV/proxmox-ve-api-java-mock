package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.apis.QemuVMAPI;
import com.lumaserv.proxmox.ve.mock.helper.DiskHelper;
import com.lumaserv.proxmox.ve.mock.state.*;
import com.lumaserv.proxmox.ve.mock.state.qemu.*;
import com.lumaserv.proxmox.ve.model.nodes.qemu.QemuVM;
import com.lumaserv.proxmox.ve.request.nodes.qemu.*;

import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class QemuVMMocker extends Mocker {

    public static void mockNodeAPI(NodeAPI nodeAPI, MockState state) {
        try {
            when(nodeAPI.getQemuVMs()).then(i -> state.qemuVMs.values().stream().map(QemuVMMocker::mockQemuVM).collect(Collectors.toList()));
            when(nodeAPI.createQemuVM(any(QemuVMCreateRequest.class))).then(i -> {
                QemuVMCreateRequest request = i.getArgument(0);
                verifyRequiredParam("vmid", request.getId());
                if(request.getId() < 100)
                    throwError(400, "vmid needs to be at least 100");
                if(state.qemuVMs.containsKey(request.getId()))
                    throwError(400, "VM with this id already exists");
                QemuVMData data = new QemuVMData();
                data.node = nodeAPI.getNodeName();
                data.id = request.getId();
                for(String k : request.getAdditionalParameters().keys()) {
                    if(k.startsWith("scsi")) {
                        int n = Integer.parseInt(k.substring(4));
                        if(n < 31) {
                            data.disks.put(k, new DiskData(data.id, k, request.getAdditionalParameters().string(k), state));
                            continue;
                        }
                    }
                    if(k.startsWith("sata")) {
                        int n = Integer.parseInt(k.substring(4));
                        if(n < 6) {
                            data.disks.put(k, new DiskData(data.id, k, request.getAdditionalParameters().string(k), state));
                            continue;
                        }
                    }
                    if(k.startsWith("virtio")) {
                        int n = Integer.parseInt(k.substring(6));
                        if(n < 16) {
                            data.disks.put(k, new DiskData(data.id, k, request.getAdditionalParameters().string(k), state));
                            continue;
                        }
                    }
                    if(k.startsWith("ide")) {
                        int n = Integer.parseInt(k.substring(4));
                        if(n < 31) {
                            data.disks.put(k, new DiskData(data.id, k, request.getAdditionalParameters().string(k), state));
                            continue;
                        }
                    }
                    if(k.startsWith("net")) {
                        int n = Integer.parseInt(k.substring(3));
                        if(n < 32) {
                            data.networks.put(n, new NetworkData(n, request.getAdditionalParameters().string(k), state));
                            continue;
                        }
                    }
                    if(k.startsWith("ipconfig")) {
                        int n = Integer.parseInt(k.substring(8));
                        if(n < 32) {
                            data.ipConfigs.put(n, new IPConfigData(n, request.getAdditionalParameters().string(k)));
                            continue;
                        }
                    }
                }
                // TODO
                state.qemuVMs.put(data.id, data);
                return "";
            });
            when(nodeAPI.qemu(anyInt())).then(i -> mockQemuVMAPI(nodeAPI, state, i.getArgument(0)));
        } catch (ProxMoxVEException ignored) {}
    }

    private static QemuVMAPI mockQemuVMAPI(NodeAPI nodeAPI, MockState state, int id) {
        QemuVMAPI api = mock(QemuVMAPI.class);
        try {
            when(api.getNodeAPI()).thenReturn(nodeAPI);
            when(api.getVmId()).thenReturn(id);
            when(api.start()).then(i -> api.start());
            when(api.start(any(QemuVMStartRequest.class))).then(i -> {
                //QemuVMStartRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if(data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmstart", data.id);
                data.startedAt = System.currentTimeMillis();
                data.started = true;
                task.finish();
                return task.upId;
            });
            when(api.shutdown()).then(i -> api.shutdown((Integer) null));
            when(api.shutdown(anyInt())).then(i -> api.shutdown(new QemuVMShutdownRequest().setTimeout(i.getArgument(0))));
            when(api.shutdown(any(QemuVMShutdownRequest.class))).then(i -> {
                //QemuVMShutdownRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if(!data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmshutdown", data.id);
                data.startedAt = 0;
                data.started = false;
                task.finish();
                return task.upId;
            });
            when(api.stop()).then(i -> api.stop(new QemuVMStopRequest()));
            when(api.stop(any(QemuVMStopRequest.class))).then(i -> {
                //QemuVMStopRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if(!data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmstop", data.id);
                data.startedAt = 0;
                data.started = false;
                task.finish();
                return task.upId;
            });
            when(api.reboot()).then(i -> api.reboot((Integer) null));
            when(api.reboot(anyInt())).then(i -> api.reboot(new QemuVMRebootRequest().setTimeout(i.getArgument(0))));
            when(api.reboot(any(QemuVMRebootRequest.class))).then(i -> {
                //QemuVMRebootRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if(!data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmreboot", data.id);
                data.startedAt = System.currentTimeMillis();
                task.finish();
                return task.upId;
            });
            when(api.delete(any(QemuVMDeleteRequest.class))).then(i -> {
                //QemuVMDeleteRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if(data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmdestroy", data.id);
                task.log.add("Removing image: 1% complete...");
                state.qemuVMs.remove(data.id);
                task.log.add("Removing image: 100% complete...done.");
                task.finish();
                return task.upId;
            });
            when(api.reset()).then(i -> api.reset(new QemuVMResetRequest()));
            when(api.reset(any(QemuVMResetRequest.class))).then(i -> {
                QemuVMData data = getVMData(state, id);
                if(!data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmreset", data.id);
                data.startedAt = System.currentTimeMillis();
                task.finish();
                return task.upId;
            });
            when(api.suspend()).then(i -> api.suspend(new QemuVMSuspendRequest()));
            when(api.suspend(any(QemuVMSuspendRequest.class))).then(i -> {
                QemuVMData data = getVMData(state, id);
                if(!data.started)
                    throwError(409, vmAlreadyRunningMsg(data.id));
                TaskData task = state.createTask(data.node, "qmsuspend", data.id);
                data.started = false;
                data.lock = "suspended";
                task.finish();
                return task.upId;
            });
            when(api.resume()).then(i -> api.resume(new QemuVMResumeRequest()));
            when(api.resume(any(QemuVMResumeRequest.class))).then(i -> {
                QemuVMData data = getVMData(state, id);
                if (data.started)
                    throwError(409, vmAlreadyRunningMsg(id));
                if (!data.lock.equals("suspended"))
                    throwError(409, "Error: VM "+id+" is not suspended");
                TaskData task = state.createTask(data.node, "qmresume", data.id);
                data.started = true;
                data.lock = "";
                task.finish();
                return task.upId;
            });
            when(api.clone(any(QemuVMCloneRequest.class))).then(i -> {
                QemuVMCloneRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                TaskData task = state.createTask(data.node, "qmclone", data.id);
                verifyRequiredParam("vmid", request.getNewId());
                if(request.getNewId() < 100)
                    throwError(400, "vmid needs to be at least 100");
                if(state.qemuVMs.containsKey(request.getNewId()))
                    throwError(400, "VM with this id already exists");
                QemuVMData newData = new QemuVMData();
                newData.id = request.getNewId();
                if (request.getTarget() != null) {
                    NodeData nodeData = state.nodes.get(request.getTarget());
                    if (nodeData == null)
                        Mocker.throwError(400, "Parameter 'target' is invalid (target node not found)");
                    newData.node = request.getTarget();
                } else {
                    newData.node = data.node;
                }
                if(request.getPool() != null) {
                    PoolData pool = state.pools.get(request.getPool());
                    if(pool == null)
                        Mocker.throwError(400, "Parameter 'pool' is invalid (pool not found)");
                    PoolData.Member member = new PoolData.Member();
                    member.type = "vm";
                    member.vmId = newData.id;
                    pool.members.add(member);
                }
                if(request.getFormat() != null && !DiskHelper.isValidFormat(request.getFormat()))
                    Mocker.throwError(400, "Parameter 'format' is invalid");
                newData.agent = data.agent;
                newData.arch = data.arch;
                newData.bootOrder = data.bootOrder;
                newData.bootDisk = data.bootDisk;
                newData.ciCustom = data.ciCustom;
                newData.ciPassword = data.ciPassword;
                newData.ciType = data.ciType;
                newData.ciUser = data.ciUser;
                newData.cores = data.cores;
                newData.cpu = data.cpu;
                newData.description = request.getDescription() != null ? request.getDescription() : data.description;
                newData.lock = "";
                newData.machine = data.machine;
                newData.memory = data.memory;
                newData.name = request.getName() != null ? request.getName() : data.name;
                newData.nameserver = data.nameserver;
                newData.osType = data.osType;
                for(int n : data.networks.keySet()) {
                    NetworkData oldNetwork = data.networks.get(n);
                    NetworkData newNetwork = new NetworkData();
                    newNetwork.bridge = oldNetwork.bridge;
                    newNetwork.model = oldNetwork.model;
                    newNetwork.firewall = oldNetwork.firewall;
                    newNetwork.rate = oldNetwork.rate;
                    newNetwork.linkDown = oldNetwork.linkDown;
                    newNetwork.tag = oldNetwork.tag;
                    newNetwork.generateMac();
                    newData.networks.put(n, newNetwork);
                }
                for(int n : data.ipConfigs.keySet()) {
                    IPConfigData oldIPConfig = data.ipConfigs.get(n);
                    IPConfigData newIPConfig = new IPConfigData();
                    newIPConfig.ip = oldIPConfig.ip;
                    newIPConfig.gateway = oldIPConfig.gateway;
                    newIPConfig.ip6 = oldIPConfig.ip6;
                    newIPConfig.gateway6 = oldIPConfig.gateway6;
                    newData.ipConfigs.put(n, newIPConfig);
                }
                StorageData requestStorage = null;
                if(request.getStorage() != null) {
                    requestStorage = state.storages.get(request.getStorage());
                    if(requestStorage == null)
                        Mocker.throwError(400, "Parameter 'storage' is invalid (target storage not found)");
                }
                for(String diskName : data.disks.keySet()) {
                    if(diskName.startsWith("unused")) // Don't clone unused volumes
                        continue;
                    DiskData oldDisk = data.disks.get(diskName);
                    DiskData newDisk = new DiskData();
                    newDisk.cache = oldDisk.cache;
                    newDisk.cdrom = oldDisk.cdrom;
                    newDisk.discard = oldDisk.discard;
                    newDisk.ssd = oldDisk.ssd;
                    newDisk.readIops = oldDisk.readIops;
                    newDisk.writeIops = oldDisk.writeIops;
                    newDisk.readMbps = oldDisk.readMbps;
                    newDisk.writeMbps = oldDisk.writeMbps;
                    if(newDisk.cdrom) {
                        newDisk.storage = oldDisk.storage;
                        newDisk.volume = oldDisk.volume;
                    } else {
                        StorageData oldStorage = state.storages.get(oldDisk.storage);
                        VolumeData oldVolume = oldStorage.images.get(oldDisk.volume);
                        StorageData newStorage = requestStorage != null ? requestStorage : oldStorage;
                        newDisk.storage = newStorage.name;
                        VolumeData newVolume = new VolumeData();
                        newVolume.format = request.getFormat() != null ? request.getFormat() : oldVolume.format;
                        newVolume.size = oldVolume.size;
                        int n = 0;
                        while(newStorage.images.containsKey("vm-" + newData.id + "-disk-" + n))
                            n++;
                        newVolume.name = "vm-" + newData.id + "-disk-" + n;
                        newStorage.images.put(newVolume.name, newVolume);
                        newDisk.volume = newVolume.name;
                    }
                    newData.disks.put(diskName, newDisk);
                }
                state.qemuVMs.put(request.getNewId(), newData);
                task.finish();
                return task.upId;
            });
            doAnswer(i -> {
                QemuVMResizeRequest request = i.getArgument(0);
                verifyRequiredParam("disk", request.getDisk());
                verifyRequiredParam("size", request.getSize());
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                DiskData diskData = data.disks.get(request.getDisk());
                if(diskData == null || diskData.cdrom)
                    throwError(400, "Unknown disk");
                StorageData storage = state.storages.get(diskData.storage);
                VolumeData volumeData = storage.images.get(diskData.volume);
                TaskData task = state.createTask(data.node, "qmresize", data.id);
                if(request.getSize().startsWith("+")) {
                    volumeData.size = volumeData.size + DiskHelper.parseSize(request.getSize().substring(1));
                } else {
                    double s = DiskHelper.parseSize(request.getSize());
                    if(s < volumeData.size)
                        throwError(400, "Size is smaller than current size");
                    volumeData.size = s;
                }
                task.finish();
                return task.upId;
            }).when(api).resize(any(QemuVMResizeRequest.class));
            doAnswer(i -> {
                QemuVMMoveDiskRequest request = i.getArgument(0);
                verifyRequiredParam("disk", request.getDisk());
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                DiskData diskData = data.disks.get(request.getDisk());
                if(diskData == null)
                    Mocker.throwError(400, "Parameter 'disk' is invalid (disk not found)");
                if(request.getFormat() != null && !DiskHelper.isValidFormat(request.getFormat()))
                    Mocker.throwError(400, "Parameter 'format' is invalid");
                StorageData oldStorage = state.storages.get(diskData.storage);
                VolumeData oldVolume = oldStorage.images.get(diskData.volume);
                int newVMId = data.id;
                StorageData newStorage = oldStorage;
                if(request.getStorage() != null && !diskData.storage.equals(request.getStorage())) {
                    newStorage = state.storages.get(request.getStorage());
                    if(newStorage == null)
                        Mocker.throwError(400, "Parameter 'storage' is invalid (storage not found)");
                }
                if(request.getTargetVMId() != null) {
                    verifyRequiredParam("target-disk", request.getTargetDisk());
                    if(!DiskHelper.isValidDisk(request.getTargetDisk()))
                        Mocker.throwError(400, "Parameter 'target-disk' is invalid");
                    QemuVMData targetVM = state.qemuVMs.get(request.getTargetVMId());
                    if(targetVM == null)
                        Mocker.throwError(400, "Parameter 'target-vmid' is invalid (vm not found)");
                    if(!data.node.equals(targetVM.node))
                        Mocker.throwError(500, "Source and target are on different nodes");
                    if(targetVM.disks.containsKey(request.getTargetDisk()))
                        Mocker.throwError(400, "Parameter 'target-disk' is invalid (disk exists on target vm)");
                    newVMId = targetVM.id;
                }
                VolumeData newVolume = new VolumeData();
                newVolume.format = request.getFormat() != null ? request.getFormat() : oldVolume.format;
                int n = 0;
                while(newStorage.images.containsKey("vm-" + newVMId + "-disk-" + n))
                    n++;
                newVolume.name = "vm-" + newVMId + "-disk-" + n;
                newVolume.size = oldVolume.size;
                newStorage.images.put(newVolume.name, newVolume);
                DiskData newDiskData = new DiskData();
                newDiskData.storage = newStorage.name;
                newDiskData.volume = newVolume.name;
                newDiskData.cache = diskData.cache;
                newDiskData.discard = diskData.discard;
                newDiskData.ssd = diskData.ssd;
                newDiskData.readIops = diskData.readIops;
                newDiskData.writeIops = diskData.writeIops;
                newDiskData.readMbps = diskData.readMbps;
                newDiskData.writeMbps = diskData.writeMbps;
                if(request.getDelete() != null && request.getDelete() > 0) {
                    data.disks.remove(request.getDisk());
                    oldStorage.images.remove(oldVolume.name);
                } else {
                    data.disks.remove(request.getDisk());
                    diskData.cdrom = false;
                    diskData.discard = false;
                    diskData.ssd = false;
                    diskData.readIops = null;
                    diskData.writeIops = null;
                    diskData.readMbps = null;
                    diskData.writeMbps = null;
                    diskData.cache = null;
                    data.disks.put(data.findUnused(), diskData);
                }
                QemuVMData targetVM = state.qemuVMs.get(newVMId);
                targetVM.disks.put(request.getTargetDisk() != null ? request.getTargetDisk() : request.getDisk(), newDiskData);
                return null;
            }).when(api).moveDisk(any(QemuVMMoveDiskRequest.class));
            when(api.migrate(any(QemuVMMigrateRequest.class))).then(i -> {
                QemuVMMigrateRequest request = i.getArgument(0);
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                verifyRequiredParam("target", request.getTarget());
                if(request.getTarget().equals(data.node))
                    Mocker.throwError(400, "VM is already on the target node");
                if(!state.nodes.containsKey(request.getTarget()))
                    Mocker.throwError(400, "Parameter 'target' is invalid (target node not found)");
                TaskData task = state.createTask(data.node, "qmigrate", data.id);
                data.node = request.getTarget();
                task.finish();
                return task.upId;
            });
        } catch (ProxMoxVEException ignored) {}
        QemuVMConfigMocker.mockQemuVMAPI(api, id, state);
        QemuVMFirewallMocker.mockQemuVMAPI(api, id, state);
        return api;
    }

    public static QemuVM mockQemuVM(QemuVMData data) {
        QemuVM vm = new QemuVM()
                .setId(data.id)
                .setName(data.name)
                .setStatus(data.started ? "running" : "stopped")
                .setUptime(data.started ? ((int)((System.currentTimeMillis() - data.startedAt) / 1000)) : null);
        return vm;
    }

    private static String vmAlreadyRunningMsg(int id) {
        return "Error: VM " + id + " already running";
    }

    private static QemuVMData getVMData (MockState state, int id) throws ProxMoxVEException {
        QemuVMData data = state.qemuVMs.get(id);
        if(data == null)
            throwError(404, "Not Found");
        return data;
    }

}
