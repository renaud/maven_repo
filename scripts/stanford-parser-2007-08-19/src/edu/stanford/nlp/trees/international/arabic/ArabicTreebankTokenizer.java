package edu.stanford.nlp.trees.international.arabic;

import edu.stanford.nlp.trees.PennTreebankTokenizer;
import edu.stanford.nlp.process.Tokenizer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 * Builds a tokenizer for English PennTreebank (release 2) trees.
 * This is currently internally implemented via a java.io.StreamTokenizer.
 *
 * @author Christopher Manning
 */
public class ArabicTreebankTokenizer extends PennTreebankTokenizer {

  public ArabicTreebankTokenizer(Reader r) {
    super(r);
  }

  /**
   * Internally fetches the next token.
   *
   * @return the next token in the token stream, or null if none exists.
   */
  public Object getNext() {
    try {
      while (true) {
        int nextToken = st.nextToken();
        switch (nextToken) {
          case java.io.StreamTokenizer.TT_EOL:
            return eolString;
          case java.io.StreamTokenizer.TT_EOF:
            return null;
          case java.io.StreamTokenizer.TT_WORD:
            if (st.sval.equals(":::")) {
              nextToken = st.nextToken();
              nextToken = st.nextToken();
              if ( ! st.sval.equals(":::")) {
                System.err.println("ArabicTreebankTokenizer assumptions broken!");
              }
            } else {
              return st.sval;
            }
            break;
          case java.io.StreamTokenizer.TT_NUMBER:
            return Double.toString(st.nval);
          default:
            char[] t = {(char) nextToken};    // (array initialization)
            return new String(t);
        }
      }
    } catch (IOException ioe) {
      // do nothing, return null
    }
    return null;
  }

  public static void main(String[] args) throws IOException {
    Tokenizer att = new ArabicTreebankTokenizer(new FileReader(args[0]));
    while (att.hasNext()) {
      System.out.println(att.next());
    }
  }

}
