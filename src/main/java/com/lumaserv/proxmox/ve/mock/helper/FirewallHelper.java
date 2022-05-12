package com.lumaserv.proxmox.ve.mock.helper;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.mock.mocker.Mocker;
import com.lumaserv.proxmox.ve.mock.state.FirewallOptionsData;
import com.lumaserv.proxmox.ve.mock.state.FirewallRuleData;
import com.lumaserv.proxmox.ve.request.firewall.FirewallOptionsUpdateRequest;
import com.lumaserv.proxmox.ve.request.firewall.FirewallRuleCreateRequest;
import com.lumaserv.proxmox.ve.request.firewall.FirewallRuleUpdateRequest;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class FirewallHelper extends Mocker {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9-_]+");

    public static void updateOptions(FirewallOptionsData options, FirewallOptionsUpdateRequest request) {
        if(request.getDhcp() != null)
            options.dhcp = request.getDhcp() > 0;
        if(request.getEnable() != null)
            options.enable = request.getEnable() > 0;
        if(request.getIpFilter() != null)
            options.ipFilter = request.getIpFilter() > 0;
        if(request.getLogLevelIn() != null)
            options.logLevelIn = request.getLogLevelIn();
        if(request.getLogLevelOut() != null)
            options.logLevelOut = request.getLogLevelOut();
        if(request.getMacFilter() != null)
            options.macFilter = request.getMacFilter() > 0;
        if(request.getNdp() != null)
            options.ndp = request.getNdp() > 0;
        if(request.getPolicyIn() != null)
            options.policyIn = request.getPolicyIn();
        if(request.getPolicyOut() != null)
            options.policyOut = request.getPolicyOut();
        if(request.getRouterAdvertisement() != null)
            options.routerAdvertisement = request.getRouterAdvertisement() > 0;
    }

    public static void createRule(List<FirewallRuleData> rules, FirewallRuleCreateRequest request, boolean allowGroup) throws ProxMoxVEException {
        verifyRequiredParam("action", request.getAction());
        verifyRequiredParam("type", request.getType());
        FirewallRuleData rule = buildRule(request, allowGroup);
        int nextPos = rules.stream().map(r -> r.pos).max(Comparator.comparingInt(p -> p)).orElse(-1) + 1;
        if(request.getPos() != null) {
            int p = Math.min(nextPos, Math.max(0, request.getPos()));
            rules.stream().filter(r -> r.pos >= p).forEach(r -> r.pos++);
            rule.pos = p;
        } else {
            rule.pos = nextPos;
        }
        rules.add(rule);
    }

    public static void updateRule(List<FirewallRuleData> rules, int pos, FirewallRuleUpdateRequest request, boolean allowGroup) throws ProxMoxVEException {
        FirewallRuleData rule = rules.stream().filter(r -> r.pos == pos).findFirst().orElse(null);
        if(rule == null)
            throwError(404, "Not Found");
        changeRule(rule, request, allowGroup);
        if(request.getMoveTo() != null) {
            int nextPos = rules.stream().map(r -> r.pos).max(Comparator.comparingInt(p -> p)).orElse(-1) + 1;
            int p = Math.min(nextPos, Math.max(0, request.getMoveTo()));
            rules.stream().filter(r -> r.pos >= p).forEach(r -> r.pos++);
            rule.pos = p;
        }
    }

    public static void deleteRule(List<FirewallRuleData> rules, int pos) throws ProxMoxVEException {
        FirewallRuleData rule = rules.stream().filter(r -> r.pos == pos).findFirst().orElse(null);
        if(rule == null)
            throwError(404, "Not Found");
        rules.remove(rule);
        rules.stream().filter(r -> r.pos > rule.pos).forEach(r -> r.pos--);
    }

    private static FirewallRuleData buildRule(FirewallRuleCreateRequest request, boolean allowGroup) throws ProxMoxVEException {
        FirewallRuleData rule = new FirewallRuleData();
        validateRule(
                request.getType(),
                allowGroup,
                request.getAction(),
                request.getIface(),
                request.getProtocol(),
                request.getSource(),
                request.getDestination(),
                request.getSourcePort(),
                request.getDestinationPort()
        );
        rule.type = request.getType();
        rule.action = request.getAction();
        rule.comment = request.getComment();
        rule.proto = request.getProtocol();
        rule.macro = request.getMacro();
        rule.dest = request.getDestination();
        rule.source = request.getSource();
        rule.sPort = request.getSourcePort();
        rule.dPort = request.getDestinationPort();
        rule.iface = request.getIface();
        rule.enable = request.getEnable() == null || request.getEnable() > 0;
        rule.icmpType = request.getIcmpType();
        return rule;
    }

    private static void changeRule(FirewallRuleData rule, FirewallRuleUpdateRequest request, boolean allowGroup) throws ProxMoxVEException {
        if(request.getDelete() != null) {
            for(String p : request.getDelete().split(",")) {
                switch (p) {
                    case "comment":
                        request.setComment(null);
                        rule.comment = null;
                        break;
                    case "iface":
                        request.setIface(null);
                        rule.iface = null;
                        break;
                    case "macro":
                        request.setMacro(null);
                        rule.macro = null;
                        break;
                    case "proto":
                        request.setProtocol(null);
                        rule.proto = null;
                        break;
                    case "source":
                        request.setSource(null);
                        rule.source = null;
                        break;
                    case "dest":
                        request.setDestination(null);
                        rule.dest = null;
                        break;
                    case "sport":
                        request.setSourcePort(null);
                        rule.sPort = null;
                        break;
                    case "dport":
                        request.setDestinationPort(null);
                        rule.dPort = null;
                        break;
                    case "icmp-type":
                        request.setIcmpType(null);
                        rule.icmpType = null;
                        break;
                    case "log":
                        request.setLog(null);
                        rule.log = null;
                        break;
                    default:
                        throwError(400, "Cannot delete '" + p + "'");
                }
            }
        }
        validateRule(
                request.getType() != null ? request.getType() : rule.type,
                allowGroup,
                request.getAction(),
                request.getIface() != null ? request.getIface() : rule.iface,
                request.getProtocol() != null ? request.getProtocol() : rule.proto,
                request.getSource() != null ? request.getSource() : rule.source,
                request.getDestination() != null ? request.getDestination() : rule.dest,
                request.getSourcePort() != null ? request.getSourcePort() : rule.sPort,
                request.getDestinationPort() != null ? request.getDestinationPort() : rule.dPort
        );
        if(request.getType() != null)
            rule.type = request.getType();
        if(request.getAction() != null)
            rule.action = request.getAction();
        if(request.getComment() != null)
            rule.comment = request.getComment();
        if(request.getProtocol() != null)
            rule.proto = request.getProtocol();
        if(request.getMacro() != null)
            rule.macro = request.getMacro();
        if(request.getDestination() != null)
            rule.dest = request.getDestination();
        if(request.getSource() != null)
            rule.source = request.getSource();
        if(request.getSourcePort() != null)
            rule.sPort = request.getSourcePort();
        if(request.getDestinationPort() != null)
            rule.dPort = request.getDestinationPort();
        if(request.getIface() != null)
            rule.iface = request.getIface();
        if(request.getEnable() != null)
            rule.enable = request.getEnable() == null || request.getEnable() > 0;
        if(request.getIcmpType() != null)
            rule.icmpType = request.getIcmpType();
    }

    private static void validateRule(String type, boolean allowGroup, String action, String iface, String proto, String source, String dest, String sport, String dport) throws ProxMoxVEException {
        switch (type) {
            case "in":
            case "out":
                switch (action) {
                    case "ACCEPT":
                    case "DROP":
                    case "REJECT":
                        break;
                    default:
                        throwError(400, "Invalid action");
                }
                break;
            case "group": {
                if(!allowGroup)
                    throwError(400, "Cannot add security group to security group");
                break;
            }
            default:
                throwError(400, "Invalid type");
        }
        boolean allowPort = false;
        if(proto != null) {
            switch (proto) {
                case "tcp":
                case "udp":
                    allowPort = true;
                    break;
                case "icmp":
                    break;
                default:
                    throwError(400, "Invalid proto");
            }
        }
        if(iface != null && !validateInterfaceParam(iface))
            throwError(400, "Invalid iface");
        if(source != null && !validateCidrParam(source, true))
            throwError(400, "Invalid source");
        if(dest != null && !validateCidrParam(dest, true))
            throwError(400, "Invalid dest");
        if(sport != null && !validatePortParam(sport))
            throwError(400, "Invalid sport");
        if(dport != null && !validatePortParam(dport))
            throwError(400, "Invalid dport");
        if(!allowPort && (sport != null || dport != null))
            throwError(400, "Protocol doesn't allow ports");
    }

    private static boolean validateInterfaceParam(String value) {
        if(!value.startsWith("net"))
            return false;
        try {
            int n = Integer.parseInt(value.substring(3));
            if(n > 31)
                return false;
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private static boolean validateCidrParam(String value, boolean allowSet) {
        if(allowSet && value.startsWith("+")) {
            if(!NAME_PATTERN.matcher(value.substring(1)).matches())
                return false;
            return true;
        }
        if(value.contains(",")) {
            for(String p : value.split(",")) {
                if(!validateCidrParam(p, false))
                    return false;
            }
            return true;
        }
        try {
            int prefix = -1;
            if(value.contains("/")) {
                prefix = Integer.parseInt(value.split("/", 2)[1]);
                value = value.split("/", 2)[0];
            }
            if(!validateIp(value))
                return false;
            if(value.contains(".")) {
                if(prefix != -1 && (prefix < 1 || prefix > 32))
                    return false;
            } else {
                if(prefix != -1 && (prefix < 1 || prefix > 128))
                    return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean validateIp(String value) {
        if(value.contains(".")) {
            String[] spl = value.split("\\.");
            if(spl.length != 4)
                return false;
            for(int i=0; i<4; i++) {
                int part = Integer.parseInt(spl[i]);
                if(part < 0 || part > 255)
                    return false;
            }
        } else {
            if(value.equals("::"))
                return true;
            String[] spl = value.split(":", -1);
            if(spl.length > 8)
                return false;
            boolean expand = false;
            for(int i=0; i<spl.length; i++) {
                if(spl[i].length() == 0) {
                    if(expand && i != spl.length - 1)
                        return false;
                    expand = true;
                }
            }
            if(spl.length != 8 && !expand)
                return false;
            for(String s : spl) {
                if(s.length() == 0)
                    continue;
                int i = Integer.parseInt(s, 16);
                if(i < 0 || i > 65535)
                    return false;
            }
        }
        return true;
    }

    private static boolean validatePortParam(String value) {
        if(value.contains(",")) {
            for(String p : value.split(",")) {
                if(!validatePortParam(p))
                    return false;
            }
            return true;
        }
        if(value.contains("-")) {
            String[] spl = value.split("-");
            if(spl.length > 2)
                return false;
            return validatePortParam(spl[0]) && validatePortParam(spl[1]);
        }
        try {
            int pn = Integer.parseInt(value);
            if(pn < 0 || pn > 65535)
                return false;
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

}
