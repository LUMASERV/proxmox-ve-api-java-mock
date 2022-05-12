package com.lumaserv.proxmox.ve.mock.state.qemu;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.mock.mocker.Mocker;
import com.lumaserv.proxmox.ve.mock.state.MockState;
import com.lumaserv.proxmox.ve.util.OptionStringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NetworkData {

    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}");

    public String model;
    public String mac;
    public String bridge;
    public Integer tag;
    public boolean firewall;
    public boolean linkDown;
    public Integer rate;

    public NetworkData() {}

    public NetworkData(int id, String optionString, MockState state) throws ProxMoxVEException {
        Map<String, String> options = OptionStringUtil.parseOptionString(optionString);
        for(String k : options.keySet()) {
            String v = options.get(k);
            switch (k) {
                case "virtio":
                case "e1000":
                case "vmxnet3":
                case "rtl8139": {
                    if(model != null)
                        Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option " + k + " (model is already set)");
                    if(!MAC_PATTERN.matcher(v).matches())
                        Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option " + k + " (invalid mac address)");
                    model = k;
                    mac = v;
                    break;
                }
                case "bridge":
                    bridge = v;
                    break;
                case "rate":
                    rate = Integer.parseInt(v);
                    if(rate < 0)
                        Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option rate (invalid rate)");
                    break;
                case "tag":
                    tag = Integer.parseInt(v);
                    if(tag < 0 || tag > 4095)
                        Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option tag (invalid vlan tag)");
                    break;
                case "link_down":
                    if(v.equals("1"))
                        linkDown = true;
                    else if(v.equals("0"))
                        linkDown = false;
                    else
                        Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option link_down");
                    break;
                case "firewall":
                    if(v.equals("1"))
                        firewall = true;
                    else if(v.equals("0"))
                        firewall = false;
                    else
                        Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option firewall");
                    break;
                default:
                    Mocker.throwError(400, "Parameter 'net" + id + "' has invalid option " + k);
                    break;
            }
        }
    }

    public String toOptionString() {
        Map<String, String> options = new HashMap<>();
        options.put(model, mac);
        options.put("bridge", bridge);
        if(tag != null)
            options.put("tag", String.valueOf(tag));
        if(firewall)
            options.put("firewall", "1");
        if(linkDown)
            options.put("link_down", "1");
        if(rate != null)
            options.put("rate", String.valueOf(rate));
        return OptionStringUtil.buildOptionString(options);
    }

}
