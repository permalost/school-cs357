package test;

import java.util.*;

/** Grocery problem generator. */
public class GroceryProbGen
{
    private ArgHandler m_argHandlers;
    private int m_numbags;
    private int m_maxbagsize;
    private int m_numitems;
    private int m_numitemtypes;
    private int m_minitemsize=1;
    private int m_maxitemsize;
    private int m_minconstraints=0;
    private int m_maxconstraints=0;
    private String m_constrainttype="";
    private Random m_rand;

    /** Sets up command line arguments, parses user prefs/parameters, and does some basic error checking. */
    public GroceryProbGen(String[] args)
    {
        m_rand=new Random();
        initArgHandlers();
        m_argHandlers.getUserPrefs(args);

        if(m_numbags<=0 || 
           m_maxbagsize < m_minitemsize || 
           m_numitems<=0 || 
           //m_numitemtypes<=0 || ###
           m_minitemsize<=0 || 
           m_maxitemsize < m_minitemsize ||
           m_maxconstraints<m_minconstraints ||
           m_maxconstraints<0) 
           {
              System.out.println("Bad value for parameter");
              m_argHandlers.usage();
           }
    }

    /** Add this applications command line arguments to my argument handler. */
    private void initArgHandlers()
    {
        m_argHandlers=new ArgHandler("GroceryProbGen");
        m_argHandlers.add(new NumBagsArg()); 
        m_argHandlers.add(new MaxBagSizeArg()); 
        m_argHandlers.add(new NumItemsArg()); 
        //m_argHandlers.add(new NumItemTypesArg()); ###
        m_argHandlers.add(new MaxItemSizeArg()); 
        m_argHandlers.add(new MinItemSizeArg()); 
        m_argHandlers.add(new ConstraintTypeArg()); 
        m_argHandlers.add(new MinConstraintsArg());
        m_argHandlers.add(new MaxConstraintsArg());
    }

    /** Prints out a randomly generated (based on user entered parameters) grocery packing problem.*/
    public void genProblem()
    {
        int itemSizeRange = m_maxitemsize-m_minitemsize+1;
        int constraintRange = m_maxconstraints-m_minconstraints+1;
        Vector<String> items = new Vector<String>();

        //for(int y=0;y<m_numitemtypes;y++) items.add("item" + y); ###
        for(int y=0;y<m_numitems;y++) items.add("item" + y);

        System.out.println(m_numbags);
        System.out.print(m_maxbagsize);
        Vector<String> scratch;
        for(int x=0;x<m_numitems;x++)
        {
            System.out.println();
            scratch = new Vector<String>(items);
            //System.out.print(scratch.remove(m_rand.nextInt(items.size()))); ###
            System.out.print(scratch.remove(x));
            System.out.print(" " + (m_rand.nextInt(itemSizeRange)+m_minitemsize));
            int numConstraints = m_rand.nextInt(constraintRange)+m_minconstraints;
            if(numConstraints > 0)
            {
                String constraintType = (m_constrainttype.equals("")) ? (m_rand.nextBoolean() ? "+" : "-") : m_constrainttype;
                System.out.print(" " + constraintType);
                for(int y=0;y<numConstraints;y++)
                {
                    if(scratch.size()==0) break;
                    System.out.print(" " + scratch.remove(m_rand.nextInt(scratch.size())));
                }
            }
        }
        System.out.println();
    }

    public static void main(String args[])
    {
        GroceryProbGen gpg = new GroceryProbGen(args);
        gpg.genProblem();
    }


/***********************************************************************/
/*************** Argument Handlers *************************************/
/***********************************************************************/
    private class NumItemTypesArg extends Argument
    {
        public NumItemTypesArg() { arg="-numitemtypes"; optional=false;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_numitemtypes=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class ConstraintTypeArg extends Argument
    {
        public ConstraintTypeArg() { arg="-constraint"; optional=true;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                ++x;
                if(!"+".equals(args[x]) && !"-".equals(args[x])) throw new Exception("Argument must be - or +");
                m_constrainttype=args[x];
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class MinConstraintsArg extends Argument
    {
        public MinConstraintsArg() { arg="-minconstraints"; optional=true;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_minconstraints=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class MaxConstraintsArg extends Argument
    {
        public MaxConstraintsArg() { arg="-maxconstraints"; optional=true;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_maxconstraints=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class MaxItemSizeArg extends Argument
    {
        public MaxItemSizeArg() { arg="-maxitemsize"; optional=false;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_maxitemsize=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class MinItemSizeArg extends Argument
    {
        public MinItemSizeArg() { arg="-minitemsize"; optional=true;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_minitemsize=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class NumItemsArg extends Argument
    {
        public NumItemsArg() { arg="-numitems"; optional=false;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_numitems=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class MaxBagSizeArg extends Argument
    {
        public MaxBagSizeArg() { arg="-maxbagsize"; optional=false;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_maxbagsize=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }

    private class NumBagsArg extends Argument
    {
        public NumBagsArg() { arg="-numbags"; optional=false;}

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                m_numbags=Integer.parseInt(args[++x]);
                processed=true;
                return ++x;
            }
            else return x;
        }
    }
}


