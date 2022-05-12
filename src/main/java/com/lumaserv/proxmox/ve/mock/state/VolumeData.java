package com.lumaserv.proxmox.ve.mock.state;

public class VolumeData {

    public String name;
    public String format;
    public double size;

    public String sizeFormatted() {
        if(size < (1./1024))
            return (size * 1048576) + "K";
        if(size < 1)
            return (size * 1024) + "M";
        if(size >= 1048576)
            return (size / 1048576) + "P";
        if(size >= 1024)
            return (size / 1024) + "T";
        return size + "G";
    }

}
