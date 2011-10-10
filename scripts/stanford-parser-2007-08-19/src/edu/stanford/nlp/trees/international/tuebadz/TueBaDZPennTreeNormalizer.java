package edu.stanford.nlp.trees.international.tuebadz;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreeNormalizer;
import edu.stanford.nlp.trees.TreebankLanguagePack;


/**
 * Tree normalizer for the TueBaDZ treebank.
 * 
 * (An adaptation of Roger Levy's NegraPennTreeNormalizer.)
 * 
 * @author Wolfgang Maier (wmaier@sfs.uni-tuebingen.de)
 */
public class TueBaDZPennTreeNormalizer extends TreeNormalizer {

  private static String root = "ROOT";
  private static String nonUnaryRoot = "ROOT"; // non-unary root

  public String rootSymbol() {
    return root;
  }

  public String nonUnaryRootSymbol() {
    return nonUnaryRoot;
  }

  protected final TreebankLanguagePack tlp;

  public TueBaDZPennTreeNormalizer() {
    this(new TueBaDZLanguagePack());
  }

  public TueBaDZPennTreeNormalizer(TreebankLanguagePack tlp) {
    this.tlp = tlp;
  }

  /**
   * Normalizes a leaf contents.
   * This implementation interns the leaf.
   */
  public String normalizeTerminal(String leaf) {
    // We could unquote * and / with backslash \ in front of them
    return leaf.intern();
  }


  /**
   * Normalizes a nonterminal contents.
   * This implementation strips functional tags, etc. and interns the
   * nonterminal.
   */
  public String normalizeNonterminal(String category) {
    return cleanUpLabel(category).intern();
  }

  /**
   * Remove things like hyphened functional tags and equals from the
   * end of a node label.
   */
  protected String cleanUpLabel(String label) {
    if (label == null) {
      label = root;
      // String constants are always interned
    }
    return label;
  }

  /**
   * Normalize a whole tree. 
   * TueBa-D/Z adaptation. Fixes trees with non-unary roots, does nothing else.
   */
  public Tree normalizeWholeTree(Tree tree, TreeFactory tf) {
    //tree.pennPrint();

    // add an extra root to non-unary roots
    if (tree.label().value().equals(root) && tree.children().length > 1) {
	//      System.out.println("Fixing tree with non-unary root.  Before...");
	//tree.pennPrint();
      Tree underRoot = tree.treeFactory().newTreeNode(nonUnaryRoot, tree.getChildrenAsList());
      tree.setChildren(new Tree[1]);
      tree.setChild(0, underRoot); 
      //System.out.println("After...");
      //tree.pennPrint();
    }

    // we just want the non-unary root fixed.
    return tree;
  }

}
