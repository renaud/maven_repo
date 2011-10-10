package edu.stanford.nlp.process;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.WordSegmenter;

import java.io.Reader;
import java.util.Iterator;

/**
 * @author Galen Andrew
 */
public class WordSegmentingTokenizer extends AbstractTokenizer {
  private Iterator wordIter;
  private Tokenizer tok;
  private WordSegmenter wordSegmenter;

  protected Object getNext() {
    while (wordIter == null || !wordIter.hasNext()) {
      if (!tok.hasNext()) {
        return null;
      }
      String s = ((Word) tok.next()).word();
      if (s == null) {
        return null;
      }
      Sentence se = segmentWords(s);
      wordIter = se.iterator();
    }
    return wordIter.next();
  }

  public WordSegmentingTokenizer(WordSegmenter wordSegmenter, Reader r) {
    this.wordSegmenter = wordSegmenter;
    tok = new WhitespaceTokenizer(r);
  }

  public Sentence segmentWords(String s) {
    return wordSegmenter.segmentWords(s);
  }

  public static TokenizerFactory factory(WordSegmenter wordSegmenter) {
    return new WordSegmentingTokenizerFactory(wordSegmenter);
  }

  private static class WordSegmentingTokenizerFactory implements TokenizerFactory {
    WordSegmenter wordSegmenter;

    public WordSegmentingTokenizerFactory(WordSegmenter wordSegmenter) {
      this.wordSegmenter = wordSegmenter;
    }

    public Iterator getIterator(Reader r) {
      return getTokenizer(r);
    }

    public Tokenizer getTokenizer(Reader r) {
      return new WordSegmentingTokenizer(wordSegmenter, r);
    }
  }
}
