package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.apis.QemuVMAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;
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
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                if(data.started)
                    throwError(409, "VM already running");
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
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                if(!data.started)
                    throwError(409, "VM not running");
                TaskData task = state.createTask(data.node, "qmshutdown", data.id);
                data.startedAt = 0;
                data.started = false;
                task.finish();
                return task.upId;
            });
            when(api.stop()).then(i -> api.stop(new QemuVMStopRequest()));
            when(api.stop(any(QemuVMStopRequest.class))).then(i -> {
                //QemuVMStopRequest request = i.getArgument(0);
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                if(!data.started)
                    throwError(409, "VM not running");
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
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                if(!data.started)
                    throwError(409, "VM not running");
                TaskData task = state.createTask(data.node, "qmreboot", data.id);
                data.startedAt = System.currentTimeMillis();
                task.finish();
                return task.upId;
            });
            when(api.delete(any(QemuVMDeleteRequest.class))).then(i -> {
                //QemuVMDeleteRequest request = i.getArgument(0);
                QemuVMData data = state.qemuVMs.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                if(data.started)
                    throwError(409, "VM is running");
                TaskData task = state.createTask(data.node, "qmdestroy", data.id);
                task.log.add("Removing image: 1% complete...");
                state.qemuVMs.remove(data.id);
                task.log.add("Removing image: 100% complete...done.");
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

}
