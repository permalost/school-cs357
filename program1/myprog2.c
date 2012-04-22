#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/times.h>
#include <time.h>
#include <pthread.h>
#include "myprog.h"
 
#ifndef CLK_TCK
#define CLK_TCK CLOCKS_PER_SEC
#endif

int evals;
int maxDepth;
int player1;
float SecPerMove;
char board[8][8];
char bestmove[12];
int me,cutoff,endgame;
long NumNodes;
int MaxDepth;

/*** For timing ***/
clock_t start;
struct tms bff;

/*** For the jump list ***/
int jumpptr = 0;
int jumplist[48][12];

/*** For the move list ***/
int numLegalMoves = 0;
int movelist[48][12];

/* Print the amount of time passed since my turn began */
void PrintTime(void)
{
    clock_t current;
    float total;

    current = times(&bff);
    total = (float) ((float)current-(float)start)/CLK_TCK;
    fprintf(stderr, "Time = %f\n", total);
}

/* Determine if I'm low on time */
int LowOnTime(void) 
{
    clock_t current;
    float total;

    current = times(&bff);
    total = (float) ((float)current-(float)start)/CLK_TCK;
    if(total >= (SecPerMove-1.0)) return 1; else return 0;
}

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
        /* If there's an enemy piece adjacent, and an empty square after hum, we can jump */
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
    int x,y;
    char move[12], board[8][8];

    memset(move,0,12*sizeof(char));
    jumpptr = numLegalMoves = 0;
    memcpy(board,state->board,64*sizeof(char));

    /* Loop through the board array, determining legal moves/jumps for each piece */
    for(y=0; y<8; y++)
    for(x=0; x<8; x++)
    {
        if(x%2 != y%2 && color(board[y][x]) == state->player && !empty(board[y][x])) {
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

double evalBoard1(State *state)
{
    int x,y;
    double rval = 0.0;
    evals++;
    for(x=0;x<8;x++)
    for(y=0;y<8;y++)
    {
        if(x%2 != y%2 && !empty(state->board[y][x])) {
            if(king(state->board[y][x])) { /* King */
                if(((state->board[y][x] & White) && !player1) ||
                   (!(state->board[y][x] & White) && player1) )
                    rval+=2.0;
                else rval-=2.0;
            } 
            else if(piece(state->board[y][x])) { /* Piece */
                if(((state->board[y][x] & White) && !player1) ||
                   (!(state->board[y][x] & White) && player1) )
                    rval+=1.0;
                else rval-=1.0;
            }
        }    
    }
    //PrintBoard(state->board);
    //fprintf(stderr,"Value = %g\n",rval);
    return rval;
}

double evalBoard2(State *currBoard)
{
    int y,x;
    double score=0.0;

    evals++;
    for(y=0; y<8; y++)
    for(x=0; x<8; x++)
    {
         if(king(currBoard->board[y][x]))
         {
             //if(color(currBoard->board[y][x]) == White) score += 2.0;
             if(currBoard->board[y][x] & White) score += 2.0;
             else score -= 2.0;
         }
         else if(piece(currBoard->board[y][x]))
         {
             if(currBoard->board[y][x] & White) score += 1.0;
             else score -= 1.0;
         }
    }

    return currBoard->player==1 ? -score : score;
}

/* Combines min and max into one function that always does max */
double minMax(State *currBoard, int depth)
{
    State nextBoard;
    double rval;
    int i;
    double maxv=-100000.0;

    if(depth <= 0) return evalBoard2(currBoard);

    FindLegalMoves(currBoard);
    for(i=0; i<currBoard->numLegalMoves; i++) 
    {
        /* Set up the next state by copying the current state and then updating the new */
        /* state to reflect the new board after performing the move */
        memcpy (&nextBoard, currBoard, sizeof(State));
        PerformMove(nextBoard.board,currBoard->movelist[i],MoveLength(currBoard->movelist[i]));
        nextBoard.player = 3-nextBoard.player;

        rval = -minMax(&nextBoard, depth-1);

        if (rval > maxv) maxv = rval;
    }    
    return maxv;
}

double minVal(State *state, int depth);
double maxVal(State *state, int depth)
{
    int x;
    double maxv=-1000000.0;
    if(depth--<=0) return evalBoard1(state);

    FindLegalMoves(state);

    for(x=0;x<state->numLegalMoves;x++)
    {
        State newState;
        memcpy(&newState, state, sizeof(State));
        PerformMove(newState.board, newState.movelist[x],MoveLength(newState.movelist[x]));
        newState.player = (newState.player==1) ? 2 : 1;

        maxv = MAX(minVal(&newState, depth),maxv);
    }

    return maxv;
}


double minVal(State *state, int depth)
{
    int x;
    double minv=100000.0;
    if(depth--<=0) return evalBoard1(state);

    FindLegalMoves(state);

    for(x=0;x<state->numLegalMoves;x++)
    {
        State newState;
        memcpy(&newState, state, sizeof(State));
        PerformMove(newState.board, newState.movelist[x],MoveLength(newState.movelist[x]));
        newState.player = (newState.player==1) ? 2 : 1;

        minv = MIN(maxVal(&newState, depth),minv);
    }

    return minv;
}

/* Employ your favorite search to find the best move here.  */
/* This example code shows you how to call the FindLegalMoves function */
/* and the PerformMove function */
void *FindBestMoveThread1(void *data)
{
    int i,x,best[50],numbest; 
    struct State state; 
    double val;
    double maxv = -1000000.0;
    int player;
    int depth=1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE,NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS,NULL);

    fprintf(stderr,"FindBestMoveThread1\n");
    player = *((int*)data);

    /* Set up the current state */
    state.player = player;
    memcpy(state.board,board,64*sizeof(char));

    /* Find the legal moves for the current state */
    FindLegalMoves(&state);

    i = rand()%state.numLegalMoves;
    memset(bestmove,0,12*sizeof(char));
    memcpy(bestmove,state.movelist[i],MoveLength(state.movelist[i]));
    evals=0;
    while(1)
    {
            maxv = -1000000.0;
            numbest=1;
            best[0]=0;
            for(x=0;x<state.numLegalMoves;x++)
            {
                State newState;
                memcpy(&newState, &state, sizeof(State));
                PerformMove(newState.board, newState.movelist[x],MoveLength(newState.movelist[x]));
                newState.player = (newState.player==1) ? 2 : 1;

                val = minVal(&newState, depth);
                if(val>maxv)
                {
                    best[0]=x;
                    numbest=1;
                    maxv = val;
                }
                if(val==maxv)
                {
                    best[numbest++]=x;
                }
            }
            memset(bestmove,0,12*sizeof(char));
            i=best[rand()%numbest]; // pick a random best move
            memcpy(bestmove, state.movelist[i], MoveLength(state.movelist[i]));
            maxDepth=depth++;
    }
    return NULL;
}



/* Employ your favorite search to find the best move.  This code is an example     */
/* of an alpha/beta search, except I have not provided the MinVal,MaxVal,EVAL      */
/* functions.  This example code shows you how to call the FindLegalMoves function */
/* and the PerformMove function */
void *FindBestMoveThread2(void *p)
{
    int player;
    int i, depth, best[50], numbest=0;
    struct State state, nextstate;
    double maxv=-100000.0,minval[50];

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE,NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS,NULL);

    fprintf(stderr,"FindBestMoveThread2\n");
    player = *((int*)p);

    /* Set up the current state */
    state.player = player;
    memcpy(state.board,board,64*sizeof(char));

    /* Find the legal moves for the current state */
    FindLegalMoves(&state);

    /* init bestmove to first move */
    memset(bestmove,0,12*sizeof(char));
    memcpy(bestmove,state.movelist[0],MoveLength(state.movelist[0]));
    evals=0;

    for(depth=1;;depth++)
    {
        maxv=-1000000.0;
        numbest=1;
        best[0] = 0;
        /* For each legal move */
        for(i=0; i<state.numLegalMoves; i++) 
        {
            /* Set up the next state by copying the current state and then updating the new */
            /* state to reflect the new board after performing the move */
            memcpy(&nextstate,&state,sizeof(struct State));
            PerformMove(nextstate.board,state.movelist[i],MoveLength(state.movelist[i]));
            if(state.player == 1) nextstate.player = 2; else nextstate.player = 1;

            /* Call your search routine to determine the value of this move.  Note: */
            /* if you choose to use alpha/beta search you will need to write the    */
            /* MinVal and MaxVal functions, as well as your heuristic eval function */
            minval[i] = -minMax(&nextstate,depth);
            if(minval[i] > maxv) 
            {
                numbest=1;
                best[0]=i;
                maxv= minval[i];
                //memcpy(bestmove,state.movelist[order[i]],12*sizeof(char));
            }
            else if(minval[i] == maxv) 
            {
                best[numbest++]=i;
            }
        }    
        //fprintf(stderr,"Evaluation of best move gives a score of %lg\n", minval[best]);
        maxDepth=depth;
        memset(bestmove,0,12*sizeof(char));
        i = best[rand()%numbest]; // Pick a random best move
        memcpy(bestmove,state.movelist[i],MoveLength(state.movelist[i]));
    }    
}

void *timerThread(void *p)
{
    int wait = (int)SecPerMove - 2;
    sleep(wait);
}


//int pthread_create (pthread_t *, const pthread_attr_t *, void *(*)(void *), void *);
void TimedFindBestMove(int player)
{
    int rval;
    pthread_t tThread, searchThread;

    // create find best move thread (searchThread)
    rval = pthread_create(&searchThread, NULL, FindBestMoveThread1, &player);
    fprintf(stderr,"Method 1 NO PRUNE explored depth %i with %i evals\n",maxDepth, evals);
    // detach searchThread cuz we don't want to join with it, we'll just kill it when we run out of time
    pthread_detach(searchThread); 
    // start kludge timer thread
    rval = pthread_create(&tThread, NULL, timerThread, NULL);

    // wait for timer thread to come back, yawn
    pthread_join(tThread,NULL);
    // smack find best move thread upside the head
    pthread_cancel(searchThread);
}

int FindBestMove(int player)
{
    TimedFindBestMove(player);
    //OldFindBestMove(player);
    return 0;
}


/* Employ your favorite search to find the best move here.  */
/* This example code shows you how to call the FindLegalMoves function */
/* and the PerformMove function */
void old_FindBestMove(int player)
{
    int i; 
    struct State state; 

    /* Set up the current state */
    state.player = player;
    memcpy(state.board,board,64*sizeof(char));
    memset(bestmove,0,12*sizeof(char));

    /* Find the legal moves for the current state */
    FindLegalMoves(&state);

    // For now, until you write your search routine, we will just set the best move
    // to be a random (legal) one, so that it plays a legal game of checkers.
    // You *will* want to replace this with a more intelligent move seleciton
    i = rand()%state.numLegalMoves;
    memcpy(bestmove,state.movelist[i],MoveLength(state.movelist[i]));
}

/* Converts a square label to it's x,y position */
void NumberToXY(char num, int *x, int *y)
{
    int i=0,newy,newx;

    for(newy=0; newy<8; newy++)
    for(newx=0; newx<8; newx++)
    {
        if(newx%2 != newy%2) {
            i++;
            if(i==(int) num) {
                *x = newx;
                *y = newy;
                return;
            }
        }
    }
    *x = 0; 
    *y = 0;
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

    /* Convert command line parameters */
    SecPerMove = (float) atof(argv[1]); /* Time allotted for each move */
    MaxDepth = (argc == 4) ? atoi(argv[3]) : -1;

fprintf(stderr, "%s SecPerMove == %lg\n", argv[0], SecPerMove);

    /* Determine if I am player 1 (red) or player 2 (white) */
    //fgets(buf, sizeof(buf), stdin);
    len=read(STDIN_FILENO,buf,1028);
    buf[len]='\0';
    if(!strncmp(buf,"Player1", strlen("Player1"))) 
    {
        fprintf(stderr, "I'm Player 1\n");
        player1 = 1; 
    }
    else 
    {
        fprintf(stderr, "I'm Player 2\n");
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
        if(bestmove[0] != 0) { /* There is a legal move */
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


