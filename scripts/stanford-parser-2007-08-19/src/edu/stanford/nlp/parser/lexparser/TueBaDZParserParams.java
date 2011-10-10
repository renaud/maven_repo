package edu.stanford.nlp.parser.lexparser;

import java.io.Reader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.trees.DiskTreebank;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreeNormalizer;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.TreeReaderFactory;
import edu.stanford.nlp.trees.TreeTransformer;
import edu.stanford.nlp.trees.international.tuebadz.TueBaDZHeadFinder;
import edu.stanford.nlp.trees.international.tuebadz.TueBaDZLanguagePack;
import edu.stanford.nlp.trees.international.tuebadz.TueBaDZPennTreeNormalizer;

/** TreebankLangParserParams for the German Tuebingen corpus.
 *
 *  The TueBaDZTreeReaderFactory has been changed in order to use a
 *  TueBaDZPennTreeNormalizer.
 *
 *  @author Roger Levy (rog@stanford.edu)
 *  @author Wolfgang Maier (wmaier@sfs.uni-tuebingen.de)
 */
public class TueBaDZParserParams extends AbstractTreebankParserParams {

  public TueBaDZParserParams() {
    super(new TueBaDZLanguagePack());
  }

  /** Returns the first sentence of TueBaDZ. */
  public List defaultTestSentence() {
    return Arrays.asList(new String[] { "Veruntreute" , "die" , "AWO" , "Spendengeld" , "?" });
  }

  public String[] sisterSplitters() {
    return new String[0];
  }

  public TreeTransformer collinizer() {
    return new TreeCollinizer(tlp);
  }

  public TreeTransformer collinizerEvalb() {
    return new TreeCollinizer(tlp);
  }

  public MemoryTreebank memoryTreebank() {
    return new MemoryTreebank(treeReaderFactory());
  }

  public DiskTreebank diskTreebank() {
    return new DiskTreebank(treeReaderFactory());
  }

  private static class TueBaDZTreeReaderFactory implements TreeReaderFactory, Serializable {

    public TreeReader newTreeReader(Reader in) {
      final TueBaDZPennTreeNormalizer tn = new TueBaDZPennTreeNormalizer();
      return new PennTreeReader(in, new LabeledScoredTreeFactory(new StringLabelFactory()), tn);
    }

    private static final long serialVersionUID = 1614799885744961795L;

  } // end class TueBaDZTreeReaderFactory

  public TreeReaderFactory treeReaderFactory() {
    return new TueBaDZTreeReaderFactory();
  }

  /** No options yet.*/
  public int setOptionFlag(String[] args, int i) {
    return i;
  }

  public void display() {
    System.out.println("TueBaDZParserParams (no options).");
  }

  /** returns a {@link TueBaDZHeadFinder}. */
  public HeadFinder headFinder() {
    return new TueBaDZHeadFinder();
  }


  /** A no-op right now. */
  public Tree transformTree(Tree t, Tree root) {
    return t;
  }

  private static final long serialVersionUID = 7303189408025355170L;

}
