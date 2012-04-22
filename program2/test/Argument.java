package test;

/** abstract class used as a basis for puting together command line argument handler/parser.  */
public abstract class Argument
{
    boolean caseSensitive=true;
    boolean optional=true;
    boolean processed=false;
    String arg;

    /** abstract method for handling the command line argument */
    public abstract int handleArg(String[] args, int x) throws Exception;

    /** determines the match between arg and what this argument expects */
    public boolean argMatch(String arg)
    {
        if(caseSensitive) return arg.equals(this.arg);
        else return arg.equalsIgnoreCase(this.arg);
    }

    /** */
    public String toString()
    {
        if(optional) return "[" + arg + "]";
        else return arg;
    }


}
