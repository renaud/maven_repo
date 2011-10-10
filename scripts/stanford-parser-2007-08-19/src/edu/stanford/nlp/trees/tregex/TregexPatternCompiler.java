package edu.stanford.nlp.trees.tregex;

import edu.stanford.nlp.process.Function;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;

/**
 * A class for compiling TregexPatterns with specific HeadFinders and or
 * basicCategoryFunctions.
 *
 * @author Galen Andrew
 */
public class TregexPatternCompiler {

  private Function<String,String> basicCatFunction = new PennTreebankLanguagePack().getBasicCategoryFunction();
  private HeadFinder headFinder = new CollinsHeadFinder();

  public static TregexPatternCompiler defaultCompiler = new TregexPatternCompiler();

  public TregexPatternCompiler() {
  }

  /**
   * A compiler that uses this basicCatFunction and the default headfinder.
   *
   * @param basicCatFunction the function mapping Strings to Strings
   */
  public TregexPatternCompiler(Function<String,String> basicCatFunction) {
    this.basicCatFunction = basicCatFunction;
  }

  /**
   * A compiler that uses this HeadFinder and the default basicCategoryFunction
   *
   * @param headFinder the HeadFinder
   */
  public TregexPatternCompiler(HeadFinder headFinder) {
    this.headFinder = headFinder;
  }

  /**
   * A compiler that uses this HeadFinder and this basicCategoryFunction
   *
   * @param headFinder       the HeadFinder
   * @param basicCatFunction hthe function mapping Strings to Strings
   */
  public TregexPatternCompiler(HeadFinder headFinder, Function<String,String> basicCatFunction) {
    this.headFinder = headFinder;
    this.basicCatFunction = basicCatFunction;
  }

  /**
   * Create a TregexPattern from this tregex string using the headFinder and
   * basicCat function this TregexPatternCompiler was created with
   *
   * @param tregex the pattern to parse
   * @return a new TregexPattern object based on this string
   * @throws ParseException
   */
  public TregexPattern compile(String tregex) throws ParseException {
    TregexPattern.setBasicCatFunction(basicCatFunction);
    Relation.setHeadFinder(headFinder);
    TregexPattern pattern = TregexParser.parse(tregex);
    pattern.setPatternString(tregex);
    return pattern;
  }
}
