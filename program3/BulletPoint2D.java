

import java.awt.geom.*;
import javax.naming.*;


/** This class is used to pass information on bullets in the environment to the bots in the environment */
public class BulletPoint2D extends ImmutablePoint2D
{
    private int xplus, yplus;

    public BulletPoint2D(double x, double y, int ID, int xplus, int yplus)
    {
        super(x,y,ID);
        this.xplus = xplus;
        this.yplus = yplus;
    }

    /** Returns the x increment for this bullet */
    public int getXPlus()
    {
        return xplus;
    }

    /** Returns the y increment for this bullet */
    public int getYPlus()
    {
        return yplus;
    }
    
    /** Creates a new object of the same class and with the same x,y,ID, xplus, and yplus values */
    public Object clone()
    {
        return new BulletPoint2D(x,y,getID(),xplus,yplus);
    }

}

