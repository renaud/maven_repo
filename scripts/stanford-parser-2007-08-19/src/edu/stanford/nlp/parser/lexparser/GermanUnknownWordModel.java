package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.stats.Counter;

import java.io.Serializable;
import java.util.*;

/**
 * An unknown word model for German.
 *
 * @author Roger Levy
 * @author Greg Donaker (corrections and modeling improvements)
 * @author Christopher Manning (generalized and improved what Greg did)
 */
public class GermanUnknownWordModel implements Serializable {

  private static final String encoding = "UTF-8";

  private static final boolean useFirst = false; //= true;
  private static boolean useEnd = true;
  private static final boolean useGT = false;
  private static boolean useFirstCap = true; // Only care if cap

  private static int endLength = 2; // only used if useEnd==true 

  private static final String unknown = "UNK";

  private static final String numberMatch = "[0-9]+\\.?[0-9]*";

  private Map<String,Counter<String>> tagHash = new HashMap<String,Counter<String>>();
  private Set seenEnd = new HashSet();

  private Map unknownGT = new HashMap();


  public GermanUnknownWordModel(Options.LexOptions op) {
    endLength = op.unknownSuffixSize;
    useEnd = op.unknownSuffixSize > 0 && op.useUnknownWordSignatures > 0;
    useFirstCap = op.useUnknownWordSignatures > 0;
  }

  public double score(IntTaggedWord itw) {
    // treat an IntTaggedWord by changing it into a TaggedWord
    return score(itw.toTaggedWord());
  }


  /** Calculate the log-prob score of a particular TaggedWord in the
   *  unknown word model.
   *  @param tw the tag->word production in TaggedWord form
   *  @return The log-prob score of a particular TaggedWord.
   */
  public double score(TaggedWord tw) {
    double logProb;

    String word = tw.word();
    String tag = tw.tag();

    // testing
    //EncodingPrintWriter.out.println("Scoring unknown word " + word + " with tag " + tag,encoding);
    // end testing    

    
    if (word.matches(numberMatch)) {
      //EncodingPrintWriter.out.println("Number match for " + word,encoding);
      if (tag.equals("CARD")) {
        logProb = 0.0;
      } else {
        logProb = Double.NEGATIVE_INFINITY;
      }
    } else {
      end:
      if (useEnd || useFirst || useFirstCap) {
					String end = getSignature(word);
        if (!seenEnd.contains(end)) {
          if (useGT) {
            logProb = scoreGT(tag);
            break end;
          } else {
            end = unknown;
          }
        }
        //System.out.println("using end-character model for for unknown word "+  word + " for tag " + tag);

        /* get the Counter of terminal rewrites for the relevant tag */
        Counter<String> wordProbs = tagHash.get(tag);

        /* if the proposed tag has never been seen before, issue a
         * warning and return probability 0 */
        if (wordProbs == null) {
          //System.err.println("Warning: proposed tag is unseen in training data!");
          logProb = Double.NEGATIVE_INFINITY;
        } else if (wordProbs.keySet().contains(end)) {
          logProb = wordProbs.getCount(end);
        } else {
          logProb = wordProbs.getCount(unknown);
        }
      } else if (useGT) {
        logProb = scoreGT(tag);
      } else {
        System.err.println("Warning: no unknown word model in place!\nGiving the combination " + word + " " + tag + " zero probability.");
        logProb = Double.NEGATIVE_INFINITY; // should never get this!
      }
    }

    //EncodingPrintWriter.out.println("Unknown word estimate for " + word + " as " + tag + ": " + logProb,encoding); //debugging
    return logProb;
  }

  private double scoreGT(String tag) {
    //System.out.println("using GT for unknown word and tag " + tag);
    double logProb;
    if (unknownGT.containsKey(tag)) {
      logProb = ((Double) unknownGT.get(tag)).doubleValue();
    } else {
      logProb = Double.NEGATIVE_INFINITY;
    }
    return logProb;
  }

  private String getSignature(String word) {
    String subStr = "";
    int n = word.length() - 1;
    if (useFirstCap) {
      String first = word.substring(0, 1);
      if (first.equals(first.toUpperCase())) {
        subStr += "C";
      } else {
        subStr += "c";
      }
    }
    if (useFirst) {
      subStr += word.substring(0, 1);
    } 
    if (useEnd) {
      subStr += word.substring(n - endLength > 0 ? n - endLength : 0, n);
    }
    return subStr;
  }


  /**
   * trains the end-character based unknown word model.
   *
   * @param trees the collection of trees to be trained over
   */
  public void train(Collection<Tree> trees) {
    if (useFirst) {
      System.out.println("Including first letter for unknown words.");
    }
    if (useEnd) {
      System.out.println("treating unknown word as the average of their equivalents by identity of last three letters.");
    }
    if (useGT) {
      System.out.println("using Good-Turing smoothing for unknown words.");
    }

    trainUnknownGT(trees);

    HashMap<String,Counter<String>> c = new HashMap<String,Counter<String>>(); // counts

    Counter<String> tc = new Counter<String>();

    for (Tree t : trees) {
      List words = t.taggedYield();
      for (Iterator j = words.iterator(); j.hasNext();) {
        TaggedWord tw = (TaggedWord) (j.next());
        String word = tw.word();
        String subString = getSignature(word);

        String tag = tw.tag();
        if ( ! c.containsKey(tag)) {
          c.put(tag, new Counter<String>());
        }
        c.get(tag).incrementCount(subString);

        tc.incrementCount(tag);

        seenEnd.add(subString);

      }
    }

    for (Iterator i = c.keySet().iterator(); i.hasNext();) {
      String tag = (String) i.next();
      Counter wc = (Counter) c.get(tag); // counts for words given a tag

      /* outer iteration is over tags */
      if (!tagHash.containsKey(tag)) {
        tagHash.put(tag, new Counter());
      }

      /* the UNKNOWN sequence is assumed to be seen once in each tag */
      // this is really sort of broken!
      tc.incrementCount(tag);
      wc.setCount(unknown, 1.0);

      /* inner iteration is over words */
      for (Iterator j = wc.keySet().iterator(); j.hasNext();) {
        String end = (String) j.next();
        double prob = Math.log(((double) wc.getCount(end)) / ((double) tc.getCount(tag)));
        ((Counter) tagHash.get(tag)).setCount(end, prob);
        //if (Test.verbose)
        //EncodingPrintWriter.out.println(tag + " rewrites as " + end + " endchar with probability " + prob,encoding);
      }
    }
  }

  /** Trains Good-Turing estimation of unknown words. */
  private void trainUnknownGT(Collection trees) {

    Counter twCount = new Counter();
    Counter wtCount = new Counter();
    Counter tagCount = new Counter();
    Counter r1 = new Counter(); // for each tag, # of words seen once
    Counter r0 = new Counter(); // for each tag, # of words not seen
    Set seenWords = new HashSet();

    int tokens = 0;

    /* get TaggedWord and total tag counts, and get set of all
     * words attested in training */
    for (Iterator i = trees.iterator(); i.hasNext();) {
      Tree t = (Tree) i.next();
      List words = t.taggedYield();
      for (Iterator j = words.iterator(); j.hasNext();) {
        tokens++;
        TaggedWord tw = (TaggedWord) (j.next());
        WordTag wt = toWordTag(tw);
        String word = wt.word();
        String tag = wt.tag();
        //if (Test.verbose) EncodingPrintWriter.out.println("recording instance of " + wt.toString(),encoding); // testing

        wtCount.incrementCount(wt);// TaggedWord has crummy equality conditions
        twCount.incrementCount(tw);//testing
        //if (Test.verbose) EncodingPrintWriter.out.println("This is the " + wtCount.getCount(wt) + "th occurrence of" + wt.toString(),encoding); // testing
        tagCount.incrementCount(tag);
        boolean alreadySeen = seenWords.add(word);

        // if (Test.verbose) if(! alreadySeen) EncodingPrintWriter.out.println("already seen " + wt.toString(),encoding); // testing

      }
    }
    
    // testing: get some stats here
    System.out.println("Total tokens: " + tokens);
    System.out.println("Total WordTag types: " + wtCount.keySet().size());
    System.out.println("Total TaggedWord types: " + twCount.keySet().size());
    System.out.println("Total tag types: " + tagCount.keySet().size());
    System.out.println("Total word types: " + seenWords.size());


    /* find # of once-seen words for each tag */
    for (Iterator i = wtCount.keySet().iterator(); i.hasNext();) {
      WordTag wt = (WordTag) i.next();
      if (wtCount.getCount(wt) == 1) {
        r1.incrementCount(wt.tag());
      }
    }

    /* find # of unseen words for each tag */
    for (Iterator i = tagCount.keySet().iterator(); i.hasNext();) {
      String tag = (String) i.next();
      for (Iterator j = seenWords.iterator(); j.hasNext();) {
        String word = (String) j.next();
        WordTag wt = new WordTag(word, tag);
        //EncodingPrintWriter.out.println("seeking " + wt.toString(),encoding); // testing
        if (!(wtCount.keySet().contains(wt))) {
          r0.incrementCount(tag);
          //EncodingPrintWriter.out.println("unseen " + wt.toString(),encoding); // testing
        } else {
          //EncodingPrintWriter.out.println("count for " + wt.toString() + " is " + wtCount.getCount(wt),encoding);
        }
      }
    }

    /* set unseen word probability for each tag */
    for (Iterator i = tagCount.keySet().iterator(); i.hasNext();) {
      String tag = (String) i.next();
      //System.out.println("Tag " + tag + ".  Word types for which seen once: " + r1.getCount(tag) + ".  Word types for which unseen: " + r0.getCount(tag) + ".  Total count token for tag: " + tagCount.getCount(tag)); // testing
      
      double logprob = Math.log(r1.getCount(tag) / (tagCount.getCount(tag) * r0.getCount(tag)));

      unknownGT.put(tag, new Double(logprob));
    }

    /* testing only: print the GT-smoothed model */
    //System.out.println("The GT-smoothing model:");
    //System.out.println(unknownGT.toString());
    //EncodingPrintWriter.out.println(wtCount.toString(),encoding);


  }

  private static WordTag toWordTag(TaggedWord tw) {
    return new WordTag(tw.word(), tw.tag());
  }

  private static final long serialVersionUID = 221L;

}
	
