package net.ebour.mytoolbox.vmware;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ebour.
 */
public class TimeSerie
{
    private List<String[]> data = new ArrayList<>();

    public void add(final String t, final String v)
    {
        this.data.add(new String[]{t, v});
    }

    public List<String[]> getData()
    {
        return data;
    }

}
