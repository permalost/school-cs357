
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

/** This bot keeps track of the shot clock, and automatically shoots if it can.  This prevents wasted attempts to shoot, while
 * at the same time shooting as much as possible.  Otherwise, it tends to move in the same direction it moved before. */
public class ClobberBot3 extends ClobberBot
{

    ClobberBotAction currAction, shotAction;
    int shotclock;

    public ClobberBot3(Clobber game)
    {
        super(game);
        mycolor = Color.white;
    }

    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
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
                    shotAction = new ClobberBotAction(ClobberBotAction.SHOOT, ClobberBotAction.UP | ClobberBotAction.RIGHT);
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
        else if(currAction==null || rand.nextInt(10)>8)
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
        return "ClobberBot3 by Tim Andersen";
    }
}


