package edu.stanford.nlp.trees.tregex;

import edu.stanford.nlp.io.NumberRangesFileFilter;
import edu.stanford.nlp.parser.lexparser.EnglishTreebankParserParams;
import edu.stanford.nlp.parser.lexparser.TreebankLangParserParams;
import edu.stanford.nlp.trees.DiskTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;
import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * A TregexMatcher can be used to match a {@link TregexPattern} against a {@link edu.stanford.nlp.trees.Tree}.
 * <p/>
 * <p> Usage should to be the same as {@link java.util.regex.Matcher}.
 * <p/>
 * <p> @author Galen Andrew
 */
public abstract class TregexMatcher {
  final Tree root;
  Tree tree;
  Map<Object, Tree> namesToNodes;
  VariableStrings variableStrings;


  // these things are used by "find"
  Iterator findIterator;
  Tree findCurrent;

  TregexMatcher(Tree root, Tree tree, Map<Object, Tree> namesToNodes, VariableStrings variableStrings) {
    this.root = root;
    this.tree = tree;
    this.namesToNodes = namesToNodes;
    this.variableStrings = variableStrings;
  }

  /**
   * Resets the matcher so that its search starts over.
   */
  public void reset() {
    findIterator = null;
    namesToNodes.clear();
  }

  /**
   * Resets the matcher to start searching on the given tree for matching subexpressions
   */
  void resetChildIter(Tree tree) {
    this.tree = tree;
    resetChildIter();
  }

  /**
   * Resets the matcher to restart search for matching subexpressions
   */
  void resetChildIter() {
  }

  /**
   * Does the pattern match the tree?  It's actually closer to java.util.regex's
   * "lookingAt" in that the root of the tree has to match the root of the pattern
   * but the whole tree does not have to be "accounted for".  Like with lookingAt
   * the beginning of the string has to match the pattern, but the whole string
   * doesn't have to be "accounted for".
   *
   * @return whether the tree matches the pattern
   */
  public abstract boolean matches();

  /** Rests the matcher and tests if it matches on the tree when rooted at <code>node</code>.
   * @param node
   * @return whether the matcher matches at node
   */
  public boolean matchesAt(Tree node) {
    resetChildIter(node);
    return matches();
  }

  /**
   * Get the last matching tree -- that is, the tree node that matches the root node of the pattern.  
   * Returns null if there has not been a match.
   *
   * @return last match
   */
  public abstract Tree getMatch();


  /**
   * Find the next match of the pattern on the tree
   *
   * @return whether there is a match somewhere in the tree
   */
  public boolean find() {
    if (findIterator == null) {
      findIterator = root.iterator();
    }
    if (findCurrent != null && matches()) {
      return true;
    }
    while (findIterator.hasNext()) {
      findCurrent = (Tree) findIterator.next();
      resetChildIter(findCurrent);
      if (matches()) {
        return true;
      }
    }
    return false;
  }

  /** 
   * Find the next match of the pattern on the tree such that the matching node (that is, the tree node matching the
   * root node of the pattern) differs from the previous matching node.
   * @return true iff another matching node is found.
   */
  public boolean findNextMatchingNode() {
    Tree lastMatchingNode = getMatch();
    while(find()) {
      if(getMatch() != lastMatchingNode)
        return true;
    }
    return false;
  }
  
  /**
   * Returns the node labeled with <code>name</code> in the pattern.
   *
   * @param name the name of the node, specified in the pattern.
   * @return node labeled by the name
   */
  public Tree getNode(Object name) {
    return namesToNodes.get(name);
  }

  /**
   * Look at a bunch of trees and tell where TreePattern and TregexPattern
   * differ at all.  Use the -p <pattern> option for hand-specifying the pattern.
   *
   * @param args TreebankPath, RangesFilter
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    Map<String,Integer> flagMap = new HashMap<String, Integer>();
    flagMap.put("-p", new Integer(1));
    Map<String,String[]> argsMap = StringUtils.argsToMap(args, flagMap);
    args = argsMap.get(null);
    String s = "VP < VBZ";
    if (argsMap.keySet().contains("-p")) {
      s = (argsMap.get("-p"))[0];
    }
    TregexPattern tregexPattern = TregexPattern.compile(s);

    TreebankLangParserParams tlpp = new EnglishTreebankParserParams();
    FileFilter testFilt = new NumberRangesFileFilter(args[1], true);
    DiskTreebank testTreebank = tlpp.diskTreebank();
    testTreebank.loadPath(new File(args[0]), testFilt);

    TreePattern treePattern = null;

    boolean print = argsMap.keySet().contains("-print");

    for (Iterator<Tree> iterator = testTreebank.iterator(); iterator.hasNext();) {
      Tree root = iterator.next();
      TregexMatcher tregexMatcher = tregexPattern.matcher(root);
      TreeMatcher treeMatcher = treePattern.matcher(root);
      Tree lastTreeMatch = null;
      // a workaround to test tregexMatcher given that treeMatcher is now returning each tree twice
      List<Tree> tregexMatchedTrees = new ArrayList<Tree>();
      List<Tree> treeMatchedTrees = new ArrayList<Tree>();
      while (treeMatcher.find()) {
        Tree treeMatchedTree = treeMatcher.getMatch();
        if (treeMatchedTree == lastTreeMatch) {
          continue;
        }
        treeMatchedTrees.add(treeMatchedTree);
        lastTreeMatch = treeMatchedTree;
      }
      while (tregexMatcher.find()) {
        Tree tregexMatchedTree = tregexMatcher.getMatch();
        tregexMatchedTrees.add(tregexMatchedTree);
      }
      if (!tregexMatchedTrees.equals(treeMatchedTrees)) {
        System.out.println("Disagreement");
        if (print) {
          System.out.println("TreeMatcher found " + treeMatchedTrees.size() + " matches:");
          for (Iterator<Tree> iter = treeMatchedTrees.iterator(); iter.hasNext();) {
            Tree tree = iter.next();
            tree.pennPrint();
          }
          System.out.println("TregexMatcher found " + tregexMatchedTrees.size() + " matches:");
          for (Iterator<Tree> iter = tregexMatchedTrees.iterator(); iter.hasNext();) {
            Tree tree = iter.next();
            tree.pennPrint();
          }
        }
      }
    }
  }
}
