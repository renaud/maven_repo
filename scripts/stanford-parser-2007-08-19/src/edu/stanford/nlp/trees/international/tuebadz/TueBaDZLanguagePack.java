package edu.stanford.nlp.trees.international.tuebadz;

import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;

/** Language pack for the Tuebingen Treebank of Written German (TueBa-D/Z).
 *  http://www.sfs.nphil.uni-tuebingen.de/en_tuebadz.shtml
 *  This treebank is in utf-8.
 * 
 *  @author Roger Levy (rog@stanford.edu)
 */
public class TueBaDZLanguagePack extends AbstractTreebankLanguagePack {

  private static String[] tuebadzPunctTags = {"$.","$,","$-LRB"};

  private static String[] tuebadzSFPunctTags = {"$."};


  private static String[] tuebadzPunctWords = { "`", "-", ",", ";", ":", "!", "?", "/", ".", "...","'", "\"", "[", "]", "*"};

  private static String[] tuebadzSFPunctWords = {".", "!", "?"};

  /**
   * The first one isused by the TueBaDZ Treebank, and the rest are used by Klein's lexparser.
   */
  private static char[] annotationIntroducingChars = {':', '^', '~', '-', '#', '='};

  /**
   * Return an array of characters at which a String should be
   * truncated to give the basic syntactic category of a label.
   * The idea here is that Penn treebank style labels follow a syntactic
   * category with various functional and crossreferencing information
   * introduced by special characters (such as "NP-SBJ=1").  This would
   * be truncated to "NP" by the array containing '-' and "=".
   *
   * @return An array of characters that set off label name suffixes
   */
  public char[] labelAnnotationIntroducingCharacters() {
    return annotationIntroducingChars;
  }

  public String[] punctuationTags() {
    return tuebadzPunctTags;
  }

  public String[] punctuationWords() {
    return tuebadzPunctWords;
  }

  public String[] sentenceFinalPunctuationTags() {
    return tuebadzSFPunctTags;
  }

  public String[] startSymbols() {
    return new String[] {"TOP"};
  }

  public String[] sentenceFinalPunctuationWords() {
    return tuebadzSFPunctWords;
  }

  public String treebankFileExtension() {
    return ".penn";
  }

}
