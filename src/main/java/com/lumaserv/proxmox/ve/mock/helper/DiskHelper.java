package com.lumaserv.proxmox.ve.mock.helper;

public class DiskHelper {

    public static double parseSize(String s) {
        char unit;
        double size;
        if(Character.isDigit(s.charAt(s.length() - 1))) {
            unit = 'g';
            size = Integer.parseInt(s);
        } else {
            unit = Character.toLowerCase(s.charAt(s.length() - 1));
            size = Integer.parseInt(s.substring(0, s.length() - 1));
        }
        switch (unit) {
            case 'k':
                size /= 1048576;
                break;
            case 'm':
                size /= 1024;
                break;
            case 't':
                size *= 1024;
                break;
            case 'p':
                size *= 1048576;
                break;
        }
        return size;
    }

    public static String sizeToString(double size) {
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
