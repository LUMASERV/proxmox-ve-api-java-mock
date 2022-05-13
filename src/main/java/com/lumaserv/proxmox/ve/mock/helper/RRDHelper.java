package com.lumaserv.proxmox.ve.mock.helper;

public class RRDHelper {

    public static double noise(double pos) {
        return (Math.sin(2 * pos) + Math.sin(Math.PI * pos) + 2) / 4;
    }

}
