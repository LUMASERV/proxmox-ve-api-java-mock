package com.lumaserv.proxmox.ve.mock.state.qemu;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.mock.mocker.Mocker;
import com.lumaserv.proxmox.ve.util.OptionStringUtil;

import java.util.HashMap;
import java.util.Map;

public class IPConfigData {

    public String ip;
    public String gateway;
    public String ip6;
    public String gateway6;

    public IPConfigData() {}

    public IPConfigData(int id, String optionString) throws ProxMoxVEException {
        Map<String, String> options = OptionStringUtil.parseOptionString(optionString);
        for(String k : options.keySet()) {
            String v = options.get(k);
            switch (k) {
                case "ip":
                    ip = v;
                    break;
                case "ip6":
                    ip6 = v;
                    break;
                case "gw":
                    gateway = v;
                    break;
                case "gw6":
                    gateway6 = v;
                    break;
                default:
                    Mocker.throwError(400, "Parameter 'ipfilter" + id + "' has invalid option " + k);
            }
        }
    }

    public String toOptionString() {
        Map<String, String> options = new HashMap<>();
        if(ip != null)
            options.put("ip", ip);
        if(gateway != null)
            options.put("gw", gateway);
        if(ip6 != null)
            options.put("ip6", ip6);
        if(gateway6 != null)
            options.put("gw6", gateway6);
        return OptionStringUtil.buildOptionString(options);
    }

}
