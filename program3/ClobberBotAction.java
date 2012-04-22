

/**
   This class stores a bot action.
*/
public class ClobberBotAction
{
    // Action related variables
    public static final int NONE = 0;
    public static final int MOVE = 1;
    public static final int SHOOT = 2;

    // direction related variables
    public static final int UP = 4;
    public static final int DOWN = 8;
    public static final int LEFT = 16;
    public static final int RIGHT = 32;

    private int todo;

    /** Initializes the action.
     *  @param action one of NONE, MOVE, SHOOT
     *  @param direction one of UP, DOWN, LEFT, RIGHT */
    public ClobberBotAction(int action, int direction)
    {
        todo = action | direction;
    }

    /** Returns action. */
    public int getAction()
    {
        return todo;
    }

}

