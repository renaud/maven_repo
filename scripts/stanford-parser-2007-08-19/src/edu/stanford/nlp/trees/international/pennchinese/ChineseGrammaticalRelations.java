package edu.stanford.nlp.trees.international.pennchinese;

import edu.stanford.nlp.trees.GrammaticalRelation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static edu.stanford.nlp.trees.GrammaticalRelation.DEPENDENT;
import static edu.stanford.nlp.trees.GrammaticalRelation.GOVERNOR;

/**
 * <code>ChineseGrammaticalRelations</code> is a
 * set of {@link GrammaticalRelation} objects for the Chinese language.
 * Examples are from CTB_001.fid
 * @author Galen Andrew
 * @author Pi-Chuan Chang
 * @author Huihsin Tseng
 * @author Marie-Catherine de Marneffe
 * @see edu.stanford.nlp.trees.GrammaticalStructure
 * @see GrammaticalRelation
 * @see ChineseGrammaticalStructure
 */
public class ChineseGrammaticalRelations {

  public static List<GrammaticalRelation> values() {
    return Collections.unmodifiableList(Arrays.asList(values));
  }

 /**
   * The "predicate" grammatical relation. 
   **/

  public static final GrammaticalRelation PREDICATE = new GrammaticalRelation("pred", "predicate", DEPENDENT, "IP", new String[]{" IP=target !> IP"});

  /**
   * The "argument" grammatical relation. 
   **/

  public static final GrammaticalRelation ARGUMENT = new GrammaticalRelation("arg", "argument", DEPENDENT, null, new String[0]);

 /**
   * The "conjunct" grammatical relation. 
   * Example: 
   * (ROOT
   * (IP
   * (NP
   *   (NP (NR \u4e0a\u6d77) (NR \u6d66\u4e1c))
   *   (NP (NN \u5f00\u53d1)
   *     (CC \u4e0e)
   *     (NN \u6cd5\u5236) (NN \u5efa\u8bbe)))
   * (VP (VV \u540c\u6b65))))
   * "The development of Shanghai 's Pudong is in step with the establishment of its legal system"
   * <code>conj</code>(\u5efa\u8bbe, \u5f00\u53d1)
   */

  public static final GrammaticalRelation CONJUNCT = new GrammaticalRelation("conj", "conjunct", DEPENDENT, "VP|NP|ADJP|PP|ADVP|UCP", new String[]{"VP|NP|ADJP|PP|ADVP|UCP < (!PU=target $+ CC)", "VP|NP|ADJP|PP|ADVP|UCP < ( __=target $+ PU $+ CC)",  "VP|NP|ADJP|PP|ADVP|UCP < ( __=target $+ (PU < /\\u3001/) )", "PP < (PP $+ PP=target )","NP <( NP=target $+  PU $+  NP )"});

  /**
   * The "copula" grammatical relation. 
   */
   
  public static final GrammaticalRelation AUX_MODIFIER = new GrammaticalRelation("cop", "copula", DEPENDENT, "VP", new String[]{" VP < VC=target"});

/**
  * The "coordination" grammatical relation.  A coordination is the relation between
   * an element and a conjunction.<p>
   * <p/>
   * Example: 
   * (ROOT
   * (IP
   * (NP
   *   (NP (NR \u4e0a\u6d77) (NR \u6d66\u4e1c))
   *   (NP (NN \u5f00\u53d1)
   *     (CC \u4e0e)
   *     (NN \u6cd5\u5236) (NN \u5efa\u8bbe)))
   * (VP (VV \u540c\u6b65))))
   * "The development of Shanghai 's Pudong is in step with the establishment of its legal system"
   * <code>cc</code>(\u5efa\u8bbe, \u4e0e)
   */
                                        
  public static final GrammaticalRelation COORDINATION = new GrammaticalRelation("cc", "coordination", DEPENDENT, "VP|NP|ADJP|PP|ADVP|UCP|IP|QP", new String[]{"VP|NP|ADJP|PP|ADVP|UCP|IP|QP < (CC=target)"});


  /**
   * The "punctuation" grammatical relation.  This is used for any piece of
   * punctuation in a clause, if punctuation is being retained in the
   * typed dependencies.
   */
  public static final GrammaticalRelation PUNCTUATION = new GrammaticalRelation("punct", "punctuation", DEPENDENT, "VP|NP|PP|IP|CP", new String[]{"__ < PU=target"});


 /**
   * The "subject" grammatical relation. 
   **/
  public static final GrammaticalRelation SUBJECT = new GrammaticalRelation("subj", "subject", ARGUMENT, null, new String[0]);

 /**
   * The "nominal subject" grammatical relation.  A nominal subject is
   * a subject which is an noun phrase.<p>
   * <p/>
   * Example: 
   * (ROOT
   * (IP
   * (NP
   *   (NP (NR \u4e0a\u6d77) (NR \u6d66\u4e1c))
   *   (NP (NN \u5f00\u53d1)
   *     (CC \u4e0e)
   *     (NN \u6cd5\u5236) (NN \u5efa\u8bbe)))
   * (VP (VV \u540c\u6b65))))
   * "The development of Shanghai 's Pudong is in step with the establishment of its legal system"
   * <code>nsubj</code>(\u540c\u6b65, \u5efa\u8bbe)
   */
 public static final GrammaticalRelation NOMINAL_SUBJECT = new GrammaticalRelation("nsubj", "nominal subject", SUBJECT, "IP|VP", new String[]{" IP <( ( NP|QP=target!< NT ) $++ ( /^VP|VCD|IP/  !< VE !<VC !<SB !<LB  ))"," NP !$+ VP < ( (  NP|DP|QP=target !< NT ) $+ ( /^VP|VCD/ !<VE !< VC !<SB !<LB))"});
 
  /**
   * The "topic" grammatical relation. 
	 * Example:
   * (IP
   *   (NP (NN \u5efa\u7b51))
   *   (VP (VC \u662f)
   *       (NP (NN \u7ecf\u6d4e) (NN \u6d3b\u52a8)))))
	 *  Construction is an economic activity.
	 * <code>nsubj</code>(\u662f, \u5efa\u7b51)
	 */

    public static final GrammaticalRelation EXT_SUBJECT = new GrammaticalRelation("top", "topic", SUBJECT, "IP|VP", new String[]{"IP|VP < ( NP|DP=target $+ ( VP < VC|VE ) )","IP < (IP=target $+ ( VP < VC|VE))"});

  /**
   * The "topic" grammatical relation. 
	 * Example:
   */

    public static final GrammaticalRelation TOP_SUBJECT = new GrammaticalRelation("topic", "topic", SUBJECT, "IP", new String[]{" VP !> IP < ( NP=target $++ NP $++  VP  )"});

 /**
   * The "npsubj" grammatical relation. 
   **/

  public static final GrammaticalRelation NOMINAL_PASSIVE_SUBJECT = new GrammaticalRelation("npsubj", "nominal passive subject", NOMINAL_SUBJECT, "IP", new String[]{"IP < (NP=target $+ (VP|IP < SB|LB))"});

 /**
   * The "csubj" grammatical relation. 
   **/

  public static final GrammaticalRelation CLAUSAL_SUBJECT = new GrammaticalRelation("csubj", "clausal subject", SUBJECT, "IP", new String[]{"IP < (IP=target $+ ( VP !< VC))"});

/**
   * The "comp" grammatical relation. 
   **/

  public static final GrammaticalRelation COMPLEMENT = new GrammaticalRelation("comp", "complement", ARGUMENT, null, new String[0]);


/**
   * The "obj" grammatical relation. 
   **/
  public static final GrammaticalRelation OBJECT = new GrammaticalRelation("obj", "object", COMPLEMENT, null, new String[0]);

/*
 * The "direct object" grammatical relation.  
 *(IP
 *   (NP (NR \u4e0a\u6d77) (NR \u6d66\u4e1c))
 *   (VP
 *         (VCD (VV \u9881\u5e03) (VV \u5b9e\u884c))
 *         (AS \u4e86)
 *           (QP (CD \u4e03\u5341\u4e00)
 *             (CLP (M \u4ef6)))
 *           (NP (NN \u6cd5\u89c4\u6027) (NN \u6587\u4ef6)))))
 *  In recent years Shanghai 's Pudong has promulgated and implemented some regulatory documents.
 * <code>dobj</code>(\u9881\u5e03, \u6587\u4ef6)
 */

  public static final GrammaticalRelation DIRECT_OBJECT = new GrammaticalRelation("dobj", "direct object", OBJECT, "CP|VP", new String[]{"VP < ( /^V*/ $+  NP $+ NP|DP=target ) !< VC ", " VP < ( /^V*/ $+ NP|DP=target ! $+ NP|DP) !< VC ","CP < (IP $++ NP=target ) !<< VC"}); 
 
  /**
   * The "indirect object" grammatical relation.  
   */
 
  public static final GrammaticalRelation INDIRECT_OBJECT = new GrammaticalRelation("iobj", "indirect object",  OBJECT, "VP", new String[]{ " CP !> VP < ( VV $+ ( NP|DP|QP|CLP=target . NP|DP ) )" });

/*
 * The "range" grammatical relation.  The indirect
 * object of a VP is the quantifier phrase which is the (dative) object
 * of the verb.<p>
 *       (VP (VV \u6210\u4ea4)
 *         (NP (NN \u836f\u54c1))
 *         (QP (CD \u4e00\u4ebf\u591a)
 *           (CLP (M \u5143))))
 * <code>range </code>(\u6210\u4ea4, \u5143)
 */
	public static final GrammaticalRelation RANGE = new GrammaticalRelation("range", "range",  OBJECT, "VP", new String[]{ " VP < ( NP|DP|QP $+ NP|DP|QP=target)" , "VP < ( VV $+ QP=target )"});
	
	 /**
   * The "prepositional object" grammatical relation.  
   * (PP (P \u6839\u636e)
   *       (NP
   *         (DNP
   *           (NP
   *             (NP (NN \u56fd\u5bb6))
   *             (CC \u548c)
   *             (NP (NR \u4e0a\u6d77\u5e02)))
   *           (DEG \u7684))
   *         (ADJP (JJ \u6709\u5173))
   *         (NP (NN \u89c4\u5b9a))))
   * Example:
   * pobj(\u6839\u636e-13, \u89c4\u5b9a-19)
   */
	
	public static final GrammaticalRelation PREPOSITIONAL_OBJECT = new GrammaticalRelation("pobj", "prepositional object", OBJECT, "^PP", new String[]{"/^PP/ < /^P/ < /^NP|^DP|QP/=target"});

 /**
   * The "localizer object" grammatical relation.  
   * (LCP
   *       (NP (NT \u8fd1\u5e74))
   *       (LC \u6765))
   * lobj(\u6765-4, \u8fd1\u5e74-3)
   */

  public static final GrammaticalRelation TIME_POSTPOSITION = new GrammaticalRelation("lobj", "localizer object", OBJECT, "LCP", new String[]{"LCP < ( NP|QP|DP=target $+ LC)"});  


/**
 * The "attribute" grammatical relation. 
 *	 (IP
 *        (NP (NR \u6d66\u4e1c))
 *      (VP (VC \u662f)
 *          (NP (NN \u5de5\u7a0b)))))
 * <code> attr </code> (\u662f, \u5de5\u7a0b)
 */

  public static final GrammaticalRelation ATTRIBUTIVE = new GrammaticalRelation("attr", "attributive", COMPLEMENT, "VP", new String[]{"VP < /^VC$/ < NP|QP=target"}); 

/**
 * The "clausal" grammatical relation. 
 *         (IP
 *             (VP
 *               (VP
 *                 (ADVP (AD \u4e00))
 *                 (VP (VV \u51fa\u73b0)))
 *               (VP
 *                 (ADVP (AD \u5c31))
 *                 (VP (SB \u88ab)
 *                   (VP (VV \u7eb3\u5165)
 *                     (NP (NN \u6cd5\u5236) (NN \u8f68\u9053)))))))))))
 * <code> ccomp </code> (\u51fa\u73b0, \u7eb3\u5165)
 */

  public static final GrammaticalRelation CLAUSAL_COMPLEMENT = new GrammaticalRelation("ccomp", "clausal complement", COMPLEMENT, "VP|ADJP|IP", new String[]{"VP|ADJP|IP < IP|VP|VRD|VCD=target"}); 

	 /**
   * The "xclausal complement" grammatical relation.  
   * Example:
   */

	public static final GrammaticalRelation XCLAUSAL_COMPLEMENT = new GrammaticalRelation("xcomp", "xclausal complement", COMPLEMENT, "VP|ADJP", new String[]{"VP !> (/^VP/ < /^VC$/ ) < (IP=target < (VP < P))", "ADJP < (IP=target <, (VP <, P))", "VP < (IP=target < (NP $+ NP|ADJP))", "VP < (/^VC/ $+ (VP=target < VC < NP))" });

 	/**
   * The "cp marker" grammatical relation.  
   * (CP
   *         (IP
   *           (VP
   *             (VP (VV \u632f\u5174)
   *               (NP (NR \u4e0a\u6d77)))
   *             (PU \uff0c)
   *             (VP (VV \u5efa\u8bbe)
   *               (NP
   *                 (NP (NN \u73b0\u4ee3\u5316))
   *                 (NP (NN \u7ecf\u6d4e) (PU \u3001) (NN \u8d38\u6613) (PU \u3001) (NN \u91d1\u878d))
   *                 (NP (NN \u4e2d\u5fc3))))))
   *         (DEC \u7684))
   * Example:
 	 *<code> cpm </code> (\u632f\u5174, \u7684)
 	 */

  public static final GrammaticalRelation COMPLEMENTIZER = new GrammaticalRelation("cpm", "complementizer",COMPLEMENT, "^CP", new String[]{"/^CP/ < (__  $++ DEC=target)"});
	
	 /**
   * The "adjectival complement" grammatical relation.  
   * Example:
   */

	public static final GrammaticalRelation ADJECTIVAL_COMPLEMENT = new GrammaticalRelation("acomp", "adjectival complement", COMPLEMENT, "VP", new String[]{"VP < (ADJP=target !$-- NP)"});

 /**
   * The "temporal complement" grammatical relation. 
   * (IP
   *           (VP
   *             (NP (NT \u4ee5\u524d))
   *             (ADVP (AD \u4e0d))
   *             (ADVP (AD \u66fe))
   *             (VP (VV \u9047\u5230) (AS \u8fc7))))
   *(VP
   *     (LCP
   *       (NP (NT \u8fd1\u5e74))
   *       (LC \u6765))
   *     (VP
   *       (VCD (VV \u9881\u5e03) (VV \u5b9e\u884c))
	 * <code> tcomp </code> (\u9047\u5230, \u4ee5\u524d)
   **/

public static final GrammaticalRelation TIMEM  = new GrammaticalRelation("tcomp", "temporal complement", COMPLEMENT, "VP|IP", new String[]{"VP|IP < (NP=target < NT !.. /^VC$/ $++  VP)"});

 /**
   * The "localizer complement" grammatical relation.  
   * (VP (VV \u5360)
   *     (LCP
   *       (QP (CD \u4e5d\u6210))
   *       (LC \u4ee5\u4e0a)))
   *   (PU \uff0c)
   *   (VP (VV \u8fbe)
   *     (QP (CD \u56db\u767e\u4e09\u5341\u516b\u70b9\u516b\u4ebf)
   *       (CLP (M \u7f8e\u5143))))
   * <code> lccomp </code> (\u5360-11, \u4ee5\u4e0a-13)
   */

 public static final GrammaticalRelation LC_COMPLEMENT = new GrammaticalRelation("lccomp", "localizer complement",COMPLEMENT, "VP|IP", new String[]{"VP|IP < LCP=target "}); 

 /**
   * The "resultative complement" grammatical relation. 
   **/

  public static final GrammaticalRelation RES_VERB = new GrammaticalRelation("rcomp", "result verb", COMPLEMENT, "VRD", new String[]{ "VRD < ( /V*/ $+ /V*/=target )"});  




 /**
   * The "modifier" grammatical relation. 
   **/


  public static final GrammaticalRelation MODIFIER = new GrammaticalRelation("mod", "modifier", DEPENDENT, null, new String[0]);


 /**
   * The "coordinated verb compound" grammatical relation. 
   *   (VCD (VV \u9881\u5e03) (VV \u5b9e\u884c))
   * cordmod(\u9881\u5e03-5, \u5b9e\u884c-6)
   **/

  public static final GrammaticalRelation VERB_COMPOUND = new GrammaticalRelation("comod", "coordinated verb compound", MODIFIER, "VCD", new String[]{"VCD < ( VV|VA $+  VV|VA=target)"});  

 /**
   * The "modal" grammatical relation.
   * (IP
   *           (NP (NN \u5229\u76ca))
   *           (VP (VV \u80fd)
   *             (VP (VV \u5f97\u5230)
   *               (NP (NN \u4fdd\u969c)))))))))
   * <code> mmod </code> (\u5f97\u5230-64, \u80fd-63)
   **/
   
  public static final GrammaticalRelation MODAL_VERB = new GrammaticalRelation("mmod", "modal verb", MODIFIER, "VP", new String[]{"VP < ( VV=target $+ VP|VRD )"});  



  /**
   * The "passive" grammatical relation. 
   */

  public static final GrammaticalRelation AUX_PASSIVE_MODIFIER = new GrammaticalRelation("pass", "passive", MODIFIER, "VP", new String[]{"VP < SB|LB=target"});

  /**
   * The "ba" grammatical relation. 
   */

 public static final GrammaticalRelation BA = new GrammaticalRelation("ba", "ba", DEPENDENT, "VP|IP", new String[]{"VP|IP < BA=target "});

 /**
   * The "temporal modifier" grammatical relation. 
 
   **/


  public static final GrammaticalRelation TEMPORAL_MODIFIER = new GrammaticalRelation("tmod", "temporal modifier", MODIFIER, "VP|IP|ADJP", new String[]{" VC|VE ! >> VP|ADJP < NP=target < NT", "VC|VE !>>IP <( NP=target < NT $++ VP !< VC|VE )"});

  /**
   * The "temporal clause" grammatical relation.  
   *(VP(PP (P \u7b49) (LCP (IP
   *                           (VP (VV \u79ef\u7d2f) (AS \u4e86)
   *                             (NP (NN \u7ecf\u9a8c))))
   *                         (LC \u4ee5\u540e)))
   *                     (ADVP (AD \u518d))
   *                     (VP (VV \u5236\u5b9a)
   *                       (NP (NN \u6cd5\u89c4) (NN \u6761\u4f8b))))
   *                  (PU \u201d)))
   *               (DEC \u7684))
   *             (NP (NN \u505a\u6cd5)))))))
   * <code> tclaus </code> (\u4ee5\u540e, \u79ef\u7d2f)
   **/

  public static final GrammaticalRelation TIME = new GrammaticalRelation("tclaus", "temporal clause",  MODIFIER, "LCP", new String[]{ "/LCP/ < ( IP=target $+ LC )"});  


 /**
   * The "relative clause modifier" grammatical relation. 
   *(CP (IP (VP (NP (NT \u4ee5\u524d))
   *             (ADVP (AD \u4e0d))
   *             (ADVP (AD \u66fe))
   *             (VP (VV \u9047\u5230) (AS \u8fc7))))
   *         (DEC \u7684))
   *       (NP
   *         (NP
   *           (ADJP (JJ \u65b0))
   *           (NP (NN \u60c5\u51b5)))
   *         (PU \u3001)
   *         (NP
   *           (ADJP (JJ \u65b0))
   *           (NP (NN \u95ee\u9898)))))))
   * (PU \u3002)))
   * the new problem that has not been encountered. 
   * <code> rcmod </code> (\u95ee\u9898, \u9047\u5230)
   **/


  public static final GrammaticalRelation RELATIVE_CLAUSE_MODIFIER = new GrammaticalRelation("rcmod", "relative clause modifier", MODIFIER, "NP", new String[]{"NP  $++ (CP=target ) > NP ", "NP  $++ (CP=target <: IP) > NP  ", "NP  $++ (CP=target)", " NP  << ( CP=target $++ NP  )"}); 

 /**
   * The "number modifier" grammatical relation. 
   * (NP
   *         (NP (NN \u62c6\u8fc1) (NN \u5de5\u4f5c))
   *         (QP (CD \u82e5\u5e72))
   *         (NP (NN \u89c4\u5b9a)))
   * num(\u4ef6-24, \u4e03\u5341\u4e00-23)
   * num(\u89c4\u5b9a-48, \u82e5\u5e72-47)
   
   */

  public static final GrammaticalRelation NUMERIC_MODIFIER = new GrammaticalRelation("numod", "numeric modifier", MODIFIER,"QP|NP", new String[]{"QP < CD=target","NP < (QP =target !< CLP )"});

 /**
   * The "ordnumber modifier" grammatical relation. 
   */

public static final GrammaticalRelation ODNUMERIC_MODIFIER = new GrammaticalRelation("ordmod", "numeric modifier", MODIFIER,"NP|QP", new String[]{"NP < QP=target < ( OD !$+ CLP )","QP < (OD=target $+ CLP)"});


 /**
   * The "classifier modifier" grammatical relation. 
   * (QP (CD \u4e03\u5341\u4e00)
   *           (CLP (M \u4ef6)))
   *         (NP (NN \u6cd5\u89c4\u6027) (NN \u6587\u4ef6)))))
	 * <code> clf </code> (\u6587\u4ef6-26, \u4ef6-24)
   */


  public static final GrammaticalRelation NUMBER_MODIFIER = new GrammaticalRelation("clf", "classifier modifier", MODIFIER, "^NP|DP|QP", new String[]{"NP|QP < ( QP  =target << M $++ NP|QP)","DP < ( DT $+ CLP=target )"});

	/*
	 * The "noun compound modifier" grammatical relation.  
	 * Example: 
   * (ROOT
   * (IP
   * (NP
   *   (NP (NR \u4e0a\u6d77) (NR \u6d66\u4e1c))
   *   (NP (NN \u5f00\u53d1)
   *     (CC \u4e0e)
   *     (NN \u6cd5\u5236) (NN \u5efa\u8bbe)))
   * (VP (VV \u540c\u6b65))))
	 * <code> nn </code> (\u6d66\u4e1c, \u4e0a\u6d77)
	 */

  public static final GrammaticalRelation NOUN_COMPOUND_MODIFIER = new GrammaticalRelation("nmod", "nn modifier", MODIFIER, "^NP", new String[]{"NP < (NN|NR|NT=target $+ NN|NR|NT)","NP < (NN|NR|NT $+ FW=target)", " NP <  (NP=target !$+ PU|CC $++ NP|PRN )"});

 /**
   * The "adjetive modifier" grammatical relation. 
   *         (NP
   *           (ADJP (JJ \u65b0))
   *           (NP (NN \u60c5\u51b5)))
   *         (PU \u3001)
   *         (NP
   *           (ADJP (JJ \u65b0))
   *           (NP (NN \u95ee\u9898)))))))
   * <code> amod </code> (\u60c5\u51b5-34, \u65b0-33)
   */


  public static final GrammaticalRelation ADJECTIVEL_MODIFIER = new GrammaticalRelation("amod", "adjectivel modifier", MODIFIER, "NP|CLP|QP", new String[]{"NP|CLP|QP < (ADJP=target $++ NP|CLP|QP ) "});


  /**
   * The "adverbial modifier" grammatical relation.  
   * (VP
   *     (ADVP (AD \u57fa\u672c))
   *     (VP (VV \u505a\u5230) (AS \u4e86)
   * advmod(\u505a\u5230-74, \u57fa\u672c-73)

   */
    
  public static final GrammaticalRelation ADVERBIAL_MODIFIER = new GrammaticalRelation("advmod", "adverbial modifier", MODIFIER, "VP|ADJP|IP|CP|PP|NP|QP", new String[]{"VP|ADJP|IP|CP|PP|NP < ADVP=target", "VP|ADJP < AD|CS=target", "QP < (ADVP=target $+ QP)","QP < ( QP $+ ADVP=target)"}); 



  /**
   * The "verb modifier" grammatical relation.  
   */

 public static final GrammaticalRelation IP_MODIFIER = new GrammaticalRelation("vmod", "participle modifier", MODIFIER, "NP", new String[]{"NP < IP=target "}); 

  /**
   * The "parenthetical modifier" grammatical relation.  
   */


 public static final GrammaticalRelation PRN_MODIFIER = new GrammaticalRelation("prnmod", "prn odifier", MODIFIER, "NP", new String[]{"NP < PRN=target "}); 

  /**
   * The "negative modifier" grammatical relation.  
   *(VP
   *             (NP (NT \u4ee5\u524d))
   *             (ADVP (AD \u4e0d))
   *             (ADVP (AD \u66fe))
   *             (VP (VV \u9047\u5230) (AS \u8fc7))))
   * neg(\u9047\u5230-30, \u4e0d-28)
   */


  public static final GrammaticalRelation NEGATION_MODIFIER = new GrammaticalRelation("neg", "negation modifier", ADVERBIAL_MODIFIER, "VP|ADJP|IP", new String[]{"VP|ADJP|IP < (AD=target < /\\u4e0d/)", "VP|ADJP|IP < (ADVP=target < (AD < /\\u4e0d/))"});
 
   /**
   * The "determiner modifier" grammatical relation.  
   * (NP
   *           (DP (DT \u8fd9\u4e9b))
   *           (NP (NN \u7ecf\u6d4e) (NN \u6d3b\u52a8)))
   * det(\u6d3b\u52a8-61, \u8fd9\u4e9b-59)
   */

 
  public static final GrammaticalRelation DETERMINER = new GrammaticalRelation("det", "determiner", MODIFIER, "^NP|DP", new String[]{"/^NP/ < (DP=target $++ NP )", "DP < DT < QP=target"});

   /**
   * The "possession modifier" grammatical relation.  
   */

  public static final GrammaticalRelation POSSESSION_MODIFIER = new GrammaticalRelation("poss", "possession modifier", MODIFIER, "NP", new String[]{"NP < ( PN=target $+ DEC $+  NP )"});


   /**
   * The "possessive marker" grammatical relation.  
   */

  public static final GrammaticalRelation POSSESSIVE_MODIFIER = new GrammaticalRelation("possm", "possessive marker", MODIFIER, "NP", new String[]{"NP < ( PN $+ DEC=target ) "});

   /**
   * The "dvp marker" grammatical relation.  
   *         (DVP
   *           (VP (VA \u7b80\u5355))
   *           (DEV \u7684))
   *         (VP (VV \u91c7\u53d6)
   * dvpm(\u7b80\u5355-7, \u7684-8)
   */

   public static final GrammaticalRelation DVP_MODIFIER = new GrammaticalRelation("dvpm", "dvp marker", MODIFIER, "DVP", new String[]{" DVP < (__ $+ DEV=target ) "});

   /**
   * The "dvp modifier" grammatical relation.  
   * (ADVP (AD \u4e0d))
   *    (VP (VC \u662f)
   *       (VP
   *         (DVP
   *           (VP (VA \u7b80\u5355))
   *           (DEV \u7684))
   *         (VP (VV \u91c7\u53d6)
   *dvpmod(\u91c7\u53d6-9, \u7b80\u5355-7)
   */

  public static final GrammaticalRelation DVPM_MODIFIER = new GrammaticalRelation("dvpmod", "dvp modifier", MODIFIER, "VP", new String[]{" VP < ( DVP=target $+ VP) "});

   /**
    * The "associative marker" grammatical relation.  
    *    (NP
    *                 (NP (NR \u6df1\u5733) (ETC \u7b49))
    *                 (NP (NN \u7279\u533a))))
    *             (DEG \u7684))
    * assm(\u7279\u533a-37, \u7684-38)
   */

  public static final GrammaticalRelation ASSOCIATIVE_MODIFIER = new GrammaticalRelation("assm", "associative marker", MODIFIER, "DNP", new String[]{" DNP < ( __ $+ DEG=target ) "});

   /**
   * The "associative modifier" grammatical relation. 
   *    (NP
   *                 (NP (NR \u6df1\u5733) (ETC \u7b49))
   *                 (NP (NN \u7279\u533a))))
   *             (DEG \u7684))
   *           (NP (NN \u7ecf\u9a8c) (NN \u6559\u8bad))))
   * assmod(\u6559\u8bad-40, \u7279\u533a-37)
   */


  public static final GrammaticalRelation ASSOCIATIVEM_MODIFIER = new GrammaticalRelation("assmod", "associative modifier", MODIFIER, "NP|QP", new String[]{"NP|QP < ( DNP =target $++ NP|QP ) "});

  /**
   * The "prepositional modifier" grammatical relation.  
   *(IP
   *  (PP (P \u5bf9)
   *   (NP (PN \u6b64)))
   * (PU \uff0c)
   * (NP (NR \u6d66\u4e1c))
   * (VP
   *   (VP
   *     (ADVP (AD \u4e0d))
   *     (VP (VC \u662f)
   *       (VP
   *         (DVP
   *           (VP (VA \u7b80\u5355))
   *           (DEV \u7684))
   *         (VP (VV \u91c7\u53d6)
   * <code> prep </code> (\u91c7\u53d6-9, \u5bf9-1)
   */

  
  public static final GrammaticalRelation PREPOSITIONAL_MODIFIER = new GrammaticalRelation("prep", "prepositional modifier", MODIFIER, "^NP|VP|IP", new String[]{"/^NP/ < /^PP/=target", "VP < /^PP/=target", "IP < /^PP/=target "});

/**
   * The "clause modifier" grammatical relation.  
   * (PP (P \u56e0\u4e3a)
   *       (IP
   *         (VP
   *           (VP
   *             (ADVP (AD \u4e00))
   *             (VP (VV \u5f00\u59cb)))
   *           (VP
   *             (ADVP (AD \u5c31))
   *             (ADVP (AD \u6bd4\u8f83))
   *             (VP (VA \u89c4\u8303)))))))
   * <code> clmpd </code> (\u56e0\u4e3a-18, \u5f00\u59cb-20)
   */

  public static final GrammaticalRelation CL_MODIFIER = new GrammaticalRelation("clmpd", "clause modifier", MODIFIER, "^PP|IP", new String[]{"PP < (P $+ IP|VP =target)", "IP < (CP=target $++ VP)"});


  /**
   * The "prepositional localizer modifier" grammatical relation.  
   * (PP (P \u5728)
   *             (LCP
   *               (NP
   *                 (DP (DT \u8fd9)
   *                   (CLP (M \u7247)))
   *                 (NP (NN \u70ed\u571f)))
   *               (LC \u4e0a))))))))
   * plmod(\u5728-25, \u4e0a-29)
   */

  public static final GrammaticalRelation PREPOSTPOSITIONAL_MODIFIER = new GrammaticalRelation("plmod", "prepositional localizer modifier", MODIFIER, "PP", new String[]{"PP < ( P $++ LCP=target )"});

  /**
   * The "aspect marker" grammatical relation.  
   * (VP
   *     (ADVP (AD \u57fa\u672c))
   *     (VP (VV \u505a\u5230) (AS \u4e86)
	 * <code> asp </code> (\u505a\u5230,\u4e86) 
   */

  public static final GrammaticalRelation PREDICATE_ASPECT = new GrammaticalRelation("asp", "aspect", MODIFIER, "VP", new String[]{"VP < ( /^V*/ $+ AS=target)"}); 

  /**
   * The "participial modifier" grammatical relation. 
   **/

  public static final GrammaticalRelation PART_VERB = new GrammaticalRelation("partmod", "particle verb", MODIFIER, "VP|IP", new String[]{"VP|IP < ( MSP=target )"});  


  /**
   * The "etc" grammatical relation.  
   *(NP
   *                 (NP (NN \u7ecf\u6d4e) (PU \u3001) (NN \u8d38\u6613) (PU \u3001) (NN \u5efa\u8bbe) (PU \u3001) (NN \u89c4\u5212) (PU \u3001) (NN \u79d1\u6280) (PU \u3001) (NN \u6587\u6559) (ETC \u7b49))
   *                 (NP (NN \u9886\u57df)))))
   * <code> etc </code> (\u529e\u6cd5-70, \u7b49-71)
   */
   
  public static final GrammaticalRelation ETC = new GrammaticalRelation("etc", "ETC", MODIFIER, "^NP", new String[]{"/^NP/ < (NN|NR . ETC=target)"});  
 
  /**
   * The "semantic dependent" grammatical relation.  
   */

  public static final GrammaticalRelation SEMANTIC_DEPENDENT = new GrammaticalRelation("sdep", "semantic dependent", DEPENDENT, null, new String[0]);

  /**
   * The "xsubj" grammatical relation.  
   *(IP
   *           (NP (PN \u6709\u4e9b))
   *           (VP
   *             (VP
   *               (ADVP (AD \u8fd8))
   *               (ADVP (AD \u53ea))
   *               (VP (VC \u662f)
   *                 (NP
   *                   (ADJP (JJ \u6682\u884c))
   *                   (NP (NN \u89c4\u5b9a)))))
   *             (PU \uff0c)
   *             (VP (VV \u6709\u5f85)
   *               (IP
   *                 (VP
   *                   (PP (P \u5728)
   *                     (LCP
   *                       (NP (NN \u5b9e\u8df5))
   *                       (LC \u4e2d)))
   *                   (ADVP (AD \u9010\u6b65))
   *                   (VP (VV \u5b8c\u5584))))))))))
   * <code> xsubj </code> (\u5b8c\u5584-26, \u6709\u4e9b-14)
   */

  public static final GrammaticalRelation CONTROLLED_SUBJECT = new GrammaticalRelation("xsubj", "controlled subject", SEMANTIC_DEPENDENT, "VP", new String[]{"VP !< NP < VP > (IP !$- NP !< NP !>> (VP < VC ) >+(VP) (VP $-- NP=target))"});




 private static final GrammaticalRelation[] values = new GrammaticalRelation[]{
GOVERNOR
,DEPENDENT
,PREDICATE
,AUX_MODIFIER
,AUX_PASSIVE_MODIFIER
,COORDINATION
,PUNCTUATION
,ARGUMENT
,SUBJECT
,NOMINAL_SUBJECT
,CLAUSAL_SUBJECT
,COMPLEMENT
,OBJECT
,DIRECT_OBJECT
,INDIRECT_OBJECT
,PREPOSITIONAL_OBJECT
,ATTRIBUTIVE
,CLAUSAL_COMPLEMENT
,XCLAUSAL_COMPLEMENT
,COMPLEMENTIZER
,ADJECTIVAL_COMPLEMENT
,MODIFIER
,TEMPORAL_MODIFIER
,RELATIVE_CLAUSE_MODIFIER
,NUMERIC_MODIFIER
,NUMBER_MODIFIER
,NOUN_COMPOUND_MODIFIER
,ADJECTIVEL_MODIFIER
,ADVERBIAL_MODIFIER
,NEGATION_MODIFIER
,DETERMINER
,POSSESSION_MODIFIER
,POSSESSIVE_MODIFIER
,PREPOSITIONAL_MODIFIER
,PREDICATE_ASPECT
,TIME_POSTPOSITION
,VERB_COMPOUND
,RES_VERB
,MODAL_VERB
,ETC,SEMANTIC_DEPENDENT
,CONTROLLED_SUBJECT
,TIME
,BA
,ASSOCIATIVE_MODIFIER
,ASSOCIATIVEM_MODIFIER
,CONJUNCT
,PREPOSTPOSITIONAL_MODIFIER
,DVP_MODIFIER
,DVPM_MODIFIER
,RANGE 
,TIMEM
,CL_MODIFIER
,EXT_SUBJECT
,ODNUMERIC_MODIFIER
,LC_COMPLEMENT
,IP_MODIFIER
,PRN_MODIFIER
,PART_VERB
,TOP_SUBJECT
};

}

