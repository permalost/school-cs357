
import java.awt.geom.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
   This class implements an example GUIClobberBot.
*/
public class GUIClobberBot extends ClobberBot implements KeyListener
{
    Dimension worldSize;
    ArrayList<ClobberBotAction> nextAction 
	= new ArrayList<ClobberBotAction>();
    private ClobberBotAction doNothing 
	= new ClobberBotAction( ClobberBotAction.MOVE, 0);
    private ClobberBotAction lastAction = new ClobberBotAction( ClobberBotAction.NONE, ClobberBotAction.UP);
    private static int lastCount = 0;



    public GUIClobberBot(Clobber game)
    {
        super(game);
	game.addKeyListener(this);
	mycolor= new Color( 204, 153, 102);
    }

    public void setEnvironment(Dimension worldSize)
    {
        this.worldSize = new Dimension(worldSize);
    }

    /** This method is called once for each bot for each turn.  The bot should look at what it knows, and make
    an appropriate decision about what to do.
    @param currState contains info on this bots current position, the position of every other bot and bullet in the system. */
    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
	    ClobberBotAction action;
	    //System.err.println( nextAction);
	    if (nextAction.size()>0)
		{
			action = nextAction.get(0);
			nextAction.remove(0);
		}
	    else action = lastAction;
	    if((action.getAction() & ClobberBotAction.MOVE) !=0) 
		    lastAction=action;
	    return action;
    }

    /** Draws the clobber bot to the screen.  The drawing should be centered at the point me, and should not be bigger than 9x9 pixels */
    public void drawMe(Graphics page, Point2D me)
    {
	int x, y;
	x = (int)(me.getX())-8;
	y = (int)(me.getY())-8;
	page.setColor( mycolor);
	page.fillArc( x+4, y, 8, 8, 0, 180);
	page.fillRect( 2 + x, 4 + y, 12, 10);
	page.fillArc( x, y+4, 4, 4, 90, 180);
	page.fillArc( x+12, y+4, 4, 4, 270, 180);
	page.fillArc( x+2, y+12, 4, 4, 180, 180);
	page.fillArc( x+10, y+12, 4, 4, 180, 180);
	page.setColor( Color.YELLOW);
	page.fillOval( 6 + x, 2 + y, 2, 2);
	page.fillOval( 10 + x, 2 + y, 2, 2);
    }

    /**
    */
    public void keyTyped(KeyEvent e) { }

    /**
    */
    public void keyReleased(KeyEvent e) {}

    /**
    */public void keyPressed(KeyEvent e) {
	//System.err.println( "Key event: " + e.getKeyCode());
	/*System.err.println( "Key event.VK_NUMPAD2: " + KeyEvent.VK_NUMPAD2 );
	System.err.println( "Key event.VK_UP: " + KeyEvent.VK_UP );*/
	switch (e.getKeyCode())
	    {
	    case 'N':
	    case KeyEvent.VK_NUMPAD2: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.DOWN));
		break;
	    case 'J':
	    case KeyEvent.VK_NUMPAD6: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.RIGHT));
		break;
	    case 'Y':
	    case KeyEvent.VK_NUMPAD8: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.UP));
		break;
	    case 'G':
	    case KeyEvent.VK_NUMPAD4: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.LEFT));
		break;
	    case 'B':
	    case KeyEvent.VK_NUMPAD1: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.DOWN | ClobberBotAction.LEFT));
		break;
	    case 'M':
	    case KeyEvent.VK_NUMPAD3: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.DOWN | ClobberBotAction.RIGHT));
		break;
	    case 'U':
	    case KeyEvent.VK_NUMPAD9: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.UP | ClobberBotAction.RIGHT));
		break;
	    case 'T':
	    case KeyEvent.VK_NUMPAD7: 
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.SHOOT,
		    ClobberBotAction.UP | ClobberBotAction.LEFT));
		break;
	    case 'W':
	    case KeyEvent.VK_UP:
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.UP));
		break;
	    case 'D':
	    case KeyEvent.VK_RIGHT:
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.RIGHT));
		break;
	    case 'X':
	    case KeyEvent.VK_DOWN:
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.DOWN));
		break;
	    case 'A':
	    case KeyEvent.VK_LEFT:
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.LEFT));
		break;
	    case 'Q':
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.LEFT | ClobberBotAction.UP));
		break;
	    case 'E':
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.RIGHT | ClobberBotAction.UP));
		break;
	    case 'Z':
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.LEFT | ClobberBotAction.DOWN));
		break;
	    case 'C':
		nextAction.add( new ClobberBotAction(
		    ClobberBotAction.MOVE, ClobberBotAction.RIGHT | ClobberBotAction.DOWN));
		break;
	    case 'S':
		nextAction.add(doNothing);
		break;
	    }
    }

    /** Return a String representation of the ClobberBot */
    public String toString()
    {
	    return "GUIClobberBot";
    }

}


