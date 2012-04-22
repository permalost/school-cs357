
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

/** This is a lot like ClobberBot3, but has an even stronger tendency to keep moving in the same direction.  Also,
 * I've given you an example of how to read the WhatIKnow state to see where all the bullets and other bots are. */
public class ClobberBot5 extends ClobberBot
{

    ClobberBotAction currAction, shotAction;
    int myOwnInt;
    static int numOfMe;
    int shotclock;
    private Vector<Bullet> bullets;
    //private Vector<Bots> bullets

    private class Bullet
    {
        double xplus;
        double yplus;
        Point2D pos;
        Point2D oldpos;
        int ID;

        boolean dangerous;
        boolean knowDirection;

        public boolean dangerous()
        {
            if(!dangerous) return dangerous;
            return (dangerous == checkMovingTowardsMe());
        }

        private boolean checkMovingTowardsMe()
        {
            return false;
        }

    }

    Vector<Bullet> getDangerousBullets(WhatIKnow currstate)
    {
        int x,y;
        ImmutablePoint2D pt;
        Bullet bul;
        Vector<Bullet> badones=new Vector<Bullet>();

        x=0;
        y=0;

        while(x<currstate.bullets.size() && y<bullets.size())
        {
            pt = (ImmutablePoint2D)(currstate.bullets.get(x));
            bul = bullets.get(y);

            assert(pt.getID() >= bul.ID);  // We shouldn't see an id less than what we're already storing

            if(pt.getID() > bul.ID)
            {
                // Add new bullet
            }
            else 
            {
                if(!bullets.get(y).knowDirection) 
                {
                    // Figure out direction
                }
                if(!bul.dangerous)
                {
                    x++;
                    y++;
                }
                else 
                {
                }

            }
        }
        return null;
    }

 /*******************************************************************************************************/
 /*******************************************************************************************************/
 /*******************************************************************************************************/
    public ClobberBot5(Clobber game)
    {
        super(game);
        myOwnInt = numOfMe++;
        mycolor = Color.green;
    }

    public void myOwnMethod()
    {
        System.out.println("Unit " + myOwnInt + " reporting, sir.");
    }


    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
        //showWhatIKnow(currState); // @@@ Uncomment this line to see it print out all bullet and bot positions every turn

        for(int x=0;x<teammates.size();x++)
        {
            ((ClobberBot5)(teammates.get(x))).myOwnMethod();
        }

        shotclock--;
        if(shotclock<=0)
        {
            shotclock=game.getShotFrequency()+1;
            switch(rand.nextInt(8))
            {
                case 0:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP);
                break;
                case 1:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN);
                break;
                case 2:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.LEFT);
                break;
                case 3:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.RIGHT);
                break;
                case 4:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP | ClobberBotAction.LEFT);
                break;
                case 5:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, 
                            ClobberBotAction.UP | ClobberBotAction.RIGHT | ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                case 6:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                default:
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
                break;
            }
            return shotAction;
        }
        else if(currAction==null || rand.nextInt(20)>18)
        {
            switch(rand.nextInt(4))
            {
                case 0:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.UP);
                break;
                case 1:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.DOWN);
                break;
                case 2:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.LEFT);
                break;
                case 3:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.RIGHT);
                break;
                case 4:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.UP | ClobberBotAction.LEFT);
                break;
                case 5:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.UP | ClobberBotAction.RIGHT);
                break;
                case 6:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                default:
                    currAction = new ClobberBotAction(ClobberBotAction.MOVE, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
                break;
            }
        }
        return currAction;
    }

    public String toString()
    {
        return "ClobberBot5 by Tim Andersen";
    }

    /** Here's an example of how to read teh WhatIKnow data structure */
    private void showWhatIKnow(WhatIKnow currState)
    {
        System.out.println("My id is " + ((ImmutablePoint2D)(currState.me)).getID() + ", I'm at position (" + 
                           currState.me.getX() + ", " + currState.me.getY() + ")");
        System.out.print("Bullets: ");
        Iterator<BulletPoint2D> it = currState.bullets.iterator();
        while(it.hasNext())
        {
            ImmutablePoint2D p = (ImmutablePoint2D)(it.next());
            System.out.print(p + ", ");
        }
        System.out.println();

        System.out.print("Bots: ");
        Iterator<BotPoint2D> bit = currState.bots.iterator();
        while(bit.hasNext())
        {
            System.out.print(bit.next() + ", ");
        }
        System.out.println();
    }

}


