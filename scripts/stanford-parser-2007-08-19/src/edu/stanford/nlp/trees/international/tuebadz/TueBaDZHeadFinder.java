package edu.stanford.nlp.trees.international.tuebadz;

import java.util.HashMap;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.AbstractCollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;

/** A HeadFinder for TueBa-D/Z.  First version.
 * @author Roger Levy (rog@csli.stanford.edu)
 */
public class TueBaDZHeadFinder extends AbstractCollinsHeadFinder {
  String left;
  String right;
  
  private boolean coordSwitch = false;
  
  public TueBaDZHeadFinder() {
    super(new TueBaDZLanguagePack());
    String excluded = String.valueOf(tlp.labelAnnotationIntroducingCharacters());
    if(excluded.indexOf("-") >= 0) {
      excluded = "-" + excluded.replaceAll("-", ""); // - can only appear at the beginning of a regex character class
    }
    headMarkedPattern = Pattern.compile("^[^" + excluded + "]*:HD"); 
    
    nonTerminalInfo = new HashMap();

    left = (coordSwitch ? "right" : "left");
    right = (coordSwitch ? "left" : "right");
    
    nonTerminalInfo.put("ROOT", new String[][]{{left, "SIMPX"},{left,"NX"},{left,"P"},{left,"PX","ADVX"},{left,"EN","EN_ADD"},{left}}); // we'll arbitrarily choose the leftmost.
    nonTerminalInfo.put("PX", new String[][]{{left, "APPR", "APPRART","PX"}});
    nonTerminalInfo.put("NX", new String[][]{{right, "NX"},{right,"NE","NN"},{right,"EN","EN_ADD","FX"},{right,"ADJX","PIS","ADVX"},{right,"CARD","TRUNC"},{right}});
    nonTerminalInfo.put("FX", new String[][]{{right, "FM","FX"}}); // junk rule for junk category :)
    nonTerminalInfo.put("ADJX", new String[][]{{right, "ADJX","ADJA","ADJD"},{right}});
    nonTerminalInfo.put("ADVX", new String[][]{{right, "ADVX"}}); // what a nice category!
    nonTerminalInfo.put("DP", new String[][]{{left}}); // no need for this really
    nonTerminalInfo.put("VXFIN", new String[][]{{left,"VXFIN"},{right,"VVFIN"}}); // not sure about left vs. right
    nonTerminalInfo.put("VXINF", new String[][]{{right,"VXINF"},{right,"VVPP","VVINF"}}); // not sure about lef vs. right for this one either
    nonTerminalInfo.put("LV", new String[][]{{right}}); // no need
    nonTerminalInfo.put("C", new String[][]{{right,"KOUS"}}); // I *think* right makes more sense for this.
    nonTerminalInfo.put("FKOORD", new String[][]{{left,"LK","C"},{right,"FKONJ","MF","VC",}}); // This one is very tough right/left because it conjoins all sorts of fields together.  Not sure about the right sonlution
    nonTerminalInfo.put("KOORD", new String[][]{{left}}); // no need.
    nonTerminalInfo.put("LK", new String[][]{{left}}); // no need.

    nonTerminalInfo.put("MF", new String[][]{{left}}); // this one is
                                                       // super-bad. MF
                                                       // does not
                                                       // designate a
                                                       // category
                                                       // corresponding
                                                       // to headship.
                                                       // Really,
                                                       // something
                                                       // totally
                                                       // different
                                                       // ought to be
                                                       // done for
                                                       // dependency.

    nonTerminalInfo.put("MFE", new String[][]{{left}}); // no need.

    nonTerminalInfo.put("NF", new String[][]{{left}}); // NF is pretty
                                                       // bad too,
                                                       // like MF.
                                                       // But it's not
                                                       // nearly so
                                                       // horribile.

    nonTerminalInfo.put("PARORD", new String[][]{{left}}); // no need.

    nonTerminalInfo.put("VC", new String[][]{{left,"VXINF"}}); // not sure
                                                       // what's right
                                                       // here, but
                                                       // it's rare
                                                       // not to have
                                                       // a head
                                                       // marked.


    nonTerminalInfo.put("VF", new String[][]{{left,"NX","ADJX","PX","ADVX","EN","SIMPX"}}); // second dtrs are always punctuation.

    nonTerminalInfo.put("FKONJ", new String[][]{{left,"LK"},{right,"VC"},{left,"MF","NF","VF"}}); // these are basically like clauses themselves...the problem is when there's no LK or VC :(

    nonTerminalInfo.put("DM", new String[][]{{left,"PTKANT"},{left,"ITJ"},{left,"KON","FM"},{left}});


    nonTerminalInfo.put("P", new String[][]{{left,"SIMPX"},{left}}); // ***NOTE*** that this is really the P-SIMPX category, but the - will make it stripped to P.

    nonTerminalInfo.put("R", new String[][]{{left,"C"},{left,"R"},{right,"VC"}}); // ***NOTE*** this is really R-SIMPX.  Also: syntactic head here.  Except for the rare ones that have neither C nor R-SIMPX dtrs.

    nonTerminalInfo.put("SIMPX", new String[][]{{left,"LK"},{right,"VC"},{left,"SIMPX"},{left,"C"},{right,"FKOORD"},{right,"MF"},{right}}); //  syntactic (finite verb) head here.  Note that when there's no LK or VC,the interesting predication tends to be annotate as inside the MF
    nonTerminalInfo.put("EN", new String[][]{{left, "NX"}}); // note that this node label starts as EN-ADD but the -ADD will get stripped off.
    nonTerminalInfo.put("EN_ADD", new String[][]{{left, "NX"}}); // just in case EN-ADD has been changed to EN_ADD

  }

  
  
  private final Pattern headMarkedPattern;
  /* Many TueBaDZ local trees have an explicitly marked head, as :HD.  (Almost!) all the time, there is only one :HD per local tree.  Use it if
   * possible. */
   protected Tree findMarkedHead(Tree t) {
     Tree[] kids = t.children();
     for (int i = 0, n = kids.length; i < n; i++) {
       if (headMarkedPattern.matcher(kids[i].label().value()).find()) {
         //System.err.println("found manually-labeled head " + kids[i] + " for tree " + t);
         return kids[i];
       }
     }
     return null;
   }
  
}
