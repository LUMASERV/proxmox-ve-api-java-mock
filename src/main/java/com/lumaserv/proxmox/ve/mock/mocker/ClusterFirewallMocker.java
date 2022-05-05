package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.ClusterAPI;
import com.lumaserv.proxmox.ve.mock.helper.FirewallHelper;
import com.lumaserv.proxmox.ve.mock.state.*;
import com.lumaserv.proxmox.ve.request.firewall.*;

import java.util.Comparator;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class ClusterFirewallMocker extends Mocker {

    public static void mockClusterAPI(ClusterAPI api, MockState state) {
        try {
            /*
            Options
             */
            when(api.getFirewallOptions()).then(i -> state.firewallOptions.toFirewallOptions());
            doAnswer(i -> {
                FirewallOptionsUpdateRequest request = i.getArgument(0);
                FirewallOptionsData options = state.firewallOptions;
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
                return null;
            }).when(api).updateFirewallOptions(any(FirewallOptionsUpdateRequest.class));
            
            /*
            Cluster Rules
             */
            doAnswer(i -> {
                FirewallRuleCreateRequest request = i.getArgument(0);
                FirewallHelper.createRule(state.firewallRules, request, false);
                return null;
            }).when(api).createFirewallRule(any(FirewallRuleCreateRequest.class));
            when(api.getFirewallRules()).then(i -> state.firewallRules.stream().sorted(Comparator.comparingInt(r -> r.pos)).map(FirewallRuleData::toFirewallRule).collect(Collectors.toList()));
            when(api.getFirewallRule(anyInt())).then(i -> {
                int pos = i.getArgument(0);
                FirewallRuleData rule = state.firewallRules.stream().filter(r -> r.pos == pos).findFirst().orElse(null);
                if(rule == null)
                    throwError(404, "Not Found");
                return rule.toFirewallRule();
            });
            doAnswer(i -> {
                int pos = i.getArgument(0);
                FirewallRuleUpdateRequest request = i.getArgument(2);
                FirewallHelper.updateRule(state.firewallRules, pos, request, false);
                return null;
            }).when(api).updateFirewallRule(anyInt(), any(FirewallRuleUpdateRequest.class));
            doAnswer(i -> {
                int pos = i.getArgument(0);
                FirewallHelper.deleteRule(state.firewallRules, pos);
                return null;
            }).when(api).deleteFirewallRule(anyInt());

            /*
            Groups
             */
            doAnswer(i -> {
                FirewallGroupCreateRequest request = i.getArgument(0);
                if(state.firewallGroups.containsKey(request.getName()))
                    throwError(400, "Group '" + request.getName() + "' already exists");
                FirewallGroupData firewallGroupData = new FirewallGroupData();
                firewallGroupData.name = request.getName();
                firewallGroupData.comment = request.getComment();
                state.firewallGroups.put(firewallGroupData.name, firewallGroupData);
                return null;
            }).when(api).createFirewallGroup(any(FirewallGroupCreateRequest.class));
            when(api.getFirewallGroups()).then(i -> state.firewallGroups.values().stream().map(FirewallGroupData::toFirewallGroup).collect(Collectors.toList()));
            doAnswer(i -> {
                String name = i.getArgument(0);
                FirewallGroupData group = state.firewallGroups.get(name);
                if(group == null)
                    throwError(404, "Not Found");
                if(group.rules.size() > 0)
                    throwError(409, "Group not empty");
                state.firewallGroups.remove(name);
                return null;
            }).when(api).deleteFirewallGroup(anyString());

            /*
            Group Rules
             */
            doAnswer(i -> {
                String groupName = i.getArgument(0);
                FirewallRuleCreateRequest request = i.getArgument(1);
                FirewallGroupData firewallGroupData = state.firewallGroups.get(groupName);
                if(firewallGroupData == null)
                    throwError(404, "Not Found");
                FirewallHelper.createRule(firewallGroupData.rules, request, false);
                return null;
            }).when(api).createFirewallGroupRule(anyString(), any(FirewallRuleCreateRequest.class));
            when(api.getFirewallGroupRules(anyString())).then(i -> {
                String name = i.getArgument(0);
                FirewallGroupData group = state.firewallGroups.get(name);
                if(group == null)
                    throwError(404, "Not Found");
                return group.rules.stream().sorted(Comparator.comparingInt(r -> r.pos)).map(FirewallRuleData::toFirewallRule).collect(Collectors.toList());
            });
            when(api.getFirewallGroupRule(anyString(), anyInt())).then(i -> {
                String name = i.getArgument(0);
                int pos = i.getArgument(1);
                FirewallGroupData group = state.firewallGroups.get(name);
                if(group == null)
                    throwError(404, "Not Found");
                FirewallRuleData rule = group.rules.stream().filter(r -> r.pos == pos).findFirst().orElse(null);
                if(rule == null)
                    throwError(404, "Not Found");
                return rule.toFirewallRule();
            });
            doAnswer(i -> {
                String groupName = i.getArgument(0);
                int pos = i.getArgument(1);
                FirewallRuleUpdateRequest request = i.getArgument(2);
                FirewallGroupData firewallGroupData = state.firewallGroups.get(groupName);
                if(firewallGroupData == null)
                    throwError(404, "Not Found");
                FirewallHelper.updateRule(firewallGroupData.rules, pos, request, false);
                return null;
            }).when(api).updateFirewallGroupRule(anyString(), anyInt(), any(FirewallRuleUpdateRequest.class));
            doAnswer(i -> {
                String groupName = i.getArgument(0);
                int pos = i.getArgument(1);
                FirewallGroupData firewallGroupData = state.firewallGroups.get(groupName);
                if(firewallGroupData == null)
                    throwError(404, "Not Found");
                FirewallHelper.deleteRule(firewallGroupData.rules, pos);
                return null;
            }).when(api).deleteFirewallGroupRule(anyString(), anyInt());

            /*
            IP Sets
             */
            doAnswer(i -> {
                FirewallIPSetCreateRequest request = i.getArgument(0);
                if(state.firewallGroups.containsKey(request.getName()))
                    throwError(400, "IP Set '" + request.getName() + "' already exists");
                FirewallIPSetData ipset = new FirewallIPSetData();
                ipset.name = request.getName();
                ipset.comment = request.getComment();
                state.firewallIpSets.put(ipset.name, ipset);
                return null;
            }).when(api).createFirewallIPSet(any(FirewallIPSetCreateRequest.class));
            when(api.getFirewallIPSets()).then(i -> state.firewallIpSets.values().stream().map(FirewallIPSetData::toFirewallIPSet).collect(Collectors.toList()));
            doAnswer(i -> {
                String name = i.getArgument(0);
                FirewallIPSetData ipset = state.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                if(ipset.entries.size() > 0)
                    throwError(409, "IP Set not empty");
                state.firewallGroups.remove(name);
                return null;
            }).when(api).deleteFirewallIPSet(anyString());

            /*
            IP Set Entries
             */
            doAnswer(i -> {
                String groupName = i.getArgument(0);
                FirewallIPSetEntryCreateRequest request = i.getArgument(1);
                FirewallIPSetData ipset = state.firewallIpSets.get(groupName);
                if(ipset == null)
                    throwError(404, "Not Found");
                verifyRequiredParam("cidr", request.getCidr());
                if(!FirewallHelper.validateIp(request.getCidr()))
                    throwError(404, "Invalid cidr");
                if(ipset.entries.containsKey(request.getCidr()))
                    throwError(409, "Entry exists");
                FirewallIPSetEntryData entry = new FirewallIPSetEntryData();
                entry.cidr = request.getCidr();
                entry.comment = request.getComment();
                entry.noMatch = request.getNoMatch() != null && request.getNoMatch() > 0;
                ipset.entries.put(entry.cidr, entry);
                return null;
            }).when(api).createFirewallIPSetEntry(anyString(), any(FirewallIPSetEntryCreateRequest.class));
            when(api.getFirewallIPSetEntries(anyString())).then(i -> {
                String name = i.getArgument(0);
                FirewallIPSetData ipset = state.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                return ipset.entries.values().stream().map(FirewallIPSetEntryData::toFirewallIPSetEntry).collect(Collectors.toList());
            });
            when(api.getFirewallIPSetEntry(anyString(), anyString())).then(i -> {
                String name = i.getArgument(0);
                String cidr = i.getArgument(1);
                FirewallIPSetData ipset = state.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                FirewallIPSetEntryData entry = ipset.entries.get(cidr);
                if(entry == null)
                    throwError(404, "Not Found");
                return entry.toFirewallIPSetEntry();
            });
            doAnswer(i -> {
                String name = i.getArgument(0);
                String cidr = i.getArgument(1);
                FirewallIPSetEntryUpdateRequest request = i.getArgument(2);
                FirewallIPSetData ipset = state.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                FirewallIPSetEntryData entry = ipset.entries.get(cidr);
                if(entry == null)
                    throwError(404, "Not Found");
                if(request.getComment() != null)
                    entry.comment = request.getComment();
                if(request.getNoMatch() != null)
                    entry.noMatch = request.getNoMatch() > 0;
                return null;
            }).when(api).updateFirewallIPSetEntry(anyString(), anyString(), any(FirewallIPSetEntryUpdateRequest.class));
            doAnswer(i -> {
                String name = i.getArgument(0);
                String cidr = i.getArgument(1);
                FirewallIPSetData ipset = state.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                if(!ipset.entries.containsKey(cidr))
                    throwError(404, "Not Found");
                return null;
            }).when(api).deleteFirewallIPSetEntry(anyString(), anyString());
        } catch (ProxMoxVEException ignored) {}
    }





}
