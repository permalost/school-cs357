#!/bin/bash

PLAYER1=random
PLAYER2=quincy2
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/random_vs_quincy2_1.0


PLAYER1=random
PLAYER2=quincy3
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/random_vs_quincy3_1.0


PLAYER1=depth
PLAYER2=quincy2
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/depth_vs_quincy2_1.0


PLAYER1=depth
PLAYER2=quincy3
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/depth_vs_quincy3_1.0


PLAYER1=quincy2
PLAYER2=quincy3
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/quincy2_vs_quincy3_1.0


PLAYER1=quincy2
PLAYER2=random
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/quincy2_vs_random_1.0


PLAYER1=quincy2
PLAYER2=depth
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/quincy2_vs_depth_1.0


PLAYER1=quincy3
PLAYER2=random
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/quincy3_vs_random_1.0


PLAYER1=quincy3
PLAYER2=depth
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/quincy3_vs_depth_1.0


PLAYER1=quincy3
PLAYER2=quincy2
TIMEOUT=1.0

echo ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
for i in {0..9}
do
    ./checkers $PLAYER1 $PLAYER2 $TIMEOUT
done | grep game | sort | uniq -c > stats/quincy3_vs_quincy2_1.0


