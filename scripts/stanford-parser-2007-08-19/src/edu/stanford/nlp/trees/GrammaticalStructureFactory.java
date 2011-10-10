package edu.stanford.nlp.trees;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.stanford.nlp.util.Filter;

/**
 * A general factory for {@link GrammaticalStructure} objects.
 *
 * Uses reflection to create objects of any subclass of 
 * {@link GrammaticalStructure}.
 *
 * @author Galen Andrew
 */
public class GrammaticalStructureFactory {

  private final Constructor con;
  private final Filter<String> puncFilter;

  /**
   * Make a new GrammaticalStructureFactory that dispenses {@link GrammaticalStructure}
   * objects of the given specific type
   * @param name the fully qualified name of a subclass of GrammaticalStructure
   */
  public GrammaticalStructureFactory(String name) {
    this(name, null);
  }

  /**
   * Make a new GrammaticalStructureFactory that dispenses
   * {@link GrammaticalStructure} objects of the given specific type
   * @param name the fully qualified name of a subclass of GrammaticalStructure
   */
  public GrammaticalStructureFactory(String name, Filter<String> puncFilter) {
    Class c, t, f;
    try {
      c = Class.forName(name);
      if (!Class.forName("edu.stanford.nlp.trees.GrammaticalStructure").isAssignableFrom(c)) {
        throw new ClassNotFoundException();
      }
      t = Class.forName("edu.stanford.nlp.trees.Tree");
      f = Class.forName("edu.stanford.nlp.util.Filter");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class " + name + " does not exist or does not extend edu.stanford.nlp.trees.GrammaticalStructure.");
    }

    try {
      if (puncFilter == null) {
        con = c.getConstructor(t);
      } else {
        con = c.getConstructor(t, f);
      }
      this.puncFilter = puncFilter;
    } catch (NoSuchMethodException e) {
      // this is impossible, given above exceptions
      throw new RuntimeException();
    }
  }

  /**
   * Vend a new {@link GrammaticalStructure} based on the given {@link Tree}.
   *
   * @param t the tree to analyze
   * @return a GrammaticalStructure based on the tree
   */
  public GrammaticalStructure newGrammaticalStructure(Tree t) {
    try {
      if (puncFilter == null) {
        return (GrammaticalStructure) con.newInstance(t);
      } else {
        return (GrammaticalStructure) con.newInstance(t, puncFilter);
      }
    } catch (InstantiationException e) {
      throw new RuntimeException("Cannot instantiate " + con.getDeclaringClass().getName());
    } catch (IllegalAccessException e) {
      throw new RuntimeException(con.getDeclaringClass().getName() + "(Tree t) does not have public access");
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

}
