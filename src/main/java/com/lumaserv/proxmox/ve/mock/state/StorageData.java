package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.mock.helper.DiskHelper;
import com.lumaserv.proxmox.ve.mock.state.qemu.VolumeData;
import org.javawebstack.abstractdata.mapper.annotation.MapperOptions;

import java.util.*;

public class StorageData {

    public String name;
    public String type;
    @MapperOptions(generic = { String.class, VolumeData.class })
    public Map<String, VolumeData> images = new HashMap<>();
    @MapperOptions(generic = { String.class, VolumeData.class })
    public Map<String, VolumeData> isos = new HashMap<>();
    @MapperOptions(generic = { String.class, VolumeData.class })
    public Map<String, VolumeData> snippets = new HashMap<>();
    @MapperOptions(generic = { String.class, VolumeData.class })
    public Map<String, VolumeData> backups = new HashMap<>();

    public String createImage(String size, int vmId, String format) {
        return createImage(DiskHelper.parseSize(size), vmId, format);
    }

    public String createImage(double size, int vmId, String format) {
        String namePrefix = "vm-" + vmId + "-disk-";
        int n = images.values().stream().filter(v -> v.name.startsWith(namePrefix)).map(v -> Integer.parseInt(v.name.substring(namePrefix.length()))).max(Comparator.comparingInt(i -> i)).orElse(-1) + 1;
        VolumeData volumeData = new VolumeData();
        volumeData.name = namePrefix + n;
        volumeData.format = format;
        volumeData.size = size;
        images.put(volumeData.name, volumeData);
        return volumeData.name;
    }

    public Map<String, VolumeData> getVolumes () {
        Map<String, VolumeData> volumes = new HashMap<>();
        volumes.putAll(images);
        volumes.putAll(isos);
        volumes.putAll(snippets);
        volumes.putAll(backups);
        return volumes;
    }
}
