package test;

import java.util.*;

/** Handler for command line argument parsing. */
public class ArgHandler
{
    /** Keeps track of all the command line argument parsers */
    private Vector<Argument> argHandlers;
    private String pname;

    /** */
    public ArgHandler(String pname)
    {
        argHandlers=new Vector<Argument>();
        this.pname=pname;
    }

    
    /** Adds a command line argument parser to this arg handler*/
    public void add(Argument arg)
    {
        argHandlers.add(arg);
    }

    /** Prints out the syntax for running this program from the command line. */
    public void usage()
    {
        System.out.print("Usage: java " + pname + " ");
        for(int x=0;x<argHandlers.size();x++)
        {
            System.out.print(argHandlers.get(x) + " ");
        }
        System.out.println();
        System.exit(-1);
    }

    /** Main loop for parsing command line arguments */
    public void getUserPrefs(String[] args)
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
        // Check non-optional arguments to make sure they were entered
        for(int z=0;z<argHandlers.size();z++)
        {
            if(!argHandlers.get(z).processed && !argHandlers.get(z).optional) usage();
        }

    }
}


