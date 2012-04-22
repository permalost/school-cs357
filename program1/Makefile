CC              := gcc
CFLAGS          := -Wall -ggdb -pthread
CPPFLAGS        := -I./ -I/usr/X11R6/include/Xm -I/usr/X11R6/include -I/usr/include/openmotif
#LDFLAGS         := -L/usr/lib/X11R6 -lXm -lXaw -lXmu -lXt -lX11
LDFLAGS         := -L/usr/X11R6/lib -L /usr/X11R6/LessTif/Motif1.2/lib -lXm -lXaw -lXmu -lXt -lX11 -lICE -lSM -pthread -L/usr/lib64/openmotif/

# Uncomment this next line if you'd like to compile the graphical version of the checkers server.
#CFLAGS          += -DGRAPHICS

all: checkers random quincy1 quincy2 quincy3 quincy4 qdepth
checkers: graphics.o

random: myprog.o
	${CC} ${CPPFLAGS} ${CFLAGS} -o $@ $^

quincy1: q1.o
	${CC} ${CPPFLAGS} ${CFLAGS} -o $@ $^

quincy2: q2.o
	${CC} ${CPPFLAGS} ${CFLAGS} -o $@ $^

quincy3: q3.o
	${CC} ${CPPFLAGS} ${CFLAGS} -o $@ $^

quincy4: q4.o
	${CC} ${CPPFLAGS} ${CFLAGS} -o $@ $^

qdepth: qdepth.o
	${CC} ${CPPFLAGS} ${CFLAGS} -o $@ $^

qdepth.o: q1.c
	${CC} ${CPPFLAGS} ${CFLAGS} -D__LIMIT_DEPTH__ -c -o $@ $^
	

.PHONY: clean
clean:	
	@-rm checkers random qdepth quincy* *.o

