package edu.stanford.nlp.trees.international.arabic;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;
import java.io.*;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.Function;

/**
 * This escaper deletes the '#' and '+' symbols that the IBM segmenter uses
 * to mark prefixes and suffixes, since they're not present in the Penn
 * Arabic treebank materials (though later we might try adding them), and
 * escapes the parenthesis characters.
 *
 * @author Christopher Manning
 */
public class IBMArabicEscaper implements Function<List<HasWord>, List<HasWord>> {

  private static Pattern p2 = Pattern.compile("\\$[a-z]+_\\((.*?)\\)");

  private static String escapeString(String w) {
    int wLen = w.length();
    if (wLen > 1) {  // only for two or more letter words
      Matcher m2 = p2.matcher(w);
      if (m2.matches()) {
        w = m2.replaceAll("$1");
      } else if (w.charAt(0) == '+') {
        w = w.substring(1);
      // doesn't seem that ther are inital - sign markers in data.
      // } else if (w.charAt(0) == '-' && w.charAt(1) != '-') {
      //  w = w.substring(1);
      } else if (w.charAt(wLen - 1) == '#') {
        w = w.substring(0, wLen - 1);
      }
    } else {
      if (w.equals("(")) {
        w = "-LRB-";
      } else if (w.equals(")")) {
        w = "-RRB-";
      }
    }
    return w;
  }


  /** <i>Note:</i> At present this clobbers the input list items.
   *  This should be fixed.
   */
  public List<HasWord> apply(List<HasWord> arg) {
    List<HasWord> ans = new ArrayList<HasWord>(arg);
    for (HasWord wd : ans) {
      wd.setWord(escapeString(wd.word()));
    }
    return ans;
  }

  /** This main method preprocesses 1-sentence per line input, making similar
   *  changes.
   *  @param args A list of filenames.  The files must be UTF-8 encoded.
   *  @throws IOException If there are any issues
   */
  public static void main(String[] args) throws IOException {
    for (String arg : args) {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(arg), "UTF-8"));
      String outFile = arg + ".sent";
      PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8")));
      String line;
      while ((line = br.readLine()) != null) {
        String[] words = line.split("\\s+");
        for (int i = 0; i < words.length; i++) {
          String w = escapeString(words[i]);
          pw.print(w);
          if (i != words.length - 1) {
            pw.print(" ");
          }
        }
        pw.println();
      }
      br.close();
      pw.close();
    }
  }

}
