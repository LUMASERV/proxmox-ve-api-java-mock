package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.apis.QemuVMAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.NodeData;
import com.lumaserv.proxmox.ve.mock.state.QemuVMData;
import com.lumaserv.proxmox.ve.mock.state.TaskData;
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
            doAnswer(i -> {
                //QemuVMResizeRequest request = i.getArgument(0);
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                // TODO
                return "";
            }).when(api).resize(any(QemuVMResizeRequest.class));
        } catch (ProxMoxVEException ignored) {}
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
