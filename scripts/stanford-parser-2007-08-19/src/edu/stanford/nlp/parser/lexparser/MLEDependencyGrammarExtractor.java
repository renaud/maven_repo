package edu.stanford.nlp.parser.lexparser;

import java.util.*;

import edu.stanford.nlp.util.Numberer;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.trees.Tree;


/** Gathers statistics on tree dependencies and then passes them to an
 *  MLEDependencyGrammar for dependency grammar construction
 *
 *  @author Dan Klein
 */
public class MLEDependencyGrammarExtractor extends AbstractTreeExtractor {

  protected Numberer wordNumberer = Numberer.getGlobalNumberer("words");
  protected Numberer tagNumberer = Numberer.getGlobalNumberer("tags");

  /** This is where all dependencies are stored (using full tag space). */
  protected Counter<IntDependency> dependencyCounter = new Counter<IntDependency>();
  //private Set dependencies = new HashSet();

  protected TreebankLangParserParams tlpParams;

  /** Whether left and right is distinguished. */
  protected boolean directional;

  /** Whether dependent distance from head is distinguished. */
  protected boolean useDistance;

  /** Whether dependent distance is distinguished more coarsely. */
  protected boolean useCoarseDistance;

  public MLEDependencyGrammarExtractor(Options op) {
    tlpParams = op.tlpParams;
    directional = op.directional;
    useDistance = op.distance;
    useCoarseDistance = op.coarseDistance;
  }

  protected void tallyRoot(Tree lt) {
    // this list is in full (not reduced) tag space
    List<IntDependency> deps = MLEDependencyGrammar.treeToDependencyList(lt);
    for (IntDependency dependency : deps) {
      dependencyCounter.incrementCount(dependency);
    }
  }

  public Object formResult() {
    wordNumberer.number(Lexicon.UNKNOWN_WORD);
    MLEDependencyGrammar dg = new MLEDependencyGrammar(tlpParams, directional, useDistance, useCoarseDistance);
    for (IntDependency dependency : dependencyCounter.keySet()) {
      dg.addRule(dependency, dependencyCounter.getCount(dependency));
    }
    return dg;
  }

} // end class MLEDependencyGrammarExtractor
