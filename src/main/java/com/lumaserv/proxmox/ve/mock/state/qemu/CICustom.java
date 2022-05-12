package com.lumaserv.proxmox.ve.mock.state.qemu;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.mock.mocker.Mocker;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.mock.state.StorageData;
import com.lumaserv.proxmox.ve.util.OptionStringUtil;

import java.util.HashMap;
import java.util.Map;

public class CICustom {

    public DiskData user;
    public DiskData network;
    public DiskData meta;

    public CICustom() {}

    public CICustom(String optionString, MockState state) throws ProxMoxVEException {
        Map<String, String> options = OptionStringUtil.parseOptionString(optionString);
        for(String k : options.keySet()) {
            switch (k) {
                case "user":
                    user = parse("user", options.get(k), state);
                    break;
                case "network":
                    network = parse("network", options.get(k), state);
                    break;
                case "meta":
                    meta = parse("meta", options.get(k), state);
                    break;
                default:
                    Mocker.throwError(400, "Parameter 'cicustom' has invalid option " + k);
            }
        }
    }

    private static DiskData parse(String o, String s, MockState state) throws ProxMoxVEException {
        DiskData data = new DiskData();
        String[] spl = s.split(":", 2);
        if(spl.length < 2)
            Mocker.throwError(400, "Parameter 'cicustom' has invalid option " + o);
        if(!spl[1].startsWith("snippets/"))
            Mocker.throwError(400, "Parameter 'cicustom' has invalid option " + o);
        data.storage = spl[0];
        StorageData storageData = state.storages.get(data.storage);
        if(storageData == null)
            Mocker.throwError(400, "Parameter 'cicustom' has invalid option " + o + " (storage not found)");
        data.volume = spl[1].substring(9);
        return data;
    }

    public String toOptionString() {
        Map<String, String> options = new HashMap<>();
        if(user != null)
            options.put("user", user.storage + ":snippets/" + user.volume);
        if(network != null)
            options.put("network", network.storage + ":snippets/" + network.volume);
        if(meta != null)
            options.put("meta", meta.storage + ":snippets/" + meta.volume);
        return OptionStringUtil.buildOptionString(options);
    }

}
