package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.apis.QemuVMAPI;
import com.lumaserv.proxmox.ve.mock.state.*;
import com.lumaserv.proxmox.ve.mock.state.qemu.DiskData;
import com.lumaserv.proxmox.ve.mock.state.qemu.IPConfigData;
import com.lumaserv.proxmox.ve.mock.state.qemu.NetworkData;
import com.lumaserv.proxmox.ve.mock.state.qemu.QemuVMData;
import com.lumaserv.proxmox.ve.model.nodes.qemu.QemuVM;
import com.lumaserv.proxmox.ve.model.nodes.qemu.QemuVMConfig;
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
                        throwError(404, "Node " + request.getTarget() + " does not exist");
                    newData.node = request.getTarget();
                } else {
                    newData.node = data.node;
                }
                newData.acpi = data.acpi;
                newData.agent = data.agent;
                newData.arch = data.arch;
                newData.args = data.args;
                newData.audio = data.audio;
                newData.autostart = data.autostart;
                newData.balloon = data.balloon;
                newData.bios = data.bios;
                newData.bootOrder = data.bootOrder;
                newData.bootDisk = data.bootDisk;
                newData.ciCustom = data.ciCustom;
                newData.ciPassword = data.ciPassword;
                newData.ciType = data.ciType;
                newData.ciUser = data.ciUser;
                newData.cores = data.cores;
                newData.cpu = data.cpu;
                newData.cpuLimit = data.cpuLimit;
                newData.cpuUnits = data.cpuUnits;
                newData.description = request.getDescription() != null ? request.getDescription() : data.description;
                newData.efiDisk = data.efiDisk;
                newData.hookScript = data.hookScript;
                newData.localTime = data.localTime;
                newData.lock = "";
                newData.machine = data.machine;
                newData.memory = data.memory;
                newData.name = request.getName() != null ? request.getName() : data.name;
                newData.nameserver = data.nameserver;
                newData.startOnBoot = data.startOnBoot;
                newData.osType = data.osType;
                state.qemuVMs.put(request.getNewId(), newData);
                task.finish();
                return task.upId;
            });
            when(api.getConfig()).then(i -> api.getConfig(new QemuVMConfigGetRequest()));
            when(api.getConfig(any(QemuVMConfigGetRequest.class))).then(i -> {
                //QemuVMConfigGetRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                return new QemuVMConfig()
                        .setAcpi(data.acpi)
                        .setAgent(data.agent)
                        .setArch(data.arch)
                        .setArgs(data.args)
                        .setAudio(data.audio)
                        .setAutostart(data.autostart)
                        .setBalloon(data.balloon)
                        .setBios(data.bios)
                        .setBootOrder(data.bootOrder)
                        .setBootDisk(data.bootDisk)
                        .setCiCustom(data.ciCustom)
                        .setCiPassword(data.ciPassword)
                        .setCiType(data.ciType)
                        .setCiUser(data.ciUser)
                        .setCores(data.cores)
                        .setCpu(data.cpu)
                        .setCpuLimit(data.cpuLimit)
                        .setCpuUnits(data.cpuUnits)
                        .setDescription(data.description)
                        .setEfiDisk(data.efiDisk)
                        .setFreeze(data.freeze)
                        .setHookScript(data.hookScript)
                        .setHotplug(data.hotplug)
                        .setHugepages(data.hugepages)
                        .setInterVMSharedMemory(data.interVMSharedMemory)
                        .setKeepHugepages(data.keepHugepages)
                        .setKeyboard(data.keyboard)
                        .setKvm(data.kvm)
                        .setLocalTime(data.localTime)
                        .setLock(data.lock)
                        .setMachine(data.machine)
                        .setMemory(data.memory)
                        .setMigrateDowntime(data.migrateDowntime)
                        .setMigrateSpeed(data.migrateSpeed)
                        .setName(data.name)
                        .setNameserver(data.nameserver)
                        .setNuma(data.numa)
                        .setStartOnBoot(data.startOnBoot)
                        .setOsType(data.osType)
                        .setProtection(data.protection)
                        .setAllowReboot(data.allowReboot)
                        .setRandomNumberGenerator(data.randomNumberGenerator)
                        .setScsiHw(data.scsiHw)
                        .setSearchDomain(data.searchDomain)
                        .setShares(data.shares)
                        .setSmbios1(data.smbios1)
                        .setSockets(data.sockets)
                        .setSpiceEnhancements(data.spiceEnhancements)
                        .setSshKeys(data.sshKeys)
                        .setStartDate(data.startDate)
                        .setStartup(data.startup)
                        .setTablet(data.tablet)
                        .setTags(data.tags)
                        .setTimeDriftFix(data.timeDriftFix)
                        .setEnableTemplate(data.enableTemplate)
                        .setVcpus(data.vcpus)
                        .setVga(data.vga)
                        .setVmGenId(data.vmGenId)
                        .setVmStateStorage(data.vmStateStorage)
                        .setWatchdog(data.watchdog);
            });
            when(api.getCurrentConfig()).then(i -> api.getConfig(new QemuVMConfigGetRequest().setCurrent(true)));
            doAnswer(i -> {
                QemuVMConfigUpdateRequest request = i.getArgument(0);
                QemuVMData data = getVMData(state, id);
                if (request.getAcpi() != null)
                    data.acpi = request.getAcpi();
                if (request.getAgent() != null)
                    data.agent = request.getAgent();
                if (request.getArch() != null)
                    data.arch = request.getArch();
                if (request.getArgs() != null)
                    data.args = request.getArgs();
                if (request.getAudio() != null)
                    data.audio = request.getAudio();
                if (request.getAutostart() != null)
                    data.autostart = request.getAutostart();
                if (request.getBalloon() != null)
                    data.balloon = request.getBalloon();
                if (request.getBios() != null)
                    data.bios = request.getBios();
                if (request.getBootOrder() != null)
                    data.bootOrder = request.getBootOrder();
                if (request.getBootDisk() != null)
                    data.bootDisk = request.getBootDisk();
                if (request.getCiCustom() != null)
                    data.ciCustom = request.getCiCustom();
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
                if (request.getCpuLimit() != null)
                    data.cpuLimit = request.getCpuLimit();
                if (request.getCpuUnits() != null)
                    data.cpuUnits = request.getCpuUnits();
                if (request.getDescription() != null)
                    data.description = request.getDescription();
                if (request.getEfiDisk() != null)
                    data.efiDisk = request.getEfiDisk();
                if (request.getFreeze() != null)
                    data.freeze = request.getFreeze();
                if (request.getHookScript() != null)
                    data.hookScript = request.getHookScript();
                if (request.getHotplug() != null)
                    data.hotplug = request.getHotplug();
                if (request.getHugepages() != null)
                    data.hugepages = request.getHugepages();
                if (request.getInterVMSharedMemory() != null)
                    data.interVMSharedMemory = request.getInterVMSharedMemory();
                if (request.getKeepHugepages() != null)
                    data.keepHugepages = request.getKeepHugepages();
                if (request.getKeyboard() != null)
                    data.keyboard = request.getKeyboard();
                if (request.getKvm() != null)
                    data.kvm = request.getKvm();
                if (request.getLocalTime() != null)
                    data.localTime = request.getLocalTime();
                if (request.getLock() != null)
                    data.lock = request.getLock();
                if (request.getMachine() != null)
                    data.machine = request.getMachine();
                if (request.getMemory() != null)
                    data.memory = request.getMemory();
                if (request.getMigrateDowntime() != null)
                    data.migrateDowntime = request.getMigrateDowntime();
                if (request.getMigrateSpeed() != null)
                    data.migrateSpeed = request.getMigrateSpeed();
                if (request.getName() != null)
                    data.name = request.getName();
                if (request.getNameserver() != null)
                    data.nameserver = request.getNameserver();
                if (request.getNuma() != null)
                    data.numa = request.getNuma();
                if (request.getStartOnBoot() != null)
                    data.startOnBoot = request.getStartOnBoot()+"";
                if (request.getOsType() != null)
                    data.osType = request.getOsType();
                if (request.getProtection() != null)
                    data.protection = request.getProtection();
                if (request.getAllowReboot() != null)
                    data.allowReboot = request.getAllowReboot();
                if (request.getRandomNumberGenerator() != null)
                    data.randomNumberGenerator = request.getRandomNumberGenerator();
                if (request.getScsiHw() != null)
                    data.scsiHw = request.getScsiHw();
                if (request.getSearchDomain() != null)
                    data.searchDomain = request.getSearchDomain();
                if (request.getShares() != null)
                    data.shares = request.getShares();
                if (request.getSmbios1() != null)
                    data.smbios1 = request.getSmbios1();
                if (request.getSockets() != null)
                    data.sockets = request.getSockets();
                if (request.getSpiceEnhancements() != null)
                    data.spiceEnhancements = request.getSpiceEnhancements();
                if (request.getSshKeys() != null)
                    data.sshKeys = request.getSshKeys();
                if (request.getStartDate() != null)
                    data.startDate = request.getStartDate();
                if (request.getStartup() != null)
                    data.startup = request.getStartup();
                if (request.getTablet() != null)
                    data.tablet = request.getTablet();
                if (request.getTags() != null)
                    data.tags = request.getTags();
                if (request.getTimeDriftFix() != null)
                    data.timeDriftFix = request.getTimeDriftFix();
                if (request.getEnableTemplate() != null)
                    data.enableTemplate = request.getEnableTemplate();
                if (request.getVcpus() != null)
                    data.vcpus = request.getVcpus();
                if (request.getVga() != null)
                    data.vga = request.getVga();
                if (request.getVmGenId() != null)
                    data.vmGenId = request.getVmGenId();
                if (request.getVmStateStorage() != null)
                    data.vmStateStorage = request.getVmStateStorage();
                if (request.getWatchdog() != null)
                    data.watchdog = request.getWatchdog();
                return null;
            }).when(api).updateConfigSync(any(QemuVMConfigUpdateRequest.class));
            when(api.updateConfig(any(QemuVMConfigUpdateRequest.class))).then(i -> {
                QemuVMData data = getVMData(state, id);
                TaskData task = state.createTask(data.node, "qmupdateconfig", data.id);
                api.updateConfigSync(i.getArgument(0));
                task.finish();
                return task.upId;
            });
            doAnswer(i -> {
                //QemuVMResizeRequest request = i.getArgument(0);
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                // TODO
                return "";
            }).when(api).resize(any(QemuVMResizeRequest.class));
        } catch (ProxMoxVEException ignored) {}
        QemuVMFirewallMocker.mockQemuVMAPI(api, id, state);
        return null;
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
