package net.ebour.mytoolbox.vmware;

/**
 * Represents the details of a VM.
 */
public final class VMInfo
{
    private String name;
    private String hostName;
    private String domainName;
    private String ipv4Address;
    private String ipv6Addresses;
    private VMPowerState powerState;

    /**
     * Constructor.  
     * @param name The name of the virtual machine
     * @param ipv4Address The ipv4 address of the virtual machine 
     * @param powerState The state of the virtual machine as described by VMPowerState enumeration
     *
     * @deprecated Use to keep compatibility. Replace with {@link VMInfo#VMInfo(String, String, VMPowerState, String, String, String)}.
     */

    public VMInfo(String name, String ipv4Address, VMPowerState powerState)
    {
        this(name, ipv4Address, powerState, "", "", "::");
    }

    /**
     * Constructor.  
     * @param name The name of the virtual machine
     * @param ipv4Address The ipv4 address of the virtual machine 
     * @param powerState The state of the virtual machine as described by VMPowerState enumeration
     * @param hostName The hostname of the virtual machine
     * @param domainName The name of the domain for the virtual machine
     * @param ipv6Addresses A list of ipv6 addresses separated by ","
     */
    public VMInfo(String name, String ipv4Address, VMPowerState powerState, String hostName, String domainName, String ipv6Addresses)
    {
        this.name = name;
        this.hostName = hostName;
        this.domainName = domainName;
        this.ipv4Address = ipv4Address;
        this.ipv6Addresses = ipv6Addresses;
        this.powerState = powerState;
    }
    /**
     * @return The name of the virtual machine.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The hostname of the virtual machine. It can be empty.
     */
    public String getHostname()
    {
        return hostName;
    }
    /**
     * @return The name of the domain for the virtual machine. It can be empty.
     */
    public String getDomainName()
    {
        return domainName;
    }

    /**
     * @return The ipv4 address of the virtual machine.
     */
    public String getIpv4Address()
    {
        return ipv4Address;
    }

    /**
     * @return The ipv4 address of the virtual machine.
     */
    public String getIpAddress()
    {
        return getIpv4Address();
    }

    /**
     * @return A list of IPv6 addresses for all enabled NICs, separated by ",".
     */
    public String getIpv6Addresses()
    {
        return ipv6Addresses;
    }

    /**
     *  @return The state of the virtual machine as described by VMPowerState enumeration.
     */
    public VMPowerState getPowerState() {
        return powerState;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s %s %s %s", name, powerState, hostName, domainName, ipv4Address, ipv6Addresses);
    }
}
