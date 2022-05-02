package com.lumaserv.proxmox.ve.mock.state;

import java.util.ArrayList;
import java.util.List;

public class PoolData {

    public String id;
    public String comment;
    public List<Member> members = new ArrayList<>();

    public static class Member {
        public String type;
        public String node;
        public Integer vmId;
        public String name;
    }

}
