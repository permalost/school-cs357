

import java.awt.geom.*;
import java.util.*;

/**
 * This class is used by the Clobber game to convey information to
 * the ClobberBots in the game.  It tells the bot about its current 
 * position, the position of other bots, and the position of bullets
 * in the world.
 */
public class WhatIKnow
{
    public BotPoint2D me;
    public Vector<BulletPoint2D> bullets;
    public Vector<BulletPoint2D> mybullets;
    public Vector<BotPoint2D> bots;

    public WhatIKnow(BotPoint2D me, Vector<BulletPoint2D> bullets, Vector<BulletPoint2D> mybullets, Vector<BotPoint2D> bots)
    {
        this.me = me;
        this.bullets = bullets;
        this.mybullets = mybullets;
        this.bots = bots;
    }
}

