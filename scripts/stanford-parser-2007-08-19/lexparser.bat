@echo off
:: Runs the English PCFG parser on one or more files, printing trees only
:: usage: lexparser fileToparse
java -server -mx150m -cp "stanford-parser.jar;" edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat "penn,typedDependenciesCollapsed" englishPCFG.ser.gz %1
