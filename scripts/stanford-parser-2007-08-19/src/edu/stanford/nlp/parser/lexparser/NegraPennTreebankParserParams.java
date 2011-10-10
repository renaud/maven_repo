package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.ling.CategoryWordTag;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.negra.*;

import java.io.Reader;
import java.io.Serializable;
import java.util.*;


/**
 * Parameter file for parsing the Penn Treebank format of the Negra
 * Treebank (German).  STILL UNDER CONSTRUCTION!
 *
 * @author Roger Levy
 */

public class NegraPennTreebankParserParams extends AbstractTreebankParserParams {

  private static final boolean DEBUG = false;

  public static boolean markRC = false;
  public static boolean markZuVP = false;

  private NegraPennLanguagePack nplp = (NegraPennLanguagePack) treebankLanguagePack();

  public NegraPennTreebankParserParams() {
    super(new NegraPennLanguagePack());
    // override output encoding: make it UTF-8
    setOutputEncoding("UTF-8");
    nplp = (NegraPennLanguagePack) treebankLanguagePack();
  }


  /**
   * returns a NegraHeadFinder
   */
  public HeadFinder headFinder() {
    return new NegraHeadFinder();
    //return new LeftHeadFinder();
  }

  /**
   * returns an ordinary Lexicon (could be tuned for German!)
   */
  public Lexicon lex(Options.LexOptions op) {
    return new GermanLexicon(op);
  }

  private NegraPennTreeReaderFactory treeReaderFactory = new NegraPennTreeReaderFactory();

  public TreeReaderFactory treeReaderFactory() {
    return treeReaderFactory;
  }

  private static class NegraPennTreeReaderFactory implements TreeReaderFactory, Serializable {

    boolean treeNormalizerInsertNPinPP = false;

    boolean treeNormalizerLeaveGF = false;

    public TreeReader newTreeReader(Reader in) {
      final NegraPennTreeNormalizer tn = new NegraPennTreeNormalizer();
      if (treeNormalizerLeaveGF) {
        tn.setLeaveGF(true);
      }
      if (treeNormalizerInsertNPinPP) {
        tn.setInsertNPinPP(true);
      }

      return new PennTreeReader(in, new LabeledScoredTreeFactory(new StringLabelFactory()), tn, new NegraPennTokenizer(in));
    }

  } // end static class NegraPennTreeReaderFactory


  /* Returns a MemoryTreebank with a NegraPennTokenizer and a
   * NegraPennTreeNormalizer */
  public MemoryTreebank memoryTreebank() {
    return new MemoryTreebank(treeReaderFactory, inputEncoding);
  }

  /* Returns a DiskTreebank with a NegraPennTokenizer and a
   * NegraPennTreeNormalizer */
  public DiskTreebank diskTreebank() {
    return new DiskTreebank(treeReaderFactory, inputEncoding);
  }

  /**
   * returns a NegraPennCollinizer
   */
  public TreeTransformer collinizer() {
    return new NegraPennCollinizer();
  }

  /**
   * returns a NegraPennCollinizer
   */
  public TreeTransformer collinizerEvalb() {
    return new NegraPennCollinizer(false);
  }

  /**
   * Returns a <code>ChineseTreebankLanguagePack</code>
   */
  public TreebankLanguagePack treebankLanguagePack() {
    return new NegraPennLanguagePack();
  }


  /* parser tuning follows */

  public String[] sisterSplitters() {
    return new String[0];
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
    if (args[i].equalsIgnoreCase("-leaveGF")) {
      treeReaderFactory.treeNormalizerLeaveGF = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-markZuVP")) {
      markZuVP = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-markRC")) {
      markRC = true;
      i++;
    } else if (args[i].equalsIgnoreCase("-insertNPinPP")) {
      treeReaderFactory.treeNormalizerInsertNPinPP = true;
      i++;
    }
    return i;
  }

  public void display() {
    System.out.println("markZuVP=" + markZuVP);
    System.out.println("insertNPinPP=" + treeReaderFactory.treeNormalizerInsertNPinPP);
    System.out.println("leaveGF=" + treeReaderFactory.treeNormalizerLeaveGF);
  }


  private String basicCat(String str) {
    return nplp.basicCategory(str);
  }

  /**
   * transformTree does all language-specific tree
   * transformations. Any parameterizations should be inside the
   * specific TreebankLangParserarams class.
   */
  public Tree transformTree(Tree t, Tree root) {
    if (t == null || t.isLeaf()) {
      return t;
    }

    List<String> annotations = new ArrayList<String>();

    CategoryWordTag lab = (CategoryWordTag) t.label();
    String word = lab.word();
    String tag = lab.tag();
    String cat = lab.value();
    String baseCat = nplp.basicCategory(cat);

    // Tree parent = t.parent(root);

    // String mcat = "";
    // if (parent != null) {
    //   mcat = parent.label().value();
    // }

    //categories -- at present there is no tag annotation!!
    if (t.isPhrasal()) {

      List childBasicCats = childBasicCats(t);

      // mark vp's headed by "zu" verbs
      if (DEBUG) {
        if (markZuVP && baseCat.equals("VP")) {
          System.out.println("child basic cats: " + childBasicCats);
        }
      }
      if (markZuVP && baseCat.equals("VP") && (childBasicCats.contains("VZ") || childBasicCats.contains("VVIZU"))) {
        if (DEBUG) System.out.println("Marked zu VP" + t);
        annotations.add("-ZU");
      }

      // mark relative clause S's
      if (markRC && (t.label() instanceof NegraLabel) && baseCat.equals("S") && ((NegraLabel) t.label()).getEdge() != null && ((NegraLabel) t.label()).getEdge().equals("RC")) {
        if (DEBUG) {
          System.out.println("annotating this guy as RC:");
          t.pennPrint();
        }
        //throw new RuntimeException("damn, not a Negra Label");

        annotations.add("-RC");
      }

    }

    // put on all the annotations
    for (String annotation : annotations) {
      cat += annotation;
    }

    t.setLabel(new CategoryWordTag(cat, word, tag));
    return t;
  }

  private List<String> childBasicCats(Tree t) {
    Tree[] kids = t.children();
    List<String> l = new ArrayList<String>();
    for (int i = 0, n = kids.length; i < n; i++) {
      l.add(basicCat(kids[i].label().value()));
    }
    return l;
  }


  /**
   * Return a default sentence for the language (for testing)
   */
  public List defaultTestSentence() {
    return Arrays.asList(new String[]{"Solch", "einen", "Zuspruch", "hat", "Angela", "Merkel", "lange", "nicht", "mehr", "erlebt", "."});
  }


  private static final long serialVersionUID = 2;

  public static void main(String[] args) {
    TreebankLangParserParams tlpp = new NegraPennTreebankParserParams();
    Treebank tb = tlpp.memoryTreebank();
    tb.loadPath(args[0]);
    for (Tree aTb : tb) {
      aTb.pennPrint();
    }
  }

}
