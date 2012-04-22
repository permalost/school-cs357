/* File: q4.h
 * Author: Quincy Bowers  quincybowers@u.boisestate.edu
 * Date: Sun October 20, 2011
 * Description: A header file for the q1 checkers AI player.
 */

#ifndef _Q4_H_
#define _Q4_H_

#define Empty 0x00
#define Piece 0x20
#define King  0x60
#define Red   0x00
#define White 0x80

#define number(x) ((x)&0x1f)
#define empty(x) ((((x)>>5)&0x03)==0?1:0)
#define piece(x) ((((x)>>5)&0x03)==1?1:0)
#define king(x) ((((x)>>5)&0x03)==3?1:0)
#define color(x) ((((x)>>7)&1)+1)
#define MIN(a,b) ((a<b) ? a : b)
#define MAX(a,b) ((a>b) ? a : b)

#define Clear 0x1f

#define HIST_LENGTH 12

struct State {
    int player;
    char board[8][8];
    char movelist[48][12]; /* The following comments were added by Tim Andersen
                              Here Scott has set the maximum number of possible legal moves to be 48.
                              Be forewarned that this number might not be correct, even though I'm
                              pretty sure it is.  
                              The second array subscript is (supposed to be) the maximum number of 
                              squares that a piece could visit in a single move.  This number is arrived
                              at be recognizing that an opponent will have at most 12 pieces on the 
                              board, and so you can at most jump twelve times.  However, the number
                              really ought to be 13 since we also have to record the starting position
                              as part of the move sequence.  I didn't code this, and I highly doubt 
                              that the natural course of a game would lead to a person jumping all twelve
                              of an opponents checkers in a single move, so I'm not going to change this. 
                              I'll leave it to the adventuresome to try and devise a way to legally generate
                              a board position that would allow such an event.  
                              Each move is represented by a sequence of numbers, the first number being 
                              the starting position of the piece to be moved, and the rest being the squares 
                              that the piece will visit (in order) during the course of a single move.  The 
                              last number in this sequence is the final position of the piece being moved.  */
    int numLegalMoves;
};

struct StateHistoryList
{
    struct State list[HIST_LENGTH];
    struct State current;
    int head;
};

void CopyState(char *dest, char src);
void ResetBoard(void);
void AddMove(char move[12]);
void FindKingMoves(char board[8][8], int x, int y);
void FindMoves(int player, char board[8][8], int x, int y);
void AddJump(char move[12]);
int FindKingJump(int player, char board[8][8], char move[12], int len, int x, int y);
int FindJump(int player, char board[8][8], char move[12], int len, int x, int y);
int FindLegalMoves(struct State *state);
void FindBestMove(int player);
void *FindBestMoveThread(void *data);
void NumberToXY(char num, int *x, int *y);
int MoveLength(char move[12]);
int TextToMove(char *mtext, char move[12]);
void MoveToText(char move[12], char *mtext);
void PerformMove(char board[8][8], char move[12], int mlen);
double evaluateBoard(struct State *state);
double minVal(double alpha, double beta, struct State *state, int depth);
double maxVal(double alpha, double beta, struct State *state, int depth);
void printBoard(struct State *state);
int boardsAreEqual(char a[8][8], char b[8][8]);

#endif  /* _Q4_H_ */
