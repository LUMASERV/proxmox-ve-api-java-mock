package com.lumaserv.proxmox.ve.mock.state;

import com.lumaserv.proxmox.ve.mock.helper.DiskHelper;
import com.lumaserv.proxmox.ve.mock.state.qemu.VolumeData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class StorageData {

    public String name;
    public Map<String, VolumeData> images = new HashMap<>();
    public Map<String, VolumeData> isos = new HashMap<>();
    public Map<String, VolumeData> snippets = new HashMap<>();
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
        images.put(name, volumeData);
        return volumeData.name;
    }

}
