package edu.stanford.nlp.trees.international.negra;

import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;

import java.io.Serializable;

/**
 * Language pack for Negra treebank, Penn format.
 *
 * @author Roger Levy
 */
public class NegraPennLanguagePack extends AbstractTreebankLanguagePack implements Serializable {

  /**
   * Gives a handle to the TreebankLanguagePack
   */
  public NegraPennLanguagePack() {
  }

  private static final String NEGRA_ENCODING = "ISO-8859-1";


  private static final String[] evalBignoredTags = {"$.", "$,"};

  private static final String[] negraSFPunctTags = {"$."};

  private static final String[] negraSFPunctWords = {".", "!", "?"};

  private static final String[] negraPunctTags = {"$.", "$,", "$*LRB*"};

  /**
   * The unicode escape is for a middle dot character
   */
  private static final String[] negraPunctWords = {"-", ",", ";", ":", "!", "?", "/", ".", "...", "\u00b7", "'", "\"", "(", ")", "*LRB*", "*RRB*"};

  /**
   * The first 3 are used by the Penn Treebank; # is used by the
   * BLLIP corpus, and ^ and ~ are used by Klein's lexparser.
   */
  private static char[] annotationIntroducingChars = {'-', '=', '|', '#', '^', '~'};

  /**
   * This is valid for "BobChrisTreeNormalizer" conventions only.
   */
  private static String[] pennStartSymbols = {"ROOT"};


  /**
   * Returns a String array of punctuation tags for this treebank/language.
   *
   * @return The punctuation tags
   */
  public String[] punctuationTags() {
    return negraPunctTags;
  }


  /**
   * Returns a String array of punctuation words for this treebank/language.
   *
   * @return The punctuation words
   */
  public String[] punctuationWords() {
    return negraPunctWords;
  }


  /**
   * Returns a String array of sentence final punctuation tags for this
   * treebank/language.
   *
   * @return The sentence final punctuation tags
   */
  public String[] sentenceFinalPunctuationTags() {
    return negraSFPunctTags;
  }

  /**
   * Returns a String array of sentence final punctuation words for this
   * treebank/language.
   *
   * @return The sentence final punctuation tags
   */
  public String[] sentenceFinalPunctuationWords() {
    return negraSFPunctWords;
  }


  /**
   * Returns a String array of punctuation tags that EVALB-style evaluation
   * should ignore for this treebank/language.
   * Traditionally, EVALB has ignored a subset of the total set of
   * punctuation tags in the English Penn Treebank (quotes and
   * period, comma, colon, etc., but not brackets)
   *
   * @return Whether this is a EVALB-ignored punctuation tag
   */
  public String[] evalBIgnoredPunctuationTags() {
    return evalBignoredTags;
  }


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


  /**
   * Returns a String array of treebank start symbols.
   *
   * @return The start symbols
   */
  public String[] startSymbols() {
    return pennStartSymbols;
  }

  /**
   * Return the input Charset encoding for the Treebank.
   * See documentation for the <code>Charset</code> class.
   *
   * @return Name of Charset
   */
  public String getEncoding() {
    return NEGRA_ENCODING;
  }

  /**
   * Returns the extension of treebank files for this treebank.
   * This is "mrg".
   */
  public String treebankFileExtension() {
    return "mrg";
  }

  public static void main(String[] args) {

  }

  private static final long serialVersionUID = 9081305982861675328L;

}
