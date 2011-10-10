package edu.stanford.nlp.misc;

import java.io.*;
import java.util.*;


public class SeeChars {

  private SeeChars() {}

  public static void seeChars(String str, String outputEncoding) {
    PrintWriter pw;
    try {
      pw = new PrintWriter(new OutputStreamWriter(System.out, outputEncoding), true);
    } catch (UnsupportedEncodingException uee) {
      System.err.println("Unsupported encoding: " + outputEncoding);
      pw = new PrintWriter(System.out, true);
    }
    seeChars(str, pw);
  }

  public static void seeChars(String str, PrintWriter pw) {
    int numCodePoints = str.codePointCount(0, str.length());
    for (int i = 0; i < numCodePoints; i++) {
      int index = str.offsetByCodePoints(0, i);
      int ch = str.codePointAt(index);
      seeCodePoint(ch, pw);
    }
  }

  public static void seeList(List sentence, String outputEncoding) {
    for (int ii = 0, len = sentence.size(); ii < len; ii++) {
      System.out.println("Word " + ii + " in " + outputEncoding);
      seeChars(sentence.get(ii).toString(), outputEncoding);
    }
  }

  public static void seeCodePoint(int ch, PrintWriter pw) {
    String chstr;
    if (ch == 10) {
      chstr = "nl";
    } else if (ch == 13) {
      chstr = "cr";
    } else {
      char[] chArr = Character.toChars(ch);
      chstr = new String(chArr);
    }
    int ty = Character.getType(ch);
    String tyStr = "";
    switch (ty) {
    case 5:
      tyStr = " other char";
      break;
    case 20:
      tyStr = " dash punct";
      break;
    case 21:
      tyStr = " start punct";
      break;
    case 22:
      tyStr = " end punct";
      break;
    case 24:
      tyStr = " other punct";
      break;
    default:
    }
    pw.println("Character " + ch + " [" + chstr +
               ", U+" + Integer.toHexString(ch).toUpperCase() +
               ", valid=" + Character.isValidCodePoint(ch) +
               ", suppl=" + Character.isSupplementaryCodePoint(ch) +
               ", mirror=" + Character.isMirrored(ch) +
               ", type=" + Character.getType(ch) + tyStr + "]");
  }

  public static void main(String[] args) {
    try {
      if (args.length < 2) {
        System.err.println("usage: java SeeChars file inCharEncoding [outCharEncoding]");
      } else {
        Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), args[1]));
        String outEncoding = (args.length >= 3) ? args[2]: args[1];
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, outEncoding), true);
        int ch;
        while ((ch = r.read()) >= 0) {
          seeCodePoint(ch, pw);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
