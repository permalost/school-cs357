/* File: q3.c
 * Author: Quincy Bowers  quincybowers@u.boisestate.edu
 * Date: Sun October 02, 2011
 * Description: A checkers AI player.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/times.h>
#include <time.h>
#include <limits.h>
#include <float.h>
#include <pthread.h>
#include <errno.h>
#include "q3.h"

/* This defines the starting depth for the iterative deepening search. */
#ifdef __LIMIT_DEPTH__
#define START_DEPTH 1
#else
#define START_DEPTH 5
#endif


float SecPerMove;
char board[8][8];
char bestmove[12];
int me,cutoff,endgame;
long NumNodes;
int MaxDepth;
char prog_name[50];

/*** For timing ***/
clock_t start;
struct tms bff;

/*** For the jump list ***/
int jumpptr = 0;
int jumplist[48][12];

/*** For the move list ***/
int numLegalMoves = 0;
int movelist[48][12];

/* Copy a square state */
void CopyState(char *dest, char src)
{
    char state;

    *dest &= Clear;
    state = src & 0xE0;
    *dest |= state;
}

/* Reset board to initial configuration */
void ResetBoard(void)
{
    int x,y;
    char pos;

    pos = 0;
    for(y=0; y<8; y++)
        for(x=0; x<8; x++)
        {
            if(x%2 != y%2) {
                board[y][x] = pos;
                if(y<3 || y>4) board[y][x] |= Piece; else board[y][x] |= Empty;
                if(y<3) board[y][x] |= Red; 
                if(y>4) board[y][x] |= White;
                pos++;
            } else board[y][x] = 0;
        }
    endgame = 0;
}

/* Add a move to the legal move list */
void AddMove(char move[12])
{
    int i;

    for(i=0; i<12; i++) movelist[numLegalMoves][i] = move[i];
    numLegalMoves++;
}

/* Finds legal non-jump moves for the King at position x,y */
void FindKingMoves(char board[8][8], int x, int y) 
{
    int i,j,x1,y1;
    char move[12];

    memset(move,0,12*sizeof(char));

    /* Check the four adjacent squares */
    for(j=-1; j<2; j+=2)
        for(i=-1; i<2; i+=2)
        {
            y1 = y+j; x1 = x+i;
            /* Make sure we're not off the edge of the board */
            if(y1<0 || y1>7 || x1<0 || x1>7) continue; 
            if(empty(board[y1][x1])) {  /* The square is empty, so we can move there */
                move[0] = number(board[y][x])+1;
                move[1] = number(board[y1][x1])+1;    
                AddMove(move);
            }
        }
}

/* Finds legal non-jump moves for the Piece at position x,y */
void FindMoves(int player, char board[8][8], int x, int y) 
{
    int i,j,x1,y1;
    char move[12];

    memset(move,0,12*sizeof(char));

    /* Check the two adjacent squares in the forward direction */
    if(player == 1) j = 1; else j = -1;
    for(i=-1; i<2; i+=2)
    {
        y1 = y+j; x1 = x+i;
        /* Make sure we're not off the edge of the board */
        if(y1<0 || y1>7 || x1<0 || x1>7) continue; 
        if(empty(board[y1][x1])) {  /* The square is empty, so we can move there */
            move[0] = number(board[y][x])+1;
            move[1] = number(board[y1][x1])+1;    
            AddMove(move);
        }
    }
}

/* Adds a jump sequence the the legal jump list */
void AddJump(char move[12])
{
    int i;

    for(i=0; i<12; i++) jumplist[jumpptr][i] = move[i];
    jumpptr++;
}

/* Finds legal jump sequences for the King at position x,y */
int FindKingJump(int player, char board[8][8], char move[12], int len, int x, int y) 
{
    int i,j,x1,y1,x2,y2,FoundJump = 0;
    char one,two,mymove[12],myboard[8][8];

    memcpy(mymove,move,12*sizeof(char));

    /* Check the four adjacent squares */
    for(j=-1; j<2; j+=2)
        for(i=-1; i<2; i+=2)
        {
            y1 = y+j; x1 = x+i;
            y2 = y+2*j; x2 = x+2*i;
            /* Make sure we're not off the edge of the board */
            if(y2<0 || y2>7 || x2<0 || x2>7) continue; 
            one = board[y1][x1];
            two = board[y2][x2];
            /* If there's an enemy piece adjacent, and an empty square after him, we can jump */
            if(!empty(one) && color(one) != player && empty(two)) {
                /* Update the state of the board, and recurse */
                memcpy(myboard,board,64*sizeof(char));
                myboard[y][x] &= Clear;
                myboard[y1][x1] &= Clear;
                mymove[len] = number(board[y2][x2])+1;
                FoundJump = FindKingJump(player,myboard,mymove,len+1,x+2*i,y+2*j);
                if(!FoundJump) {
                    FoundJump = 1;
                    AddJump(mymove);
                }
            }
        }
    return FoundJump;
}

/* Finds legal jump sequences for the Piece at position x,y */
int FindJump(int player, char board[8][8], char move[12], int len, int x, int y) 
{
    int i,j,x1,y1,x2,y2,FoundJump = 0;
    char one,two,mymove[12],myboard[8][8];

    memcpy(mymove,move,12*sizeof(char));

    /* Check the two adjacent squares in the forward direction */
    if(player == 1) j = 1; else j = -1;
    for(i=-1; i<2; i+=2)
    {
        y1 = y+j; x1 = x+i;
        y2 = y+2*j; x2 = x+2*i;
        /* Make sure we're not off the edge of the board */
        if(y2<0 || y2>7 || x2<0 || x2>7) continue; 
        one = board[y1][x1];
        two = board[y2][x2];
        /* If there's an enemy piece adjacent, and an empty square after hum, we can jump */
        if(!empty(one) && color(one) != player && empty(two)) {
            /* Update the state of the board, and recurse */
            memcpy(myboard,board,64*sizeof(char));
            myboard[y][x] &= Clear;
            myboard[y1][x1] &= Clear;
            mymove[len] = number(board[y2][x2])+1;
            FoundJump = FindJump(player,myboard,mymove,len+1,x+2*i,y+2*j);
            if(!FoundJump) {
                FoundJump = 1;
                AddJump(mymove);
            }
        }
    }
    return FoundJump;
}

/* Determines all of the legal moves possible for a given state */
int FindLegalMoves(struct State *state)
{
    int x, y, i;
    char move[12], board[8][8];

    memset(move,0,12*sizeof(char));
    jumpptr = numLegalMoves = 0;
    memcpy(board,state->board,64*sizeof(char));

    /* Loop through the board array, determining legal moves/jumps for each piece */
    for(i=1; i<=32; ++i)
    {
        NumberToXY(i, &x, &y);

        if(color(board[y][x]) == state->player && !empty(board[y][x])) 
        {
            if(king(board[y][x])) { /* King */
                move[0] = number(board[y][x])+1;
                FindKingJump(state->player,board,move,1,x,y);
                if(!jumpptr) FindKingMoves(board,x,y);
            } 
            else if(piece(board[y][x])) { /* Piece */
                move[0] = number(board[y][x])+1;
                FindJump(state->player,board,move,1,x,y);
                if(!jumpptr) FindMoves(state->player,board,x,y);    
            }
        }    
    }
    if(jumpptr) {
        for(x=0; x<jumpptr; x++) 
            for(y=0; y<12; y++) 
                state->movelist[x][y] = jumplist[x][y];
        state->numLegalMoves = jumpptr;
    } 
    else {
        for(x=0; x<numLegalMoves; x++) 
            for(y=0; y<12; y++) 
                state->movelist[x][y] = movelist[x][y];
        state->numLegalMoves = numLegalMoves;
    }
    return (jumpptr+numLegalMoves);
}

/*
 * Finds the best move available in the time limit.
 */
void FindBestMove(int player)
{
    int status;
    unsigned long wake_time;
    pthread_t searchThread;

    /* Create a new thread to search for the best move. */
    if ((status = pthread_create(&searchThread, NULL, FindBestMoveThread, &player)) != 0)
    {
        fprintf(stderr, "Failed to create search thread!\n");

        switch (status)
        {
            case EAGAIN:
                fprintf(stderr, "The system does not have the resources to create more threads.\n");
                break;
            case EINVAL:
                fprintf(stderr, "Invalid attribute specified by pthread_create.\n");
                break;
            case EPERM:
                fprintf(stderr, "You have insufficient privelidges to create a thread with these parameters.\n");
                break;
            default:
                fprintf(stderr, "An unknown error [%d] was returned by pthread_create.\n", status);
                break;
        }

        exit(status);
    }

    /* Detach the search thread.  It will just keep running until we cancel it 
     * later. */
    pthread_detach(searchThread);

    /* Start a timer by calling usleep for (SecPerMove - 1ms), which should give
     * us plenty of time to kill the thread and return our move to the server. */
    wake_time = ((unsigned long)(SecPerMove * 1000000L)) - 1000L;
    usleep(wake_time);

    /* And notify the search thread that we need an answer NOW!!! */
    pthread_cancel(searchThread);
}


/* Employ your favorite search to find the best move here.  */
/* This example code shows you how to call the FindLegalMoves function */
/* and the PerformMove function */
void *FindBestMoveThread(void *arg)
{
    int i,x, depth; 
    double val;
    double alpha = DBL_MIN;
    double beta  = DBL_MAX;
    struct State state; 
    struct State newState; 
    int player = *((int *)arg);
    
    int max_depth = (MaxDepth == -1) ? INT_MAX : MaxDepth;

    /* Set up the current state */
    state.player = player;
    memcpy(state.board,board,64*sizeof(char));
    memset(bestmove,0,12*sizeof(char));

    /* Find the legal moves for the current state */
    FindLegalMoves(&state);

    /* Pick a random move as the best move just to get started. */
    i = rand() % state.numLegalMoves;
    memcpy(bestmove, state.movelist[i], MoveLength(state.movelist[i]));

    /* If there is just one legal move it has already been copied to bestmove.
     * Perform that move immediately.  Otherwise, we search. */
    if (state.numLegalMoves > 1)
    {
        /* Do an iterative deepening search for the best move.  This uses alpha
         * beta pruning to discard unnecessary branches from the search. */
        for (depth=START_DEPTH; depth <= max_depth; depth++)
        {
            /* Check to see if this thread has been cancelled before moving on. */
            pthread_testcancel();

            /* Walk the move list and find the best one. */
            for (x=0; x<state.numLegalMoves; ++x)
            {
                /* Copy the current board state. */
                memcpy(&newState, &state, sizeof(struct State));

                /* Perform a move on the copied state. */
                PerformMove(newState.board, 
                        state.movelist[x],
                        MoveLength(state.movelist[x]));

                /* Toggle the current player. */
                newState.player = (newState.player == 1) ? 2 : 1;

                /* Perform a depth limited MiniMax search for the best move.
                 * Uses Alpha-Beta pruning. */
                val = minVal(alpha, beta, &newState, depth);

                if (val > alpha)
                {
                    i     = x;
                    alpha = val;
                }
            }

            /* Clear the best move and then copy in the new best move. */
            memset(bestmove, 0, 12*sizeof(char));
            memcpy(bestmove, state.movelist[i], MoveLength(state.movelist[i]));
        }
    }

    return NULL;
}

/*
 * Evaluates the board from the perspective of this AI player and returns a
 * score indicating the board's utility.
 *
 * Higher board scores are better boards for this player than lower ones.
 *
 * The board evaluation assigns a score to each player based on the number and
 * type of pieces it controls and the positions they occupy.  The two scores are
 * then used to create a ratio of (AI_player / opponent).  A score of 1.0
 * means the players are evenly matched.  Scores less than 1.0 mean a 
 * disadvantage to the this AI player.  Scores greater than 1.0 mean an 
 * advantage to the AI player.
 *
 * @param state the board state being evaluated.
 *
 * @return the utility score for this board state from the AI player's
 *         perspective.
 */
double evaluateBoard(struct State *state)
{
    double score       = 0.0;
    double red_score   = 0.0;
    double white_score = 0.0;
    int i, x, y;

    #define PAWN_SCORE       1.0
    #define KING_SCORE       1.4
    #define EDGE_SCORE       0.35
    #define MAIN_DIAG_SCORE  0.4
    #define HIGH_RANK_SCORE  0.25
    #define WIN_SCORE        DBL_MAX
    #define LOSE_SCORE       DBL_MIN

    /* If this board represents a loss of game return a really bad score. */
    if (state->numLegalMoves == 0)
    {
        return LOSE_SCORE;
    }

    for (i=1; i<=32; ++i)
    {
        NumberToXY(i, &x, &y);

        /* Only evaluate positions with pieces. */
        if (empty(state->board[y][x]))  continue;

        /* WHITE PIECE */
        if (state->board[y][x] & White)
        {
            /* King or Pawn? */
            if (king(state->board[y][x]))
            {
                white_score += KING_SCORE;

                /* On main diagonal?  Increases likelihood of jumping. */
                if (x + y == 7)
                {
                    white_score += MAIN_DIAG_SCORE;
                }
            }
            else
            {
                white_score += PAWN_SCORE;

                /* Is this pawn in enemy territory? */
                if (y > 4)
                {
                    white_score += HIGH_RANK_SCORE;
                }

                /* Next to safe edge? */
                if (x==0 || x==7)
                {
                    white_score += EDGE_SCORE;
                }
            }
        } 
        /* RED PIECE */
        else
        {
            /* King or Pawn? */
            if (king(state->board[y][x]))
            {
                red_score += KING_SCORE;

                /* On main diagonal?  Increases likelihood of jumping. */
                if (x + y == 7)
                {
                    red_score += MAIN_DIAG_SCORE;
                }
            }
            else
            {
                red_score += PAWN_SCORE;

                /* Is this pawn in enemy territory? */
                if (y < 3)
                {
                    red_score += HIGH_RANK_SCORE;
                }

                /* Next to safe edge? */
                if (x==0 || x==7)
                {
                    red_score += EDGE_SCORE;
                }
            }
        }
    }

    score = (me == 1) ? red_score / white_score : white_score / red_score;

/*
    printBoard(state);
    fprintf(stderr, "Red Score   = %f\n", red_score);
    fprintf(stderr, "White Score = %f\n", white_score);
    fprintf(stderr, "Board Score = %f\n\n", score);
*/

    /* Return the score as is if we are player1 (Red) and negated otherwise. */
    return score;
}



/*
 * Returns the minimum value from a tree.
 */
double minVal(double alpha, double beta, struct State *state, int depth)
{
    int x;
    struct State newState;

    FindLegalMoves(state);

    /* If we've reached the depth limit then evaluate this node and return its
     * value. */
    if (depth <= 0 || state->numLegalMoves == 0)
    {
        return evaluateBoard(state);
    }
    --depth;    /* descend one level in the tree */

    /* Walk the move list and find the best one. */
    for (x=0; x<state->numLegalMoves; ++x)
    {
        /* Check to see if this thread has been cancelled before moving on. */
        pthread_testcancel();

        /* Copy the current board state. */
        memcpy(&newState, state, sizeof(struct State));

        /* Perform a move on the copied state. */
        PerformMove(newState.board, 
                    newState.movelist[x],
                    MoveLength(newState.movelist[x]));

        /* Toggle the current player. */
        newState.player = (newState.player == 1) ? 2 : 1;

        /* Perform a depth limited MiniMax search for the best move.
         * Uses Alpha-Beta pruning. */
        beta = MIN(beta, maxVal(alpha, beta, &newState, depth));

        if (beta <= alpha)
        {
            return alpha;
        }
    }

    return beta;
}

/*
 * Returns the maximum value from a tree.
 */
double maxVal(double alpha, double beta, struct State *state, int depth)
{
    int x;
    struct State newState;

    FindLegalMoves(state);

    /* If we've reached the depth limit then evaluate this node and return its
     * value. */
    if (depth <= 0 || state->numLegalMoves == 0)
    {
        return evaluateBoard(state);
    }
    --depth;    /* descend one level in the tree */

    /* Walk the move list and find the best one. */
    for (x=0; x<state->numLegalMoves; ++x)
    {
        /* Check to see if this thread has been cancelled before moving on. */
        pthread_testcancel();

        /* Copy the current board state. */
        memcpy(&newState, state, sizeof(struct State));

        /* Perform a move on the copied state. */
        PerformMove(newState.board, 
                    newState.movelist[x],
                    MoveLength(newState.movelist[x]));

        /* Toggle the current player. */
        newState.player = (newState.player == 1) ? 2 : 1;

        /* Perform a depth limited MiniMax search for the best move.
         * Uses Alpha-Beta pruning. */
        alpha = MAX(alpha, minVal(alpha, beta, &newState, depth));

        if (alpha >= beta)
        {
            return beta;
        }
    }

    return alpha;
}

/*
 * Prints a string representation of the board.
 */
void printBoard(struct State *state)
{
    int x,y;

    for (y=0; y<8; ++y)
    {
        for (x=0; x<8; ++x)
        {
            if (!empty(state->board[y][x])) 
            {
                if (state->board[y][x] & White)
                {
                    if (king(state->board[y][x]))
                    {
                        fprintf(stderr, "W");
                    }
                    else
                    {
                        fprintf(stderr, "w");
                    }
                }
                else
                {
                    if (king(state->board[y][x]))
                    {
                        fprintf(stderr, "R");
                    }
                    else
                    {
                        fprintf(stderr, "r");
                    }
                }
            }
            else
            {
                if (x%2 == y%2)
                {
                    fprintf(stderr, "â§");
                }
                else
                {
                    fprintf(stderr, "â¡");
                }
            }
        }
        fprintf(stderr, "\n");
    }
}

/* Converts a square label to it's x,y position */
void NumberToXY(char num, int *x, int *y)
{
    static int xvals[] = {
        1, 3, 5, 7,
        0, 2, 4, 6,
        1, 3, 5, 7,
        0, 2, 4, 6,
        1, 3, 5, 7,
        0, 2, 4, 6,
        1, 3, 5, 7,
        0, 2, 4, 6
    };

    static int yvals[] = {
        0, 0, 0, 0,
        1, 1, 1, 1,
        2, 2, 2, 2,
        3, 3, 3, 3,
        4, 4, 4, 4,
        5, 5, 5, 5,
        6, 6, 6, 6,
        7, 7, 7, 7
    };

    if (num > 0 && num < 33)
    {
        *x = xvals[num-1];
        *y = yvals[num-1];
    }
    else
    {
        fprintf(stderr, "ERROR :: Invalid square value %d passed to NumberToXY!\n", num);
        exit(1);
    }
}

/* Returns the length of a move */
int MoveLength(char move[12])
{
    int i;

    i = 0;
    while(i<12 && move[i]) i++;
    return i;
}    

/* Converts the text version of a move to its integer array version */
int TextToMove(char *mtext, char move[12])
{
    int i=0,len=0,last;
    char val,num[64];

    while(mtext[i] != '\0') {
        last = i;
        while(mtext[i] != '\0' && mtext[i] != '-') i++;
        strncpy(num,&mtext[last],i-last);
        num[i-last] = '\0';
        val = (char) atoi(num);
        if(val <= 0 || val > 32) return 0;
        move[len] = val;
        len++;
        if(mtext[i] != '\0') i++;
    }
    if(len<2 || len>12) return 0; else return len;
}

/* Converts the integer array version of a move to its text version */
void MoveToText(char move[12], char *mtext)
{
    int i;
    char temp[8];

    mtext[0] = '\0';
    for(i=0; i<12; i++) {
        if(move[i]) {
            sprintf(temp,"%d",(int)move[i]);
            strcat(mtext,temp);
            strcat(mtext,"-");
        }
    }
    mtext[strlen(mtext)-1] = '\0';
}

/* Performs a move on the board, updating the state of the board */
void PerformMove(char board[8][8], char move[12], int mlen)
{
    int i,j,x,y,x1,y1,x2,y2;

    NumberToXY(move[0],&x,&y);
    NumberToXY(move[mlen-1],&x1,&y1);
    CopyState(&board[y1][x1],board[y][x]);
    if(y1 == 0 || y1 == 7) board[y1][x1] |= King;
    board[y][x] &= Clear;
    NumberToXY(move[1],&x2,&y2);
    if(abs(x2-x) == 2) {
        for(i=0,j=1; j<mlen; i++,j++) {
            if(move[i] > move[j]) {
                y1 = -1; 
                if((move[i]-move[j]) == 9) x1 = -1; else x1 = 1;
            }
            else {
                y1 = 1;
                if((move[j]-move[i]) == 7) x1 = -1; else x1 = 1;
            }
            NumberToXY(move[i],&x,&y);
            board[y+y1][x+x1] &= Clear;
        }
    }
}

int main(int argc, char *argv[])
{
    char buf[1028],move[12];
    int len,mlen;
    int player1;

    strcpy(prog_name, argv[0]);
    
    /* Convert command line parameters */
    SecPerMove = (float) atof(argv[1]); /* Time allotted for each move */

#ifdef __LIMIT_DEPTH__
    MaxDepth = (argc == 3) ? atoi(argv[2]) : -1;
#else
    MaxDepth = -1;
#endif /* __LIMIT_DEPTH__ */

    fprintf(stderr, "MaxDepth == %d\n", MaxDepth);
    fprintf(stderr, "%s SecPerMove == %lg\n", argv[0], SecPerMove);

    /* Determine if I am player 1 (red) or player 2 (white) */
    //fgets(buf, sizeof(buf), stdin);
    len=read(STDIN_FILENO,buf,1028);
    buf[len]='\0';
    if(!strncmp(buf,"Player1", strlen("Player1"))) 
    {
        fprintf(stderr, "%s is Player 1 (Red)\n", prog_name);
        player1 = 1; 
    }
    else 
    {
        fprintf(stderr, "%s is Player 2 (White)\n", prog_name);
        player1 = 0;
    }
    if(player1) me = 1; else me = 2;

    /* Set up the board */ 
    ResetBoard();
    srand((unsigned int)time(0));

    if (player1) {
        start = times(&bff);
        goto determine_next_move;
    }

    for(;;) {
        /* Read the other player's move from the pipe */
        //fgets(buf, sizeof(buf), stdin);
        len=read(STDIN_FILENO,buf,1028);
        buf[len]='\0';
        start = times(&bff);
        memset(move,0,12*sizeof(char));

        /* Update the board to reflect opponents move */
        mlen = TextToMove(buf,move);
        PerformMove(board,move,mlen);

determine_next_move:
        /* Find my move, update board, and write move to pipe */
        if(player1) FindBestMove(1); else FindBestMove(2);

        /* There is a legal move */
        if(bestmove[0] != 0) 
        {
            mlen = MoveLength(bestmove);
            PerformMove(board,bestmove,mlen);
            MoveToText(bestmove,buf);
        }
        else exit(1); /* No legal moves available, so I have lost */

        /* Write the move to the pipe */
        //printf("%s", buf);
        write(STDOUT_FILENO,buf,strlen(buf));
        fflush(stdout);
    }

    return 0;
}


