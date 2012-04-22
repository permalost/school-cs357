                                                                   Quincy Bowers
                                                                     CompSci 357
                                                                       Fall 2011
                                                                       Program 2
                                                               November 14, 2011

Included Files
--------------
    .
    ├── ConstraintsSatisfier.java
    ├── CSBag.java
    ├── CSItem.java
    ├── ItemConstrained.java
    ├── Makefile
    ├── NoSpaceInBag.java
    ├── readme.txt
    ├── results.txt
    ├── SubtreeUnsolvable.java
    └── UnsolvableConstraintsProblem.java


Running the Program
-------------------
The provided Makefile will compile the java source code and create a small shell
script to run the program.

    $ make
    $ ./qbowers [problem file]    


Design
------
I designed my program using the backtracking algorithm with forward checking.
My original attempt used a truly recursive backtracking function.  For larger
problems I started overflowing the stack though, so I converted the recursive
function into an equivalent iterative one.

Another attempt at the problem used the Min-conflicts algorithm.  That did not
get very far though.  The question of how long you should search before deciding
the problem is unsolvable comes into play.  I didn't come up with any ways to 
address this didn't amount to giving up so I went back to backtracking.


Fast Fails
----------
As soon as the input file is processed there are a couple of early fails we can
detect.  The first is really easy.  There is an item that is bigger than any
bag.  The second is only slighlty more difficult.  We can sum the total size of
all of the items in the problem and compare that to the total amount of bag
space.  If there is more item space than bag space we can fail immediately with
out doing any search.


Forward Checking
----------------
Whenever a bag is assigned to an item we can quickly notify the remaining 
unnassigned items that this occurred.  Any unnassigned item which has a 
constraint against the item that was just assigned can have the assigned bag 
removed from its domain.  If any item's domain is reduced to a null set because
of this we know that the subtree from this node in the search is unsolvable and
we can backtrack.

Forward checking is handled in two stages.  The first stage happens as the 
program tries to make the assignment.  The bag is removed from the domains of
any items which are constraints of the item being assigned to.  If this does not
result in any item having an empty domain that packing was successful.

At this point the program checks to see if we have a complete assignment.  If
so then the program immediately returns successful and prints the solution.
Otherwise we move onto the second stage of forward checking.  In this stage we
reduce the domains of all of the items in the problem by removing the just 
assigned bag anywhere it is found.  This reduces the total number of branches
that need to be searched.

This may result in an unsolvable subtree as well.  If so the item will have to
be unpacked from the bag and the domains of each item in the problem restored.


Variable Ordering
-----------------
At each level of the search an item must be selected for assignment.  I used the
MRV heuristic which says we should select the item with the minimum number of
available bags to assign.  In the event of ties I take the item with more 
constraints, and in the event of another tie the degree heuristic is used.
Finally, if this also results in a tie the larger item is selected.

This is implemented with an iterator over the items in the problem.  When the
iterator is constructed the list of items is sorted using mergesort.  The items
themselves implement Comparable and the compareTo method implements the ordering
described above.


Testing
-------
I constructed two very simple problems by hand.  One had a few solutions, and 
the other had no solutions.  These were used as a baseline for minumum 
functionality.  I tested extensively on the test problems during development as
well.  I ran the provided cspopt.txt program on each of the provided test 
programs to determine the desired results.  I then ran my own program against 
the same problems to compare results.  The following table summarizes my
findings.

    problem    cspopt     mine
    -------    -------    -------
    g1.txt     failure    NO HALT
    g2.txt     success    success
    g3.txt     failure    failure
    g4.txt     success    success
    g5.txt     success    success
    g6.txt     NO HALT    NO HALT
    g7.txt     failure    failure
    g8.txt     success    NO HALT
    g9.txt     success    NO HALT
    g10.txt    NO HALT    NO HALT
    g11.txt    failure    failure

I found there were two provided problems that never returned an answer using the
cspopt.txt program.  I am using rather small values of never here.  Neither g6
or g10 returned with an hour in my testing.

My own program doesn't halt on five of the test problems.  Since all of the 
problems that do halt match the expected outcome I believe that my program is
too inneficient too solve these problems in a reasonable time or that it is 
naive about some trick to solving these.  The latter possibility requires 
Feynman's Problem Solving Algorithm: 

    1. Write down the problem. 
    2. Think very hard. 
    3. Write down the answer

I did this for days without getting past step 2.  A bright idea did finally 
occur to me though.  I started profiling the code.  Profiling pointed out a few
big hot spots in the code but I ultimately did not solve my no halting problem.
Despite days of stepping through the debugger and a few hours of profiling, I
don't see a better solution at this time.


Results
-------
As detailed in the table above, my output matches the cspopt.txt program for
problems in which it returns an answer.  Something is going on that I have not 
yet been able to pinpoint.  I am not too worried about it though since the 
cspopt.txt program suffers, to a lesser extent, the same issues.


