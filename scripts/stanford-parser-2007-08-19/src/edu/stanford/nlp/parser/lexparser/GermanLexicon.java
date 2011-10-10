package edu.stanford.nlp.parser.lexparser;

import java.util.Collection;
import edu.stanford.nlp.trees.Tree;

/**
 * A lexicon class for German.  Extends the current Lexicon class,
 * overriding its score and train methods to include a
 * GermanUnknownWordModel.
 *
 * @author Roger Levy
 */
class GermanLexicon extends BaseLexicon {

  private GermanUnknownWordModel unknown;

  public GermanLexicon(Options.LexOptions op) {
    super(op);
    unknown = new GermanUnknownWordModel(op);
  }

  /* trains on a collection of trees */
  public void train(Collection<Tree> trees) {
    super.train(trees);
    unknown.train(trees);
  }

  public float score(IntTaggedWord iTW, int loc) {

    double c_W = seenCounter.getCount(iTW);

    boolean seen = (c_W > 0.0);

    if (seen) {
      return super.score(iTW, loc);
    } else {
      return (float) unknown.score(iTW);
    }
  }

  private static final long serialVersionUID = 221L;

}
