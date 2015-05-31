package net.ebour.mytoolbox.vmware;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ebour on 31/05/15.
 */
public class VmwareServer
{
    private String workingDir = "";
    private String datacenter;
    private String user;
    private String password;
    private String folder;
    private String resourcePool;
    private String host;

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getWorkingDir()
    {
        return this.workingDir;
    }

    public String getDataCenter()
    {
        return this.datacenter;
    }

    public String getUser()
    {
        return this.user;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getFolder()
    {
        return this.folder;
    }

    public String getHost()
    {
        return this.host;
    }

    public String getResourcePool()
    {
        return this.resourcePool;
    }

    public URL getUrl() throws MalformedURLException
    {
        return new URL("https://" + getHost() + "/sdk");
    }
}
