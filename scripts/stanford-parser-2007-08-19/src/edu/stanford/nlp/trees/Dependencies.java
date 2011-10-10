package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.AbstractMapLabel;
import edu.stanford.nlp.util.Filter;

import java.util.Comparator;

/** Utilities for Dependency objects.
 * 
 *  @author Christopher Manning
 */
public class Dependencies {

  private Dependencies() {}


  public static class DependentPuncTagRejectFilter implements Filter<Dependency> {

    private Filter tagRejectFilter;

    public DependentPuncTagRejectFilter(Filter trf) {
      tagRejectFilter = trf;
    }
      
    public boolean accept(Dependency d) {
      if (d == null) {
        return false;
      }
      if ( ! (d.dependent() instanceof HasTag)) {
        return false;
      }
      String tag = ((HasTag) d.dependent()).tag();
      return tagRejectFilter.accept(tag);
    }

  } // end class DependentPuncTagRejectFilter


  public static class DependentPuncWordRejectFilter implements Filter<Dependency> {

    private final Filter wordRejectFilter;

    /** @param wrf A filter that rejects punctuation words.
     */ 
    public DependentPuncWordRejectFilter(Filter wrf) {
      // System.err.println("wrf is " + wrf);
      wordRejectFilter = wrf;
    }
      
    public boolean accept(Dependency d) {
      if (d == null) {
        return false;
      }
      if ( ! (d.dependent() instanceof HasWord)) {
        return false;
      }
      String word = ((HasWord) d.dependent()).word();
      // System.err.println("Dep: kid is " + ((MapLabel) d.dependent()).toString("value{map}"));
      return wordRejectFilter.accept(word);
    }

  } // end class DependentPuncWordRejectFilter


  // extra class guarantees correct lazy loading (Bloch p.194)
  private static class ComparatorHolder {

    private static class DependencyIdxComparator implements Comparator<Dependency> {

      public int compare(Dependency dep1, Dependency dep2) {
        AbstractMapLabel dep1lab = (AbstractMapLabel) dep1.dependent();
        AbstractMapLabel dep2lab = (AbstractMapLabel) dep2.dependent();
        int dep1idx = dep1lab.index();
        int dep2idx = dep2lab.index();
        return dep1idx - dep2idx;
      }

    }
    
    private static final Comparator<Dependency> dc = new DependencyIdxComparator();
  
  }

  public static Comparator<Dependency> dependencyIndexComparator() {
    return ComparatorHolder.dc;
  }

}
