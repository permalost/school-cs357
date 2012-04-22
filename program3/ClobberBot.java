
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

/**
   This class implements an example ClobberBot that makes random moves.  All ClobberBots should extend this 
   class and override the takeTurn and drawMe methods.  
*/
public class ClobberBot 
{
    public Dimension worldSize;
    public Random rand = new Random();
    public Color mycolor;
    public Clobber game;
    public Vector<ClobberBot> teammates = new Vector<ClobberBot>();
    private int id;

    public ClobberBot(Clobber game)
    {
        mycolor=Color.white;
        this.game = game;
    }

    /** Used by the game to inform each bot of its game ID.  You can use this to 
        search through the list of bot positions you are passed each turn to differentiate
        between your teamates and your foes */
    public void informID(int id)
    {
        this.id = id;
    }

    /** public method you can use to query for a bots game ID */
    public int getID()
    {
        return id;
    }

    public void setEnvironment(Dimension worldSize)
    {
        this.worldSize = new Dimension(worldSize);
    }

    /** Used by the game to tell each bot who its teamates are before the game begins. */
    public void teammate(ClobberBot bot)
    {
        System.out.println("Adding teammate " + bot + " to " + this + "'s team.");
        teammates.add(bot);
    }

    /** This method is called once for each bot for each turn.  The bot should look at what it knows, and make
    an appropriate decision about what to do.
    @param currState contains info on this bots current position, the position of every other bot and bullet in the system. */
    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
        switch(rand.nextInt(8))
        {
            case 0:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP);
            case 1:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN);
            case 2:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.LEFT);
            case 3:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.RIGHT);
            case 4:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP | ClobberBotAction.LEFT);
            case 5:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.UP | ClobberBotAction.RIGHT);
            case 6:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN | ClobberBotAction.LEFT);
            default:
                return new ClobberBotAction(rand.nextInt(2)+1, ClobberBotAction.DOWN | ClobberBotAction.RIGHT);
        }
    }

    /** Draws the clobber bot to the screen.  The drawing should be centered at the point me, and should not be bigger than 9x9 pixels */
    public void drawMe(Graphics page, Point2D me)
    {
        int x,y;
        x=(int)me.getX() - Clobber.MAX_BOT_GIRTH/2 -1;
        y=(int)me.getY() - Clobber.MAX_BOT_GIRTH/2 -1;
        page.setColor(mycolor);
        page.fillOval(x,y, Clobber.MAX_BOT_GIRTH,Clobber.MAX_BOT_GIRTH);
    }

    /** Your bots identifier string.  It must be unique from other players, since I use it to 
        determine who your teamates are. You can include you rlogin name in the id to guarantee uniqueness. */
    public String toString()
    {
        return "ClobberBot by Tim Andersen";
    }
}


