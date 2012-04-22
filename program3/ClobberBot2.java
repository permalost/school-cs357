
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

/** ClobberBot2 is a lot like ClobberBot, but it tends to keep doing what it did before (with the exception of the
 * shoot action). */
public class ClobberBot2 extends ClobberBot
{

    ClobberBotAction currAction;

    public ClobberBot2(Clobber game)
    {
        super(game);
        mycolor = Color.white;
    }

    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
        if(currAction==null || ((currAction.getAction() & ClobberBotAction.SHOOT)>0) || rand.nextInt(10)>8)
        {
            switch(rand.nextInt(8))
            {
                case 0:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP);
                break;
                case 1:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN);
                break;
                case 2:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.LEFT);
                break;
                case 3:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.RIGHT);
                break;
                case 4:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP | ClobberBotAction.LEFT);
                break;
                case 5:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP | ClobberBotAction.RIGHT);
                break;
                case 6:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
                break;
                default:
                    currAction = new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
                break;
            }
        }
        return currAction;
    }

    public String toString()
    {
        return "ClobberBot2 by Tim Andersen";
    }
}


