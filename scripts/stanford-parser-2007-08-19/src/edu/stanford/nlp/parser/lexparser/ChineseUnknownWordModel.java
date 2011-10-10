package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.io.EncodingPrintWriter;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.stats.Counter;

import java.io.Serializable;
import java.util.*;

/**
 * Stores, trains, and scores with an unknown word model.  A couple
 * of filters deterministically force rewrites for certain proper
 * nouns, dates, and cardinal and ordinal numbers; when none of these
 * filters are met, either the distribution of terminals with the same
 * first character is used, or Good-Turing smoothing is used. Although
 * this is developed for Chinese, the training and storage methods
 * could be used cross-linguistically.
 *
 * @author Roger Levy
 */
public class ChineseUnknownWordModel implements Serializable {

  private static final String encoding = "GB18030"; // used only for debugging

  private static final boolean VERBOSE = false;

  private boolean useFirst = true;
  private boolean useGT = false;
  boolean useUnicodeType = false;

  private static final String unknown = "UNK";


  /* These strings are stored in ascii-stype Unicode encoding.  To
   * edit them, either use the Unicode codes or use native2ascii or a
   * similar program to convert the file into a Chinese encoding, then
   * convert back. */
  private static final String dateMatch = ".*[\u5e74\u6708\u65e5\u53f7]";
  private static final String numberMatch = ".*[\uff10\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19\uff11\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u4ebf].*";
  private static final String ordinalMatch = "\u7b2c.*";
  private static final String properNameMatch = ".*\u00b7.*";

  private Map<String,Counter<String>> tagHash = new HashMap<String,Counter<String>>();
  private Set seenFirst = new HashSet();
  private Map unknownGT = new HashMap();


  public ChineseUnknownWordModel() {
  }

  void useGoodTuring() {
    useGT = true;
    useFirst = false;
  }

  public double score(IntTaggedWord itw) {
    // treat an IntTaggedWord by changing it into a TaggedWord
    return score(itw.toTaggedWord());
  }


  /* Returns the log-prob score of a particular TaggedWord in the
   * unknown word model.  Uses some primitive character type analysis to
   * deterministically assign some words to tags, with improperly 
   * normalized probabilities.
   * 
   * @param tw the tag to word production in TaggedWord form
   */
  public double score(TaggedWord tw) {
    double logProb;

    String word = tw.word();
    String tag = tw.tag();

    // if (VERBOSE) EncodingPrintWriter.out.println("Scoring unknown word |" + word + "| with tag " + tag, encoding);

    if (word.matches(dateMatch)) {
      //EncodingPrintWriter.out.println("Date match for " + word,encoding);
      if (tag.equals("NT")) {
        logProb = 0.0;
      } else {
        logProb = Double.NEGATIVE_INFINITY;
      }
    } else if (word.matches(numberMatch)) {
      //EncodingPrintWriter.out.println("Number match for " + word,encoding);
      if (tag.equals("CD") && (!word.matches(ordinalMatch))) {
        logProb = 0.0;
      } else if (tag.equals("OD") && word.matches(ordinalMatch)) {
        logProb = 0.0;
      } else {
        logProb = Double.NEGATIVE_INFINITY;
      }
    } else if (word.matches(properNameMatch)) {
      //EncodingPrintWriter.out.println("Proper name match for " + word,encoding);
      if (tag.equals("NR")) {
        logProb = 0.0;
      } else {
        logProb = Double.NEGATIVE_INFINITY;
      }
    } else if (false) {
      // this didn't seem to work -- too categorical
      int type = Character.getType(word.charAt(0));
      // the below may not normalize probs over options, but is probably okay
      if (type == Character.START_PUNCTUATION) {
        if (tag.equals("PU-LPAREN") || tag.equals("PU-PAREN") ||
            tag.equals("PU-LQUOTE") || tag.equals("PU-QUOTE") ||
            tag.equals("PU")) {
          // if (VERBOSE) System.err.println("ChineseUWM: unknown L Punc");
          logProb = 0.0;
        } else {
          logProb = Double.NEGATIVE_INFINITY;
        }
      } else if (type == Character.END_PUNCTUATION) {
        if (tag.equals("PU-RPAREN") || tag.equals("PU-PAREN") ||
            tag.equals("PU-RQUOTE") || tag.equals("PU-QUOTE") ||
            tag.equals("PU")) {
          // if (VERBOSE) System.err.println("ChineseUWM: unknown R Punc");
          logProb = 0.0;
        } else {
          logProb = Double.NEGATIVE_INFINITY;
        }
      } else {
        if (tag.equals("PU-OTHER") || tag.equals("PU-ENDSENT") ||
            tag.equals("PU")) {
          // if (VERBOSE) System.err.println("ChineseUWM: unknown O Punc");
          logProb = 0.0;
        } else {
          logProb = Double.NEGATIVE_INFINITY;
        }
      }
    } else {
      first:
      if (useFirst) {
        String first = word.substring(0, 1);
        if (useUnicodeType) {
          char ch = word.charAt(0);
          int type = Character.getType(ch);
          if (type != Character.OTHER_LETTER) {
            // standard Chinese characters are of type "OTHER_LETTER"!!
            first = Integer.toString(type);
          }
        }
        if ( ! seenFirst.contains(first)) {
          if (useGT) {
            logProb = scoreGT(tag);
            break first;
          } else {
            first = unknown;
          }
        }

        /* get the Counter of terminal rewrites for the relevant tag */
        Counter<String> wordProbs = tagHash.get(tag);

        /* if the proposed tag has never been seen before, issue a
           warning and return probability 0. */
        if (wordProbs == null) {
          if (VERBOSE) System.err.println("Warning: proposed tag is unseen in training data!");
          logProb = Double.NEGATIVE_INFINITY;
        } else if (wordProbs.containsKey(first)) {
          logProb = wordProbs.getCount(first);
        } else {
          logProb = wordProbs.getCount(unknown);
        }
      } else if (useGT) {
        logProb = scoreGT(tag);
      } else {
        if (VERBOSE) System.err.println("Warning: no unknown word model in place!\nGiving the combination " + word + " " + tag + " zero probability.");
        logProb = Double.NEGATIVE_INFINITY; // should never get this!
      }
    }

    if (VERBOSE) EncodingPrintWriter.out.println("Unknown word estimate for " + word + " as " + tag + ": " + logProb,encoding);
    return logProb;
  }

  private double scoreGT(String tag) {
    if (VERBOSE) System.err.println("using GT for unknown word and tag " + tag);
    double logProb;
    if (unknownGT.containsKey(tag)) {
      logProb = ((Double) unknownGT.get(tag)).doubleValue();
    } else {
      logProb = Double.NEGATIVE_INFINITY;
    }
    return logProb;
  }


  /**
   * trains the first-character based unknown word model.
   *
   * @param trees the collection of trees to be trained over
   */
  public void train(Collection<Tree> trees) {
    if (useFirst) {
      System.err.println("ChineseUWM: treating unknown word as the average of their equivalents by first-character identity. useUnicodeType: " + useUnicodeType);
    } 
    if (useGT) {
      System.err.println("ChineseUWM: using Good-Turing smoothing for unknown words.");
    }

    trainUnknownGT(trees);

    HashMap c = new HashMap(); // counts

    Counter tc = new Counter();

    for (Tree t : trees) {
      List words = t.taggedYield();
      for (Iterator j = words.iterator(); j.hasNext();) {
        TaggedWord tw = (TaggedWord) (j.next());
        String word = tw.word();
        String first = tw.word().substring(0, 1);
        if (useUnicodeType) {
          char ch = word.charAt(0);
          int type = Character.getType(ch);
          if (type != Character.OTHER_LETTER) {
            // standard Chinese characters are of type "OTHER_LETTER"!!
            first = Integer.toString(type);
          }
        }
        String tag = tw.tag();
        if (!c.containsKey(tag)) {
          c.put(tag, new Counter());
        }
        ((Counter) c.get(tag)).incrementCount(first);

        tc.incrementCount(tag);

        seenFirst.add(first);
      }
    }

    for (Iterator i = c.keySet().iterator(); i.hasNext();) {
      String tag = (String) i.next();
      Counter wc = (Counter) c.get(tag); // counts for words given a tag

      /* outer iteration is over tags */
      if ( ! tagHash.containsKey(tag)) {
        tagHash.put(tag, new Counter<String>());
      }

      /* the UNKNOWN first character is assumed to be seen once in each tag */
      // this is really sort of broken!
      tc.incrementCount(tag);
      wc.setCount(unknown, 1.0);

      /* inner iteration is over words */
      for (Iterator j = wc.keySet().iterator(); j.hasNext();) {
        String first = (String) j.next();
        double prob = Math.log(((double) (wc.getCount(first))) / ((double) (tc.getCount(tag))));
        tagHash.get(tag).setCount(first, prob);
        //if (Test.verbose)
        //EncodingPrintWriter.out.println(tag + " rewrites as " + first + " firstchar with probability " + prob,encoding);
      }
    }
  }

  /* trains Good-Turing estimation of unknown words */
  private void trainUnknownGT(Collection<Tree> trees) {

    Counter twCount = new Counter();
    Counter wtCount = new Counter();
    Counter tagCount = new Counter();
    Counter r1 = new Counter(); // for each tag, # of words seen once
    Counter r0 = new Counter(); // for each tag, # of words not seen
    Set seenWords = new HashSet();

    int tokens = 0;

    /* get TaggedWord and total tag counts, and get set of all
     * words attested in training */
    for (Tree t : trees) {
      List words = t.taggedYield();
      for (Iterator j = words.iterator(); j.hasNext();) {
        tokens++;
        TaggedWord tw = (TaggedWord) (j.next());
        WordTag wt = toWordTag(tw);
        String word = wt.word();
        String tag = wt.tag();
        //if (Test.verbose) EncodingPrintWriter.out.println("recording instance of " + wt.toString(),encoding); // testing

        wtCount.incrementCount(wt);
        twCount.incrementCount(tw);
        //if (Test.verbose) EncodingPrintWriter.out.println("This is the " + wtCount.countOf(wt) + "th occurrence of" + wt.toString(),encoding); // testing
        tagCount.incrementCount(tag);
        boolean alreadySeen = seenWords.add(word);

        // if (Test.verbose) if(! alreadySeen) EncodingPrintWriter.out.println("already seen " + wt.toString(),encoding); // testing

      }
    }
    
    // testing: get some stats here
    System.err.println("Total tokens: " + tokens + " [num words + numSent (boundarySymbols)]");
    System.err.println("Total WordTag types: " + wtCount.keySet().size());
    System.err.println("Total TaggedWord types: " + twCount.keySet().size() + " [should equal word types!]");
    System.err.println("Total tag types: " + tagCount.keySet().size());
    System.err.println("Total word types: " + seenWords.size());


    /* find # of once-seen words for each tag */
    for (Iterator i = wtCount.keySet().iterator(); i.hasNext();) {
      WordTag wt = (WordTag) i.next();
      if ((wtCount.getCount(wt)) == 1.0) {
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
        if (!(wtCount.containsKey(wt))) {
          r0.incrementCount(tag);
          //EncodingPrintWriter.out.println("unseen " + wt.toString(),encoding); // testing
        } else {
          //EncodingPrintWriter.out.println("count for " + wt.toString() + " is " + wtCount.countOf(wt),encoding);
        }
      }
    }

    /* set unseen word probability for each tag */
    for (Iterator i = tagCount.keySet().iterator(); i.hasNext();) {
      String tag = (String) i.next();
      //System.err.println("Tag " + tag + ".  Word types for which seen once: " + r1.countOf(tag) + ".  Word types for which unseen: " + r0.countOf(tag) + ".  Total count token for tag: " + tagCount.countOf(tag)); // testing

      double logprob = Math.log((r1.getCount(tag)) / ((tagCount.getCount(tag)) * (r0.getCount(tag))));

      unknownGT.put(tag, new Double(logprob));
    }

    /* testing only: print the GT-smoothed model */
    //System.err.println("The GT-smoothing model:");
    //System.err.println(unknownGT.toString());
    //EncodingPrintWriter.out.println(wtCount.toString(),encoding);
  }

  public static void main(String[] args) {
    System.out.println("Testing unknown matching");
    String s = "\u5218\u00b7\u9769\u547d";
    if (s.matches(properNameMatch)) {
      System.out.println("hooray names!");
    } else {
      System.out.println("Uh-oh names!");
    }
    String s1 = "\uff13\uff10\uff10\uff10";
    if (s1.matches(numberMatch)) {
      System.out.println("hooray numbers!");
    } else {
      System.out.println("Uh-oh numbers!");
    }
    String s11 = "\u767e\u5206\u4e4b\u56db\u5341\u4e09\u70b9\u4e8c";
    if (s1.matches(numberMatch)) {
      System.out.println("hooray numbers!");
    } else {
      System.out.println("Uh-oh numbers!");
    }
    String s12 = "\u767e\u5206\u4e4b\u4e09\u5341\u516b\u70b9\u516d";
    if (s1.matches(numberMatch)) {
      System.out.println("hooray numbers!");
    } else {
      System.out.println("Uh-oh numbers!");
    }
    String s2 = "\u4e09\u6708";
    if (s2.matches(dateMatch)) {
      System.out.println("hooray dates!");
    } else {
      System.out.println("Uh-oh dates!");
    }

    System.out.println("Testing tagged word");
    Counter c = new Counter();
    TaggedWord tw1 = new TaggedWord("w", "t");
    c.incrementCount(tw1);
    TaggedWord tw2 = new TaggedWord("w", "t2");
    System.out.println(c.containsKey(tw2));
    System.out.println(tw1.equals(tw2));

    WordTag wt1 = toWordTag(tw1);
    WordTag wt2 = toWordTag(tw2);
    WordTag wt3 = new WordTag("w", "t2");
    System.out.println(wt1.equals(wt2));
    System.out.println(wt2.equals(wt3));
  }

  private static WordTag toWordTag(TaggedWord tw) {
    return new WordTag(tw.word(), tw.tag());
  }

  private static final long serialVersionUID = 221L;

}
	
