package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEClient;
import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.mock.helper.RRDHelper;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.NodeData;
import com.lumaserv.proxmox.ve.mock.state.TaskData;
import com.lumaserv.proxmox.ve.model.Task;
import com.lumaserv.proxmox.ve.model.TaskLogLine;
import com.lumaserv.proxmox.ve.model.nodes.Node;
import com.lumaserv.proxmox.ve.model.nodes.NodeRRDFrame;
import com.lumaserv.proxmox.ve.request.nodes.RRDDataGetRequest;
import com.lumaserv.proxmox.ve.request.nodes.TaskGetRequest;
import com.lumaserv.proxmox.ve.request.nodes.TaskLogRequest;
import com.lumaserv.proxmox.ve.request.nodes.storage.NodeStorageGetRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class NodeMocker extends Mocker {

    public static void mockClient(ProxMoxVEClient client, MockState state, Consumer<MockState> onChange) {
        try {
            when(client.getNodes()).then(i -> state.nodes.values().stream().map(NodeMocker::mockNode).collect(Collectors.toList()));
            when(client.nodes(anyString())).then(i -> mockNodeAPI(client, state, i.getArgument(0), onChange));
        } catch (ProxMoxVEException ignored) {}
    }

    public static NodeAPI mockNodeAPI(ProxMoxVEClient client, MockState state, String name, Consumer<MockState> onChange) {
        NodeAPI api = mock(NodeAPI.class);
        NodeStorageMocker.mockNodeAPI(api, state);
        try {
            when(api.getClient()).thenReturn(client);
            when(api.getNodeName()).thenReturn(name);
            when(api.getTask(anyString())).then(i -> {
                String upid = i.getArgument(0);
                TaskData data = state.tasks.stream().filter(t -> t.upId.equals(upid)).findFirst().orElse(null);
                if(data == null)
                    throwError(404, "Not Found");
                return data.toTask();
            });
            when(api.getTaskLog(anyString())).then(i -> api.getTaskLog(i.getArgument(0), new TaskLogRequest()));
            when(api.getTaskLog(anyString(), any(TaskLogRequest.class))).then(i -> {
                String upid = i.getArgument(0);
                TaskLogRequest request = i.getArgument(1);
                TaskData data = state.tasks.stream().filter(t -> t.node.equals(name) && t.upId.equals(upid)).findFirst().orElse(null);
                if(data == null)
                    throwError(404, "Not Found");
                List<TaskLogLine> lines = new ArrayList<>();
                int start = request.getStart() != null ? request.getStart() : 0;
                if(start >= data.log.size())
                    return lines;
                int limit = request.getLimit() != null ? request.getLimit() : Integer.MAX_VALUE;
                for(int j=start; j<Math.min(data.log.size(), start + limit); j++)
                    lines.add(new TaskLogLine().setLine(j + 1).setText(data.log.get(j)));
                return lines;
            });
            when(api.getTasks()).then(i -> api.getTasks(new TaskGetRequest()));
            when(api.getTasks(any(TaskGetRequest.class))).then(i -> {
                TaskGetRequest request = i.getArgument(0);
                Stream<TaskData> taskStream = state.tasks.stream().filter(t -> t.node.equals(name));
                if(request.getSince() != null)
                    taskStream = taskStream.filter(t -> t.start >= request.getSince());
                if(request.getUntil() != null)
                    taskStream = taskStream.filter(t -> t.start <= request.getUntil());
                if(request.getVmId() != null)
                    taskStream = taskStream.filter(t -> t.vmId != null && t.vmId == request.getVmId());
                if(request.getSource() != null && request.getSource().equals("active"))
                    taskStream = taskStream.filter(t -> t.end == 0);
                if(request.getErrors() != null && request.getErrors() > 0)
                    taskStream = taskStream.filter(t -> t.error);
                if(request.getUserFilter() != null)
                    taskStream = taskStream.filter(t -> t.user.equals(request.getUserFilter()));
                if(request.getTypeFilter() != null)
                    taskStream = taskStream.filter(t -> t.type.equals(request.getTypeFilter()));
                if(request.getStatusFilter() != null) {
                    List<String> states = Arrays.asList(request.getStatusFilter().split(","));
                    taskStream = taskStream.filter(t -> states.contains(t.status));
                }
                List<TaskData> taskDatas = taskStream.collect(Collectors.toList());
                List<Task> tasks = new ArrayList<>();
                int start = request.getStart() != null ? request.getStart().intValue() : 0;
                if(start >= taskDatas.size())
                    return tasks;
                int limit = request.getLimit() != null ? request.getLimit() : Integer.MAX_VALUE;
                for(int j=start; j<Math.min(taskDatas.size(), start + limit); j++)
                    tasks.add(taskDatas.get(j).toTask());
                return tasks;
            });
            doAnswer(i -> {
                String upid = i.getArgument(0);
                TaskData data = state.tasks.stream().filter(t -> t.upId.equals(upid)).findFirst().orElse(null);
                if(data == null)
                    throwError(404, "Not Found");
                data.status = "stopped";
                data.end = System.currentTimeMillis();
                onChange.accept(state);
                return null;
            }).when(api).stopTask(anyString());
            when(api.getRRDData(any(RRDDataGetRequest.class))).then(i -> {
                RRDDataGetRequest request = i.getArgument(0);
                List<NodeRRDFrame> frames = new ArrayList<>();
                Random random = new Random();
                double memorySeed = random.nextDouble() * 1000;
                double cpuSeed = random.nextDouble() * 1000;
                for(int f=0; f<75; f++) {
                    frames.add(new NodeRRDFrame()
                            .setMemoryTotal(65536L)
                            .setMemoryUsed(RRDHelper.noise(memorySeed + f) * 65536)
                            .setMaxCpu(12d)
                            .setCpu(RRDHelper.noise(cpuSeed + f) * 12)
                            .setNetIn(0d)
                            .setNetOut(0d)
                            .setLoadAverage(0d)
                            .setSwapTotal(1024L)
                            .setSwapUsed(0L)
                            .setRootTotal(128L)
                            .setRootUsed(0d)
                    );
                }
                return frames;
            });
        } catch (ProxMoxVEException ignored) {}
        QemuVMMocker.mockNodeAPI(api, state, onChange);
        return api;
    }

    public static Node mockNode(NodeData data) {
        return new Node()
                .setName(data.name);
    }

}
