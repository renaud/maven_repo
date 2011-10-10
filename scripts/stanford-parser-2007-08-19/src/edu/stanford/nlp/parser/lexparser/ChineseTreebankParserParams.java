package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.io.NumberRangesFileFilter;
import edu.stanford.nlp.io.EncodingPrintWriter;
import edu.stanford.nlp.misc.SeeChars;
import edu.stanford.nlp.ling.CategoryWordTag;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.*;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.StringUtils;

import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Parameter file for parsing the Penn Chinese Treebank.  Includes
 * category enrichments specific to the Penn Chinese Treebank.
 *
 * @author Roger Levy
 * @author Christopher Manning
 * @author Galen Andrew
 */

public class ChineseTreebankParserParams extends AbstractTreebankParserParams {

  /**
   * The variable ctlp stores the same thing as the tlp variable in
   * AbstractTreebankParserParams, but pre-cast to be a
   * ChineseTreebankLanguagePack.
   */
  private ChineseTreebankLanguagePack ctlp;
  public boolean charTags = false;
  public boolean useCharacterBasedLexicon = false;
  public boolean useMaxentLexicon = false;
  public boolean useMaxentDepGrammar = false;
  public boolean segmentMarkov = false;
  public boolean segmentMaxMatch = false;
  public boolean sunJurafskyHeadFinder = false;
  public boolean bikelHeadFinder = false;
  public boolean discardFrags = false;
  public boolean useSimilarWordMap = false;

  private Lexicon lex;
  private WordSegmenter segmenter;

  private static void printlnErr(String s) {
    EncodingPrintWriter.err.println(s, ChineseTreebankLanguagePack.ENCODING);
  }

  public ChineseTreebankParserParams() {
    super(new ChineseTreebankLanguagePack());
    ctlp = (ChineseTreebankLanguagePack) super.treebankLanguagePack();
  }

  /**
   * Returns a ChineseHeadFinder
   */
  public HeadFinder headFinder() {
    if (sunJurafskyHeadFinder) {
      return new SunJurafskyChineseHeadFinder();
    } else if (bikelHeadFinder) {
      return new BikelChineseHeadFinder();
    } else {
      return new ChineseHeadFinder();
    }
  }

  /**
   * Returns a ChineseLexicon
   */
  public Lexicon lex(Options.LexOptions op) {
    if (useCharacterBasedLexicon) {
      return lex = new ChineseCharacterBasedLexicon();
    // } else if (useMaxentLexicon) {
    // return lex = new ChineseMaxentLexicon();
    }

    ChineseLexicon clex = new ChineseLexicon(op);
    WordSegmenter seg;
    /*if (segmentMarkov) {
      seg = new ChineseMarkovWordSegmenter();
    } else if (segmentMaxMatch) {
      seg = new MaxMatchSegmenter();
      }*/
    seg = segmenter;

    if (seg != null) {
      lex = new ChineseLexiconAndWordSegmenter(clex, seg);
    } else {
      lex = clex;
    }

    return lex;
  }

  public double[] MLEDependencyGrammarSmoothingParams() {
    return new double[]{5.8, 17.7, 6.5, 0.4};
  }

  public TreeReaderFactory treeReaderFactory() {
    return new TreeReaderFactory() {
      public TreeReader newTreeReader(Reader in) {
        final TreeNormalizer tn = new CTBErrorCorrectingTreeNormalizer(splitNPTMP, splitPPTMP, splitXPTMP, charTags);
        if (discardFrags) {
          return new FragDiscardingPennTreeReader(in, new LabeledScoredTreeFactory(new StringLabelFactory()), tn, new CHTBTokenizer(in));
        } else {
          return new PennTreeReader(in, new LabeledScoredTreeFactory(new StringLabelFactory()), tn, new CHTBTokenizer(in));
        }
      }
    };
  }

  /**
   * This was originally written to correct a few errors Galen found in CTB3.
   * The thinking was that perhaps when we get CTB4 they would be gone and we
   * could revert to BobChris.  Alas, CTB4 contained only more errors....
   * It has since been extended to allow some functional tags from CTB to be
   * maintained.  This is so far much easier than in NPTmpRetainingTN, since
   * we don't do any tag percolation (helped by CTB marking temporal nouns).
   */

  private static class CTBErrorCorrectingTreeNormalizer extends BobChrisTreeNormalizer {

    private static final Pattern NPTmpPattern = Pattern.compile("NP.*-TMP.*");
    private static final Pattern PPTmpPattern = Pattern.compile("PP.*-TMP.*");
    private static final Pattern TmpPattern = Pattern.compile(".*-TMP.*");

    private CharacterLevelTagExtender tagExtender;

    private boolean splitNPTMP;
    private boolean splitPPTMP;
    private boolean splitXPTMP;

    /**
     * Build a CTBErrorCorrectingTreeNormalizer.
     *
     * @param splitNPTMP Temporal annotation on NPs
     * @param splitPPTMP Temporal annotation on PPs
     * @param splitXPTMP Temporal annotation on any phrase marked in CTB
     */

    public CTBErrorCorrectingTreeNormalizer(boolean splitNPTMP, boolean splitPPTMP, boolean splitXPTMP, boolean charTags) {
      this.splitNPTMP = splitNPTMP;
      this.splitPPTMP = splitPPTMP;
      this.splitXPTMP = splitXPTMP;
      if (charTags) {
        tagExtender = new CharacterLevelTagExtender();
      }
    }


    /**
     * Remove things like hyphened functional tags and equals from the
     * end of a node label.  But keep occasional functional tags as
     * determined by class parameters, particularly TMP
     *
     * @param label The label to be cleaned up
     */
    protected String cleanUpLabel(String label) {
      if (label == null) {
        return "ROOT";
      } else {
        boolean nptemp = NPTmpPattern.matcher(label).matches();
        boolean pptemp = PPTmpPattern.matcher(label).matches();
        boolean anytemp = TmpPattern.matcher(label).matches();
        label = tlp.basicCategory(label);
        if (anytemp && splitXPTMP) {
          label += "-TMP";
        } else if (pptemp && splitPPTMP) {
          label = label + "-TMP";
        } else if (nptemp && splitNPTMP) {
          label = label + "-TMP";
        }
        return label;
      }
    }


    public Tree normalizeWholeTree(Tree tree, TreeFactory tf) {
      Tree newTree = tree.prune(new Filter<Tree>() {
        public boolean accept(Tree t) {
          Tree[] kids = t.children();
          Label l = t.label();
          if ((l != null) && l.value() != null && // there appears to be a mistake in CTB3 where the label "-NONE-1" is used once
                  // presumably it should be "-NONE-" and be spliced out here.
                  (l.value().matches("-NONE-.*")) && !t.isLeaf() && kids.length == 1 && kids[0].isLeaf()) {
            // Delete empty/trace nodes (ones marked '-NONE-')
            if ( ! l.value().equals("-NONE-")) {
              printlnErr("Deleting errant node " + l.value() + " as if -NONE-: " + t);
            }
            return false;
          }
          return true;
        }
      }, tf).spliceOut(new Filter<Tree>() {
        // this one deletes A over A phrasal node unary rewrites
        public boolean accept(Tree t) {
          if (t.isLeaf() || t.isPreTerminal() || t.children().length != 1) {
            return true;
          }
          return ! (t.label() != null && t.label().equals(t.children()[0].label()));
        }
      }, tf);

      // Report non-unary initial rewrites & fix 'obvious ones'
      Tree[] kids = newTree.children();
      if (kids.length > 1) {
      /* -------------- don't do this as probably shouldn't for test set (and doesn't help anyway)
        if (kids.length == 2 &&
            "PU".equals(kids[kids.length - 1].value()) &&
            kids[0].isPhrasal()) {
          printlnErr("Correcting error: non-unary initial rewrite fixed by tucking punctuation inside constituent: " + newTree.localTree());
          List kidkids = kids[0].getChildrenAsList();
          kidkids.add(kids[1]);
          Tree bigger = tf.newTreeNode(kids[0].label(), kidkids);
          newTree = tf.newTreeNode(newTree.label(), Collections.singletonList(bigger));
        } else {
      -------------------- */
          printlnErr("Possible error: non-unary initial rewrite: " +
                             newTree.localTree());
        // }
      }
      // note that there's also at least 1 tree that is an IP with no surrounding ROOT node

      // there are also several places where "NP" is used as a preterminal tag
      // and presumably should be "NN"
      // a couple of other random errors are corrected here
      for (Tree subtree : newTree) {
        if (subtree.isPreTerminal()) {
          if (subtree.value().matches("NP")) {
            if (ChineseTreebankLanguagePack.chineseDouHaoAcceptFilter().accept(subtree.firstChild().value())) {
              printlnErr("Correcting error: NP preterminal over douhao; preterminal changed to PU: " + subtree);
              subtree.setValue("PU");
            } else if (subtree.parent(newTree).value().matches("NP")) {
              printlnErr("Correcting error: NP preterminal w/ NP parent; preterminal changed to NN: " + subtree.parent(newTree));
              subtree.setValue("NN");
            } else {
              printlnErr("Correcting error: NP preterminal w/o NP parent, changing preterminal to NN: " + subtree.parent(newTree));
              // Tree newChild = tf.newTreeNode("NN", Collections.singletonList(subtree.firstChild()));
              // subtree.setChildren(Collections.singletonList(newChild));
              subtree.setValue("NN");
            }
          } else if (subtree.value().matches("PU")) {
            if (subtree.firstChild().value().matches("\u4ed6")) {
              printlnErr("Correcting error: \"\u4ed6\" under PU tag; tag changed to PN: " + subtree);
              subtree.setValue("PN");
            } else if (subtree.firstChild().value().matches("tw|\u534A\u7A74\u5F0F")) {
              printlnErr("Correcting error: \"" + subtree.firstChild().value() + "\" under PU tag; tag changed to NN: " + subtree);
              subtree.setValue("NN");
            } else if (subtree.firstChild().value().matches("33")) {
              printlnErr("Correcting error: \"33\" under PU tag; tag changed to CD: " + subtree);
              subtree.setValue("CD");
            }
          }
        } else if (subtree.value().matches("NN")) {
          printlnErr("Correcting error: NN phrasal tag changed to NP: " + subtree);
          subtree.setValue("NP");
        } else if (subtree.value().matches("MSP")) {
          printlnErr("Correcting error: MSP phrasal tag changed to VP: " + subtree);
          subtree.setValue("VP");
        }
      }

      if (tagExtender != null) {
        newTree = tagExtender.transformTree(newTree);
      }
      return newTree;
    }

  } // end class CTBErrorCorrectingTreeNormalizer

  /**
   * Uses a DiskTreebank with a CHTBTokenizer and a
   * BobChrisTreeNormalizer.
   */
  public DiskTreebank diskTreebank() {
    String encoding = inputEncoding;
    if (!java.nio.charset.Charset.isSupported(encoding)) {
      printlnErr("Warning: desired encoding " + encoding + " not accepted. ");
      printlnErr("Using UTF-8 to construct DiskTreebank");
      encoding = "UTF-8";
    }

    return new DiskTreebank(treeReaderFactory(), encoding);
  }


  /**
   * Uses a MemoryTreebank with a CHTBTokenizer and a
   * BobChrisTreeNormalizer
   */
  public MemoryTreebank memoryTreebank() {
    String encoding = inputEncoding;
    if (!java.nio.charset.Charset.isSupported(encoding)) {
      System.out.println("Warning: desired encoding " + encoding + " not accepted. ");
      System.out.println("Using UTF-8 to construct MemoryTreebank");
      encoding = "UTF-8";
    }

    return new MemoryTreebank(treeReaderFactory(), encoding);
  }


  /**
   * Returns a ChineseCollinizer
   */
  public TreeTransformer collinizer() {
    return new ChineseCollinizer(ctlp);
  }

  /**
   * Returns a ChineseCollinizer that doesn't delete punctuation
   */
  public TreeTransformer collinizerEvalb() {
    return new ChineseCollinizer(ctlp, false);
  }

  //   /** Returns a <code>ChineseTreebankLanguagePack</code> */
  //   public TreebankLanguagePack treebankLanguagePack() {
  //     return new ChineseTreebankLanguagePack();
  //   }


  /* --------- not used now
    // Automatically generated by ParentAnnotationStats -- preferably don't edit
    private static String[] splitters1 = new String[] {"VA^VCD", "NP^NP", "NP^VP", "NP^IP", "NP^DNP", "NP^PP", "NP^LCP", "NP^PRN", "NP^QP", "PP^IP", "PP^NP", "NN^FRAG", "NN^NP", "NT^FRAG", "NT^NP", "NR^FRAG", "NR^NP", "VV^FRAG", "VV^VRD", "VV^VCD", "VV^VP", "VV^VSB", "VP^VP", "VP^IP", "VP^DVP", "IP^ROOT", "IP^IP", "IP^CP", "IP^VP", "IP^PP", "IP^NP", "IP^LCP", "CP^IP", "QP^NP", "QP^PP", "QP^VP", "ADVP^CP", "CC^VP", "CC^NP", "CC^IP", "CC^QP", "PU^NP", "PU^FRAG", "PU^IP", "PU^VP", "PU^PRN", "PU^QP", "PU^LST", "NP^DNP~QP", "NT^NP~NP", "NT^NP~VP", "NT^NP~IP", "NT^NP~LCP", "NT^NP~PP", "NT^NP~PRN", "NT^NP~QP", "NT^NP~DNP", "NP^NP~VP", "NP^NP~NP", "NP^NP~IP", "NP^NP~PP", "NP^NP~DNP", "NP^NP~LCP", "NN^NP~VP", "NN^NP~IP", "NN^NP~NP", "NN^NP~PP", "NN^NP~DNP", "NN^NP~LCP", "NN^NP~UCP", "NN^NP~QP", "NN^NP~PRN", "M^CLP~DP", "M^CLP~QP", "M^CLP~NP", "M^CLP~CLP", "CD^QP~VP", "CD^QP~NP", "CD^QP~QP", "CD^QP~LCP", "CD^QP~PP", "CD^QP~DNP", "CD^QP~DP", "CD^QP~IP", "IP^IP~IP", "IP^IP~ROOT", "IP^IP~VP", "LC^LCP~PP", "LC^LCP~IP", "NP^VP~IP", "NP^VP~VP", "AD^ADVP~IP", "AD^ADVP~QP", "AD^ADVP~VP", "AD^ADVP~NP", "AD^ADVP~PP", "AD^ADVP~ADVP", "NP^IP~ROOT", "NP^IP~IP", "NP^IP~CP", "NP^IP~VP", "DT^DP~PP", "P^PP~IP", "P^PP~NP", "P^PP~VP", "P^PP~DNP", "VV^VP~IP", "VV^VP~VP", "PU^IP~IP", "PU^IP~VP", "PU^IP~ROOT", "PU^IP~CP", "JJ^ADJP~DNP", "JJ^ADJP~ADJP", "NR^NP~IP", "NR^NP~NP", "NR^NP~PP", "NR^NP~VP", "NR^NP~DNP", "NR^NP~LCP", "NR^NP~PRN", "NP^PP~NP", "NP^PP~IP", "NP^PP~DNP", "VA^VP~VP", "VA^VP~IP", "VA^VP~DVP", "VP^VP~VP", "VP^VP~IP", "VP^VP~DVP", "VP^IP~ROOT", "VP^IP~CP", "VP^IP~IP", "VP^IP~VP", "VP^IP~PP", "VP^IP~LCP", "VP^IP~NP", "PN^NP~NP", "PN^NP~IP", "PN^NP~PP"};
    private static String[] splitters2 = new String[] {"VA^VCD", "NP^NP", "NP^VP", "NP^IP", "NP^DNP", "NP^PP", "NP^LCP", "NN^FRAG", "NN^NP", "NT^FRAG", "NT^NP", "NR^FRAG", "NR^NP", "VV^FRAG", "VV^VRD", "VV^VCD", "VV^VP", "VV^VSB", "VP^VP", "VP^IP", "VP^DVP", "IP^ROOT", "IP^IP", "IP^CP", "IP^VP", "IP^PP", "CP^IP", "ADVP^CP", "CC^VP", "CC^NP", "PU^NP", "PU^FRAG", "PU^IP", "PU^VP", "PU^PRN", "NT^NP~NP", "NT^NP~VP", "NT^NP~IP", "NT^NP~LCP", "NT^NP~PP", "NP^NP~VP", "NP^NP~NP", "NP^NP~IP", "NP^NP~PP", "NP^NP~DNP", "NN^NP~VP", "NN^NP~IP", "NN^NP~NP", "NN^NP~PP", "NN^NP~DNP", "NN^NP~LCP", "NN^NP~UCP", "NN^NP~QP", "NN^NP~PRN", "M^CLP~DP", "CD^QP~VP", "CD^QP~NP", "CD^QP~QP", "CD^QP~LCP", "CD^QP~PP", "CD^QP~DNP", "CD^QP~DP", "LC^LCP~PP", "NP^VP~IP", "NP^VP~VP", "AD^ADVP~IP", "AD^ADVP~QP", "AD^ADVP~VP", "AD^ADVP~NP", "NP^IP~ROOT", "NP^IP~IP", "NP^IP~CP", "NP^IP~VP", "P^PP~IP", "P^PP~NP", "P^PP~VP", "P^PP~DNP", "VV^VP~IP", "VV^VP~VP", "PU^IP~IP", "PU^IP~VP", "PU^IP~ROOT", "PU^IP~CP", "JJ^ADJP~DNP", "NR^NP~IP", "NR^NP~NP", "NR^NP~PP", "NR^NP~VP", "NR^NP~DNP", "NR^NP~LCP", "NP^PP~NP", "VA^VP~VP", "VA^VP~IP", "VP^VP~VP", "VP^IP~ROOT", "VP^IP~CP", "VP^IP~IP", "VP^IP~VP", "VP^IP~PP", "VP^IP~LCP", "VP^IP~NP", "PN^NP~NP"};
    private static String[] splitters3 = new String[] {"NP^NP", "NP^VP", "NP^IP", "NP^DNP", "NP^PP", "NP^LCP", "NN^FRAG", "NN^NP", "NT^FRAG", "NR^FRAG", "NR^NP", "VV^FRAG", "VV^VRD", "VV^VCD", "VV^VP", "VV^VSB", "VP^VP", "VP^IP", "IP^ROOT", "IP^IP", "IP^CP", "IP^VP", "PU^NP", "PU^FRAG", "PU^IP", "PU^VP", "PU^PRN", "NP^NP~VP", "NN^NP~VP", "NN^NP~IP", "NN^NP~NP", "NN^NP~PP", "NN^NP~DNP", "NN^NP~LCP", "M^CLP~DP", "CD^QP~VP", "CD^QP~NP", "CD^QP~QP", "AD^ADVP~IP", "AD^ADVP~QP", "AD^ADVP~VP", "P^PP~IP", "VV^VP~IP", "VV^VP~VP", "PU^IP~IP", "PU^IP~VP", "NR^NP~IP", "NR^NP~NP", "NR^NP~PP", "NR^NP~VP", "VP^VP~VP", "VP^IP~ROOT", "VP^IP~CP", "VP^IP~IP", "VP^IP~VP"};
    private static String[] splitters4 = new String[] {"NP^NP", "NP^VP", "NP^IP", "NN^FRAG", "NT^FRAG", "NR^FRAG", "VV^FRAG", "VV^VRD", "VV^VCD", "VP^VP", "VP^IP", "IP^ROOT", "IP^IP", "IP^CP", "IP^VP", "PU^NP", "PU^FRAG", "PU^IP", "PU^VP", "NN^NP~VP", "NN^NP~IP", "NN^NP~NP", "NN^NP~PP", "NN^NP~DNP", "NN^NP~LCP", "CD^QP~VP", "CD^QP~NP", "AD^ADVP~IP", "VV^VP~IP", "VV^VP~VP", "NR^NP~IP", "VP^IP~ROOT", "VP^IP~CP"};
    // these ones were built by hand.
    // one can't tag split under FRAG or everything breaks, because of those
    // big flat FRAGs....
    private static String[] splitters5 =
      new String[] {"NN^FRAG", "NT^FRAG", "NR^FRAG", "VV^FRAG",
                    "VV^VCD", "VV^VRD",
                    "NP^NP", "VP^VP",
                    "IP^ROOT", "IP^IP",
                    "PU^NP", "PU^FRAG",
                    "P^PP~VP", "P^PP~IP"};
    private static String[] splitters6 =
      new String[] {"VV^VCD", "VV^VRD",
                    "NP^NP", "VP^VP",
                    "IP^ROOT", "IP^IP",
                    "PU^NP",
                    "P^PP~VP", "P^PP~IP"};
    private static String[] splitters7 =
      new String[] {"NP^NP", "VP^VP",
                    "IP^ROOT", "IP^IP",
                    "PU^NP",
                    "P^PP~VP", "P^PP~IP"};
    private static String[] splitters8 =
      new String[] {"IP^ROOT", "IP^IP",
                    "PU^NP",
                    "P^PP~VP", "P^PP~IP"};
    private static String[] splitters9 =
      new String[] {"VV^VCD", "VV^VRD",
                    "NP^NP", "VP^VP",
                    "IP^ROOT", "IP^IP",
                    "P^PP~VP", "P^PP~IP"};
    private static String[] splitters10 =
      new String[] {"NP^NP", "VP^VP",
                    "IP^ROOT", "IP^IP",
                    "P^PP~VP", "P^PP~IP"};


    public String[] splitters() {
      switch (selectiveSplitLevel) {
      case 1:
        return splitters1;
      case 2:
        return splitters2;
      case 3:
        return splitters3;
      case 4:
        return splitters4;
      case 5:
        return splitters5;
      case 6:
        return splitters6;
      case 7:
        return splitters7;
      case 8:
        return splitters8;
      case 9:
        return splitters9;
      case 10:
        return splitters10;
      default:
        return new String[0];
      }
    }
  ------------------ */

  public String[] sisterSplitters() {
    return StringUtils.EMPTY_STRING_ARRAY;
  }

  /**
   * transformTree does all language-specific tree
   * transformations. Any parameterizations should be inside the
   * specific TreebankLangParserParams class.
   */
  public Tree transformTree(Tree t, Tree root) {
    if (t == null || t.isLeaf()) {
      return t;
    }

    String parentStr;
    String grandParentStr;
    Tree parent;
    Tree grandParent;
    if (root == null || t.equals(root)) {
      parent = null;
      parentStr = "";
    } else {
      parent = t.parent(root);
      parentStr = parent.label().value();
    }
    if (parent == null || parent.equals(root)) {
      grandParent = null;
      grandParentStr = "";
    } else {
      grandParent = parent.parent(root);
      grandParentStr = grandParent.label().value();
    }

    String baseParentStr = ctlp.basicCategory(parentStr);
    String baseGrandParentStr = ctlp.basicCategory(grandParentStr);

    CategoryWordTag lab = (CategoryWordTag) t.label();
    String word = lab.word();
    String tag = lab.tag();
    String baseTag = ctlp.basicCategory(tag);
    String category = lab.value();
    String baseCategory = ctlp.basicCategory(category);

    if (t.isPreTerminal()) { // it's a POS tag
      List leftAunts = listBasicCategories(SisterAnnotationStats.leftSisterLabels(parent, grandParent));
      List rightAunts = listBasicCategories(SisterAnnotationStats.rightSisterLabels(parent, grandParent));

      // Chinese-specific punctuation splits
      if (chineseSplitPunct && baseTag.equals("PU")) {
        if (ctlp.chineseDouHaoAcceptFilter().accept(word)) {
          tag = tag + "-DOU";
          // System.out.println("Punct: Split dou hao"); // debugging
        } else if (ctlp.chineseCommaAcceptFilter().accept(word)) {
          tag = tag + "-COMMA";
          // System.out.println("Punct: Split comma"); // debugging
        } else if (ctlp.chineseColonAcceptFilter().accept(word)) {
          tag = tag + "-COLON";
          // System.out.println("Punct: Split colon"); // debugging
        } else if (ctlp.chineseQuoteMarkAcceptFilter().accept(word)) {
          if (chineseSplitPunctLR) {
            if (ctlp.chineseLeftQuoteMarkAcceptFilter().accept(word)) {
              tag += "-LQUOTE";
            } else {
              tag += "-RQUOTE";
            }
          } else {
            tag = tag + "-QUOTE";
          }
          // System.out.println("Punct: Split quote"); // debugging
        } else if (ctlp.chineseEndSentenceAcceptFilter().accept(word)) {
          tag = tag + "-ENDSENT";
          // System.out.println("Punct: Split end sent"); // debugging
        } else if (ctlp.chineseParenthesisAcceptFilter().accept(word)) {
          if (chineseSplitPunctLR) {
            if (ctlp.chineseLeftParenthesisAcceptFilter().accept(word)) {
              tag += "-LPAREN";
            } else {
              tag += "-RPAREN";
            }
          } else {
            tag += "-PAREN";
            //printlnErr("Just used -PAREN annotation");
            //printlnErr(word);
            //throw new RuntimeException();
          }
          // System.out.println("Punct: Split paren"); // debugging
        } else if (ctlp.chineseDashAcceptFilter().accept(word)) {
          tag = tag + "-DASH";
          // System.out.println("Punct: Split dash"); // debugging
        } else if (ctlp.chineseOtherAcceptFilter().accept(word)) {
          tag = tag + "-OTHER";
        } else {
          printlnErr("Unknown punct (you should add it to CTLP): " + tag + " " + word);
          SeeChars.seeChars(word, ChineseTreebankLanguagePack.ENCODING);
        }
      } else if (chineseSplitDouHao) {   // only split DouHao
        if (ctlp.chineseDouHaoAcceptFilter().accept(word) && baseTag.equals("PU")) {
          tag = tag + "-DOU";
        }
      }

      // Chinese-specific POS tag splits (non-punctuation)

      if (tagWordSize) {
        int l = word.length();
        tag += "-" + l + "CHARS";
      }

      if (mergeNNVV & baseTag.equals("NN")) {
        tag = "VV";
      }

      if ((chineseSelectiveTagPA || chineseVerySelectiveTagPA) && (baseTag.equals("CC") || baseTag.equals("P"))) {
        tag += "-" + baseParentStr;
      }
      if (chineseSelectiveTagPA && (baseTag.equals("VV"))) {
        tag += "-" + baseParentStr;
      }

      if (markMultiNtag && tag.startsWith("N")) {
        for (int i = 0; i < parent.numChildren(); i++) {
          if (parent.children()[i].label().value().startsWith("N") && parent.children()[i] != t) {
            tag += "=N";
            //System.out.println("Found multi=N rewrite");
          }
        }
      }

      if (markVVsisterIP && baseTag.equals("VV")) {
        boolean seenIP = false;
        for (int i = 0; i < parent.numChildren(); i++) {
          if (parent.children()[i].label().value().startsWith("IP")) {
            seenIP = true;
          }
        }
        if (seenIP) {
          tag += "-IP";
          //System.out.println("Found VV with IP sister"); // testing
        }
      }

      if (markPsisterIP && baseTag.equals("P")) {
        boolean seenIP = false;
        for (int i = 0; i < parent.numChildren(); i++) {
          if (parent.children()[i].label().value().startsWith("IP")) {
            seenIP = true;
          }
        }
        if (seenIP) {
          tag += "-IP";
        }
      }

      if (markADgrandchildOfIP && baseTag.equals("AD") && baseGrandParentStr.equals("IP")) {
        tag += "~IP";
        //System.out.println("Found AD with IP grandparent"); // testing
      }

      if (gpaAD && baseTag.equals("AD")) {
        tag += "~" + baseGrandParentStr;
        //System.out.println("Found AD with grandparent " + grandParentStr); // testing
      }

      if (markPostverbalP && leftAunts.contains("VV") && baseTag.equals("P")) {
        //System.out.println("Found post-verbal P");
        tag += "^=lVV";
      }

      // end Chinese-specific tag splits

      Label label = new CategoryWordTag(tag, word, tag);
      t.setLabel(label);
    } else {
      // it's a phrasal category
      Tree[] kids = t.children();

      // Chinese-specific category splits
      List leftSis = listBasicCategories(SisterAnnotationStats.leftSisterLabels(t, parent));
      List rightSis = listBasicCategories(SisterAnnotationStats.rightSisterLabels(t, parent));

      if (paRootDtr && baseParentStr.equals("ROOT")) {
        category += "^ROOT";
      }

      if (markIPsisterBA && baseCategory.equals("IP")) {
        if (leftSis.contains("BA")) {
          category += "=BA";
          //System.out.println("Found IP sister of BA");
        }
      }

      if (dominatesV && hasV(t.preTerminalYield())) {
        // mark categories containing a verb
        category += "-v";
      }

      if (markIPsisterVVorP && baseCategory.equals("IP")) {
        if (leftSis.contains("VV") || leftSis.contains("P")) {
          category += "=VVP";
        }
      }

      if (markIPsisDEC && baseCategory.equals("IP")) {
        if (rightSis.contains("DEC")) {
          category += "=DEC";
          //System.out.println("Found prenominal IP");
        }
      }

      if (chineseSplitVP3 && category.startsWith("VP")) {
        boolean hasCC = false;
        boolean hasPU = false;
        boolean hasLexV = false;
        for (int i = 0; i < kids.length; i++) {
          if (kids[i].label().value().startsWith("CC")) {
            hasCC = true;
          }
          if (kids[i].label().value().startsWith("PU")) {
            hasPU = true;
          }
          if (StringUtils.lookingAt(kids[i].label().value(), "(V[ACEV]|VCD|VCP|VNV|VPT|VRD|VSB)")) {
            hasLexV = true;
          }
        }
        if (hasCC || (hasPU && !hasLexV)) {
          category += "-CRD";
          //System.out.println("Found coordinate VP"); // testing
        } else if (hasLexV) {
          category += "-COMP";
          //System.out.println("Found complementing VP"); // testing
        } else {
          category += "-ADJT";
          //System.out.println("Found adjoining VP"); // testing
        }
      }

      if (markVPadjunct && parentStr.startsWith("VP")) {
        Tree[] sisters = parent.children();
        boolean hasVPsister = false;
        boolean hasCC = false;
        boolean hasPU = false;
        boolean hasLexV = false;
        for (int i = 0; i < sisters.length; i++) {
          if (sisters[i].label().value().startsWith("VP")) {
            hasVPsister = true;
          }
          if (sisters[i].label().value().startsWith("CC")) {
            hasCC = true;
          }
          if (sisters[i].label().value().startsWith("PU")) {
            hasPU = true;
          }
          if (StringUtils.lookingAt(sisters[i].label().value(), "(V[ACEV]|VCD|VCP|VNV|VPT|VRD|VSB)")) {
            hasLexV = true;
          }
        }
        if (hasVPsister && !(hasCC || hasPU || hasLexV)) {
          category += "-VPADJ";
          //System.out.println("Found adjunct of VP"); // testing
        }
      }

      if (markNPmodNP && baseCategory.equals("NP") && baseParentStr.equals("NP")) {
        if (rightSis.contains("NP")) {
          category += "=MODIFIERNP";
          //System.out.println("Found NP modifier of NP"); // testing
        }
      }

      if (markModifiedNP && baseCategory.equals("NP") && baseParentStr.equals("NP")) {
        if (rightSis.size() == 0 && (leftSis.contains("ADJP") || leftSis.contains("NP") || leftSis.contains("DNP") || leftSis.contains("QP") || leftSis.contains("CP") || leftSis.contains("PP"))) {
          category += "=MODIFIEDNP";
          //System.out.println("Found modified NP"); // testing
        }
      }

      if (markNPconj && baseCategory.equals("NP") && baseParentStr.equals("NP")) {
        if (rightSis.contains("CC") || rightSis.contains("PU") || leftSis.contains("CC") || leftSis.contains("PU")) {
          category += "=CONJ";
          //System.out.println("Found NP conjunct"); // testing
        }
      }

      if (markIPconj && baseCategory.equals("IP") && baseParentStr.equals("IP")) {
        Tree[] sisters = parent.children();
        boolean hasCommaSis = false;
        boolean hasIPSis = false;
        for (int i = 0; i < sisters.length; i++) {
          if (ctlp.basicCategory(sisters[i].label().value()).equals("PU") && ctlp.chineseCommaAcceptFilter().accept(sisters[i].children()[0].label().toString())) {
            hasCommaSis = true;
            //System.out.println("Found CommaSis"); // testing
          }
          if (ctlp.basicCategory(sisters[i].label().value()).equals("IP") && sisters[i] != t) {
            hasIPSis = true;
          }
        }
        if (hasCommaSis && hasIPSis) {
          category += "-CONJ";
          //System.out.println("Found IP conjunct"); // testing
        }
      }

      if (unaryIP && baseCategory.equals("IP") && t.numChildren() == 1) {
        category += "-U";
        //System.out.println("Found unary IP"); //testing
      }
      if (unaryCP && baseCategory.equals("CP") && t.numChildren() == 1) {
        category += "-U";
        //System.out.println("Found unary CP"); //testing
      }

      if (splitBaseNP && baseCategory.equals("NP")) {
        if (t.isPrePreTerminal()) {
          category = category + "-B";
        }
      }

      //if (Test.verbose) printlnErr(baseCategory + " " + leftSis.toString()); //debugging

      if (markPostverbalPP && leftSis.contains("VV") && baseCategory.equals("PP")) {
        //System.out.println("Found post-verbal PP");
        category += "=lVV";
      }

      if ((markADgrandchildOfIP || gpaAD) && listBasicCategories(SisterAnnotationStats.kidLabels(t)).contains("AD")) {
        category += "^ADVP";
      }

      if (markCC) {
        // was: for (int i = 0; i < kids.length; i++) {
        // This second version takes an idea from Collins: don't count
        // marginal conjunctions which don't conjoin 2 things.
        for (int i = 1; i < kids.length - 1; i++) {
          String cat2 = kids[i].label().value();
          if (cat2.startsWith("CC")) {
            category += "-CC";
          }
        }
      }

      Label label = new CategoryWordTag(category, word, tag);
      t.setLabel(label);
    }
    return t;
  }


  /**
   * Chinese: Split the dou hao (a punctuation mark separating
   * members of a list) from other punctuation.  Good but included below.
   */
  public static boolean chineseSplitDouHao = false;
  /**
   * Chinese: split Chinese punctuation several ways, along the lines
   * of English punctuation plus another category for the dou hao.  Good.
   */
  public static boolean chineseSplitPunct = true;
  /**
   * Chinese: split left right/paren quote (if chineseSplitPunct is also
   * true.  Only very marginal gains, but seems positive.
   */
  public static boolean chineseSplitPunctLR = false;

  /**
   * Chinese: mark VVs that are sister of IP (communication &
   * small-clause-taking verbs).  Good: give 0.5%
   */
  public static boolean markVVsisterIP = true;

  /**
   * Chinese: mark P's that are sister of IP.  Negative effect
   */
  public static boolean markPsisterIP = true;

  /**
   * Chinese: mark IP's that are sister of VV or P.  These rarely
   * have punctuation. Small positive effect.
   */
  public static boolean markIPsisterVVorP = true;


  /**
   * Chinese: mark ADs that are grandchild of IP.
   */
  public static boolean markADgrandchildOfIP = false;
  /**
   * Grandparent annotate all AD.  Seems slightly negative.
   */
  public static boolean gpaAD = true;

  // using tagPA on Chinese 100k is negative.

  public static boolean chineseVerySelectiveTagPA = false;
  public static boolean chineseSelectiveTagPA = false;

  /**
   * Chinese: mark IPs that are sister of BA.  These always have
   * overt NP.  Very slightly positive.
   */
  public static boolean markIPsisterBA = true;

  /**
   * Chinese: mark phrases that are adjuncts of VP (these tend to be
   * locatives/temporals, and have a specific distribution).
   * Necessary even with chineseSplitVP3 and parent annotation because
   * parent annotation happens with unsplit parent categories.
   * Slightly positive.
   */
  public static boolean markVPadjunct = true;

  /**
   * Chinese: mark NP modifiers of NPs. Quite positive (0.5%)
   */
  public static boolean markNPmodNP = true;

  /**
   * Chinese: mark left-modified NPs (rightmost NPs with a left-side
   * mod).  Slightly positive.
   */
  public static boolean markModifiedNP = true;

  /**
   * Chinese: mark NPs that are conjuncts.  Negative on small set.
   */
  public static boolean markNPconj = true;

  /**
   * Chinese: mark nominal tags that are part of multi-nominal
   * rewrites.  Doesn't seem any good.
   */
  public static boolean markMultiNtag = false;

  /**
   * Chinese: mark IPs that are part of prenominal modifiers. Negative.
   */
  public static boolean markIPsisDEC = true;

  /**
   * Chinese: mark IPs that are conjuncts.  Or those that have
   * (adjuncts or subjects)
   */
  public static boolean markIPconj = false;
  public static boolean markIPadjsubj = false;

  /**
   * Chinese: split VPs into VP-COMP, VP-CRD, VP-ADJ.  Negative value.
   */
  public static boolean chineseSplitVP3 = true;

  /** Chinese: if an IP has no subject (including no empty-category
   * subject), then it should only have an NP (adjunct) daughter if
   * it's a coordinate IP and the NP scopes over the conjunct
   * IPs. (sometimes this NP daughter is adjoined in an IP -> NP
   * IP_coord structure, sometimes the IP conjuncts are at the same
   * level as the NP).  In other cases NP adjuncts should be inside
   * VP.  So: an IP dominating neither a non-subject NP nor another IP
   * should have no NP daughters.  BUT this generalization breaks down
   * when you try to extend it to IPs ignoring their empty subjects.
   * So the simplest thing to do would be to mark non-subject dtrs of
   * IP....  but I think we need to leave the SBJ functional tagging
   * on categories to be consistent about this.
   *
   * Update: I tried retaining SBJ markers with
   * SbjRetainingTreeNormalizer but it works worse than using
   * markVPadjunct.
   */

  /**
   * Chinese: merge NN and VV.  A lark.
   */
  public static boolean mergeNNVV = false;

  // XXXX upto in testing

  /**
   * Chinese: unary category marking
   */
  public static boolean unaryIP = false;
  public static boolean unaryCP = false;

  /**
   * Chinese: parent annotate daughter of root.  Meant only for
   * selectivesplit=false.
   */
  public static boolean paRootDtr = false; // true

  /**
   * Chinese: mark P with a left aunt VV, and PP with a left sister
   * VV.  Note that it's necessary to mark both to thread the
   * context-marking.  Used to identify post-verbal P's, which are
   * rare.
   */
  public static boolean markPostverbalP = false;
  public static boolean markPostverbalPP = false;


  // Not used now
  // /** How selectively to split. */
  // public static int selectiveSplitLevel = 1;

  /**
   * Mark base NPs.  Good.
   */
  public static boolean splitBaseNP = false;

  /**
   * Annotate tags for number of characters contained.
   */
  public static boolean tagWordSize = false;

  /**
   * Mark phrases which are conjunctions.
   * Appears negative, even with 200K words training data.
   */
  public static boolean markCC = false;

  /**
   * Whether to retain the -TMP functional tag on various phrasal
   * categories.  On 80K words training, minutely helpful; on 200K
   * words, best option gives 0.6%.  Doing
   * splitNPTMP and splitPPTMP (but not splitXPTMP) is best.
   */
  public static boolean splitNPTMP = false;
  public static boolean splitPPTMP = false;
  public static boolean splitXPTMP = false;

  /**
   * Verbal distance -- mark whether symbol dominates a verb (V*).
   * Seems bad for Chinese.
   */
  public static boolean dominatesV = false;


  public void display() {
    String chineseParams = "Using ChineseTreebankParserParams" + " chineseSplitDouHao=" + chineseSplitDouHao + " chineseSplitPunct=" + chineseSplitPunct + " chineseSplitPunctLR=" + chineseSplitPunctLR + " markVVsisterIP=" + markVVsisterIP + " markVPadjunct=" + markVPadjunct + " chineseSplitVP3=" + chineseSplitVP3 + " mergeNNVV=" + mergeNNVV + " unaryIP=" + unaryIP + " unaryCP=" + unaryCP + " paRootDtr=" + paRootDtr + " markPsisterIP=" + markPsisterIP + " markIPsisterVVorP=" + markIPsisterVVorP + " markADgrandchildOfIP=" + markADgrandchildOfIP + " gpaAD=" + gpaAD + " markIPsisterBA=" + markIPsisterBA + " markNPmodNP=" + markNPmodNP + " markNPconj=" + markNPconj + " markMultiNtag=" + markMultiNtag + " markIPsisDEC=" + markIPsisDEC + " markIPconj=" + markIPconj + " markIPadjsubj=" + markIPadjsubj + " markPostverbalP=" + markPostverbalP + " markPostverbalPP=" + markPostverbalPP
            //      + " selSplitLevel=" + selectiveSplitLevel
            + " baseNP=" + splitBaseNP + " headFinder=" + (sunJurafskyHeadFinder ? "sunJurafsky" : (bikelHeadFinder ? "bikel" : "levy")) + " discardFrags=" + discardFrags  + " dominatesV=" + dominatesV;
    printlnErr(chineseParams);
  }


  private List<String> listBasicCategories(List<String> l) {
    List<String> l1 = new ArrayList<String>();
    for (String s : l) {
      l1.add(ctlp.basicCategory(s));
    }
    return l1;
  }

  // TODO: Rewrite this as general matching predicate
  private static boolean hasV(List tags) {
    for (int i = 0, tsize = tags.size(); i < tsize; i++) {
      String str = tags.get(i).toString();
      if (str.startsWith("V")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Set language-specific options according to flags.
   * This routine should process the option starting in args[i] (which
   * might potentially be several arguments long if it takes arguments).
   * It should return the index after the last index it consumed in
   * processing.  In particular, if it cannot process the current option,
   * the return value should be i.
   */
  public int setOptionFlag(String[] args, int i) {
    // if (args[i].equalsIgnoreCase("-vSelSplitLevel") &&
    //            (i+1 < args.length)) {
    //   selectiveSplitLevel = Integer.parseInt(args[i+1]);
    //   i+=2;
    // } else
    if (args[i].equalsIgnoreCase("-paRootDtr")) {
      paRootDtr = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-unaryIP")) {
      unaryIP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-unaryCP")) {
      unaryCP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markPostverbalP")) {
      markPostverbalP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markPostverbalPP")) {
      markPostverbalPP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-baseNP")) {
      splitBaseNP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markVVsisterIP")) {
      markVVsisterIP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markPsisterIP")) {
      markPsisterIP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markIPsisterVVorP")) {
      markIPsisterVVorP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markIPsisterBA")) {
      markIPsisterBA = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-dominatesV")) {
      dominatesV = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-gpaAD")) {
      gpaAD = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markVPadjunct")) {
      markVPadjunct = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markNPmodNP")) {
      markNPmodNP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markModifiedNP")) {
      markModifiedNP = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markNPconj")) {
      markNPconj = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chineseSplitPunct")) {
      chineseSplitPunct = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chineseSplitPunctLR")) {
      chineseSplitPunct = true;
      chineseSplitPunctLR = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chineseSelectiveTagPA")) {
      chineseSelectiveTagPA = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chineseVerySelectiveTagPA")) {
      chineseVerySelectiveTagPA = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-markIPsisDEC")) {
      markIPsisDEC = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chineseSplitVP3")) {
      chineseSplitVP3 = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-tagWordSize")) {
      tagWordSize = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-vanilla")) {
      chineseSplitDouHao = false;
      chineseSplitPunct = false;
      chineseSplitPunctLR = false;
      markVVsisterIP = false;
      markPsisterIP = false;
      markIPsisterVVorP = false;
      markADgrandchildOfIP = false;
      gpaAD = false;
      markIPsisterBA = false;
      markVPadjunct = false;
      markNPmodNP = false;
      markModifiedNP = false;
      markNPconj = false;
      markMultiNtag = false;
      markIPsisDEC = false;
      markIPconj = false;
      markIPadjsubj = false;
      chineseSplitVP3 = false;
      mergeNNVV = false;
      unaryIP = false;
      unaryCP = false;
      paRootDtr = false;
      markPostverbalP = false;
      markPostverbalPP = false;
      splitBaseNP = false;
      // selectiveSplitLevel = 0;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-acl03chinese")) {
      Train.markovOrder = 1;
      Train.markovFactor = true;
      chineseSplitDouHao = false;
      chineseSplitPunct = true;
      chineseSplitPunctLR = true;
      markVVsisterIP = true;
      markPsisterIP = true;
      markIPsisterVVorP = true;
      markADgrandchildOfIP = false;
      gpaAD = true;
      markIPsisterBA = false;
      markVPadjunct = true;
      markNPmodNP = true;
      markModifiedNP = true;
      markNPconj = true;
      markMultiNtag = false;
      markIPsisDEC = true;
      markIPconj = false;
      markIPadjsubj = false;
      chineseSplitVP3 = true;
      mergeNNVV = false;
      unaryIP = true;
      unaryCP = true;
      paRootDtr = true;
      markPostverbalP = false;
      markPostverbalPP = false;
      splitBaseNP = false;
      // selectiveSplitLevel = 0;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chineseFactored")) {
      chineseSplitDouHao = false;
      chineseSplitPunct = true;
      chineseSplitPunctLR = true;
      markVVsisterIP = true;
      markPsisterIP = true;
      markIPsisterVVorP = true;
      markADgrandchildOfIP = false;
      gpaAD = true;
      markIPsisterBA = true;
      markVPadjunct = true;
      markNPmodNP = true;
      markModifiedNP = true;
      markNPconj = true;
      markMultiNtag = false;
      markIPsisDEC = true;
      markIPconj = false;
      markIPadjsubj = false;
      chineseSplitVP3 = true;
      mergeNNVV = false;
      unaryIP = true;
      unaryCP = true;
      paRootDtr = true;
      markPostverbalP = false;
      markPostverbalPP = false;
      splitBaseNP = false;
      // selectiveSplitLevel = 0;
      chineseVerySelectiveTagPA = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-chinesePCFG")) {
      Train.markovOrder = 2;
      Train.markovFactor = true;
      Train.HSEL_CUT = 5;
      Train.PA = true;
      Train.gPA = true;
      Train.selectiveSplit = false;
      chineseSplitDouHao = false;
      chineseSplitPunct = true;
      chineseSplitPunctLR = true;
      markVVsisterIP = true;
      markPsisterIP = false;
      markIPsisterVVorP = true;
      markADgrandchildOfIP = false;
      gpaAD = false;
      markIPsisterBA = true;
      markVPadjunct = true;
      markNPmodNP = true;
      markModifiedNP = true;
      markNPconj = false;
      markMultiNtag = false;
      markIPsisDEC = false;
      markIPconj = false;
      markIPadjsubj = false;
      chineseSplitVP3 = false;
      mergeNNVV = false;
      unaryIP = false;
      unaryCP = false;
      paRootDtr = false;
      markPostverbalP = false;
      markPostverbalPP = false;
      splitBaseNP = false;
      // selectiveSplitLevel = 0;
      chineseVerySelectiveTagPA = true;
      i += 1;
    } else if (args[i].equalsIgnoreCase("-sunHead")) {
      sunJurafskyHeadFinder = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-bikelHead")) {
      bikelHeadFinder = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-discardFrags")) {
      discardFrags = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-charLex")) {
      useCharacterBasedLexicon = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-charUnk")) {
      ChineseLexicon.useCharBasedUnknownWordModel = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-gtUnknown")) {
      ChineseLexicon.useGoodTuringUnknownWordModel = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-maxentUnk")) {
      // ChineseLexicon.useMaxentUnknownWordModel = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-tuneSigma")) {
      // ChineseMaxentLexicon.tuneSigma = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-trainCountThresh") && (i + 1 < args.length)) {
      // ChineseMaxentLexicon.trainCountThreshold = Integer.parseInt(args[i + 1]);
      i += 2;
    } else if (args[i].equalsIgnoreCase("-markCC")) {
      markCC = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-segmentMarkov") || args[i].equalsIgnoreCase("-segmentWords")) {
      segmentMarkov = true;
      try {
        segmenter = (WordSegmenter) Class.forName("edu.stanford.nlp.parser.lexparser.ChineseMarkovWordSegmenter").newInstance();
      } catch (Exception e) {
        printlnErr("Couldn't instantiate segmenter " + "edu.stanford.nlp.parser.lexparser.ChineseMarkovWordSegmenter" + ": " + e);
      }
      i++;
    } else if (args[i].equalsIgnoreCase("-segmentMaxMatch")) {
      segmentMaxMatch = true;
      try {
        segmenter = (WordSegmenter) Class.forName("edu.stanford.nlp.parser.lexparser.MaxMatchSegmenter").newInstance();
      } catch (Exception e) {
        printlnErr("Couldn't instantiate segmenter " + "edu.stanford.nlp.parser.lexparser.MaxMatchSegmenter" + ": " + e);
      }
      i++;
    } else if (args[i].equalsIgnoreCase("-maxentLex")) {
      // useMaxentLexicon = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-fixUnkFunctionWords")) {
      // ChineseMaxentLexicon.fixUnkFunctionWords = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-similarWordSmoothing")) {
      useSimilarWordMap = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-maxentLexSeenTagsOnly")) {
      // useMaxentLexicon = true;
      // ChineseMaxentLexicon.seenTagsOnly = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-maxentLexFeatLevel") && (i + 1 < args.length)) {
      // ChineseMaxentLexicon.featureLevel = Integer.parseInt(args[i + 1]);
      i += 2;
    } else if (args[i].equalsIgnoreCase("-maxentDepGrammarFeatLevel") && (i + 1 < args.length)) {
      depGramFeatureLevel = Integer.parseInt(args[i + 1]);
      i += 2;
    } else if (args[i].equalsIgnoreCase("-maxentDepGrammar")) {
      // useMaxentDepGrammar = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-splitNPTMP")) {
      splitNPTMP = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-splitPPTMP")) {
      splitPPTMP = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-splitXPTMP")) {
      splitXPTMP = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-segmenter")) {
      try {
        segmenter = (WordSegmenter) Class.forName(args[i + 1]).newInstance();
      } catch (Exception e) {
        printlnErr("Couldn't instantiate segmenter " + args[i + 1] + ": " + e);
      }
      i += 2;
    }

    return i;
  }

  private int depGramFeatureLevel = 0;

  public Extractor dependencyGrammarExtractor(final Options op) {
    /* ----------
    if (useMaxentDepGrammar) {
      return new Extractor() {
        public Object extract(Collection<Tree> trees) {
          ChineseWordFeatureExtractor wfe = new ChineseWordFeatureExtractor(trees);
          ChineseWordFeatureExtractor wfe2 = new ChineseWordFeatureExtractor(trees);
          wfe.setFeatureLevel(2);
          wfe2.turnOffWordFeatures = true;
          wfe2.setFeatureLevel(depGramFeatureLevel);
          MaxentDependencyGrammar dg = new MaxentDependencyGrammar(op.tlpParams, wfe, wfe2, true, false, false);
          dg.train(trees);
          return dg;
        }

        public Object extract(Iterator<Tree> iterator, Function<Tree, Tree> f) {
          throw new UnsupportedOperationException();
        }
      };
    } else ------- */
    if (useSimilarWordMap) {
      return new MLEDependencyGrammarExtractor(op) {
        public Object formResult() {
          wordNumberer.number(Lexicon.UNKNOWN_WORD);
          ChineseSimWordAvgDepGrammar dg = new ChineseSimWordAvgDepGrammar(tlpParams, directional, useDistance, useCoarseDistance);
          if (lex == null) {
            throw new RuntimeException("Attempt to create ChineseSimWordAvgDepGrammar before Lexicon!!!");
          } else {
            dg.setLex(lex);
          }
          for (IntDependency dependency : dependencyCounter.keySet()) {
            dg.addRule(dependency, dependencyCounter.getCount(dependency));
          }
          return dg;
        }

     };
    } else {
      return new MLEDependencyGrammarExtractor(op);
    }
  }

  /**
   * Return a default sentence for the language (for testing)
   */
  public List defaultTestSentence() {
    return Arrays.asList(new String[]{"\u951f\u65a4\u62f7", "\u951f\u65a4\u62f7", "\u5b66\u6821", "\u951f\u65a4\u62f7", "\u5b66\u4e60", "\u951f\u65a4\u62f7"});
  }


  private static final long serialVersionUID = 2;


  /**
   * For testing: loads a treebank and prints the trees.
   */
  public static void main(String[] args) {
    TreebankLangParserParams tlpp = new ChineseTreebankParserParams();
    System.out.println("Default encoding is: " +
                       tlpp.diskTreebank().encoding());

    if (args.length < 2) {
      printlnErr("Usage: edu.stanford.nlp.parser.lexparser.ChineseTreebankParserParams treesPath fileRange");
    } else {
      Treebank m = tlpp.diskTreebank();
      m.loadPath(args[0], new NumberRangesFileFilter(args[1], false));

      for (Tree t : m ) {
        t.pennPrint(tlpp.pw());
      }
      System.out.println("There were " + m.size() + " trees.");
    }
  }

}
