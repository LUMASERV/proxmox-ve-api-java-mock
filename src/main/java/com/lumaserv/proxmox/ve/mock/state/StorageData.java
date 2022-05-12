package com.lumaserv.proxmox.ve.mock.state;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class StorageData {

    public String name;
    public Map<String, VolumeData> images = new HashMap<>();
    public Map<String, VolumeData> isos = new HashMap<>();
    public Map<String, VolumeData> snippets = new HashMap<>();
    public Map<String, VolumeData> backups = new HashMap<>();

    public String createImage(int size, String unit, int vmId, String format) {
        return createImage(size, unit.length() == 0 ? 'g' : unit.charAt(0), vmId, format);
    }

    public String createImage(int size, char unit, int vmId, String format) {
        unit = Character.toLowerCase(unit);
        double sizeDouble = size;
        switch (unit) {
            case 'k':
                sizeDouble /= 1048576;
                break;
            case 'm':
                sizeDouble /= 1024;
                break;
            case 't':
                sizeDouble *= 1024;
                break;
            case 'p':
                sizeDouble *= 1048576;
                break;
        }
        String namePrefix = "vm-" + vmId + "-disk-";
        int n = images.values().stream().filter(v -> v.name.startsWith(namePrefix)).map(v -> Integer.parseInt(v.name.substring(namePrefix.length()))).max(Comparator.comparingInt(i -> i)).orElse(-1) + 1;
        VolumeData volumeData = new VolumeData();
        volumeData.name = namePrefix + n;
        volumeData.format = format;
        volumeData.size = sizeDouble;
        images.put(name, volumeData);
        return volumeData.name;
    }

}
