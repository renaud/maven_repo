package edu.stanford.nlp.trees.international.pennchinese;

import edu.stanford.nlp.parser.lexparser.ChineseTreebankParserParams;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.StringUtils;

import java.io.*;
import java.util.*;

import static edu.stanford.nlp.trees.GrammaticalRelation.DEPENDENT;

/**
 * A GrammaticalStructure for Chinese.
 *
 * @author Galen Andrew
 * @author Pi-Chuan Chang
 */
public class ChineseGrammaticalStructure extends GrammaticalStructure {

  private static HeadFinder shf = new ChineseSemanticHeadFinder();
  //private static HeadFinder shf = new ChineseHeadFinder();


  /**
   * Construct a new <code>GrammaticalStructure</code> from an
   * existing parse tree.  The new <code>GrammaticalStructure</code>
   * has the same tree structure and label values as the given tree
   * (but no shared storage).  As part of construction, the parse tree
   * is analyzed using definitions from {@link GrammaticalRelation
   * <code>GrammaticalRelation</code>} to populate the new
   * <code>GrammaticalStructure</code> with as many labeled
   * grammatical relations as it can.
   */
  public ChineseGrammaticalStructure(Tree t) {
    this(t, new ChineseTreebankLanguagePack().punctuationWordRejectFilter());
  }

  public ChineseGrammaticalStructure(Tree t, Filter<String> puncFilter) {
    super(t, ChineseGrammaticalRelations.values(), shf, puncFilter);
  }

  protected void collapseDependencies(Collection<TypedDependency> list) {
    //      collapseConj(list);
    collapsePrepAndPoss(list);
    //      collapseMultiwordPreps(list);
  }

  private void collapsePrepAndPoss(Collection<TypedDependency> list) {
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();

    // Construct a map from tree nodes to the set of typed
    // dependencies in which the node appears as governor.
    Map<TreeGraphNode, Set<TypedDependency>> map = new HashMap<TreeGraphNode, Set<TypedDependency>>();
    for (TypedDependency typedDep : list) {
      if (!map.containsKey(typedDep.gov())) {
        map.put(typedDep.gov(), new HashSet<TypedDependency>());
      }
      map.get(typedDep.gov()).add(typedDep);
    }
    //System.err.println("here's the map: " + map);

    for (TypedDependency td1 : list) {
      if (td1.reln() != GrammaticalRelation.KILL) {
        TreeGraphNode td1Dep = td1.dep();
        String td1DepPOS = td1Dep.parent().value();
        // find all other typedDeps having our dep as gov
        Set<TypedDependency> possibles = map.get(td1Dep);
        if (possibles != null) {
          // look for the "second half"
          for (TypedDependency td2 : possibles) {
            TreeGraphNode td2Dep = td2.dep();
            String td2DepPOS = td2Dep.parent().value();
            if (td2 != null && td1.reln() == DEPENDENT && td2.reln() == DEPENDENT && td1DepPOS.equals("P")) {
              TypedDependency td3 = new TypedDependency(td1Dep.value(), td1.gov(), td2.dep());
              //System.err.println("adding: " + td3);
              newTypedDeps.add(td3);
              td1.setReln(GrammaticalRelation.KILL);        // remember these are "used up"
              td2.setReln(GrammaticalRelation.KILL);        // remember these are "used up"
            }
          }

          // Now we need to see if there any TDs that will be "orphaned"
          // by this collapse.  Example: if we have:
          //   dep(drew, on)
          //   dep(on, book)
          //   dep(on, right)
          // the first two will be collapsed to on(drew, book), but then
          // the third one will be orphaned, since its governor no
          // longer appears.  So, change its governor to 'drew'.
          if (td1.reln().equals(GrammaticalRelation.KILL)) {
            for (TypedDependency td2 : possibles) {
              if (!td2.reln().equals(GrammaticalRelation.KILL)) {
                //System.err.println("td1 & td2: " + td1 + " & " + td2);
                td2.setGov(td1.gov());
              }
            }
          }
        }
      }
    }

    // now copy remaining unkilled TDs from here to new
    for (TypedDependency td : list) {
      if (!td.reln().equals(GrammaticalRelation.KILL)) {
        newTypedDeps.add(td);
      }
    }

    list.clear();                            // forget all (esp. killed) TDs
    list.addAll(newTypedDeps);
  }


  /**
   * Just for testing.
   * Usage: <br> <code>
   * java edu.stanford.nlp.trees.ChineseGrammaticalStructure -treeFile [treeFile] <br>
   * java ChineseGrammaticalStructure -sentFile [sentenceFile] </code>
   *
   * @param args Command line args as above
   */
  public static void main(String[] args) {

    // System.out.print("GrammaticalRelations under DEPENDENT:");
    // System.out.println(DEPENDENT.toPrettyString());

    Treebank tb = new MemoryTreebank();
    Properties props = StringUtils.argsToProperties(args);
    String treeFileName = props.getProperty("treeFile");
    String sentFileName = props.getProperty("sentFile");
    String hf = props.getProperty("hf");

    try {
      if (hf != null) {
        shf = (HeadFinder)Class.forName(hf).newInstance();
        System.err.println("Using "+hf);
      }
    } catch (Exception e) {
      throw new RuntimeException("Fail to use HeadFinder: "+hf);
    }

    ChineseTreebankParserParams ctpp = new ChineseTreebankParserParams();

    if (args.length == 0) {
      System.err.println("Please provide treeFile or sentFile");
    } else {
      if (treeFileName != null) {
        try {
          TreeReaderFactory trf = ctpp.treeReaderFactory();
          TreeReader tr = trf.newTreeReader(new InputStreamReader(new FileInputStream(treeFileName), "GB18030"));
          Tree t;
          while ((t = tr.readTree()) != null) {
            tb.add(t);
          }
        } catch (IOException e) {
          throw new RuntimeException("File problem: " + e);
        }
      } else if (sentFileName != null) {
        LexicalizedParser lp = new LexicalizedParser("/u/nlp/data/lexparser/chineseFactored.ser.gz");
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new FileReader(sentFileName));
        } catch (FileNotFoundException e) {
          System.err.println("Cannot find " + sentFileName);
          System.exit(1);
        }
        try {
          System.out.println("Processing sentence file " + sentFileName);
          String line;
          while ((line = reader.readLine()) != null) {
            CHTBTokenizer chtb = new CHTBTokenizer(new StringReader(line));
            List words = chtb.tokenize();
            lp.parse(words);
            Tree parseTree = lp.getBestParse();
            tb.add(parseTree);
          }
          reader.close();
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("IOexception reading key file " + sentFileName);
          System.exit(1);
        }
      }
    }

    System.out.println("Phrase structure tree, then dependencies, then collapsed dependencies");
    for (Tree t : tb) {

      System.out.println("==================================================");
      GrammaticalStructure gs = new ChineseGrammaticalStructure(t);

      t.pennPrint();

      //System.out.println("----------------------------");
      //TreeGraph tg = new TreeGraph(t);
      //System.out.println(tg);

      System.out.println("----------------------------");
      System.out.println(gs);

      System.out.println("----------------------------");
      System.out.println(StringUtils.join(gs.typedDependencies(true), "\n"));

      System.out.println("----------------------------");
      System.out.println(StringUtils.join(gs.typedDependenciesCollapsed(true), "\n"));

      //gs.printTypedDependencies("xml");
    }
  }


}
