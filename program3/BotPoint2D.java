

import java.awt.geom.*;
import javax.naming.*;


public class BotPoint2D extends ImmutablePoint2D
{
    public BotPoint2D(double x, double y, int ID)
    {
        super(x,y,ID);
    }
    
    /** Creates a new object of the same class and with the same x,y and ID values */
    public Object clone()
    {
        return new BotPoint2D(x,y,getID());
    }
}

