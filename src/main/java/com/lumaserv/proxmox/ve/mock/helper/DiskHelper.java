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
            return ((int) (size * 1048576)) + "K";
        if(size < 1)
            return ((int) (size * 1024)) + "M";
        if(size >= 1048576)
            return ((int) (size / 1048576)) + "P";
        if(size >= 1024)
            return ((int) (size / 1024)) + "T";
        return ((int) size) + "G";
    }

    public static boolean isValidFormat(String format) {
        if(format.equals("qcow2") || format.equals("raw") || format.equals("vmdk"))
            return true;
        return false;
    }

    public static boolean isValidDisk(String disk) {
        try {
            if(disk.startsWith("ide")) {
                int n = Integer.parseInt(disk.substring(3));
                if(n < 0 || n > 3)
                    return false;
                return true;
            }
            if(disk.startsWith("scsi")) {
                int n = Integer.parseInt(disk.substring(4));
                if(n < 0 || n > 30)
                    return false;
                return true;
            }
            if(disk.startsWith("sata")) {
                int n = Integer.parseInt(disk.substring(4));
                if(n < 0 || n > 5)
                    return false;
                return true;
            }
            if(disk.startsWith("virtio")) {
                int n = Integer.parseInt(disk.substring(6));
                if(n < 0 || n > 15)
                    return false;
                return true;
            }
            if(disk.startsWith("unused")) {
                int n = Integer.parseInt(disk.substring(6));
                if(n < 0 || n > 255)
                    return false;
                return true;
            }
        } catch (NumberFormatException ignored) {}
        return false;
    }

}
