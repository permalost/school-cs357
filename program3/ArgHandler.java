
/** This is an abstract class that I use as a basis for puting together my command
 * line argument handler/parser.  This makes it easier for me to keep the usage
 * printout and actuall command line argument requirements consistant.  To see how 
 * I use this look in Clobber.java. */
public abstract class ArgHandler
{
    boolean caseSensitive=true;;
    boolean optional=true;;
    String arg;

    public abstract int handleArg(String[] args, int x) throws Exception;

    public boolean argMatch(String arg)
    {
        if(caseSensitive) return arg.equals(this.arg);
        else return arg.equalsIgnoreCase(this.arg);
    }

    public String toString()
    {
        if(optional) return "[" + arg + "]";
        else return arg;
    }


}
