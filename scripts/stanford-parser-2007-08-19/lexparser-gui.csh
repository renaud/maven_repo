#!/bin/csh -f
#
# usage ./lexparser-gui [parserDataFilename [textFileName]]
#
set scriptdir=`dirname $0`
java -server -mx600m -cp "$scriptdir/stanford-parser.jar:" edu.stanford.nlp.parser.ui.Parser $*
