package org.rdengine.util.localimage;

public class GridItem
{
    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_CAMERA = 1;
    private int type = 0;
    private String path;
    private String time;
    private int section;
    private boolean isMulitable;
    private boolean isChecked;

    public GridItem(String path, String time)
    {
        super();
        this.path = path;
        this.time = time;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public int getSection()
    {
        return section;
    }

    public void setSection(int section)
    {
        this.section = section;
    }

    public boolean isChecked()
    {
        return isChecked;
    }

    public void setChecked(boolean isChecked)
    {
        this.isChecked = isChecked;
    }

    public boolean isMulitable()
    {
        return isMulitable;
    }

    public void setMulitable(boolean isMulitable)
    {
        this.isMulitable = isMulitable;
    }

}
