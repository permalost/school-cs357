

import java.awt.geom.*;
import javax.naming.*;


/** This class provides an immutable form of the Point2D.Double class */
public class ImmutablePoint2D extends Point2D.Double
{
    private int ID;

    public ImmutablePoint2D(double x, double y, int ID)
    {
        this.x=x;
        this.y=y;
        this.ID=ID;
    }
    
    /** This method returns the ID of this point. */
    public int getID()
    {
        return ID;
    }

    /** This operation is not supported by ImmutablePoint2D */
    public void setLocation(Point2D pos) 
    {
        throw new RuntimeException("Operation not supported");
    }

    /** This operation is not supported by ImmutablePoint2D */
    public void setLocation(double x, double y) 
    {
        throw new RuntimeException("Operation not supported");
    }

    /** Creates a new object of the same class and with the same x,y and ID values */
    public Object clone()
    {
        return new ImmutablePoint2D(x,y,ID);
    }

    public String toString()
    {
        return "(" + getID() + ", " + getX() + ", " + getY() + ")";

    }
}

