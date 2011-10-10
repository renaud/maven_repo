package edu.stanford.nlp.trees.tregex.tsurgeon;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;


/**
 * An abstract class for patterns to manipulate {@link Tree}s when
 * successfully matched on with a {@link TregexMatcher}.
 *
 * @author Roger Levy
 */
public abstract class TsurgeonPattern {

  protected static final TsurgeonPattern[] EMPTY_TSURGEON_ARRAY = new TsurgeonPattern[0];

  TsurgeonPatternRoot root;
  String label;
  TsurgeonPattern[] children;

  TsurgeonPattern(String label, TsurgeonPattern[] children) {
    this.label = label;
    this.children = children;
  }

  protected void setRoot(TsurgeonPatternRoot root) {
    this.root = root;
    for (TsurgeonPattern child : children) {
      child.setRoot(root);
    }
  }

  public String toString() {
    StringBuilder resultSB = new StringBuilder();
    resultSB.append(label);
    if (children.length > 0) {
      resultSB.append("(");
      for (int i = 0; i < children.length; i++) {
        resultSB.append(children[i]);
        if (i < children.length - 1) {
          resultSB.append(", ");
        }
      }
      resultSB.append(")");
    }
    return resultSB.toString();
  }

  /**
   * Evaluates the pattern against a {@link Tree} and a {@link TregexMatcher}
   * that has been successfully matched against the tree.
   *
   * @param t the {@link Tree} that has been matched upon; typically this tree will be destructively modified.
   * @param m the successfully matched {@link TregexMatcher}
   * @return some node in the tree; depends on implementation and use of the specific subclass.
   */
  public abstract Tree evaluate(Tree t, TregexMatcher m);

}
