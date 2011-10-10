package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.negra.NegraPennLanguagePack;

import java.util.ArrayList;
import java.util.List;


class NegraPennCollinizer implements TreeTransformer {

  private TreebankLanguagePack tlp = new NegraPennLanguagePack();
  private final boolean deletePunct;

  public NegraPennCollinizer() {
    this(true);
  }

  public NegraPennCollinizer(boolean deletePunct) {
    this.deletePunct = deletePunct;
  }

  protected TreeFactory tf = new LabeledScoredTreeFactory();

  public Tree transformTree(Tree tree) {
    Label l = tree.label();
    String s = l.value();
    if (tree.isLeaf()) {
      return tf.newLeaf(s);   // makes it a StringLabel
    }
    s = tlp.basicCategory(s);
    if (deletePunct) {
      // this is broken as it's not the right thing to do when there
      // is any tag ambiguity -- and there is for ' (POS/'').  Sentences
      // can then have more or less words.  It's also unnecessary for EVALB,
      // since it ignores punctuation anyway
      if (tree.isPreTerminal() && tlp.isEvalBIgnoredPunctuationTag(s)) {
        return null;
      }
    }
    // TEMPORARY: eliminate the TOPP constituent
    if (tree.children()[0].label().value().equals("TOPP")) {
      System.err.println("Found a TOPP");
      tree.setChildren(tree.children()[0].children());
    }

    // Negra has lots of non-unary roots; delete unary roots
    if (tlp.isStartSymbol(s) && tree.children().length == 1) {
      // NB: This deletes the boundary symbol, which is in the tree!
      return transformTree(tree.children()[0]);
    }
    List children = new ArrayList();
    for (int cNum = 0; cNum < tree.children().length; cNum++) {
      Tree child = tree.children()[cNum];
      Tree newChild = transformTree(child);
      if (newChild != null) {
        children.add(newChild);
      }
    }
    if (children.size() == 0) {
      return null;
    }
    return tf.newTreeNode(new StringLabel(s), children);
  }
}
