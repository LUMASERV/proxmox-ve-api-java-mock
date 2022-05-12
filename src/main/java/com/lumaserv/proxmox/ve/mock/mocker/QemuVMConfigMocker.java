package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.QemuVMAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.TaskData;
import com.lumaserv.proxmox.ve.mock.state.qemu.*;
import com.lumaserv.proxmox.ve.model.nodes.qemu.QemuVMConfig;
import com.lumaserv.proxmox.ve.request.nodes.qemu.QemuVMConfigGetRequest;
import com.lumaserv.proxmox.ve.request.nodes.qemu.QemuVMConfigUpdateRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QemuVMConfigMocker extends Mocker {

    public static void mockQemuVMAPI(QemuVMAPI api, int id, MockState state) {
        try {
            when(api.getConfig()).then(i -> api.getConfig(new QemuVMConfigGetRequest()));
            when(api.getConfig(any(QemuVMConfigGetRequest.class))).then(i -> {
                //QemuVMConfigGetRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                QemuVMConfig config = new QemuVMConfig()
                        .setAgent(data.agent)
                        .setArch(data.arch)
                        .setBootOrder(data.bootOrder)
                        .setBootDisk(data.bootDisk)
                        .setCiCustom(data.ciCustom == null ? null : data.ciCustom.toOptionString())
                        .setCiPassword(data.ciPassword)
                        .setCiType(data.ciType)
                        .setCiUser(data.ciUser)
                        .setCores(data.cores)
                        .setCpu(data.cpu)
                        .setDescription(data.description)
                        .setLock(data.lock)
                        .setMachine(data.machine)
                        .setMemory(data.memory)
                        .setName(data.name)
                        .setNameserver(data.nameserver)
                        .setOsType(data.osType)
                        .setSearchDomain(data.searchDomain)
                        .setSshKeys(data.sshKeys)
                        .setStartup(data.startup);
                for(String k : data.disks.keySet()) {
                    DiskData diskData = data.disks.get(k);
                    if(k.startsWith("scsi")) {
                        config.getScsi().put(Integer.parseInt(k.substring(4)), diskData.toOptionsString(state));
                    } else if(k.startsWith("sata")) {
                        config.getSata().put(Integer.parseInt(k.substring(4)), diskData.toOptionsString(state));
                    } else if(k.startsWith("ide")) {
                        config.getIde().put(Integer.parseInt(k.substring(3)), diskData.toOptionsString(state));
                    } else if(k.startsWith("virtio")) {
                        config.getVirtio().put(Integer.parseInt(k.substring(6)), diskData.toOptionsString(state));
                    }
                }
                for(Integer n : data.networks.keySet()) {
                    NetworkData networkData = data.networks.get(n);
                    config.getNet().put(n, networkData.toOptionString());
                }
                for(Integer n : data.ipConfigs.keySet()) {
                    IPConfigData ipConfigData = data.ipConfigs.get(n);
                    config.getNet().put(n, ipConfigData.toOptionString());
                }
                return config;
            });
            when(api.getCurrentConfig()).then(i -> api.getConfig(new QemuVMConfigGetRequest().setCurrent(true)));
            doAnswer(i -> {
                QemuVMConfigUpdateRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if (request.getAgent() != null)
                    data.agent = request.getAgent();
                if (request.getArch() != null)
                    data.arch = request.getArch();
                if (request.getBootOrder() != null)
                    data.bootOrder = request.getBootOrder();
                if (request.getBootDisk() != null)
                    data.bootDisk = request.getBootDisk();
                if (request.getCiCustom() != null)
                    data.ciCustom = new CICustom(request.getCiCustom(), state);
                if (request.getCiPassword() != null)
                    data.ciPassword = request.getCiPassword();
                if (request.getCiType() != null)
                    data.ciType = request.getCiType();
                if (request.getCiUser() != null)
                    data.ciUser = request.getCiUser();
                if (request.getCores() != null)
                    data.cores = request.getCores();
                if (request.getCpu() != null)
                    data.cpu = request.getCpu();
                if (request.getDescription() != null)
                    data.description = request.getDescription();
                if (request.getLock() != null)
                    data.lock = request.getLock();
                if (request.getMachine() != null)
                    data.machine = request.getMachine();
                if (request.getMemory() != null)
                    data.memory = request.getMemory();
                if (request.getName() != null)
                    data.name = request.getName();
                if (request.getNameserver() != null)
                    data.nameserver = request.getNameserver();
                if (request.getOsType() != null)
                    data.osType = request.getOsType();
                if (request.getSearchDomain() != null)
                    data.searchDomain = request.getSearchDomain();
                if (request.getSshKeys() != null)
                    data.sshKeys = request.getSshKeys();
                if (request.getStartup() != null)
                    data.startup = request.getStartup();
                // TODO Implement Disk Creation
                if (request.getDelete() != null) {
                    for(String k : request.getDelete().split(",")) {
                        if(k.startsWith("scsi") || k.startsWith("sata") || k.startsWith("ide") || k.startsWith("virtio")) {
                            DiskData diskData = data.disks.get(k);
                            if(diskData != null) {
                                if(!diskData.cdrom) {
                                    int unused = 0;
                                    while (data.unused.containsKey(unused))
                                        unused++;
                                    data.unused.put(unused, diskData.storage + ":" + diskData.volume);
                                }
                                data.disks.remove(k);
                            }
                        }
                        if(k.startsWith("net")) {
                            int n = Integer.parseInt(k.substring(3));
                            if(data.networks.containsKey(n))
                                data.networks.remove(n);
                            if(data.ipConfigs.containsKey(n))
                                data.ipConfigs.remove(n);
                        }
                        if(k.startsWith("ipconfig")) {
                            int n = Integer.parseInt(k.substring(8));
                            if(data.ipConfigs.containsKey(n))
                                data.ipConfigs.remove(n);
                        }
                        switch (k) {
                            case "sshkeys":
                                data.sshKeys = null;
                                break;
                            case "description":
                                data.description = null;
                                break;
                            case "cicustom":
                                data.ciCustom = null;
                                break;
                            case "cipassword":
                                data.ciPassword = null;
                                break;
                            case "ciuser":
                                data.ciUser = null;
                                break;
                            case "citype":
                                data.ciType = null;
                                break;
                            case "nameserver":
                                data.nameserver = null;
                                break;
                            case "searchdomain":
                                data.searchDomain = null;
                                break;
                        }
                    }
                }
                return null;
            }).when(api).updateConfigSync(any(QemuVMConfigUpdateRequest.class));
            when(api.updateConfig(any(QemuVMConfigUpdateRequest.class))).then(i -> {
                QemuVMData data = getVMData(state, id);
                TaskData task = state.createTask(data.node, "qmupdateconfig", data.id);
                api.updateConfigSync(i.getArgument(0));
                task.finish();
                return task.upId;
            });
            when(api.getSnapshotConfig(anyString())).then(i -> api.getConfig());
        } catch (ProxMoxVEException ignored) {}
    }

    private static QemuVMData getVMData (MockState state, int id) throws ProxMoxVEException {
        QemuVMData data = state.qemuVMs.get(id);
        if(data == null)
            throwError(404, "Not Found");
        return data;
    }

}
