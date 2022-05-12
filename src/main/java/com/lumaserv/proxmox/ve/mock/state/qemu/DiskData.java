package com.lumaserv.proxmox.ve.mock.state.qemu;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.mock.helper.DiskHelper;
import com.lumaserv.proxmox.ve.mock.mocker.Mocker;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.StorageData;
import com.lumaserv.proxmox.ve.util.OptionStringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiskData {

    private static final Pattern VOLUME_SIZE_PATTERN = Pattern.compile("([0-9])([KkMmGgTtPp]?)");

    public String storage;
    public String volume;
    public String cache;
    public boolean discard;
    public Integer readIops;
    public Integer writeIops;
    public Integer readMbps;
    public Integer writeMbps;
    public boolean ssd;
    public boolean cdrom;

    public DiskData() {}

    public DiskData(int vmId, String name, String optionString, MockState state) throws ProxMoxVEException {
        Map<String, String> options = OptionStringUtil.parseOptionString(optionString, "path");
        if(!options.containsKey("path"))
            Mocker.throwError(400, "Parameter '" + name + "' is missing required option path");
        if(options.containsKey("media")) {
            if(!options.get("media").equals("cdrom"))
                Mocker.throwError(400, "Parameter '" + name + "' is missing required option media");
            cdrom = true;
        }
        for(String k : options.keySet()) {
            String v = options.get(k);
            switch (k) {
                case "path": {
                    storage = options.get("path").split(":", 2)[0];
                    if(cdrom) {
                        if(!(storage.equals("none") || storage.equals("cdrom"))) {
                            if(!options.get("path").contains(":"))
                                Mocker.throwError(400, "Parameter '" + name + "' has invalid option path (missing volume name)");
                            StorageData storageData = state.storages.get(storage);
                            if(storageData == null)
                                Mocker.throwError(400, "Parameter '" + k + "' has invalid option path (storage not found)");
                            volume = options.get("path").split(":", 2)[1].substring(4);
                            if(!storageData.isos.containsKey(volume))
                                Mocker.throwError(400, "Parameter '" + k + "' has invalid option path (volume not found)");
                        }
                    } else {
                        if(!options.get("path").contains(":"))
                            Mocker.throwError(400, "Parameter '" + name + "' has invalid option path (missing volume name)");
                        StorageData storageData = state.storages.get(storage);
                        if(storageData == null)
                            Mocker.throwError(400, "Parameter '" + k + "' has invalid option path (storage not found)");
                        String path = options.get("path").split(":", 2)[1];
                        Matcher matcher = VOLUME_SIZE_PATTERN.matcher(path);
                        if(matcher.matches()) {
                            String format = options.getOrDefault("format", "qcow2");
                            switch (format) {
                                case "raw":
                                case "qcow2":
                                    break;
                                default:
                                    Mocker.throwError(400, "Parameter '" + k + "' has invalid option format");
                            }
                            volume = storageData.createImage(path, vmId, format);
                        } else {
                            if(!storageData.images.containsKey(path))
                                Mocker.throwError(400, "Parameter '" + k + "' has invalid option path (volume not found)");
                            volume = path;
                        }
                    }
                    break;
                }
                case "cache":
                    switch (v) {
                        case "unsafe":
                        case "directsync":
                        case "writethrough":
                        case "writeback":
                        case "none":
                            break;
                        default:
                            Mocker.throwError(400, "Parameter '" + k + "' has invalid option cache");
                    }
                    cache = v;
                    break;
                case "discard":
                    switch (v) {
                        case "on":
                            discard = true;
                        case "off":
                            discard = false;
                        default:
                            Mocker.throwError(400, "Parameter '" + k + "' has invalid option discard");
                    }
                    break;
                case "ssd":
                    switch (v) {
                        case "1":
                            ssd = true;
                        case "0":
                            ssd = false;
                        default:
                            Mocker.throwError(400, "Parameter '" + k + "' has invalid option ssd");
                    }
                case "iops_rd":
                    readIops = Integer.parseInt(v);
                    if(readIops < 0)
                        Mocker.throwError(400, "Parameter '" + k + "' has invalid option iops_rd");
                    break;
                case "iops_wr":
                    writeIops = Integer.parseInt(v);
                    if(writeIops < 0)
                        Mocker.throwError(400, "Parameter '" + k + "' has invalid option iops_wr");
                    break;
                case "mbps_rd":
                    readMbps = Integer.parseInt(v);
                    if(readMbps < 0)
                        Mocker.throwError(400, "Parameter '" + k + "' has invalid option mbps_rd");
                    break;
                case "mbps_wr":
                    writeMbps = Integer.parseInt(v);
                    if(writeMbps < 0)
                        Mocker.throwError(400, "Parameter '" + k + "' has invalid option mbps_wr");
                    break;
                case "media":
                case "format":
                    continue;
                default:
                    Mocker.throwError(400, "Parameter '" + name + "' has invalid option " + k);
                    break;
            }
        }
    }

    public String toOptionsString(MockState state) {
        Map<String, String> options = new HashMap<>();
        if(cdrom) {
            if(volume != null) {
                options.put("path", storage + ":" + volume);
                options.put("size", DiskHelper.sizeToString(state.storages.get(storage).isos.get(volume).size));
            } else {
                options.put("path", storage);
            }
            options.put("media", "cdrom");
            return OptionStringUtil.buildOptionString(options, "path");
        }
        options.put("path", storage + ":" + volume);
        options.put("size", DiskHelper.sizeToString(state.storages.get(storage).images.get(volume).size));
        if(cache != null)
            options.put("cache", cache);
        if(discard)
            options.put("discard", "on");
        if(readIops != null)
            options.put("iops_rd", String.valueOf(readIops));
        if(writeIops != null)
            options.put("iops_wr", String.valueOf(writeIops));
        if(readMbps != null)
            options.put("mbps_rd", String.valueOf(readMbps));
        if(writeMbps != null)
            options.put("mbpr_wr", String.valueOf(writeMbps));
        if(ssd)
            options.put("ssd", "1");
        return OptionStringUtil.buildOptionString(options, "path");
    }

}
