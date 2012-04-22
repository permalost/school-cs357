#-------------------------------------------------------------------------------
# Makefile to control build process of the constraints satisfaction solver.
#
# Author: Quincy Bowers
# Date: November 14, 2011
#-------------------------------------------------------------------------------

.SUFFIXES: .java .class

.java.class:
	javac $*.java

CLASSES = $(wildcard *.java)

all: classes qbowers

classes: $(CLASSES:.java=.class)

qbowers:
	echo '#!/bin/sh' > qbowers
	echo java ConstraintsSatisfier '$$*' >> qbowers
	chmod 755 qbowers

clean:
	rm -f *.class qbowers tags

