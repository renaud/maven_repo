package edu.stanford.nlp.io;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * A convenience IO class with print and println statements to
 * standard output and standard error allowing encoding in an
 * arbitrary character set.
 *
 * @author Roger Levy
 * @author Christopher Manning
 */

public class EncodingPrintWriter {

  private static final String DEFAULT_ENCODING = "UTF-8";

  private static PrintWriter cachedErrWriter;
  private static String cachedErrEncoding = "";

  private static PrintWriter cachedOutWriter;
  private static String cachedOutEncoding = "";

  // uninstantiable
  private EncodingPrintWriter() {}

  /**
   * Print methods wrapped around System.err
   */
  public static class err {

    private err() {} // uninstantiable

    private static void setupErrWriter(String encoding) {
      if (cachedErrWriter == null ||
          encoding != null && cachedErrEncoding != encoding) {
        if (encoding == null) {
          encoding = DEFAULT_ENCODING;
        }
        try {
          cachedErrWriter = new PrintWriter(new OutputStreamWriter(System.err, encoding), true);
          cachedErrEncoding = encoding;
        } catch (UnsupportedEncodingException e) {
          System.err.println("Error " + e + "Printing as default encoding.");
          cachedErrWriter = new PrintWriter(new OutputStreamWriter(System.err), true);
          cachedErrEncoding = "";
        }
      }
    }

    public static void println(String o, String encoding) {
      setupErrWriter(encoding);
      cachedErrWriter.println(o);
    }

    public static void print(String o, String encoding) {
      setupErrWriter(encoding);
      cachedErrWriter.print(o);
      cachedErrWriter.flush();
    }

  } // end static class err


  /**
   * Print methods wrapped around System.out
   */
  public static class out {

    private out() {} // uninstantiable

    private static void setupOutWriter(String encoding) {
      if (cachedOutWriter == null ||
          encoding != null && cachedOutEncoding != encoding) {
        if (encoding == null) {
          encoding = DEFAULT_ENCODING;
        }
        try {
          cachedOutWriter = new PrintWriter(new OutputStreamWriter(System.err, encoding), true);
          cachedOutEncoding = encoding;
        } catch (UnsupportedEncodingException e) {
          System.err.println("Error " + e + "Printing as default encoding.");
          cachedOutWriter = new PrintWriter(new OutputStreamWriter(System.err), true);
          cachedOutEncoding = "";
        }
      }
    }

    public static void println(String o, String encoding) {
      setupOutWriter(encoding);
      cachedOutWriter.println(o);

    }

    public static void print(String o, String encoding) {
      setupOutWriter(encoding);
      cachedOutWriter.print(o);
      cachedOutWriter.flush();
    }

  } // end static class out

}
