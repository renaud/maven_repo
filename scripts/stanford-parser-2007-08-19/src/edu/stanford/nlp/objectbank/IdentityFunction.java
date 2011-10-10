package edu.stanford.nlp.objectbank;

import edu.stanford.nlp.process.Function;


/**
 * @author Jenny Finkel
 */

public class IdentityFunction implements Function {

  /**
   * @param o The Object to be returned
   * @return o
   */
  public Object apply(Object o) {
    return o;
  }

}
