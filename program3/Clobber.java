

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.lang.reflect.*;



/**
 * Clobber is a great game where bots shoot the heck out of each other.  
 */
public class Clobber extends JFrame
{
/**************************************************************************************************/
/************************ Instance Data ***********************************************************/
/**************************************************************************************************/
    private int _width;
    private int _height;
    private int min_x;
    private int min_y;
    private int max_x;
    private int max_y;
    private int delay=25;
    private int teamsize=1;
    private int bot_bul_thresh;
    private int bot_bot_thresh;

    private int shoot_frequency=20;
    private int bot_step_size=2;
    private int bullet_step_size=4;

    private Vector<ClobberBotMan> bots;
    private Vector<ClobberBotMan> deadbots;
    private Vector<ClobberBullet> bullets;

    private Vector<String> playerFileNames;
    private Vector<String> playerDirNames;

    private Vector<ArgHandler> argHandlers;
    private Random rand = new Random();

    private boolean tournament=false;
    private int numTournaments=0;
    private boolean display=true;

    private int nextID=0;

    private JPanel panel;

    private Graphics screen;
    private Graphics page;
    private Image imgBuffer;
    private Graphics graphicsBuffer;

/**************************************************************************************************/
/************************ Public Constants ********************************************************/
/**************************************************************************************************/
    public static final int EDGE_BUFFER_LEFT=0;
    public static final int EDGE_BUFFER_TOP=0;
    public static final int EDGE_BUFFER_RIGHT=7;
    public static final int EDGE_BUFFER_BOTTOM=39;
    public static final int BOT_EDGE_BUFFER=7;
    public static final int BULLET_EDGE_BUFFER=-2;
    public static final int MAX_BOT_GIRTH=15;
    public static final int BULLET_GIRTH=5;
    public static final int MIN_START_DISTANCE=MAX_BOT_GIRTH*4;

/**************************************************************************************************/
/************************ Inner Class declarations ************************************************/
/**************************************************************************************************/
    private abstract class ClobberObject
    {
        Point2D pos;
        Point2D oldpos;
        boolean deleteMe;
        int clobberObjectID;

        public void updatePosition(int i, int j)
        {
            if(i<min_x) i=min_x;
            else if(i>=max_x) i=max_x;
            if(j<min_y) j=min_y;
            else if(j>=max_y) j=max_y;

            oldpos.setLocation(pos);
            pos.setLocation(i,j);
        }

        public double distance(ClobberObject o)
        {
            return pos.distance(o.pos);
        }

        public abstract boolean collision(ClobberObject o);
    }

    private class ClobberBotRes 
    {
        int numKills;
        int numSurvived;
        int numDied;
        int exceptionCount;
        int points;
        ClobberBot bot;

        public String toString()
        {
            return bot + "\t" + points + "\t" + numDied + "\t" + numKills + "\t" + numSurvived + "\t" + exceptionCount + "\n";
        }

        public ClobberBotRes(ClobberBot bot)
        {
            numKills=0;
            numDied=0;
            exceptionCount=0;
            points=0;
            this.bot = bot;
        }
    }


    private class ClobberBotMan extends ClobberObject
    {
        ClobberBot bot;
        ClobberBotAction action;
        int shotclock;
        int numKills;
        int numDied;
        int exceptionCount;
        ClobberBotRes res;
        int bmin_x;
        int bmax_x;
        int bmin_y;
        int bmax_y;

        public ClobberBotMan(ClobberBot bot, double x, double y, ClobberBotRes res)
        {
            numKills=0;
            numDied=0;
            exceptionCount=0;
            this.bot = bot;
            this.res = res;
            pos = new Point2D.Double(x,y);
            oldpos = new Point2D.Double(x,y);
            deleteMe=false;
            clobberObjectID = nextID++;
            bmin_x=min_x+BOT_EDGE_BUFFER;
            bmax_x=max_x-BOT_EDGE_BUFFER;
            bmin_y=min_y+BOT_EDGE_BUFFER;
            bmax_y=max_y-BOT_EDGE_BUFFER;
        }

        public void updatePosition(int i, int j)
        {
            if(i<bmin_x) i=bmin_x;
            else if(i>=bmax_x) i=bmax_x;
            if(j<bmin_y) j=bmin_y;
            else if(j>=bmax_y) j=bmax_y;

            oldpos.setLocation(pos);
            pos.setLocation(i,j);
        }

        public boolean collision(ClobberObject o)
        {
            if(o instanceof ClobberBotMan)
                return (this.distance(o) < bot_bot_thresh);
            if(o instanceof ClobberBullet)
                return (this.distance(o) < bot_bul_thresh);
            return false;
        }

        public String toString()
        {
            return bot + ": kills = " + numKills + ", deaths = " + numDied;
        }
    }

    private class ClobberBullet extends ClobberObject
    {
        ClobberBotMan owner;
        int xplus, yplus;
        int bmin_x;
        int bmax_x;
        int bmin_y;
        int bmax_y;

        public ClobberBullet(ClobberBotMan owner, double x, double y, int xplus, int yplus)
        {
            this.owner = owner;
            pos = new Point2D.Double(x,y);
            oldpos = new Point2D.Double(x,y);
            this.xplus = xplus;
            this.yplus = yplus;
            deleteMe=false;
            clobberObjectID = nextID++;
            bmin_x=min_x+BULLET_EDGE_BUFFER;
            bmax_x=max_x-BULLET_EDGE_BUFFER;
            bmin_y=min_y+BULLET_EDGE_BUFFER;
            bmax_y=max_y-BULLET_EDGE_BUFFER;
        }

        public void updatePosition(int i, int j)
        {
            if(i<bmin_x) i=bmin_x;
            else if(i>=bmax_x) i=bmax_x;
            if(j<bmin_y) j=bmin_y;
            else if(j>=bmax_y) j=bmax_y;

            oldpos.setLocation(pos);
            pos.setLocation(i,j);
        }


        public boolean collision(ClobberObject o)
        {
            if(o instanceof ClobberBullet)
                return (this.distance(o) < bot_bul_thresh);
            return false;
        }
    }

/**************************************************************************************************/
/************************ Argument Handler Classes ************************************************/
/**************************************************************************************************/
    private class NoDisplayArg extends ArgHandler
    {
        public NoDisplayArg() { arg="-nodisplay"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                display=false;
                return ++x;
            }
            else return x;
        }
    }

    private class TournamentArg extends ArgHandler
    {
        public TournamentArg() { arg="-tournament"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                tournament=true;
                display=false;
                numTournaments = Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }

    private class TeamSizeArg extends ArgHandler
    {
        public TeamSizeArg() { arg="-teamsize"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                teamsize=Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }

    private class DelayArg extends ArgHandler
    {
        public DelayArg() { arg="-delay"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                delay=Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }

    private class LoadPlayerArg extends ArgHandler
    {
        public LoadPlayerArg() { arg="-loadplayer"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                System.out.println("Matched -loadplayer");
                playerFileNames.add(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " player.class]"; }
    }

    private class LoadPlayersArg extends ArgHandler
    {
        public LoadPlayersArg() { arg="-loadplayers"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                playerDirNames.add(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " list_name.txt]"; }
    }


    private class ShotFrequencyArg extends ArgHandler
    {
        public ShotFrequencyArg() { arg="-shotFrequency"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                shoot_frequency=Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }

    private class BotStepSizeArg extends ArgHandler
    {
        public BotStepSizeArg() { arg="-botStepSize"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                bot_step_size=Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }

    private class BulletStepSizeArg extends ArgHandler
    {
        public BulletStepSizeArg() { arg="-bulletStepSize"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                bullet_step_size=Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
    }

/**************************************************************************************************/
/************************ Getter methods **********************************************************/
/**************************************************************************************************/
    /** returns the max x coordinate position */
    public int getMaxX()
    {
        return max_x;
    }

    /** returns the max y coordinate position */
    public int getMaxY()
    {
        return max_y;
    }

    /** returns the min x coordinate position */
    public int getMinX()
    {
        return min_x;
    }

    /** returns the min y coordinate position */
    public int getMinY()
    {
        return min_y;
    }

    /** returns the number of turns that must elapse between shots */
    public int getShotFrequency()
    {
        return shoot_frequency;
    }
    /** returns the number number of steps in the x and/or y direction a bot may move per turn */
    public int getBotStepSize()
    {
        return bot_step_size;
    }
    /** returns the number number of steps in the x and/or y direction a bullet moves per turn */
    public int getBulletStepSize()
    {
        return bullet_step_size;
    }

/**************************************************************************************************/
/************************ Constructors and game initalization code ********************************/
/**************************************************************************************************/
    /** Initializes the argument handlers for parsing command line arguments */
    private void initArgHandlers()
    {
        argHandlers = new Vector<ArgHandler>();
        argHandlers.add(new TeamSizeArg()); 
        argHandlers.add(new NoDisplayArg()); 
        argHandlers.add(new TournamentArg()); 
        argHandlers.add(new DelayArg()); 
        argHandlers.add(new ShotFrequencyArg()); 
        argHandlers.add(new BotStepSizeArg()); 
        argHandlers.add(new BulletStepSizeArg()); 
        argHandlers.add(new LoadPlayerArg()); 
        argHandlers.add(new LoadPlayersArg()); 
    }

    /** Main loop for parsing command line arguments */
    private void getUserPrefs(String[] args)
    {
        int x=0;
        int y=0;
        try
        {
            while(x<args.length)
            {
                for(int z=0;z<argHandlers.size();z++)
                {
                    y=argHandlers.get(z).handleArg(args,x);
                    if(x!=y) break;
                }
                if(x==y) usage();
                x=y;
            }
        }
        catch(Exception e)
        {
            usage();
        }
    }


    private double getMinDistanceToBot(ClobberObject o)
    {
        double mindistance = 10000000000000.0;
        for(int x=0;x<bots.size();x++)
        {
            double distance = bots.get(x).distance(o);
            if(distance < mindistance) mindistance = distance;
        }
        return mindistance;
    }

    private void tallyResults()
    {
        for(int x=0;x<bots.size();x++)
        {
            bots.get(x).res.numKills += bots.get(x).numKills;
            bots.get(x).res.points += (bots.get(x).numKills * 10);
            bots.get(x).res.points += 4;
            bots.get(x).res.numSurvived++;
        }
        for(int x=0;x<deadbots.size();x++)
        {
            deadbots.get(x).res.numKills += deadbots.get(x).numKills;
            deadbots.get(x).res.points += (deadbots.get(x).numKills * 1);
            deadbots.get(x).res.numDied++;
            deadbots.get(x).res.exceptionCount += deadbots.get(x).exceptionCount;
        }
    }

    private void informTeams(Vector<ClobberBot> clobberbots)
    {
        for(int x=0;x<clobberbots.size();x++)
        {
            for(int y=x+1;y<clobberbots.size();y++)
            {
                if(clobberbots.get(x).toString().equals(clobberbots.get(y).toString()))
                {
                    clobberbots.get(x).teammate(clobberbots.get(y));
                    clobberbots.get(y).teammate(clobberbots.get(x));
                }
            }
        }
    }


    private void addBotsToGame(Vector<ClobberBot> clobberbots)
    {
        for(int x=0;x<clobberbots.size();x++)
            addBotToGame(clobberbots.get(x),null);
    }

    private void addBotToGame(ClobberBot bot, ClobberBotRes res)
    {
        int numtries=0;
        ClobberBotMan botman;
        double mindistance;
        do
        {
            numtries++;
            if(numtries > 1000) throw new RuntimeException("I Can't find a place to put all the bots");
            botman = new ClobberBotMan(bot, rand.nextInt(_width), rand.nextInt(_height), res);
            mindistance = getMinDistanceToBot(botman);
        } while(mindistance < MIN_START_DISTANCE);
        bot.informID(botman.clobberObjectID);
        bots.add(botman);
    }

    /** Constructor called from main to create a game */
    private Clobber(String[] args)
    {
        playerFileNames = new Vector<String>();
        playerDirNames = new Vector<String>();
        initArgHandlers();
        getUserPrefs(args);
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setBackground(Color.BLACK);
        this.setContentPane(panel);
        setBackground(Color.black);

        this.show();
        _width=(int)(this.getContentPane().getSize().getWidth());
        _height=(int)(this.getContentPane().getSize().getHeight());
        _width=600;
        _height=600;
	imgBuffer = createImage(_width,_height);
	graphicsBuffer = imgBuffer.getGraphics();
	page=graphicsBuffer;
        this.dispose();

        min_x=EDGE_BUFFER_LEFT;
        min_y=EDGE_BUFFER_TOP;
        max_x=_width-EDGE_BUFFER_RIGHT;
        max_y=_height-EDGE_BUFFER_BOTTOM;

        bot_bul_thresh = (MAX_BOT_GIRTH + BULLET_GIRTH)/2;
        bot_bot_thresh = (MAX_BOT_GIRTH);

        bots = new Vector<ClobberBotMan>();
        deadbots = new Vector<ClobberBotMan>();
        bullets = new Vector<ClobberBullet>(); 
    }


/**************************************************************************************************/
/************************ Game loop code **********************************************************/
/**************************************************************************************************/
    /** Fills out WhatIKnow data structure with the current world state */
    private WhatIKnow getWorldState(ClobberBotMan cbm)
    {
        BotPoint2D me = new BotPoint2D(cbm.pos.getX(), cbm.pos.getY(),cbm.clobberObjectID);

        Vector<BulletPoint2D> bulpts= new Vector<BulletPoint2D>();
        Vector<BulletPoint2D> mybulpts= new Vector<BulletPoint2D>();
        for(int x=0;x<bullets.size();x++)
            if(cbm != bullets.get(x).owner) bulpts.add(new BulletPoint2D(bullets.get(x).pos.getX(), bullets.get(x).pos.getY(), bullets.get(x).clobberObjectID, bullets.get(x).xplus, bullets.get(x).yplus));
            else mybulpts.add(new BulletPoint2D(bullets.get(x).pos.getX(), bullets.get(x).pos.getY(), bullets.get(x).clobberObjectID, bullets.get(x).xplus, bullets.get(x).yplus));

        Vector<BotPoint2D> botpts = new Vector<BotPoint2D>();
        for(int x=0;x<bots.size();x++)
            if(cbm != bots.get(x)) botpts.add(new BotPoint2D(bots.get(x).pos.getX(), bots.get(x).pos.getY(), bots.get(x).clobberObjectID));

        return new WhatIKnow(me, bulpts, mybulpts, botpts);
    }

    /** Asks each bot what it wants to do*/
    private void getBotActions()
    {
        for(int x=0;x<bots.size();x++)
        {
            ClobberBotMan cbm = bots.get(x);
            try
            {
                cbm.action = cbm.bot.takeTurn(getWorldState(cbm));  
            }
            catch(Exception e)
            {
                System.out.println(e);
                e.printStackTrace();
                cbm.action=null;
                cbm.deleteMe=true;
                cbm.numDied++;
                cbm.exceptionCount++;
            }
        }
    }

    /** performs the set of actions the bots have decided upon.  This may result in a bot moving or shooting  */
    private void performBotActions()
    {
        for(int x=0;x<bots.size();x++)
        {
            ClobberBotMan cbm = bots.get(x);
            if(cbm.shotclock>0) cbm.shotclock--;
            int i=(int)(cbm.pos.getX());
            int j=(int)(cbm.pos.getY());
            if(cbm.action==null) return;
            int action = cbm.action.getAction();
            if((action & ClobberBotAction.MOVE)>0)
            {
                if((action & ClobberBotAction.UP)>0) j -= bot_step_size;
                if((action & ClobberBotAction.DOWN)>0) j += bot_step_size;
                if((action & ClobberBotAction.LEFT)>0) i -= bot_step_size;
                if((action & ClobberBotAction.RIGHT)>0) i += bot_step_size;
                cbm.updatePosition(i,j);
            }
            else if(((action & ClobberBotAction.SHOOT)>0) && (cbm.shotclock<=0))
            {
                cbm.shotclock=shoot_frequency;
                int xplus=0; int yplus=0;
                if((action & ClobberBotAction.UP)>0) yplus -= bullet_step_size;
                if((action & ClobberBotAction.DOWN)>0) yplus += bullet_step_size;
                if((action & ClobberBotAction.LEFT)>0) xplus -= bullet_step_size;
                if((action & ClobberBotAction.RIGHT)>0) xplus += bullet_step_size;
                if(xplus!=0 || yplus!=0) bullets.add(new ClobberBullet(cbm, cbm.pos.getX(), cbm.pos.getY(), xplus, yplus));
            }
        }
    }

    /** updates the positions of all the bullets */
    private void updateBulletPositions()
    {
        for(int x=bullets.size()-1;x>=0;x--)
        {
            ClobberBullet bul = bullets.get(x);
            int i=(int)(bul.pos.getX()) + bul.xplus;
            int j=(int)(bul.pos.getY()) + bul.yplus;
            bul.updatePosition(i,j);
            i=(int)(bul.pos.getX());
            j=(int)(bul.pos.getY());
            if((i<=min_x) || (i>=max_x) || (j<=min_y) || (j>=max_y)) bullets.remove(x);
        }
    }

    /** Checks for collisions between bullets and bots */
    private void checkBulletVSBotCollisions()
    {
        for(int x=bullets.size()-1;x>=0;x--)
        {
            ClobberBullet bul = bullets.get(x);
            for(int y=bots.size()-1;y>=0;y--)
            {
                ClobberBotMan bot = bots.get(y);
                if(bul.owner==bot) continue;
                //if(bot.distance(bul) < bot_bul_thresh)
                if(bot.collision(bul)) 
                {
                    //System.out.println("Ouch!");
                    if(!bot.deleteMe) bot.numDied++;

                    bot.deleteMe=true;
                    bul.deleteMe=true;
                    bul.owner.numKills++; //@@@ Could potentially overcount kills
                }
            }
            if(bul.deleteMe) bullets.remove(x);
        }
    }

    /** Checks for collisions between bots */
    private void checkBotVSBotCollisions()
    {
        for(int x=bots.size()-1;x>=0;x--)
        {
            ClobberBotMan bot1 = bots.get(x);
            for(int y=x-1;y>=0;y--)
            {
                if(y==x) continue;
                ClobberBotMan bot2 = bots.get(y);
                //if(bot1.distance(bot2) < bot_bot_thresh)
                if(bot1.collision(bot2))
                {
                    if(!bot1.deleteMe) bot1.numDied++;
                    if(!bot2.deleteMe) bot2.numDied++;
                    //System.out.println("Ooof!");

                    bot1.deleteMe=true;
                    bot2.deleteMe=true;
                    bot1.numKills++; // @@@ Could overcount kills here
                    bot2.numKills++; // @@@ could overcount kills here
                }
            }
            if(bot1.deleteMe) deadbots.add(bots.remove(x));
        }
    }

    /** Updates the display. */
    private void updateDisplay(Thread t)
    {
        if(!display) return;
        try { t.sleep(delay); } catch(Exception e){} 

        page.clearRect(0,0,_width,_height);
        for(int x=0;x<bots.size();x++)
        {
            ClobberBotMan cbm = bots.get(x);
            cbm.bot.drawMe(page, cbm.pos);
        }
        for(int x=bullets.size()-1;x>=0;x--)
        {
            ClobberBullet bul = bullets.get(x);
            int i=(int)(bul.pos.getX());
            int j=(int)(bul.pos.getY());
            int n=i-Clobber.BULLET_GIRTH/2 -1;
            int m=j-Clobber.BULLET_GIRTH/2 -1;
            page.setColor(Color.green);
            page.fillOval(n,m,BULLET_GIRTH,BULLET_GIRTH);
        }

	screen.drawImage(imgBuffer,0,0,this);

        try { t.wait(); } catch(Exception e){}
    }

    /** Prints the curr score of each bot to the screen */
    public void printResults()
    {
        System.out.println("Living bots");
        for(int x=0;x<bots.size();x++)
        {
            System.out.println(bots.get(x));
        }
        System.out.println("Dead bots");
        for(int x=0;x<deadbots.size();x++)
        {
            System.out.println(deadbots.get(x));
        }
    }

    /** Main game loop */
    private void play()
    {
        int x=0;
        int turnLimit=10000;
        if(display) 
        {
            this.show();
            screen = panel.getGraphics();
        }

        Thread t = new Thread();
        t.start();

        while(x<turnLimit)
        {
            x++;
            getBotActions();
            performBotActions();
            updateBulletPositions();

            if(display) updateDisplay(t);

            checkBulletVSBotCollisions();
            checkBotVSBotCollisions();

            if(bots.size()<=1) break;
        }
        if(x==turnLimit)
        {
            //System.out.println("****************Turn Limit Reached **************************");
        } 

        if(display) 
        {
            this.dispose();
        }
    }

    /** Prints out the syntax for running this program from the command line. */
    public void usage()
    {
        System.out.print("Usage: java Clobber ");
        for(int x=0;x<argHandlers.size();x++)
        {
            System.out.print(argHandlers.get(x));
        }
        System.out.println();
        System.exit(-1);
    }

    class MyClassLoader extends ClassLoader {
       Class cls;
       
       public Class retClass () {
          return cls;
       }

       public MyClassLoader (String classname) throws Exception
       {
             FileInputStream in = new FileInputStream (classname);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             int ch;
             while ((ch = in.read()) != -1) {
                buffer.write(ch);
             }
             byte[] rec = buffer.toByteArray();
             cls = defineClass (classname.substring(0,classname.length()-6), rec, 0, rec.length);
       }
    }

    private void loadHuman(Vector<ClobberBot> bots) throws Exception
    {
            bots.add(new GUIClobberBot(this));
    }

    private void loadPlayerFromFile(Vector<ClobberBot> bots, String fname) throws Exception
    {
        System.out.println("Loading player " + fname);
        Class cls = new MyClassLoader(fname).retClass();
        Constructor[] ct = cls.getConstructors();
        Object[] oarr = new Object[1];
        oarr[0]=this;
        for(int x=0;x<teamsize;x++) bots.add((ClobberBot)(ct[0].newInstance(this)));
        //bots.add((ClobberBot)(ct[0].newInstance(oarr)));
    }

    private void loadPlayersInDir(Vector<ClobberBot> bots, String dirname) throws Exception
    {
        File dir = new File(dirname);
        File[] contents = dir.listFiles();
        for(int x=0;x<contents.length;x++)
        {
            String fname = contents[x].toString();
            StringTokenizer st = new StringTokenizer(fname, "\\/");
            st.nextToken();
            playerFileNames.add(st.nextToken());
        }
    }

    private void loadPlayersFromFile(Vector<ClobberBot> bots, String fname) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(fname));
        String line;
        while((line=br.readLine()) != null)
        {
            playerFileNames.add(line);
        }
    }


    private Vector<ClobberBot> loadBots2() throws Exception
    {
        Vector<ClobberBot> bots  = new Vector<ClobberBot>();
        Iterator<String> it = playerDirNames.iterator();
        while(it.hasNext())
        {
            loadPlayersFromFile(bots, it.next());
        }

        it = playerFileNames.iterator();
        while(it.hasNext())
        {
            loadPlayerFromFile(bots, it.next());
        }

        return bots;
    }

    private Vector<ClobberBot> loadBots()
    {

        Vector<ClobberBot> bots  = new Vector<ClobberBot>();

        bots.add(new ClobberBot(null));
        bots.add(new ClobberBot2(null));
        bots.add(new ClobberBot3(this));
        bots.add(new ClobberBot4(this));
        bots.add(new ClobberBot5(this));

        //bots.add(new aalgawas(this ));
        //bots.add(new chaney(this ));
        //bots.add(new Dglen(this ));
        //bots.add(new eatlakso(this ));
        //bots.add(new eatlakso(this ));
        //bots.add(new eatlakso(this ));
        //bots.add(new eatlakso(this ));
        //bots.add(new glyons(this ));
        //bots.add(new jdohse(this ));
        //bots.add(new jdohse(this ));
        //bots.add(new jdohse(this ));
        //bots.add(new jdohse(this ));
        //bots.add(new jdohse(this ));
        //bots.add(new Jrodrigu(this ));
        //bots.add(new kybaker(this ));
        //bots.add(new sobendor(this ));
        //bots.add(new achild(this ));
        //bots.add(new achild(this ));
        //bots.add(new achild(this ));
        //bots.add(new achild(this ));
        //bots.add(new achild(this ));


        return bots;
    }


    private class ClobberTourney
    {
        Vector<ClobberBotRes> b225;
        Vector<ClobberBotRes> b125_1;
        Vector<ClobberBotRes> b125_2;
        Vector<ClobberBotRes> b125_3;
        Vector<ClobberBotRes> brand;
        Vector<ClobberBotRes> ball;
        String[] args;
        Clobber dummy;

        private Vector<ClobberBotRes> load225Bots()
        {
            Vector<ClobberBotRes> clobberbots = new Vector<ClobberBotRes>();

            return clobberbots;
            
        }

        private Vector<ClobberBotRes> loadRandomBots()
        {
            Vector<ClobberBotRes> clobberbots = new Vector<ClobberBotRes>();

            clobberbots.add(new ClobberBotRes(new ClobberBot(null)));
            clobberbots.add(new ClobberBotRes(new ClobberBot2(null)));
            clobberbots.add(new ClobberBotRes(new ClobberBot3(null)));
            clobberbots.add(new ClobberBotRes(new ClobberBot4(null)));

            return clobberbots;
        }

        private Vector<ClobberBotRes> loadSection1Bots()
        {
            Vector<ClobberBotRes> clobberbots = new Vector<ClobberBotRes>();

            clobberbots.add(new ClobberBotRes(new p1(dummy)));
            clobberbots.add(new ClobberBotRes(new p2(dummy)));
            clobberbots.add(new ClobberBotRes(new p3(dummy)));
            clobberbots.add(new ClobberBotRes(new p4(dummy)));
            clobberbots.add(new ClobberBotRes(new p5(dummy)));
            clobberbots.add(new ClobberBotRes(new p6(dummy)));
            clobberbots.add(new ClobberBotRes(new p7(dummy)));
            clobberbots.add(new ClobberBotRes(new p8(dummy)));
            clobberbots.add(new ClobberBotRes(new p9(dummy)));

            //clobberbots.add(new ClobberBotRes(new mweaver(dummy)));
            //clobberbots.add(new ClobberBotRes(new crsteven(dummy)));
            //clobberbots.add(new ClobberBotRes(new Dstone(dummy)));
            //clobberbots.add(new ClobberBotRes(new epeterso(dummy)));
            //clobberbots.add(new ClobberBotRes(new ClobberBotJA(dummy)));
            //clobberbots.add(new ClobberBotRes(new jhodges(dummy)));
            //clobberbots.add(new ClobberBotRes(new mgraybil(dummy)));
            //clobberbots.add(new ClobberBotRes(new mlampe(dummy)));
            //clobberbots.add(new ClobberBotRes(new agraham(dummy)));

            return clobberbots;
        }
        private Vector<ClobberBotRes> loadSection2Bots()
        {
            Vector<ClobberBotRes> clobberbots = new Vector<ClobberBotRes>();
            //clobberbots.add(new ClobberBotRes(new (dummy)));
            return clobberbots;
        }
        private Vector<ClobberBotRes> loadSection3Bots()
        {
            Vector<ClobberBotRes> clobberbots = new Vector<ClobberBotRes>();

            //clobberbots.add(new ClobberBotRes(new aalgawas(dummy)));
            //clobberbots.add(new ClobberBotRes(new chaney(dummy)));
            //clobberbots.add(new ClobberBotRes(new glyons(dummy)));
            //clobberbots.add(new ClobberBotRes(new jdohse(dummy)));
            //clobberbots.add(new ClobberBotRes(new Dglen(dummy)));
            //clobberbots.add(new ClobberBotRes(new Jrodrigu(dummy)));
            //clobberbots.add(new ClobberBotRes(new eatlakso(dummy)));
            //clobberbots.add(new ClobberBotRes(new kybaker(dummy)));
            //clobberbots.add(new ClobberBotRes(new sobendor(dummy)));

            return clobberbots;
        }

        public String toString(Vector<ClobberBotRes> bots)
        {
            String str="BOT\tPoints\tnumDied\tnumKills\tnumSurvived\texcpetions\n";
            for(int x=0;x<bots.size();x++)
                str+=bots.get(x).toString();
            return str;
        }

        public String toString()
        {
            String str="";
            if(brand!=null) str += "Random bots\n" + toString(brand);
            if(b225!=null) str += "225 bots\n" + toString(b225);
            if(b125_1!=null) str += "125 section 1 bots\n" + toString(b125_1);
            if(b125_2!=null) str += "125 section 2 bots\n" + toString(b125_2);
            if(b125_3!=null) str += "125 section 3 bots\n" + toString(b125_3);
            return str;
        }

        private ClobberBot constructInstance(ClobberBot bot) throws Exception
        {
            Class c = bot.getClass();
            Class[] carr = {Clobber.class};
            Constructor<ClobberBot> con = c.getConstructor(carr);
            return con.newInstance(dummy);
        }

        public void runVSAllTourney(Vector<ClobberBotRes> bots, int numgames) throws Exception
        {
            for(int x=0;x<numgames;x++)
            {
                Clobber cb = new Clobber(args);
                for(int y=0;y<bots.size();y++) 
                    cb.addBotToGame(constructInstance(bots.get(y).bot), bots.get(y));
                cb.play();
                cb.tallyResults();
            }
        }

        public void runPairwiseVSRandomTourney(Vector<ClobberBotRes> bots) throws Exception
        {
            Vector<ClobberBotRes> randBots = loadRandomBots();
            for(int x=0;x<bots.size();x++)
            {
                for(int y=0;y<randBots.size();y++)
                {
                    //System.out.println("Playing " + bots.get(x).bot + " vs " + bots.get(y).bot);
                    Clobber cb = new Clobber(args);
                    cb.addBotToGame(constructInstance(bots.get(x).bot), bots.get(x));
                    cb.addBotToGame(constructInstance(randBots.get(y).bot), randBots.get(y));
                    cb.play();
                    cb.tallyResults();
                }
            }
        }

        public void runPairwiseTourney(Vector<ClobberBotRes> bots) throws Exception
        {
            for(int x=0;x<bots.size();x++)
            {
                for(int y=x+1;y<bots.size();y++)
                {
                    //System.out.println("Playing " + bots.get(x).bot + " vs " + bots.get(y).bot);
                    Clobber cb = new Clobber(args);
                    cb.addBotToGame(constructInstance(bots.get(x).bot), bots.get(x));
                    cb.addBotToGame(constructInstance(bots.get(y).bot), bots.get(y));
                    cb.play();
                    cb.tallyResults();
                }
            }
        }

        public ClobberTourney(String[] args, Clobber fud)
        {
            this.args=args;
            //dummy = new Clobber(args);
            dummy = fud;

            b225   = load225Bots();
            b125_1 = loadSection1Bots();
            //b125_2 = loadSection2Bots();
            //b125_3 = loadSection3Bots();
            brand  = loadRandomBots();
            ball = new Vector<ClobberBotRes>();

            if(b225!=null) for(int x=0;x<b225.size();x++) ball.add(b225.get(x));
            if(b125_1!=null) for(int x=0;x<b125_1.size();x++) ball.add(b125_1.get(x));
            if(b125_2!=null) for(int x=0;x<b125_2.size();x++) ball.add(b125_2.get(x));
            if(b125_3!=null) for(int x=0;x<b125_3.size();x++) ball.add(b125_3.get(x));
            if(brand!=null) for(int x=0;x<brand.size();x++) ball.add(brand.get(x));
        }

        /** Not completed yet. */
        private void runTourney() throws Exception
        {
            for(int x=0;x<numTournaments;x++)
            {
                System.out.println("Running tourney " + (x+1));
                runVSAllTourney(ball, 1);
                runPairwiseTourney(ball);
                runPairwiseVSRandomTourney(ball);
            }
        }


    }


/**************************************************************************************************/
/************************ Main method *************************************************************/
/**************************************************************************************************/
    public static void main(String[] args) throws Exception
    {
        Clobber game = new Clobber(args);

        if(game.tournament) 
        {
            ClobberTourney ct;

            System.out.println("\n\n****************** Running VS all tourney **********************");
            ct = game.new ClobberTourney(args, game);
            for(int x=0;x<game.numTournaments;x++) ct.runVSAllTourney(ct.ball, 10);
            System.out.println(ct);

            System.out.println("\n\n**************************** Running pairwise tourney ***************************");
            ct = game.new ClobberTourney(args, game);
            for(int x=0;x<game.numTournaments;x++) ct.runPairwiseTourney(ct.ball);
            System.out.println(ct);

            System.out.println("\n\n****************** Running pairwise VS random tourney **********************");
            ct = game.new ClobberTourney(args, game);
            for(int x=0;x<game.numTournaments;x++) ct.runPairwiseVSRandomTourney(ct.ball);
            System.out.println(ct);
        }
        else 
        {
            Vector<ClobberBot> bots;
            if(game.playerFileNames.size()>0 || game.playerDirNames.size() > 0)
                bots = game.loadBots2();
            else bots = game.loadBots();
            //game.loadHuman(bots);
            game.addBotsToGame(bots);
            game.informTeams(bots);
            game.play();
            game.printResults();
        }
    }
}





