#!/bin/csh -f
#
# Runs the English PCFG parser on one or more files, printing trees only
# usage: ./lexparser.csh fileToparse+
#
set scriptdir=`dirname $0`
java -server -mx150m -cp "$scriptdir/stanford-parser.jar:" edu.stanford.nlp.parser.lexparser.LexicalizedParser $scriptdir/englishPCFG.ser.gz $*
