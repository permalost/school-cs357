/*
 * File: qbowers.java
 */

import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * A ClobberBot.
 *
 * @author Quincy Bowers  quincybowers@u.boisestate.edu
 * @version 1.0
 */
public class qbowers extends ClobberBot
{
    public static final int NORTH     = ClobberBotAction.UP;
    public static final int SOUTH     = ClobberBotAction.DOWN;
    public static final int EAST      = ClobberBotAction.RIGHT;
    public static final int WEST      = ClobberBotAction.LEFT;
    public static final int NORTHWEST = ClobberBotAction.UP|ClobberBotAction.LEFT;
    public static final int NORTHEAST = ClobberBotAction.UP|ClobberBotAction.RIGHT;
    public static final int SOUTHWEST = ClobberBotAction.DOWN|ClobberBotAction.LEFT;
    public static final int SOUTHEAST = ClobberBotAction.DOWN|ClobberBotAction.RIGHT;
    
    public static final double ANGLE_WEST      = Math.toRadians(180.0);
    public static final double ANGLE_NORTHWEST = Math.toRadians(225.0);
    public static final double ANGLE_NORTH     = Math.toRadians(270.0);
    public static final double ANGLE_NORTHEAST = Math.toRadians(315.0);
    public static final double ANGLE_EAST2     = Math.toRadians(360.0);
    public static final double ANGLE_EAST      = Math.toRadians(0.0);
    public static final double ANGLE_SOUTHEAST = Math.toRadians(45.0);
    public static final double ANGLE_SOUTH     = Math.toRadians(90.0);
    public static final double ANGLE_SOUTHWEST = Math.toRadians(135.0);
    
    public static final int GAME_WIDTH  = 600;
    public static final int GAME_HEIGHT = 600;
    public static final int WALL_BUFFER = 50;
    
    public Dimension worldSize;
    public Random rand = new Random();
    public Clobber game;
    public Vector<ClobberBot> teammates = new Vector<ClobberBot>();
    private int id;
    private BufferedImage myImage;
    private BufferedImage myImageN;
    private BufferedImage myImageS;
    private BufferedImage myImageE;
    private BufferedImage myImageW;
    private BufferedImage myImageNW;
    private BufferedImage myImageNE;
    private BufferedImage myImageSW;
    private BufferedImage myImageSE;
    private int idle;
    private int my_direction;

    int shotclock;

    /**
     * Constructor for the ClobberBot.
     *
     * @param game the game arena.
     */
    public qbowers(Clobber game)
    {
        super(game);
            
        this.game = game;
        
        shotclock = 0;
        idle = 0;
        
        // Load the bot graphic
        try
        {
            myImage = ImageIO.read(new File("qbowers.png"));
        }
        catch (IOException e)
        {
            System.err.println("qbowers.class: ERROR Encountered IOException while reading in image.\n");
            System.err.println(e.getMessage());
        }

        // Create rotated versions of the bot graphic.
        AffineTransform transform = new AffineTransform();
        
        transform.rotate(0, myImage.getWidth()/2, myImage.getHeight()/2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageS = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageSW = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageW = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageNW = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageN = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageNE = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageE = op.filter(myImage, null);
            
        transform.rotate(Math.toRadians(45.0), myImage.getWidth()/2, myImage.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        myImageSE = op.filter(myImage, null);
    }


    /**
     * Used by the game to inform each bot of its game ID.
     *
     * You can use this to search through the list of bot positions you are 
     * passed each turn to differentiate between your teamates and your foes
     *
     * @param the id to be assigned to this bot.
     */
    public void informID(int id)
    {
        this.id = id;
    }


    /**
     * Retrieve the game ID for this bot.
     *
     * Each bot exposes this method so you can keep track of them.
     *
     * @return the game id of this bot.
     */
    public int getID()
    {
        return id;
    }


    /**
     * Sets the dimensions of the world.
     *
     * @param worldSize the dimensions for the world.
     */
    public void setEnvironment(Dimension worldSize)
    {
        this.worldSize = new Dimension(worldSize);
    }


    /** 
     * Used by the game to tell each bot who its team mates are before the game 
     * begins.
     *
     * @param bot the bot to add to the team.
     */
    public void teammate(ClobberBot bot)
    {
        System.out.println("Adding teammate " + bot + " to " + this + "'s team.");
        teammates.add(bot);
    }


    /** 
     * This method is called once for each bot for each turn.
     *
     * The bot should look at what it knows, and make an appropriate decision 
     * about what to do.
     *
     * @param currState contains info on this bots current position, the 
     *                  position of every other bot and bullet in the system. 
     *
     * @return the action to take.
     */
    public ClobberBotAction takeTurn(WhatIKnow currState)
    {
        --shotclock;
        
        double closest = Double.MAX_VALUE;
        Bullet killer = null;
        
        /* Dodge bullets */
        Iterator<BulletPoint2D> it = currState.bullets.iterator();
        while(it.hasNext())
        {
            Bullet b = new Bullet(it.next());
            
            if (b.attacking(currState.me))
            {
                double distance = b.distance(currState.me);
                
                if (distance < closest)
                {
                    closest = distance;
                    killer = b;
                }
            }
        }
            
        if (killer != null)
        {
            idle = 0;
            return new ClobberBotAction(ClobberBotAction.MOVE, 
                                        getDodgeDirection(killer, currState.me));
        }

        /* Track bots */
        Iterator<BotPoint2D> bit = currState.bots.iterator();
        double nearest           = Double.MAX_VALUE;
        BotPoint2D target        = null;
        
        BOT:
        while(bit.hasNext())
        {
            BotPoint2D b = bit.next();
            
            for (ClobberBot c : teammates)
            {
                if (c.getID() == b.getID())
                {
                    continue BOT;
                }
            }
            
            double distance = b.distance(currState.me);
            
            if (distance < nearest)
            {
                nearest = distance;
                target = b;
            }
        }

        /* Take a shot */
        if (shotclock <= 0 && target != null)
        {
            // Reset the shotclock
            shotclock=game.getShotFrequency()+1;
            
            my_direction = directionTo(currState.me, target);
            return new ClobberBotAction(ClobberBotAction.SHOOT, my_direction);
        }
        
        /* Move toward closest enemy */
        if (target != null && target.distance(currState.me) > 100)
        {
            int direction = directionTo(currState.me, target);
            idle = 0;
            return new ClobberBotAction(ClobberBotAction.MOVE, direction);
        }
        
        /* Move away from walls */
        if (currState.me.x < WALL_BUFFER)
        {
            idle = 0;
            return new ClobberBotAction(ClobberBotAction.MOVE, EAST);
        }
        else if (currState.me.x > GAME_WIDTH - WALL_BUFFER)
        {
            idle = 0;
            return new ClobberBotAction(ClobberBotAction.MOVE, WEST);
        }
        else if (currState.me.y < WALL_BUFFER)
        {
            idle = 0;
            return new ClobberBotAction(ClobberBotAction.MOVE, SOUTH);
        }
        else if (currState.me.x > GAME_HEIGHT - WALL_BUFFER)
        {
            idle = 0;
            return new ClobberBotAction(ClobberBotAction.MOVE, NORTH);
        }
        
        if (idle > 18)
        {
            randomMove();
        }
        
        // If we get here we should just do nothing.
        ++idle;
        return new ClobberBotAction(ClobberBotAction.NONE, ClobberBotAction.NONE);
    }
    
    
    /**
     * 
     * @param b
     * @param me
     * @return
     */
    private int getDodgeDirection(Bullet b, BotPoint2D me)
    {
        switch (b.direction)
        {
            case NORTH:
                if (b.x < me.x)
                { return NORTHEAST; }
                else
                { return NORTHWEST; }
            case SOUTH:
                if (b.x < me.x)
                { return SOUTHEAST; }
                else
                { return SOUTHWEST; }
            case WEST:
                if (b.y < me.y)
                { return SOUTHWEST; }
                else
                { return NORTHWEST; }
            case EAST:
                if (b.y < me.y)
                { return SOUTHEAST; }
                else
                { return NORTHEAST; }
            case NORTHWEST:
                if (relativeBulletPosition(b, me) < 0.0)
                { return NORTHEAST; }
                else
                { return SOUTHWEST; }
            case SOUTHEAST:
                if (relativeBulletPosition(b, me) < 0.0)
                { return NORTHEAST; }
                else
                { return SOUTHWEST; }
            case NORTHEAST:
                if (relativeBulletPosition(b, me) < 0.0)
                { return SOUTHEAST; }
                else
                { return NORTHWEST; }
            case SOUTHWEST:
                if (relativeBulletPosition(b, me) < 0.0)
                { return SOUTHEAST; }
                else
                { return NORTHWEST; }
        }
        
        return 0;
    }
    
    /**
     * Find the closest cardinal direction from p1 to p2.
     * 
     * @param p1
     * @param p2
     * @return
     */
    private int directionTo(Point2D.Double p1, Point2D.Double p2)
    {
        double x = p2.x - p1.x;
        double y = p2.y - p1.y;
        double smallest = Double.MAX_VALUE; 
        double distance = 0.0;
        double angle = Math.atan2(y, x);
        int direction = 0;
        
        if (angle < 0)
        {
            angle = angle + 2 * Math.PI;
        }
        else if (angle >= (2 * Math.PI))
        {
            angle = angle - 2 * Math.PI;
        }
        
        // NORTH 
        distance = Math.abs(ANGLE_NORTH - angle);
        if (distance < smallest) { smallest = distance; direction = NORTH; }
        
        // SOUTH 
        distance = Math.abs(ANGLE_SOUTH - angle);
        if (distance < smallest) { smallest = distance; direction = SOUTH; }
        
        // EAST 
        distance = Math.abs(ANGLE_EAST - angle);
        if (distance < smallest) { smallest = distance; direction = EAST; }
        
        // We need to also check if the angle is near 360 degrees.
        distance = Math.abs(ANGLE_EAST2 - angle);
        if (distance < smallest) { smallest = distance; direction = EAST; }
        
        // WEST 
        distance = Math.abs(ANGLE_WEST - angle);
        if (distance < smallest) { smallest = distance; direction = WEST; }
        
        // NORTHWEST 
        distance = Math.abs(ANGLE_NORTHWEST - angle);
        if (distance < smallest) { smallest = distance; direction = NORTHWEST; }
        
        // NORTHEAST 
        distance = Math.abs(ANGLE_NORTHEAST - angle);
        if (distance < smallest) { smallest = distance; direction = NORTHEAST; }
        
        // SOUTHWEST
        distance = Math.abs(ANGLE_SOUTHWEST - angle);
        if (distance < smallest) { smallest = distance; direction = SOUTHWEST; }
        
        // SOUTHEAST
        distance = Math.abs(ANGLE_SOUTHEAST - angle);
        if (distance < smallest) { smallest = distance; direction = SOUTHEAST; }
        
        return direction;
    }

    /**
     * 
     * @return
     */
    private ClobberBotAction randomMove()
    {
        switch(rand.nextInt(8))
        {
            case 0:
                return new ClobberBotAction(ClobberBotAction.MOVE, NORTH);
            case 1:
                return new ClobberBotAction(ClobberBotAction.MOVE, SOUTH);
            case 2:
                return new ClobberBotAction(ClobberBotAction.MOVE, WEST);
            case 3:
                return new ClobberBotAction(ClobberBotAction.MOVE, EAST);
            case 4:
                return new ClobberBotAction(ClobberBotAction.MOVE, NORTHWEST);
            case 5:
                return new ClobberBotAction(ClobberBotAction.MOVE, NORTHEAST);
            case 6:
                return new ClobberBotAction(ClobberBotAction.MOVE, SOUTHWEST);
            default:
                return new ClobberBotAction(ClobberBotAction.MOVE, SOUTHEAST);
        }
    }
    
    /**
     * Returns a random shot.
     * @return
     */
    private ClobberBotAction randomShot()
    {
        switch(rand.nextInt(8))
        {
            case 0:
                return new ClobberBotAction(ClobberBotAction.SHOOT, NORTH);
            case 1:
                return new ClobberBotAction(ClobberBotAction.SHOOT, SOUTH);
            case 2:
                return new ClobberBotAction(ClobberBotAction.SHOOT, WEST);
            case 3:
                return new ClobberBotAction(ClobberBotAction.SHOOT, EAST);
            case 4:
                return new ClobberBotAction(ClobberBotAction.SHOOT, NORTHWEST);
            case 5:
                return new ClobberBotAction(ClobberBotAction.SHOOT, NORTHEAST);
            case 6:
                return new ClobberBotAction(ClobberBotAction.SHOOT, SOUTHWEST);
            default:
                return new ClobberBotAction(ClobberBotAction.SHOOT, SOUTHEAST);
        }
    }
    
    /**
     * Determines where the bullet is in relation to a line drawn through me.
     * 
     * A line is drawn through me in the same direction the bullet is traveling.
     * Then we determine which side of that line the bullet is on using the
     * cross product method.
     * 
     * @param b the point for the bullet.
     * @param me the point for me.
     * @return 0.0 if the bullet is on the same line as me, a negative number if
     *         the bullet should be considered below the line, and a positive 
     *         number otherwise.
     */
    private double relativeBulletPosition(Bullet b, BotPoint2D me)
    {
        Line2D.Double line = null;
        Point2D.Double p1;
        Point2D.Double p2;
        
        switch (b.direction)
        {
            case NORTH:
            case SOUTH:
                line = new Line2D.Double(me.x, Double.MIN_VALUE, me.x, Double.MAX_VALUE);
            case WEST:
            case EAST:
                line = new Line2D.Double(Double.MIN_VALUE, me.y, Double.MAX_VALUE, me.y);
            case NORTHWEST:
            case SOUTHEAST:
                p1 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_NORTHWEST), me.y - 1000 * Math.sin(ANGLE_NORTHWEST));
                p2 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_SOUTHEAST), me.y - 1000 * Math.sin(ANGLE_SOUTHEAST));
                line = new Line2D.Double(p1, p2);
            default: //NORTHEAST & SOUTHWEST
                p1 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_NORTHEAST), me.y - 1000 * Math.sin(ANGLE_NORTHEAST));
                p2 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_SOUTHWEST), me.y - 1000 * Math.sin(ANGLE_SOUTHWEST));
                line = new Line2D.Double(p1, p2);
        }
 
        return (line.x2 - line.x1) * (b.y - line.y1)
             - (line.y2 - line.y1) * (b.x - line.x1);
    }

    /**
     * Check to see if a given bot is a candidate to be shot.
     * 
     * @param b
     * @return
     */
    private int inMySights(BotPoint2D b, BotPoint2D me)
    {
        Line2D.Double attack_vector = null;
        Point2D.Double p1;
        
        Rectangle2D.Double target 
            = new Rectangle2D.Double(b.x - Clobber.MAX_BOT_GIRTH*1.5,
                                     b.y - Clobber.MAX_BOT_GIRTH*1.5,
                                     Clobber.MAX_BOT_GIRTH*1.5,
                                     Clobber.MAX_BOT_GIRTH*1.5);
        
        // NORTH 
        attack_vector = new Line2D.Double(me.x, me.y, me.x, Double.MIN_VALUE);
        if (attack_vector.intersects(target))
        {
            return NORTH;
        }
        
        // SOUTH 
        attack_vector = new Line2D.Double(me.x, me.y, me.x, Double.MAX_VALUE);
        if (attack_vector.intersects(target))
        {
            return SOUTH;
        }
        
        // EAST 
        attack_vector = new Line2D.Double(me.x, me.y, Double.MAX_VALUE, me.y);
        if (attack_vector.intersects(target))
        {
            return EAST;
        }
        
        // WEST 
        attack_vector = new Line2D.Double(me.x, me.y, Double.MIN_VALUE, me.y);
        if (attack_vector.intersects(target))
        {
            return WEST;
        }
        
        // NORTHWEST 
        p1 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_NORTHWEST), me.y - 1000 * Math.sin(ANGLE_NORTHWEST));
        attack_vector = new Line2D.Double(me, p1);
        if (attack_vector.intersects(target))
        {
            return NORTHWEST;
        }
        
        // NORTHEAST 
        p1 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_NORTHEAST), me.y - 1000 * Math.sin(ANGLE_NORTHEAST));
        attack_vector = new Line2D.Double(me, p1);
        if (attack_vector.intersects(target))
        {
            return NORTHEAST;
        }
        
        // SOUTHWEST
        p1 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_SOUTHWEST), me.y - 1000 * Math.sin(ANGLE_SOUTHWEST));
        attack_vector = new Line2D.Double(me, p1);
        if (attack_vector.intersects(target))
        {
            return SOUTHWEST;
        }
        
        // SOUTHEAST
        p1 = new Point2D.Double(me.x - 1000 * Math.cos(ANGLE_SOUTHEAST), me.y - 1000 * Math.sin(ANGLE_SOUTHEAST));
        attack_vector = new Line2D.Double(me, p1);
        if (attack_vector.intersects(target))
        {
            return SOUTHEAST;
        }
            
        return 0;
    }

    /** 
     * Draws the clobber bot to the screen.
     *
     * The drawing should be centered at the point me, and should not be bigger
     * than 9x9 pixels 
     *
     * @param page the graphics page to draw on.
     * @param me the point at which this bot is centered.
     */
    public void drawMe(Graphics page, Point2D me)
    {
        int x,y;
        x = (int)me.getX() - Clobber.MAX_BOT_GIRTH/2 -1;
        y = (int)me.getY() - Clobber.MAX_BOT_GIRTH/2 -1;
        
        switch (my_direction)
        {
            case NORTH:
                page.drawImage(myImageN, x, y, null);
                break;
            case SOUTH:
                page.drawImage(myImageS, x, y, null);
                break;
            case WEST:
                page.drawImage(myImageW, x, y, null);
                break;
            case EAST:
                page.drawImage(myImageE, x, y, null);
                break;
            case NORTHWEST:
                page.drawImage(myImageNW, x, y, null);
                break;
            case SOUTHEAST:
                page.drawImage(myImageSE, x, y, null);
                break;
            case NORTHEAST:
                page.drawImage(myImageNE, x, y, null);
                break;
            case SOUTHWEST:
                page.drawImage(myImageSW, x, y, null);
                break;
            
        }
    }


    /**
     * The identifier string for this bot.
     *
     * It must be unique from other players, since it is used to determine who 
     * your team mates are.  You can include your login name in the id to 
     * guarantee uniqueness.
     */
    public String toString()
    {
        return "qbowers";
    }
    
    /** 
     * Here's an example of how to read the WhatIKnow data structure
     */
    protected void showWhatIKnow(WhatIKnow currState)
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

    
    /**
     * Represents a bullet in the game.
     * 
     * @author Quincy Bowers  quincybowers@u.boisestate.edu
     */
    public class Bullet extends Point2D
    {
        public int id;
        public double x;
        public double y;
        public double vx;
        public double vy;
        public int direction;

        public Bullet(BulletPoint2D b)
        {
            id = b.getID();
            x  = b.x;
            y  = b.y;
            vx = b.getXPlus();
            vy = b.getYPlus();
            
            if      (vx == 0 && vy <  0) { direction = NORTH;     }
            else if (vx == 0 && vy >  0) { direction = SOUTH;     }
            else if (vx <  0 && vy == 0) { direction = WEST;      }
            else if (vx >  0 && vy == 0) { direction = EAST;      }
            else if (vx <  0 && vy <  0) { direction = NORTHWEST; }
            else if (vx >  0 && vy <  0) { direction = NORTHEAST; }
            else if (vx <  0 && vy >  0) { direction = SOUTHWEST; }
            else if (vx >  0 && vy >  0) { direction = SOUTHEAST; }
        }
        
        
        /**
         * Check if a bullet is going to kill me in the next four turns.
         * 
         * @param me
         * @return
         */
        public Boolean attacking(BotPoint2D me)
        {
            Line2D.Double attack_vector = new Line2D.Double(x, y, me.x, me.y);
            
            // If the bullet is more than 16 pixels away, ignore it.
            if (me.distance(this) > 28.0)
            {
                return false;
            }
            
            if (attack_vector.intersects(me.x - Clobber.MAX_BOT_GIRTH/2,
                                         me.y - Clobber.MAX_BOT_GIRTH/2,
                                         Clobber.MAX_BOT_GIRTH,
                                         Clobber.MAX_BOT_GIRTH))
            {
                return true;
            }
                
            return false;
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setLocation(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }
}

