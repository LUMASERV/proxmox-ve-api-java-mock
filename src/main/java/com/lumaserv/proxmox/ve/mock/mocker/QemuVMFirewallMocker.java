package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import com.lumaserv.proxmox.ve.apis.QemuVMAPI;
import com.lumaserv.proxmox.ve.mock.helper.FirewallHelper;
import com.lumaserv.proxmox.ve.mock.state.*;
import com.lumaserv.proxmox.ve.request.firewall.*;

import java.util.Comparator;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class QemuVMFirewallMocker extends Mocker {

    public static void mockQemuVMAPI(QemuVMAPI api, int id, MockState state) {
        try {
            /*
            Options
             */
            when(api.getFirewallOptions()).then(i -> {
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                return qemuVMData.firewallOptions.toFirewallOptions();
            });
            doAnswer(i -> {
                FirewallOptionsUpdateRequest request = i.getArgument(0);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallHelper.updateOptions(qemuVMData.firewallOptions, request);
                return null;
            }).when(api).updateFirewallOptions(any(FirewallOptionsUpdateRequest.class));

            /*
            VM Rules
             */
            doAnswer(i -> {
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallRuleCreateRequest request = i.getArgument(0);
                FirewallHelper.createRule(qemuVMData.firewallRules, request, false);
                return null;
            }).when(api).createFirewallRule(any(FirewallRuleCreateRequest.class));
            when(api.getFirewallRules()).then(i -> {
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                return qemuVMData.firewallRules.stream().sorted(Comparator.comparingInt(r -> r.pos)).map(FirewallRuleData::toFirewallRule).collect(Collectors.toList());
            });
            when(api.getFirewallRule(anyInt())).then(i -> {
                int pos = i.getArgument(0);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallRuleData rule = qemuVMData.firewallRules.stream().filter(r -> r.pos == pos).findFirst().orElse(null);
                if(rule == null)
                    throwError(404, "Not Found");
                return rule.toFirewallRule();
            });
            doAnswer(i -> {
                int pos = i.getArgument(0);
                FirewallRuleUpdateRequest request = i.getArgument(2);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallHelper.updateRule(qemuVMData.firewallRules, pos, request, false);
                return null;
            }).when(api).updateFirewallRule(anyInt(), any(FirewallRuleUpdateRequest.class));
            doAnswer(i -> {
                int pos = i.getArgument(0);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallHelper.deleteRule(qemuVMData.firewallRules, pos);
                return null;
            }).when(api).deleteFirewallRule(anyInt());

            /*
            IP Sets
             */
            doAnswer(i -> {
                FirewallIPSetCreateRequest request = i.getArgument(0);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                if(state.firewallGroups.containsKey(request.getName()))
                    throwError(400, "IP Set '" + request.getName() + "' already exists");
                FirewallIPSetData ipset = new FirewallIPSetData();
                ipset.name = request.getName();
                ipset.comment = request.getComment();
                qemuVMData.firewallIpSets.put(ipset.name, ipset);
                return null;
            }).when(api).createFirewallIPSet(any(FirewallIPSetCreateRequest.class));
            when(api.getFirewallIPSets()).then(i -> {
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                return qemuVMData.firewallIpSets.values().stream().map(FirewallIPSetData::toFirewallIPSet).collect(Collectors.toList());
            });
            doAnswer(i -> {
                String name = i.getArgument(0);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallIPSetData ipset = state.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                if(ipset.entries.size() > 0)
                    throwError(409, "IP Set not empty");
                qemuVMData.firewallIpSets.remove(name);
                return null;
            }).when(api).deleteFirewallIPSet(anyString());

            /*
            IP Set Entries
             */
            doAnswer(i -> {
                String groupName = i.getArgument(0);
                FirewallIPSetEntryCreateRequest request = i.getArgument(1);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallIPSetData ipset = qemuVMData.firewallIpSets.get(groupName);
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
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallIPSetData ipset = qemuVMData.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                return ipset.entries.values().stream().map(FirewallIPSetEntryData::toFirewallIPSetEntry).collect(Collectors.toList());
            });
            when(api.getFirewallIPSetEntry(anyString(), anyString())).then(i -> {
                String name = i.getArgument(0);
                String cidr = i.getArgument(1);
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallIPSetData ipset = qemuVMData.firewallIpSets.get(name);
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
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallIPSetEntryUpdateRequest request = i.getArgument(2);
                FirewallIPSetData ipset = qemuVMData.firewallIpSets.get(name);
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
                QemuVMData qemuVMData = state.qemuVMs.get(id);
                if(qemuVMData == null)
                    throwError(404, "Not Found");
                FirewallIPSetData ipset = qemuVMData.firewallIpSets.get(name);
                if(ipset == null)
                    throwError(404, "Not Found");
                if(!ipset.entries.containsKey(cidr))
                    throwError(404, "Not Found");
                return null;
            }).when(api).deleteFirewallIPSetEntry(anyString(), anyString());
        } catch (ProxMoxVEException ignored) {}
    }

}
