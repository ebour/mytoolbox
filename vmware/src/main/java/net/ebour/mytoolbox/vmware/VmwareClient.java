package net.ebour.mytoolbox.vmware;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

import java.util.*;
import java.util.logging.Level;

import static java.util.Arrays.asList;

/**
 * Created by ebour.
 */
public class VmwareClient
{
    private static final long WAIT_SLEEP_TIME_MS = 5000;

    private       ServiceInstance serviceInstance;
    private       VmwareServer    vmwareServer;

    public VmwareClient(final VmwareServer vmwareServer)
    {
        this.vmwareServer = vmwareServer;
    }

    public VMInfo shutdownVM(String vmName) throws Exception
    {
        VirtualMachine vm = findVM(vmName);
        new VmwTask.PowerOffVMTask(vm, vmName).waitForTask();

        return getVMInfo(vm);
    }

    public void destroyVM(String vmName) throws Exception
    {
        VirtualMachine vm = findVM(vmName);
        if(isVmStarted(vmName))
        {
            shutdownVM(vmName);
        }
        new VmwTask.DestroyVMTask(vm, vmName).waitForTask();
    }

    public VMPowerState getVmPowerState(String vmName) throws Exception
    {
        return getVMInfo(vmName).getPowerState();
    }

    public boolean isVmStarted(String vmName) throws Exception
    {
        return getVmPowerState(vmName) == VMPowerState.PoweredOn;
    }

    public VMInfo startVM(String vmName) throws Exception
    {
        VirtualMachine vm = findVM(vmName);
        new VmwTask.PowerOnVMTask(vm, vmName).waitForTask();
        waitForOSBoot(vm);

        return getVMInfo(vm);
    }

    public VMInfo createVM(String vmName, String template) throws Exception
    {
        VirtualMachine vm = findVM(vmName);
        ResourcePool resourcePool = findResourcePool(findDatacenter());

        new VmwTask.CreateVMTask(vm, resourcePool, findFolder(vmwareServer.getWorkingDir()), template, vmName).waitForTask();

        return startVM(vmName);
    }

    public VMInfo getVMInfo(final String vmName) throws Exception
    {
        VirtualMachine vm = findVM(vmName);
        return getVMInfo(vm);
    }

    private VMInfo getVMInfo(VirtualMachine vm) {
        // ipv4 address
        String ipv4Address = vm.getGuest().getIpAddress();
        if (isNullOrEmpty(ipv4Address)) {
            ipv4Address = "0.0.0.0";
        }

        // all addresses for enabled NICs both IPv4 and IPv6
        StringBuilder addr = new StringBuilder();
        try {
            for (int loop=1; loop<=5; loop++) {
                for (GuestNicInfo nic : vm.getGuest().getNet()) {
                    if (nic.isConnected()) {
                        try {
                            String[] ipAddrInfo = nic.getIpAddress();
                            for (String ipAddress : ipAddrInfo) {
                                if (ipAddress.matches(".*:.*.:.*"))
                                {
                                    if (ipAddress.startsWith("fe80")) { ; }
                                    else
                                    {
                                        if (addr.length() > 0)
                                        {
                                            addr.append(',');
                                        }
                                        addr.append(ipAddress);
                                    }
                                }
                            }
                        } catch (Exception e) { ; }

                    }
                }
                if (addr.length() == 0) {
//		        	System.out.println("Sleeping 1 min and check again");
                    Thread.sleep(60000);
                } else {
                    break;
                }
            }
        } catch (Exception e) { ; }

        String ipv6Addresses;
        if (addr.length() == 0)
        {
            ipv6Addresses = "::";
        }
        else
        {
            ipv6Addresses = addr.toString();
        }
        // hostname
        String hostname = vm.getGuest().getHostName();

        // domainname
        String domainName = "";
        if (vm.getGuest().getGuestFamily().contains("linux"))
        {
            GuestStackInfo[] stackInfo = vm.getGuest().getIpStack();
            if (stackInfo != null && stackInfo.length > 0)
            {
                NetDnsConfigInfo dnsConfig = stackInfo[0].getDnsConfig();
                domainName = dnsConfig.getDomainName();
            }
        }
        else if (vm.getGuest().getGuestFamily().contains("windows"))
        {
            for (GuestNicInfo nics : vm.getGuest().getNet())
            {
                if (nics.isConnected())
                {
                    try {
                        domainName = nics.getDnsConfig().getDomainName();
                        break;
                    } catch (Exception e) { ; }
                }
            }
        }

        if (domainName == null || domainName.isEmpty())
        {
            domainName = "";
        }

        return new VMInfo(vm.getName(), ipv4Address, getVMPowerState(vm), hostname, domainName, ipv6Addresses);
    }

    private VMPowerState getVMPowerState(VirtualMachine vm) {
        if (getPowerState(vm).equals(VirtualMachinePowerState.poweredOn)) {
            return VMPowerState.PoweredOn;
        } else if (getPowerState(vm).equals(VirtualMachinePowerState.poweredOff)) {
            return VMPowerState.PoweredOff;
        } else if (getPowerState(vm).equals(VirtualMachinePowerState.suspended)) {
            return VMPowerState.Suspended;
        }
        return VMPowerState.Unknown;
    }

    private boolean isPoweredOn(VirtualMachine vm) {
        return getPowerState(vm) == VirtualMachinePowerState.poweredOn;
    }

    private VirtualMachinePowerState getPowerState(VirtualMachine vm) {
        VirtualMachineRuntimeInfo runtimeInfo = vm.getRuntime();
        if (runtimeInfo == null) {
            return null;
        }
        return runtimeInfo.getPowerState();
    }

    private boolean isTemplate(VirtualMachine vm) {
        return isTemplate(vm, null);
    }

    private boolean isTemplate(VirtualMachine vm, VirtualMachineConfigInfo configInfo) {
        if (configInfo == null) {
            configInfo = vm.getConfig();
            if (configInfo == null) {
                return false;
            }
        }
        return configInfo.isTemplate();
    }

    private boolean isNullOrEmpty(String param) {
        if (param == null || param.length() == 0) {
            return true;
        }
        return false;
    }

    private void waitForOSBoot(VirtualMachine vm) {
        GuestInfo guest = vm.getGuest();
        while (isNullOrEmpty(guest.getIpAddress()) || guest.getNet() == null) {
            try {
                Thread.sleep(WAIT_SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                ;
            }
            guest = vm.getGuest();
        }
    }

    private Datacenter findDatacenter()
            throws Exception
    {
        InventoryNavigator navigator = new InventoryNavigator(serviceInstance.getRootFolder());
        String datacenterName = vmwareServer.getDataCenter();
        Datacenter dataCenter = null;
        try
        {
            dataCenter = (Datacenter)navigator.searchManagedEntity("Datacenter", datacenterName);
        }
        catch (Exception e)
        {
            final String msg = String.format("VMWare exception finding data center '%s'", new Object[] { datacenterName });            
            throw new Exception(msg);
        }
        return dataCenter;
    }

    private ResourcePool findResourcePool(Datacenter dataCenter)
            throws Exception
    {
        InventoryNavigator navigator = new InventoryNavigator(dataCenter);
        String resourcePoolName = vmwareServer.getResourcePool();
        ResourcePool resourcePool = null;
        try
        {
            resourcePool = (ResourcePool)navigator.searchManagedEntity("ResourcePool", resourcePoolName);
        }
        catch (Exception e)
        {
            final String msg = String.format("VMWare exception finding resource pool '%s'", new Object[] { resourcePoolName });            
            throw new Exception(msg, e);
        }
        return resourcePool;
    }

    private VirtualMachine findVM(final String vmName)
            throws Exception
    {
        final InventoryNavigator navigator = new InventoryNavigator(serviceInstance.getRootFolder());
        VirtualMachine vm = null;
        try
        {
            vm = (VirtualMachine)navigator.searchManagedEntity("VirtualMachine", vmName);
        }
        catch (Exception e)
        {
            final String msg = String.format("VMWare exception finding VM '%s'", new Object[] { vmName });
            throw new Exception(msg, e);
        }
        if (vm == null)
        {
            final String msg = String.format("VM '%s' not found", new Object[] { vmName });
            throw new Exception(msg);
        }
        return vm;
    }

    public void login() throws Exception
    {
        try
        {
            this.serviceInstance = new ServiceInstance(vmwareServer.getUrl(), vmwareServer.getUser(), vmwareServer.getPassword(), true);
        }
        catch (Exception e)
        {
            final String msg = String.format("VMWare login failed for user '%s'", new Object[] { vmwareServer.getUser() });
            throw new Exception(msg, e);
        }

    }

    public Map<String, TimeSerie> getPerformanceData(final String hostname, final Calendar startTime, final Calendar endTime, int intervalInSec, final String... metricKeys) throws Exception
    {
        PerformanceManager performanceManager = serviceInstance.getPerformanceManager();

        VirtualMachine vm = findVm(hostname);

        final List<PerfMetricId> metrics = asList(performanceManager.queryAvailablePerfMetric(vm, startTime, endTime, intervalInSec));
        final int[] metricIds = new int[metrics.size()];
        for(int idx = 0; idx < metrics.size(); idx++)
        {
            final PerfMetricId metric = metrics.get(idx);
            metricIds[idx] = metric.getCounterId();
        }

        final List<PerfMetricId> perfMetricIds = new ArrayList<>();
        int idx = 0;
        final PerfCounterInfo[] perfCounterInfos = performanceManager.queryPerfCounter(metricIds);
        for(PerfCounterInfo perfCounterInfo : perfCounterInfos)
        {
            final String key = perfCounterInfo.getGroupInfo().getKey()
                    + "." + perfCounterInfo.getNameInfo().getKey()
                    + "." + perfCounterInfo.getRollupType().toString();
            for(String metricKey : metricKeys)
            {
                if(key.equalsIgnoreCase(metricKey))
                {
                    perfMetricIds.add(metrics.get(idx));
                    break;
                }
            }

            // System.out.println("metric key: " + perfCounterInfo.getRollupType().name() + "." + perfCounterInfo.getGroupInfo().getKey() + "." + perfCounterInfo.getNameInfo().getKey() + "." + perfCounterInfo.getUnitInfo().getKey() + "." + perfCounterInfo.getLevel());
            idx++;
        }

        PerfQuerySpec[] perfQuery = new PerfQuerySpec[]{new PerfQuerySpec()};

        perfQuery[0].entity = vm.getMOR();
        perfQuery[0].metricId = perfMetricIds.toArray(new PerfMetricId[perfMetricIds.size()]);
        perfQuery[0].maxSample = 100;
        perfQuery[0].startTime = startTime;
        perfQuery[0].endTime = endTime;
        perfQuery[0].intervalId = intervalInSec;
        perfQuery[0].format = "normal";

        PerfEntityMetricBase[] perfData = performanceManager.queryPerf(perfQuery);

        final Map<String, TimeSerie> series = new HashMap<>();
        for (int i = 0; perfData != null && i < perfData.length; i++)
        {
            final TimeSerie serie = new TimeSerie();

            PerfEntityMetricBase val = perfData[i];
            PerfEntityMetric pem = (PerfEntityMetric) val;
            PerfMetricSeries[] vals = pem.getValue(); // timestamps
            PerfSampleInfo[] infos = pem.getSampleInfo(); // values

            for (int j = 0; vals != null && j < vals.length; ++j)
            {
                PerfMetricIntSeries val1 = (PerfMetricIntSeries) vals[j];
                PerfCounterInfo perfCounterInfo = perfCounterInfos[findPerfMetricId(metrics, val1.getId())];

                final String key = perfCounterInfo.getGroupInfo().getKey()
                        + "." + perfCounterInfo.getNameInfo().getKey()
                        + "." + perfCounterInfo.getRollupType().toString();

                if(!series.containsKey(key))
                {
                    series.put(key, serie);
                }

                long[] longs = val1.getValue(); // a value
                for (int k = 0; k < longs.length; k++)
                {
                    serie.add(String.valueOf(infos[k].getTimestamp().getTime().getTime()), String.valueOf(longs[k]));
                }
            }
        }

        return series;
    }

    private int findPerfMetricId(final List<PerfMetricId> metrics, final PerfMetricId id) throws Exception
    {
        int pos = 0;
        for(PerfMetricId perfMetricId : metrics)
        {
            if(perfMetricId.counterId == id.counterId)
            {
                return pos;
            }
            pos ++;
        }
        throw new Exception("Unable to find PerfMetricId!");
    }

    public void logout()
    {
        serviceInstance.getServerConnection().logout();
    }

    private Folder findFolder(String folderName) throws Exception
    {
        return findFolder(serviceInstance.getRootFolder(), folderName);
    }

    private Folder findFolder(Folder folderRoot, String folderName) throws Exception
    {
        InventoryNavigator navigator = new InventoryNavigator(folderRoot);
        Folder folder = null;
        try
        {
            folder = (Folder)navigator.searchManagedEntity("Folder", folderName);
        }
        catch (Exception e)
        {
            String msg = String.format("VMWare exception finding folder '%s'", new Object[] { folderName });           
            throw new Exception(msg);
        }
        return folder;
    }

    private Folder createFolder(Folder folderRoot, String folderName) throws Exception
    {
        try
        {
            return folderRoot.createFolder(folderName);
        }
        catch (Exception e)
        {
            String msg = String.format("VMWare exception creating folder '%s'", new Object[] { folderName });
            throw new Exception(msg);
        }
    }

    private VirtualMachine findVm(final String hostname) throws Exception
    {
        final InventoryNavigator navigator = new InventoryNavigator(serviceInstance.getRootFolder());
        final ManagedEntity[] vms = navigator.searchManagedEntities("VirtualMachine");
        VirtualMachine vm = null;
        try
        {
            for(ManagedEntity virtualMachine : vms)
            {
                if(virtualMachine instanceof VirtualMachine)
                {
                    final String vmHostname = ((VirtualMachine)virtualMachine).getGuest().getHostName();
                    System.out.println("vm: " + virtualMachine.getName());
                    if(vmHostname == null || vmHostname.isEmpty())
                    {
                        continue;
                    }
                    else if(vmHostname.contentEquals(hostname.trim()))
                    {
                        vm = (VirtualMachine) virtualMachine;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = String.format("VMWare exception finding VM '%s'", new Object[] { hostname });
            throw new Exception(msg);
        }
        if (vm == null)
        {
            String msg = String.format("VM '%s' not found", new Object[] { hostname });
            throw new Exception(msg);
        }
        return vm;
    }
}
