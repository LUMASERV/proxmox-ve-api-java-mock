package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.NodeAPI;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.StorageData;
import com.lumaserv.proxmox.ve.mock.state.qemu.VolumeData;
import com.lumaserv.proxmox.ve.model.storage.Storage;
import com.lumaserv.proxmox.ve.model.storage.StorageVolume;
import com.lumaserv.proxmox.ve.request.nodes.storage.NodeStorageGetRequest;
import com.lumaserv.proxmox.ve.request.nodes.storage.StorageVolumeGetRequest;

import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class NodeStorageMocker extends Mocker {
    public static void mockNodeAPI (NodeAPI api, MockState state) {
        try {
            when(api.getStorages()).thenAnswer(i -> api.getStorages(new NodeStorageGetRequest()));
            when(api.getStorages(any(NodeStorageGetRequest.class))).then(i -> {
                //NodeStorageGetRequest request = i.getArgument(0);
                return state.storages.values().stream().map(NodeStorageMocker::mockStorage).collect(Collectors.toList());
            });
            when(api.getStorageVolumes(anyString())).then(i -> api.getStorageVolumes(i.getArgument(0), new StorageVolumeGetRequest()));
            when(api.getStorageVolumes(anyString(), any(StorageVolumeGetRequest.class))).then(i -> {
                String id = i.getArgument(0);
                //StorageVolumeGetRequest request = i.getArgument(1);
                StorageData data = state.storages.get(id);
                if(data == null)
                    throwError(404, "Not Found");
                return data.getVolumes().entrySet().stream().map(entry -> mockStorageVolume(entry.getKey(), entry.getValue()));
            });
            when(api.getStorageVolume(anyString(), anyString())).then(i -> {
                String id = i.getArgument(0);
                String volumeId = i.getArgument(1);
                StorageData storage = state.storages.get(id);
                if(storage == null)
                    throwError(404, "Not Found");
                VolumeData volume = storage.getVolumes().get(volumeId);
                if (volume == null)
                    throwError(404, "Not Found");
                return mockStorageVolume(volumeId, volume);
            });
            /*
            when(api.createStorageVolume(anyString(), any(StorageVolumeCreateRequest.class))).thenAnswer(i -> {
                String id = i.getArgument(0);
                StorageVolumeCreateRequest request = i.getArgument(1);
                verifyRequiredParam("filename", request);
                verifyRequiredParam("size", request);
                verifyRequiredParam("vmid", request);
                StorageData storage = state.storages.get(id);
                if(storage == null)
                    throwError(404, "Not Found");
                QemuVMData vm = state.qemuVMs.get(request.getVmId());
                if (vm == null)
                    throwError(404, "Not Found");
                TaskData task = state.createTask(api.getNodeName(), "addvol", request.getVmId());
                VolumeData volumeData = new VolumeData();
                volumeData.format = request.getFormat();
                volumeData.name = request.getFileName();
                volumeData.size = Double.parseDouble(request.getSize());
                // TODO: Add to volumes?
                return mockStorageVolume("1337", volumeData); // TODO: Change ID to generated
            });
            */
        } catch (ProxMoxVEException ignored) {}
    }

    private static Storage mockStorage (StorageData data) {
        return new Storage()
                .setActive(1)
                .setName(data.name)
                .setType(data.type);
    }

    private static StorageVolume mockStorageVolume(String id, VolumeData data) {
        return new StorageVolume()
                .setId(id)
                .setName(data.name)
                .setFormat(data.format)
                .setSize((long) data.size);
    }
}
