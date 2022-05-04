package com.lumaserv.proxmox.ve.mock.state;

import java.util.ArrayList;
import java.util.List;

public class TaskData {

    public Integer vmId;
    public String upId;
    public String node;
    public String type;
    public String user;
    public String status;
    public long start;
    public long end;
    public boolean error;
    public List<String> log = new ArrayList<>();

    public void finish() {
        finish(null);
    }

    public void finish(String error) {
        if(log.size() == 0 || !log.get(log.size() - 1).startsWith("TASK ")) {
            if(error != null) {
                this.error = true;
                log.add("TASK ERROR: " + error);
            } else {
                log.add("TASK OK");
            }
        }
        status = "stopped";
    }

}
