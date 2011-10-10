package edu.stanford.nlp.trees;

import static edu.stanford.nlp.trees.GrammaticalRelation.DEPENDENT;
import static edu.stanford.nlp.trees.GrammaticalRelation.GOVERNOR;
import edu.stanford.nlp.util.StringUtils;

import java.util.*;


/**
 * <code>EnglishGrammaticalRelations</code> is a
 * set of {@link GrammaticalRelation} objects for the English language.
 * <p/>
 * Grammatical relations can either be shown in their basic form, where each
 * input token receives a relation, or "collapsed" which does certain normalizations
 * which group words or turns them into relations. See
 * {@link EnglishGrammaticalStructure}.  What is presented here mainly
 * shows the basic form, though there is some mixture. The "collapsed" grammatical
 * relations primarily differ as follows:
 * <ul>
 * <li>Some multiword conjunctions and prepositions are treated as single
 * words, and then processed as below.</li>
 * <li>Prepositions do not appear as words but are turned into new "prep" or "prepc"
 * grammatical relations, one for each preposition.</li>
 * <li>Conjunctions do not appear as words but are turned into new "conj"
 * grammatical relations, one for each conjunction.</li>
 * <li>The possessive "'s" is deleted, leaving just the relation between the
 * possessor and possessum.</li>
 * <li>Agents is passive sentences are recognized and marked as agent and not as prep_by.</li>
 * </ul>
 * <p/>
 * This set of English grammatical relations is not intended to be
 * exhaustive or immutable.  It's just where we're at now.
 *
 * <p/>
 * See {@link GrammaticalRelation} for details of fields and matching.
 * <p/>
 *
 * If using LexicalizedParser, it should be run with the
 * <code>-retainTmpSubcategories</code> option and one of the
 * <code>-splitTMP</code> options (e.g., <code>-splitTMP 1</code>) in order to
 * get these temporal NP dependencies right!
 * <p/>
 * <i>Implementation note: </i> To add a new grammatical relation:
 * <ul>
 * <li> Governor nodes of the grammatical relations should be the lowest ones.</li>
 * <li> Check the semantic head rules in SemanticHeadFinder and
 * ModCollinsHeadFinder, both in the trees package.</li>
 * <li> Create and define the GrammaticalRelation similarly to the others.</li>
 * <li> Add it to the <code>values</code> array at the end of the file.</li>
 * </ul>
 *
 *
 * @author Bill MacCartney
 * @author Marie-Catherine de Marneffe
 * @author Christopher Manning
 * @author Galen Andrew (refactoring English-specific stuff)
 * @see GrammaticalStructure
 * @see GrammaticalRelation
 * @see EnglishGrammaticalStructure
 */

public class EnglishGrammaticalRelations {

  public static List<GrammaticalRelation> values() {
    return Collections.unmodifiableList(Arrays.asList(values));
  }

  /**
   * The "predicate" grammatical relation.  The predicate of a
   * clause is the main VP of that clause; the predicate of a
   * subject is the predicate of the clause to which the subject
   * belongs.<p>
   * <p/>
   * Example: <br/>
   * "Reagan died" &rarr; <code>pred</code>(Reagan, died)
   */
  public static final GrammaticalRelation PREDICATE = new GrammaticalRelation("pred", "predicate", DEPENDENT, "S|SINV", new String[]{"S < VP=target"});

  /**
   * The "auxiliary" grammatical relation.  An auxiliary of a clause is a
   * non-main verb of the clause.<p>
   * <p/>
   * Example: <br/>
   * "Reagan has died" &rarr; <code>aux</code>(died, has)
   */
  public static final GrammaticalRelation AUX_MODIFIER = new GrammaticalRelation("aux", "auxiliary", DEPENDENT, "VP|SQ|SINV", new String[]{"VP < VP < /^(?:TO|MD|VB.*)$/=target", "SQ|SINV < (/^VB|MD/=target $++ /^(?:VP|ADJP)/)"});

  /**
   * The "passive auxiliary" grammatical relation.  A passive auxiliary of a
   * clause is a
   * non-main verb of the clause which contains the passive information.
   * <p/>
   * Example: <br/>
   * "Kennedy has been killed" &rarr; <code>auxpass</code>(killed, been)
   */
  public static final GrammaticalRelation AUX_PASSIVE_MODIFIER = new GrammaticalRelation("auxpass", "passive auxiliary", AUX_MODIFIER, "VP|SQ", new String[]{"VP < (/^(?:VB|AUXG?)/=target < /be|was|'s|is|are|were|been|being|am|Been|Being|WAS|IS|get|got|getting|gets|Get|gotten|becomes|become|became|felt|feels|feel|seems|seem|seemed|remains|remained|remain/) < (VP|ADJP < VBN|VBD)", "VP < (/^(?:VB|AUXG?)/=target < /be|was|'s|is|are|were|been|being|am|Been|Being|WAS|IS|get|got|getting|gets|Get|gotten|becomes|become|became|felt|feels|feel|seems|seem|seemed|remains|remained|remain/) < (VP|ADJP < (VP|ADJP < VBN|VBD) < CC)", "SQ < (/^(?:VB|AUX)/=target < /^(?:was|is|are|were|am|Was|Is|Are|Were|Am|WAS|IS|ARE|WERE|AM)$/ $++ (VP < /^VB[DN]$/))"});

  /**
   * The "copula" grammatical relation.  A copula is the relation between
   * the complement of a copular verb and the copular verb.<p>
   * <p/>
   * Examples: <br/>
   * "Bill is big" &rarr; <code>cop</code>(big, is) <br/>
   * "Bill is an honest man" &rarr; <code>cop</code>(man, is)
   */
  public static final GrammaticalRelation COPULA = new GrammaticalRelation("cop", "copula", AUX_MODIFIER, "VP|SQ", new String[]{"VP < (/^VB/=target < /(?i)^(?:am|'m|are|'re|is|'s|be|being|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/ [ $++ (ADJP|NP !< VBN) | $++ (S <: (ADJP < JJ)) ] )", "SQ <, (/^VB/=target < /(?i)^(am|are|is|was|were)$/) !$ /^WH*/" });

  /**
   * The "conjunct" grammatical relation.  A conjunct is the relation between
   * two elements connected by a conjunction word.<p>
   * <p/>
   * Example: <br/>
   * "Bill is big and honest" &rarr; <code>conj</code>(big, honest)
   */
  public static final GrammaticalRelation CONJUNCT = new GrammaticalRelation("conj", "conjunct", DEPENDENT, "VP|NP|ADJP|PP|QP|ADVP|UCP|S|NX|SBAR",
    new String[]{
      // remember conjunction can be left or right headed....
      // non-parenthetical in suitable phrase with conjunction to left
      "VP|ADJP|PP|QP|NP|ADVP|UCP|S|NX|SBAR < (CC|CONJP $+ !PRN=target) !<, CC",
      // non-parenthetical in suitable phrase with conj then adverb to left
      "VP|ADJP|PP|NP|ADVP|UCP|S|NX|SBAR < (CC|CONJP $+ (ADVP $+ !PRN=target))",
      // to the right of a comma
      "VP|ADJP|PP|NP|ADVP|UCP|S|NX|SBAR < CC|CONJP < (/^,$/ $+ /^(A|N|V|PP|PRP|J|W|R|S)/=target)",
      // to the right of a parenthetical
      "VP|ADJP|PP|NP|ADVP|UCP|S|NX|SBAR < CC|CONJP < (PRN $+ /^(A|N|V|PP|PRP|J|W|R|S)/=target)",
      // to the left of a comma for at least NX
      "NX < CC|CONJP < (/^,$/ $- /^(A|N|V|PP|PRP|J|W|R|S)/=target)"
    });

  /**
   * The "coordination" grammatical relation.  A coordination is the relation
   * between an element and a conjunction.
   * <p/>
   * Example: <br/>
   * "Bill is big and honest." &rarr; <code>cc</code>(big, and)
   */
  public static final GrammaticalRelation COORDINATION = new GrammaticalRelation("cc", "coordination", DEPENDENT, "S|VP|NP|ADJP|PP|QP|ADVP|UCP|NX|SBAR", new String[]{"S|VP|NP|QP|ADJP|PP|ADVP|UCP|NX|SBAR < (CC|CONJP=target !< /either|neither|both|Either|Neither|Both/)"});

  /**
   * The "punctuation" grammatical relation.  This is used for any piece of
   * punctuation in a clause, if punctuation is being retained in the
   * typed dependencies.
   * <p/>
   * Example: <br/>
   * "Go home!" &rarr; <code>punct</code>(Go, !)
   */
  public static final GrammaticalRelation PUNCTUATION = new GrammaticalRelation("punct", "punctuation", DEPENDENT, "S|NP|VP|SQ|PRN|SINV|SBAR|UCP", new String[]{"__ < /^(?:\\.|:|,|''|``|-LRB-|-RRB-)$/=target"});

  /**
   * The "argument" grammatical relation.  An argument of a VP is a
   * subject or complement of that VP; an argument of a clause is
   * an argument of the VP which is the predicate of that
   * clause.<p>
   * <p/>
   * Example: <br/>
   * "Clinton defeated Dole" &rarr; <code>arg</code>(defeated, Clinton), <code>arg</code>(defeated, Dole)
   */
  public static final GrammaticalRelation ARGUMENT = new GrammaticalRelation("arg", "argument", DEPENDENT, null, new String[0]);

  /**
   * The "subject" grammatical relation.  The subject of a VP is
   * the noun or clause that performs or experiences the VP; the
   * subject of a clause is the subject of the VP which is the
   * predicate of that clause.<p>
   * <p/>
   * Examples: <br/>
   * "Clinton defeated Dole" &rarr; <code>subj</code>(defeated, Clinton) <br/>
   * "What she said is untrue" &rarr; <code>subj</code>(is, What she said)
   */
  public static final GrammaticalRelation SUBJECT = new GrammaticalRelation("subj", "subject", ARGUMENT, null, new String[0]);

  /**
   * The "nominal subject" grammatical relation.  A nominal subject is
   * a subject which is an noun phrase.<p>
   * <p/>
   * Example: <br/>
   * "Clinton defeated Dole" &rarr; <code>nsubj</code>(defeated, Clinton)
   */
  public static final GrammaticalRelation NOMINAL_SUBJECT = new GrammaticalRelation("nsubj", "nominal subject", SUBJECT, "S|SQ|SBARQ|SINV|SBAR", new String[]{"S < ((NP|WHNP=target !< EX !< (/^NN/ < (/^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/))) $++ VP)",
                                                                                                                                                              "S < ( NP=target < (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/) !$++ NP $++VP)",
                                                                                                                                                              "SQ < ((NP=target !< EX) $++ VP)", "SQ < ((NP=target !< EX) $- /^VB/ !$++ VP)", "SQ < ((NP=target !< EX) $- (RB $- /^VB/) ![$++ VP])",
                                                                                                                                                              "SBARQ < WHNP=target < (SQ < (VP ![$-- NP]))", "SBARQ < (SQ=target < /^VB/ !< VP)",
                                                                                                                                                              "SINV < (VP|VBZ|VBD $+ /^NP|WHNP$/=target)", // matches subj in SINV
                                                                                                                                                              "S < (NP=target $+ NP|ADJP) > VP", //matches subj in xcomp like "He considered him a friend"
                                                                                                                                                              "SBAR <, WHNP=target < (S < (VP !$-- NP) !< SBAR)", // matches subj in relative clauses
                                                                                                                                                              "SBAR !< WHNP < (S !< (NP $++ VP)) > (VP > (S $- WHNP=target))", // matches subj in relative clauses
                                                                                                                                                              "SQ < ((NP < EX) $++ NP=target)", // matches subj in existential "there" SQ
                                                                                                                                                              "S < (NP < EX) < (VP < NP=target)"}); // matches subj in existential "there" S

  /**
   * The "nominal passive subject" grammatical relation.  A nominal passive
   * subject is a subject of a passive which is an noun phrase.<p>
   * <p/>
   * Example: <br/>
   * "Dole was defeated by Clinton" &rarr; <code>nsubjpass</code>(defeated, Dole)
   */
  public static final GrammaticalRelation NOMINAL_PASSIVE_SUBJECT = new GrammaticalRelation("nsubjpass", "nominal passive subject", NOMINAL_SUBJECT, "S|VP", new String[]{"S < /^NP|WHNP$/=target < (VP|SQ < (VP < VBN|VBD) < (/^(VB|AUX)/ < /be|was|is|are|were|been|being|'s|'re|'m|am|Been|Being|WAS|IS|get|got|getting|gets|Get|gotten|becomes|become|became|felt|feels|feel|seems|seem|seemed|remains|remained|remain/))", "S < /^(NP|WHNP)$/=target < (VP|SQ <+(VP) (VP < VBN|VBD > (VP < (/^(VB|AUX)/ < /be|was|is|are|were|been|being|'s|'re|'m|am|Been|Being|WAS|IS|get|got|getting|gets|Get|gotten|becomes|become|became|felt|feels|feel|seems|seem|seemed|remains|remained|remain/))))",
                                                                                                                                                                          /*"VP <, VBN > NP=target"*/});

  /**
   * The "clausal subject" grammatical relation.  A clausal subject is
   * a subject which is a clause.<p>
   * <p/>
   * Examples: (subject is "what she said" in both examples) <br/>
   * "What she said makes sense" &rarr; <code>csubj</code>(makes, said) <br/>
   * "What she said is untrue" &rarr; <code>csubj</code>(untrue, said)
   */
  public static final GrammaticalRelation CLAUSAL_SUBJECT = new GrammaticalRelation("csubj", "clausal subject", SUBJECT, "S", new String[]{"S < (SBAR|S=target !$+ /^,$/ $++ (VP !$-- NP))"});


  /**
     * The "clausal passive subject" grammatical relation.  A clausal passive subject is
     * a subject of a passive verb which is a clause.<p>
     * <p/>
     * Example: (subject is "that she lied") <br/>
     * "That she lied was suspected by everyone" &rarr; <code>csubjpass</code>(suspected, lied)
     */
    public static final GrammaticalRelation CLAUSAL_PASSIVE_SUBJECT = new GrammaticalRelation("csubjpass", "clausal subject", CLAUSAL_SUBJECT, "S", new String[]{"S < (SBAR|S=target !$+ /^,$/ $++ (VP < (VP < VBN|VBD) < (/^(VB|AUX)/ < /be|was|is|are|were|been|being|'s|'re|'m|am|Been|Being|WAS|IS|get|got|getting|gets|Get|gotten|becomes|become|became|felt|feels|feel|seems|seem|seemed|remains|remained|remain/) !$-- NP))",
                                                                                                                                                                 "S < (SBAR|S=target !$+ /^,$/ $++ (VP <+(VP) (VP < VBN|VBD > (VP < (/^(VB|AUX)/ < /be|was|is|are|were|been|being|'s|'re|'m|am|Been|Being|WAS|IS|get|got|getting|gets|Get|gotten|becomes|become|became|felt|feels|feel|seems|seem|seemed|remains|remained|remain/))) !$-- NP))"});


  /**
   * The "complement" grammatical relation.  A complement of a VP
   * is any object (direct or indirect) of that VP, or a clause or
   * adjectival phrase which functions like an object; a complement
   * of a clause is an complement of the VP which is the predicate
   * of that clause.<p>
   * <p/>
   * Examples: <br/>
   * "She gave me a raise" &rarr;
   * <code>comp</code>(gave, me),
   * <code>comp</code>(gave, a raise) <br/>
   * "I like to swim" &rarr;
   * <code>comp</code>(like, to swim)
   */
  public static final GrammaticalRelation COMPLEMENT = new GrammaticalRelation("comp", "complement", ARGUMENT, null, new String[0]);

  /**
   * The "object" grammatical relation.  An object of a VP
   * is any direct object or indirect object of that VP; an object
   * of a clause is an object of the VP which is the predicate
   * of that clause.<p>
   * <p/>
   * Examples: <br/>
   * "She gave me a raise" &rarr;
   * <code>obj</code>(gave, me),
   * <code>obj</code>(gave, raise)
   */
  public static final GrammaticalRelation OBJECT = new GrammaticalRelation("obj", "object", COMPLEMENT, null, new String[0]);

  /**
   * The "direct object" grammatical relation.  The direct object
   * of a VP is the noun phrase which is the (accusative) object of
   * the verb; the direct object of a clause is the direct object of the VP
   * which is the predicate of that clause.<p>
   * <p/>
   * Example: <br/>
   * "She gave me a raise" &rarr;
   * <code>dobj</code>(gave, raise)
   */
  public static final GrammaticalRelation DIRECT_OBJECT = new GrammaticalRelation("dobj", "direct object", OBJECT, "SBARQ|VP|SBAR",
                                                                                  new String[]{"VP < (NP $+ (/^NP|WHNP$/=target !< (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/))) !<(/^VB/ < /^(am|is|are|being|Being|be|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/) ", // case with an iobj before
                                                                                               "VP < (NP < (NP $+ (/^NP|WHNP$/=target !< (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/))))!< (/^VB/ < /^(am|is|are|be|being|Being|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/)",
                                                                                               "VP !<(/^VB/ < /^(am|is|are|be|being|Being|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/) < (/^(NP|WHNP)$/=target !< (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/) !$+ NP)",
                                                                                               "VP !<(/^VB/ < /^(am|is|are|be|being|Being|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/) < (/^(NP|WHNP)$/=target $+ NP-TMP)", // match "give it next week"
                                                                                               "VP !<(/^VB/ < /^(am|is|are|be|being|Being|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/) < (/^(NP|WHNP)$/=target $+ (NP < (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/)))",
                                                                                               "SBARQ <, (WHNP=target !< WRB) << (VP !< (S < (VP < TO)) $-- NP)",
                                                                                               "SBAR <, (WHNP=target !< WRB) < (S < NP < (VP !< SBAR !< (S < (VP < TO))))", // matches direct object in relative clauses "I saw the book that you bought"
                                                                                               "SBAR !< WHNP < (S < (NP $++ (VP !$++ NP))) > (VP > (S < NP $- WHNP=target))", // matches direct object in relative clauses "I saw the book that you said you bought"
                                                                                               // we now don't match "VBG > PP $+ NP=target", since it seems better to CM to regard these quasi preposition uses (like "including soya") as prepositions rather than verbs with objects -- that's certainly what the phrase structure at least suggests in the PTB.  They're now matched as pobj
                                                                                  });

  /**
   * The "indirect object" grammatical relation.  The indirect
   * object of a VP is the noun phrase which is the (dative) object
   * of the verb; the indirect object of a clause is the indirect
   * object of the VP which is the predicate of that clause.
   * <p/>
   * Example:  <br/>
   * "She gave me a raise" &rarr;
   * <code>iobj</code>(gave, me)
   */
  public static final GrammaticalRelation INDIRECT_OBJECT = new GrammaticalRelation("iobj", "indirect object", OBJECT, "VP", new String[]{"VP <(NP=target !< (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/) $+ (NP !< (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/)))", "VP < (NP=target < (NP $++ NP !$ CC !$ CONJP !$ /^,$/ !$++ /^:$/))"});

  /**
   * The "prepositional object" grammatical relation.  The object of a
   * preposition is the head of a noun phrase following the preposition.
   * (The preposition in turn may be modifying a noun, verb, etc.)
   * We here define cases of VBG quasi-prepositions like "including",
   * "concerning", etc. as instances of pobj.
   * <p/>
   * Example: <br/>
   * "I sat on the chair" &rarr;
   * <code>pobj</code>(on, chair)
   */
  public static final GrammaticalRelation PREPOSITIONAL_OBJECT = new GrammaticalRelation("pobj", "prepositional object", OBJECT, "^PP(?:-TMP)?$", new String[]{"/^PP(?:-TMP)?$/ < /^IN|VBG|TO/ < /^NP(?:-TMP)?$/=target", "/^PP(?:-TMP)?$/ < (/^IN|VBG|TO/ $+ (ADVP=target < (ADVP < /^NP(?:-TMP)?$/)))"});

  /**
   * The "prepositional complement" grammatical relation. The prepositional complement of
   * a preposition is the head of a sentence following the preposition.
   * <p/>
   * Examples: <br/>
   * "We have no useful information on whether users are at risk" &arr;
   * <code>pcomp</code>(on, are) <br/>
   * "They heard about you missing classes." &arr;
   * <code>pcomp</code>(about, missing)
   *
   */
  public static final GrammaticalRelation PREPOSITIONAL_COMPLEMENT = new GrammaticalRelation("pcomp", "prepositional complement", OBJECT, "^PP(?:-TMP)?$", new String[]{"/^PP(?:-TMP)?$/ < (IN $+ SBAR|S=target)"});

  /**
   * The "attributive" grammatical relation. The attributive is the complement of a
   * verb such as "to be, to seem, to appear".
   *
   */
  public static final GrammaticalRelation ATTRIBUTIVE = new GrammaticalRelation("attr", "attributive", COMPLEMENT, "VP|SBARQ|SQ", new String[]{"VP !$ (NP < EX) < NP=target <(/^VB/ < /^(am|is|are|be|being|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/)", "SBARQ < (WHNP=target $+ (SQ < (/^VB/ < /^(am|is|are|'s|'re|'m|was|were|seem|seems|seemed|appear|appears|appeared|stay|stays|stayed|remain|remains|remained|resemble|resembles|resembled|become|becomes|became)$/ !$++ (VP < VBG))))", // "What is that?"
                                                                                                                                               "SQ <, (/^VB/ < /^(Am|am|Is|is|Are|are|be|being|'s|'re|'m|Was|was|Were|were)$/) < (NP=target $-- (NP !< EX))"});  //"Is he the man?"

  /**
   * The "clausal complement" grammatical relation.  A clausal
   * complement of a VP or an ADJP is a clause with internal subject
   * which functions like an object of the verb or of the adjective;
   * a clausal complement of a clause is the clausal
   * complement of the VP or of the ADJP which is the predicate of that
   * clause.  Such clausal complements are usually finite (though there
   * are occasional remnant English subjunctives).<p>
   * <p/>
   * Example: <br/>
   * "He says that you like to swim" &rarr;
   * <code>ccomp</code>(says, like) <br/>
   * "I am certain that he did it" &rarr;
   * <code>ccomp</code>(certain, did)
   */

  public static final GrammaticalRelation CLAUSAL_COMPLEMENT = new GrammaticalRelation("ccomp", "clausal complement", COMPLEMENT, "VP|SINV|S|ADJP", new String[]{// note if you add more words in the pattern, be sure to add them in the ADV_CLAUSE_MODIFIER too!
    "VP < (S=target < (VP !<, TO|VBG) !$-- NP)", "VP < (SBAR=target < (S <+(S) VP) <, (IN|DT < /^(that|whether)$/))","VP < (SBAR=target < (S < VP) !$-- NP !<, IN)", "S|SINV < (S|SBARQ=target $+ /^(,|.|'')$/ !$- /^:|CC$/ !< (VP < TO|VBG))",// to find "...", he said or "...?" he asked.
    "ADJP < (SBAR=target < (S < VP))",
    "S <, (SBAR=target <, (IN < /^([Tt]hat|[Ww]hether)$/) !$+ VP)"});// That ... he know

  /**
   * The "xclausal complement" grammatical relation.  An xcomp
   * complement of a VP or an ADJP is a clausal complement with an external
   * subject.  These xcomps are always non-finite.
   * (Only "TO-clause" are recognized.)
   * <p>
   * <p/>
   * Examples: <br/>
   * "I like to swim" &rarr;
   * <code>xcomp</code>(like, swim) <br/>
   * "I am ready to leave" &rarr;
   * <code>xcomp</code>(ready, leave)
   */
  public static final GrammaticalRelation XCLAUSAL_COMPLEMENT = new GrammaticalRelation("xcomp", "xclausal complement", COMPLEMENT, "VP|ADJP", new String[]{"VP !> (VP < (VB < be)) < (S=target !$- (NN < /^order$/) < (VP < TO))", "ADJP < (S=target <, (VP <, TO))", "VP < (S=target !$- (NN < /^order$/) < (NP $+ NP|ADJP))", "VP < (/^VB/ $+ (VP=target < VB < NP))", // to find "help sustain ...
                                                                                                                                                            "VP !> (VP < (VB < be)) < (SBAR=target < (S !$- (NN < /^order$/) < (VP < TO)))", "VP > VP < (S=target !$- (NN < /^order$/) <: NP)"
  });

  /**
   * The "complementizer" grammatical relation.  A
   * complementizer of a clausal complement is the word introducing it.
   * <p/>
   * <p/>
   * Example: <br/>
   * "He says that you like to swim" &rarr;
   * <code>complm</code>(like, that)
   */
  public static final GrammaticalRelation COMPLEMENTIZER = new GrammaticalRelation("complm", "complementizer", COMPLEMENT,
          "SBAR",
          new String[]{
                  "SBAR <, (IN|DT=target < /^(that|whether)$/) $-- /^VB/",
                  "SBAR <, (IN|DT=target < /^(that|whether)$/) $- NP",
                  "SBAR <, (IN|DT=target < /^(that|whether)$/) > ADJP|PP",
                  "SBAR <, (IN|DT=target < /^(That|Whether)$/)"
          });

  /**
   * The "marker" grammatical relation.  A
   * marker of an adverbial clausal complement is the word introducing it.
   * <p/>
   * Example: <br/>
   * "U.S. forces have been engaged in intense fighting after insurgents launched simultaneous attacks" &rarr;
   * <code>mark</code>(launched, after)
   */
  public static final GrammaticalRelation MARKER = new GrammaticalRelation("mark", "marker", COMPLEMENT, "^SBAR(?:-TMP)?$", new String[]{"/^SBAR(?:-TMP)?$/ <, (IN=target !< /^([Tt]hat|[Ww]hether)$/) < S"});


  /**
   * The "relative" grammatical relation.  A
   * relative of a relative clause is the head word of the WH-phrase
   * introducing it.
   * <p/>
   * <p/>
   * Examples: <br/>
   * "I saw the man you love" &rarr;
   * <code>rel</code>(love, that) <br/>
   * "I saw the man whose wife you love" &rarr;
   * <code>rel</code>(love, wife) <br/>
   */
  public static final GrammaticalRelation RELATIVE = new GrammaticalRelation("rel", "relative", COMPLEMENT, "SBAR", new String[]{"SBAR <, /^WH/=target > NP"});

  /**
   * The "referent" grammatical relation.  A
   * referent of NP is a relative word introducing a relative clause modifying the NP.
   * <p/>
   * Example: <br/>
   * "I saw the book which you bought" &rarr;
   * <code>ref</code>(book, which)
   */
  public static final GrammaticalRelation REFERENT = new GrammaticalRelation("ref", "referent", DEPENDENT, "NP", new String[]{"NP $+ (SBAR < (WHNP=target !< /^WP\\$/)) > NP", "NP $+ (SBAR < (WHPP < (WHNP=target !< /^WP\\$/))) > NP", "NP $+ (/^(,|PP|PRN)$/ $+ (SBAR < (WHNP=target !< /^WP\\$/)))", "NP $+ (/^(,|PP|PRN)$/ $+ (SBAR < (WHPP < (WHNP=target !< /^WP\\$/)))) > NP"// to find referent for "the man, who I trust, ..." as well as referent in structure such as NP PP SBAR
  });

  // !< /^WP\$/ is added to prevent something like "whose wife" to get a referent between the antecedent and "wife"
  // which is the head of the WHNP

  /**
   * The "expletive" grammatical relation.
   * This relation captures an existential there.
   * <p/>
   * <p/>
   * Example: <br/>
   * "There is a statue in the corner" &rarr;
   * <code>expl</code>(is, there)
   */
  public static final GrammaticalRelation EXPLETIVE = new GrammaticalRelation("expl", "expletive", DEPENDENT, "S|SQ", new String[]{"S|SQ < (NP=target < EX)"});


  /**
   * The "adjectival complement" grammatical relation.  An
   * adjectival complement of a VP is a adjectival phrase which
   * functions like an object of the verb; an adjectival complement
   * of a clause is the adjectival complement of the VP which is
   * the predicate of that clause.<p>
   * <p/>
   * Example: <br/>
   * "She looks very beautiful" &rarr;
   * <code>acomp</code>(looks, very beautiful)
   */
  public static final GrammaticalRelation ADJECTIVAL_COMPLEMENT = new GrammaticalRelation("acomp", "adjectival complement", COMPLEMENT, "VP", new String[]{"VP < (ADJP=target !$-- NP)"});

  /**
   * The "modifier" grammatical relation.  A modifier of a VP is
   * any constituent that serves to modify the meaning of the VP
   * (but is not an <code>ARGUMENT</code> of that
   * VP); a modifier of a clause is an modifier of the VP which is
   * the predicate of that clause.<p>
   * <p/>
   * Examples: <br/>
   * "Last night, I swam in the pool" &rarr;
   * <code>mod</code>(swam, in the pool),
   * <code>mod</code>(swam, last night)
   */
  public static final GrammaticalRelation MODIFIER = new GrammaticalRelation("mod", "modifier", DEPENDENT, null, new String[0]);


  /**
   * The "adverbial clause modifier" grammatical relation.  An adverbial clause
   * modifier of a VP is a clause modifying the verb (temporal clauses, consequences, conditional clauses, etc.)
   * <p/>
   * Examples: <br/>
   * "The accident happened as the night was falling" &rarr;
   * <code>advcl</code>(happened, falling) <br/>
   * "If you know who did it, you should tell the teacher" &rarr;
   * <code>advcl</code>(tell, know)
   */
  public static final GrammaticalRelation ADV_CLAUSE_MODIFIER = new GrammaticalRelation("advcl", "adverbial clause modifier", MODIFIER, "VP|S|SQ", new String[]{"VP < (/^SBAR(?:-TMP)?$/=target <, (IN !< /^([Tt]hat|[Ww]hether)$/ !$+ (NN < /^order$/)))", "S|SQ <, (/^SBAR(?:-TMP)?$/=target <, (IN !< /^([Tt]hat|[Ww]hether)$/ !$+ (NN < /^order$/)) !$+ VP)",
                                                                                                                                                             "S|SQ <, (/^SBAR(?:-TMP)?$/=target <2 (IN !< /^([Tt]hat|[Ww]hether)$/ !$+ (NN < /^order$/)))", // to get "rather than"
                                                                                                                                                                "S|SQ <, (SBAR=target <, (WHADVP|WHNP < WRB))", "S|SQ <, (PP=target <, RB)"});
  // !$+ (NN < /^order$/) has been added so that "in order to" is not marked as an advcl
  
  /**
   * The "purpose clause modifier" grammatical relation.  A purpose clause
   * modifier of a VP is a clause headed by "(in order) to" specifying a
   * purpose.  Note: at present we only recognize ones that have
   * "in order to" as otherwise we can't give our surface representations
   * distinguish these from xcomp's. We can also recognize "to" clauses
   * introduced by "be VBN".
   * <p/>
   * Example: <br/>
   * "He talked to the president in order to secure the account" &rarr;
   * <code>purpcl</code>(talked, secure)
   */
  public static final GrammaticalRelation PURPOSE_CLAUSE_MODIFIER = new GrammaticalRelation("purpcl", "purpose clause modifier", MODIFIER, "VP", new String[]{"VP < (/^SBAR/=target < (IN < in) < (NN < order) < (S < (VP < TO)))", "VP > (VP < (VB < be)) < (S=target < (VP < TO|VBG) !$-- NP)"});

  /**
   * The "temporal modifier" grammatical relation.  A temporal
   * modifier of a VP or an ADJP is any constituent that serves to modify the
   * meaning of the VP or the ADJP by specifying a time; a temporal modifier of a
   * clause is an temporal modifier of the VP which is the
   * predicate of that clause.<p>
   * <p/>
   * Example: <br/>
   * "Last night, I swam in the pool" &rarr;
   * <code>tmod</code>(swam, night)
   */
  public static final GrammaticalRelation TEMPORAL_MODIFIER = new GrammaticalRelation("tmod", "temporal modifier", MODIFIER, "VP|S|ADJP", new String[]{"VP|ADJP < /^NP-TMP$/=target",
          "VP < (NP=target < (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/))",
          "S < (/^NP-TMP$/=target $++ (NP $++ VP))",
          "S < (NP=target < (/^NN/ < /^Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|years?|months?|weeks?|days?|mornings?|evenings?|January|February|March|April|May|June|July|August|September|October|November|December|[Tt]oday|[Yy]esterday|[Tt]omorrow|[Ss]pring|[Ss]ummer|[Ff]all|[Aa]utumn|[Ww]inter$/) $++ (NP $++ VP))"});

  /**
   * The "relative clause modifier" grammatical relation.  A relative clause
   * modifier of an NP is a relative clause modifying the NP.  The link
   * points from the head noun of the NP to the head of the relative clause,
   * normally a verb.
   * <p/>
   * <p/>
   * Examples: <br/>
   * "I saw the man you love" &rarr;
   * <code>rcmod</code>(man, love)  <br/>
   * "I saw the book which you bought" &rarr;
   * <code>rcmod</code>(book, bought)
   */
  public static final GrammaticalRelation RELATIVE_CLAUSE_MODIFIER = new GrammaticalRelation("rcmod", "relative clause modifier", MODIFIER, "NP", new String[]{"NP $++ (SBAR=target < WHPP|WHNP) > NP",
          "NP $++ (SBAR=target <: S) > NP",
          "NP $++ (SBAR=target < (WHADVP < (WRB </^(where|why)/))) > NP", // this pattern is restricted to where and why because "when" is usually incorrectly parsed: temporal clauses are put inside the NP; 2nd is for case of relative clauses with no relativizer (it doesn't distinguish whether actually gapped).
          "NP $++ RRC=target"});
  /**
   * The "adjectival modifier" grammatical relation.  An adjectival
   * modifier of an NP is any adjectival phrase that serves to modify
   * the meaning of the NP.<p>
   * <p/>
   * Example: <br/>
   * "Sam eats red meat" &rarr;
   * <code>amod</code>(meat, red)
   */
  public static final GrammaticalRelation ADJECTIVAL_MODIFIER = new GrammaticalRelation("amod", "adjectival modifier", MODIFIER, "^NP(?:-TMP|-ADV)?|NX|WHNP$", new String[]{"/^NP(?:-TMP|-ADV)?|NX|WHNP$/ < (ADJP|WHADJP|JJ|JJR|JJS|VBN|VBG|VBD=target !$- CC)"});

  /**
   * The "numeric modifier" grammatical relation.  A numeric
   * modifier of an NP is any number phrase that serves to modify
   * the meaning of the NP.<p>
   * <p/>
   * Example: <br/>
   * "Sam eats 3 sheep" &rarr;
   * <code>num</code>(sheep, 3)
   */
  public static final GrammaticalRelation NUMERIC_MODIFIER = new GrammaticalRelation("num", "numeric modifier", MODIFIER,
          "NP(?:-TMP|-ADV)?",
          new String[]{
                  "/^NP(?:-TMP|-ADV)?$/ < (CD|QP=target !$- CC)",
                  "/^NP(?:-TMP|-ADV)?$/ < (ADJP=target <: QP)"
          });

  /**
   * The "compound number modifier" grammatical relation.  A compound number
   * modifier is a part of a number phrase or currency amount.
   * <p/>
   * Example: <br/>
   * "I lost $ 3.2 billion" &rarr;
   * <code>number</code>($, billion)
   */
  public static final GrammaticalRelation NUMBER_MODIFIER = new GrammaticalRelation("number", "compound number modifier", MODIFIER,
          "QP",
          new String[]{
                  "QP < (CD=target !$- CC)"
          });

  /**
   * The "quantifier phrase modifier" grammatical relation.  A quantifier
   * modifier is an element modifying the head of a QP consitutent.
   * <p/>
   * Example: <br/>
   * "About 200 people came to the party" &rarr;
   * <code>quantmod</code>(200, About)
   */
  public static final GrammaticalRelation QUANTIFIER_MODIFIER = new GrammaticalRelation("quantmod", "quantifier modifier", MODIFIER,
         "QP", new String[] {"QP < /^IN|RB|DT|JJ|XS$/=target"});

  /**
   * The "noun compound modifier" grammatical relation.  A noun compound
   * modifier of an NP is any noun that serves to modify the head noun.
   * Note that this has all nouns modify the rightmost a la Penn headship
   * rules.  There is no intelligent noun compound analysis. <p>
   * <p/>
   * Example: <br/>
   * "Oil price futures" &rarr;
   * <code>nn</code>(futures, oil),
   * <code>nn</code>(futures, price)
   */
  public static final GrammaticalRelation NOUN_COMPOUND_MODIFIER = new GrammaticalRelation("nn", "nn modifier", MODIFIER, "^NP(?:-TMP|-ADV)?$", new String[]{"/^NP(?:-TMP|-ADV)?$/ < (NP|NN|NNS|NNP|NNPS|FW=target $++ NN|NNS|NNP|NNPS|FW|CD !<- POS !$- /^,$/ )", "/^NP(?:-TMP|-ADV)?$/ < (NP|NN|NNS|NNP|NNPS|FW=target !<- POS $+ JJ|JJR|JJS) <# NN|NNS|NNP|NNPS !<- POS"});

  /**
   * The "appositional modifier" grammatical relation.  An appositional
   * modifier of an NP is an NP that serves to modify
   * the meaning of the NP.  It includes parenthesized examples
   * <p/>
   * Examples: <br/>
   * "Sam, my brother, eats red meat" &rarr;
   * <code>appos</code>(Sam, brother) <br/>
   * "Bill (John's cousin)" &rarr; <code>appos</code>(Bill, cousin)
   */
  public static final GrammaticalRelation APPOSITIONAL_MODIFIER = new GrammaticalRelation("appos", "appositional modifier", MODIFIER, "^NP(?:-TMP|-ADV)?$",
          new String[]{
                  "/^NP(?:-TMP|-ADV)?$/ < (NP=target $- /^,$/ $-- NP !$ CC|CONJP)",
                  "/^NP(?:-TMP|-ADV)?$/ < (PRN=target < (NP < /^NNS?|CD$/ $-- /^-LRB-$/ $+ /^-RRB-$/))",
                  "/^NP(?:-TMP|-ADV)?$/ < (NNP $+ (/^,$/ $+ NNP=target)) !< CC|CONJP"
          });
  // last pattern with NNP doesn't work because leftmost NNP is deemed head in a
  // structure like (NP (NNP Norway) (, ,) (NNP Verdens_Gang) (, ,))

  /**
   * The "abbreviation appositional modifier" grammatical relation.
   * An abbreviation modifier of an NP is an NP that serves to abbreviate
   * the NP.<p>
   * <p/>
   * Example: <br/>
   * "The Australian Broadcasting Corporation (ABC)" &rarr;
   * <code>abbrev</code>(Corporation, ABC)
   */
  public static final GrammaticalRelation ABBREVIATION_MODIFIER = new GrammaticalRelation("abbrev", "abbreviation modifier", APPOSITIONAL_MODIFIER, "^NP(?:-TMP|-ADV)?$", new String[]{"/^NP(?:-TMP|-ADV)?$/ < (PRN=target < (NP < NNP $- /^-LRB-$/ $+ /^-RRB-$/))", "/^NP(?:-TMP|-ADV)?$/ < (PRN=target < (NNP $- /^-LRB-$/ $+ /^-RRB-$/))"});

  /**
   * The "participial modifier" grammatical relation.  A participial
   * modifier of an NP or VP is a VP[part] that serves to modify
   * the meaning of the NP or VP.<p>
   * <p/>
   * Examples: <br/>
   * "truffles picked during the spring are tasty" &rarr;
   * <code>partmod</code>(truffles, picked) <br/>
   * "Bill picked Fred for the team demonstrating his incompetence" &rarr;
   * <code>partmod</code>(picked, demonstrating)
   */
  public static final GrammaticalRelation PARTICIPIAL_MODIFIER = new GrammaticalRelation("partmod", "participial modifier", MODIFIER, "^NP(?:-TMP|-ADV)?|VP|S$",
        new String[]{"/^NP(?:-TMP|-ADV)?$/ < (VP=target < VBG|VBN $-- NP)", "VP < (S=target !< NP < (VP < VBG))",
                     "/^NP(?:-TMP|-ADV)?$/ < (/^,$/ $+ (VP=target <, VBG|VBN))", // to get "MBUSA, headquarted ..."
                     "S <, (NP $+ (/^,$/ $+ (S=target < (VP <, VBG|VBN))))" // to get "John, knowing ..., announced "
                    });

  /**
   * The "infinitival modifier" grammatical relation.  A participial
   * modifier of an NP is an S/VP that serves to modify
   * the meaning of the NP.<p>
   * <p/>
   * Example: <br/>
   * "points to establish are ..." &rarr;
   * <code>infmod</code>(points, establish)
   */
  public static final GrammaticalRelation INFINITIVAL_MODIFIER = new GrammaticalRelation("infmod", "infinitival modifier", MODIFIER, "^NP(?:-TMP|-ADV)?$", new String[]{"/^NP(?:-[A-Z]+)?$/ < (S=target < (VP < TO) $-- /^NP|NNP?S?$/)"});

  /**
   * The "adverbial modifier" grammatical relation.  An adverbial
   * modifier of a word is an RB or ADVP that serves to modify
   * the meaning of the word.<p>
   * <p/>
   * Examples: <br/>
   * "genetically modified food" &rarr;
   * <code>advmod</code>(modified, genetically) <br/>
   * "less often" &rarr;
   * <code>advmod</code>(often, less)
   */
  public static final GrammaticalRelation ADVERBIAL_MODIFIER = new GrammaticalRelation("advmod", "adverbial modifier", MODIFIER,
          "VP|ADJP|WHADJP|ADVP|WHADVP|S|SBAR|SINV|SQ|SBARQ|XS|NP(?:-TMP|-ADV)?|RRC",
          new String[]{
                  "/^VP|ADJP|WHADJP|S|SBAR|SINV|SQ|XS|NP(?:-TMP|-ADV)?|RRC$/ < RB|RBR|RBS|WRB|ADVP|WHADVP=target",
                  // this next one avoids adverb conjunctions matching as advmod
                  "ADVP|WHADVP < RB|RBR|RBS|WRB|ADVP|WHADVP|JJ=target !< CC !< CONJP", // added JJ to cacth How long
                  "SBAR < (WHNP=target < WRB)",
                  "SBARQ <, WHADVP=target",
                  "XS < /^JJ$/=target" //this one gets "at least" advmod(at, least) or "fewer than" advmod(than, fewer)
          });

  /**
   * The "negation modifier" grammatical relation.  The negation modifier
   * is the relation between a negation word and the word it modifies.
   * <p/>
   * Examples: <br/>
   * "Bill is not a scientist" &rarr;
   * <code>neg</code>(scientist, not) <br/>
   * "Bill doesn't drive" &rarr;
   * <code>neg</code>(drive, n't)
   */
  public static final GrammaticalRelation NEGATION_MODIFIER = new GrammaticalRelation("neg", "negation modifier", ADVERBIAL_MODIFIER, "VP|ADJP|S|SBAR|SINV|SQ", new String[]{"VP|ADJP|SQ|S < (RB=target < /not|n't|never/)", "VP|ADJP|S|SBAR|SINV < (ADVP=target < (RB < /not|n't|never/))", "VP > SQ  $-- (RB=target < /not|n't|never/)"});

  /**
   * The "measure-phrase" grammatical relation. The measure-phrase is the relation between
   * the head of an ADJP/ADVP and the head of a measure-phrase modifying the ADJP/ADVP.
   * <p/>
   * Example: <br/>
   * "The director is 65 years old" &rarr;
   * <code>measure</code>(old, years)
   */
  public static final GrammaticalRelation MEASURE_PHRASE = new GrammaticalRelation("measure", "measure-phrase", MODIFIER, "ADJP|ADVP", new String[]{"ADJP <- JJ <, (NP=target !< NNP)", "ADVP|ADJP <# (/^JJ|IN$/ $- NP=target)"});


  /**
   * The "determiner" grammatical relation.
   * <p> <p/>
   * Examples: <br/>
   * "The man is here" &rarr; <code>det</code>(man,the) <br/>
   * "Which man do you prefer?" &rarr; <code>det</code>(man,which)
   */
  public static final GrammaticalRelation DETERMINER = new GrammaticalRelation("det", "determiner", MODIFIER, "^NP(?:-TMP|-ADV)?|WHNP", new String[]{"/^NP(?:-TMP|-ADV)?$/ < (DT=target !</both|either|neither/ !$- DT !$++ CC $++ /^N[NX]/)","/^NP(?:-TMP|-ADV)?$/ < (DT=target < /both|either|neither/ !$- DT !$++ CC $++ /^N[NX]/ !$++ (NP < CC))", "/^NP(?:-TMP|-ADV)?$/ < (DT=target !< /both|neither|either/ $++ CC $++ /^N[NX]/)","/^NP(?:-TMP|-ADV)?$/ < (DT=target $++ (/^JJ/ !$+ /^NN/) !$++CC)", "/^NP(?:-TMP|-ADV)?$/ < (RB=target $++ (/P?DT/ $+ /^NN/))", "WHNP < (NP $-- (WHNP=target < WDT))", "WHNP < (/^NN/ $-- WDT|WP=target)"});


  /**
     * The "predeterminer" grammatical relation.
     * <p> <p/>
     * Example: <br/>
     * "All the boys are here" &rarr; <code>predet</code>(boys,all)
     */
    public static final GrammaticalRelation PREDETERMINER = new GrammaticalRelation("predet", "predeterminer", MODIFIER, "^NP(?:-TMP|-ADV)?", new String[]{"/^NP(?:-TMP|-ADV)?$/ < (PDT|DT=target $+ /DT|PRP\\$/ $++ /^N[NX]/ !$++ CC)", "/^NP(?:-TMP|-ADV)?$/ < (PDT|DT=target $+ DT $++ (/^JJ/ !$+ /^NN/)) !$++ CC"});


  /**
       * The "preconjunct" grammatical relation.
       * <p> <p/>
       * Example: <br/>
       * "Both the boys and the girls are here" &rarr; <code>preconj</code>(boys,both)
       */
      public static final GrammaticalRelation PRECONJUNCT = new GrammaticalRelation("preconj", "preconjunct", MODIFIER, "S|VP|ADJP|PP|ADVP|UCP|NX|SBAR|^NP(?:-TMP|-ADV)?", new String[]{"/^NP(?:-TMP|-ADV)?|NX$/ < (PDT|CC=target < /both|neither|either/ $++ /^N[NX]/) $++ CC", "/^NP(?:-TMP|-ADV)?|NX$/ < (PDT|CC=target < /both|either|neither/ $++ (/^JJ/ !$+ /^NN/)) $++ CC", "/^NP(?:-TMP|-ADV)?|NX$/ < (PDT|CC|DT=target < /both|either|neither/ $++ CC)", "/^NP(?:-TMP|-ADV)?|NX$/ < (PDT|CC|DT=target </both|either|neither/) < (NP < CC)", "S|VP|ADJP|PP|ADVP|UCP|NX|SBAR < (PDT|DT|CC=target < /both|either|neither/ $++ CC)"});

  /**
   * The "possession" grammatical relation.<p>
   * </p>
   * Examples: <br/>
   * "their offices" &rarr;
   * <code>poss</code>(offices, their)<br/>
   * "Bill 's clothes" &rarr;
   * <code>poss</code>(clothes, Bill)
   */
  public static final GrammaticalRelation POSSESSION_MODIFIER = new GrammaticalRelation("poss", "possession modifier", MODIFIER, "^NP(?:-TMP|-ADV)?$", new String[]{"/^NP(?:-TMP|-ADV)?$/ < (/^PRP\\$/=target $++ /^NN/)", "/^NP(?:-TMP|-ADV)?$/ < (NP=target < POS)", "/^NP(?:-TMP|-ADV)?$/ < (NNS=target $+ (POS < /'/))"});


  /**
   * The "possessive" grammatical relation.<p>
   * </p>
   * Example: <br/>
   * "John's book" &rarr;
   * <code>possessive</code>(John, 's)
   */
  public static final GrammaticalRelation POSSESSIVE_MODIFIER = new GrammaticalRelation("possessive", "possessive modifier", MODIFIER, "^NP(?:-TMP|-ADV)?$", new String[]{"/^NP(?:-TMP|-ADV)?$/ <- POS=target"});

  /**
   * The "prepositional modifier" grammatical relation.  A prepositional
   * modifier of a verb, adjective, or noun is any prepositional phrase that serves to modify
   * the meaning of the verb, adjective, or noun.<p>
   * <p/>
   * Examples: <br/>
   * "I saw a cat in a hat" &rarr;
   * <code>prep</code>(cat, in) <br/>
   * "I saw a cat with a telescope" &rarr;
   * <code>prep</code>(saw, with) <br/>
   * "He is responsible for meals" &rarr;
   * <code>prep</code>(responsible, for)
   */
  public static final GrammaticalRelation PREPOSITIONAL_MODIFIER = new GrammaticalRelation("prep", "prepositional modifier", MODIFIER,
          "NP(?:-TMP|-ADV)?|VP|S|SINV|ADJP",
          new String[]{
                  "/^NP(?:-TMP|-ADV)?|VP|ADJP$/ < /^PP(?:-TMP)?$/=target",
                  "S|SINV < (/^PP(?:-TMP)?$/=target !< SBAR) < VP"
          });

  /**
   * The "phrasal verb particle" grammatical relation.  The "phrasal verb particle"
   * relation identifies phrasal verb.<p>
   * <p/>
   * Example: <br/>
   * "They shut down the station." &rarr;
   * <code>prt</code>(shut, down)
   */
  public static final GrammaticalRelation PHRASAL_VERB_PARTICLE = new GrammaticalRelation("prt", "phrasal verb particle", MODIFIER, "VP", new String[]{"VP < PRT=target"});


  /**
   * The "semantic dependent" grammatical relation has been
   * introduced as a supertype for the controlling subject relation.
   */
  public static final GrammaticalRelation SEMANTIC_DEPENDENT = new GrammaticalRelation("sdep", "semantic dependent", DEPENDENT, null, new String[0]);

  /**
   * The "controlling subject" grammatical relation.<p>
   * <p/>
   * Example: <br/>
   * "Tom likes to eat fish" &rarr;
   * <code>xsubj</code>(eat, Tom)
   */
  public static final GrammaticalRelation CONTROLLING_SUBJECT = new GrammaticalRelation("xsubj", "controlling subject", SEMANTIC_DEPENDENT, "VP", new String[]{"VP < TO > (S !$- NP !< NP !>> (VP < (VB < be)) >+(VP) (VP $-- NP=target))"});

  /**
   * The "agent" grammatical relation. The agent of a passive VP
   * is the complement introduced by "by" and doing the action.<p>
   * <p/>
   * Example: <br/>
   * "The man has been killed by the police" &rarr;
   * <code>agent</code>(killed, police)
   */
  public static final GrammaticalRelation AGENT = new GrammaticalRelation("agent", "agent", DEPENDENT, null, new String[0]);


  /** A list of GrammaticalRelation values.  New GrammaticalRelations must be
   *  added to this list (until we make this an enum!).
   *  The GR recognizers are tried in the order listed.  A taxonomic
   *  relationship trumps an ordering relationship, but otherwise, the first
   *  listed relation will appear in dependency output.  Known ordering
   *  constraints where both match include:
   *  <ul>
   *  <li>NUMERIC_MODIFIER &lt; ADJECTIVAL_MODIFIER
   *  </ul>
   */
  private static final GrammaticalRelation[] values = new GrammaticalRelation[]{GOVERNOR, DEPENDENT, PREDICATE, ATTRIBUTIVE, AUX_MODIFIER, AUX_PASSIVE_MODIFIER, COPULA, CONJUNCT, COORDINATION, PUNCTUATION, ARGUMENT, SUBJECT, NOMINAL_SUBJECT, NOMINAL_PASSIVE_SUBJECT, CLAUSAL_SUBJECT, CLAUSAL_PASSIVE_SUBJECT, COMPLEMENT, OBJECT, DIRECT_OBJECT, INDIRECT_OBJECT, PREPOSITIONAL_OBJECT, PREPOSITIONAL_COMPLEMENT, CLAUSAL_COMPLEMENT, XCLAUSAL_COMPLEMENT, COMPLEMENTIZER, MARKER, RELATIVE, REFERENT, EXPLETIVE, ADJECTIVAL_COMPLEMENT, MODIFIER, ADV_CLAUSE_MODIFIER, TEMPORAL_MODIFIER, RELATIVE_CLAUSE_MODIFIER, NUMERIC_MODIFIER, ADJECTIVAL_MODIFIER, NOUN_COMPOUND_MODIFIER, APPOSITIONAL_MODIFIER, ABBREVIATION_MODIFIER, PARTICIPIAL_MODIFIER, INFINITIVAL_MODIFIER, ADVERBIAL_MODIFIER, NEGATION_MODIFIER, DETERMINER, PREDETERMINER, PRECONJUNCT, POSSESSION_MODIFIER, POSSESSIVE_MODIFIER, PREPOSITIONAL_MODIFIER, PHRASAL_VERB_PARTICLE, SEMANTIC_DEPENDENT, CONTROLLING_SUBJECT, AGENT, NUMBER_MODIFIER, PURPOSE_CLAUSE_MODIFIER, QUANTIFIER_MODIFIER, MEASURE_PHRASE};
  //private static final GrammaticalRelation[] values = new GrammaticalRelation[]{GOVERNOR, DEPENDENT, PREDICATE, AUX_MODIFIER, AUX_PASSIVE_MODIFIER, CONJUNCT, COORDINATION, ARGUMENT, SUBJECT, NOMINAL_SUBJECT, NOMINAL_PASSIVE_SUBJECT, CLAUSAL_SUBJECT, COMPLEMENT, OBJECT, DIRECT_OBJECT, INDIRECT_OBJECT, CLAUSAL_COMPLEMENT, XCLAUSAL_COMPLEMENT, COMPLEMENTIZER, MARKER, RELATIVE, REFERENT, EXPLETIVE, ADJECTIVAL_COMPLEMENT, MODIFIER, ADV_CLAUSE_MODIFIER, TEMPORAL_MODIFIER, RELATIVE_CLAUSE_MODIFIER, ADJECTIVAL_MODIFIER, NUMERIC_MODIFIER, NOUN_COMPOUND_MODIFIER, APPOSITIONAL_MODIFIER, ABBREVIATION_MODIFIER, PARTICIPIAL_MODIFIER, INFINITIVAL_MODIFIER, ADVERBIAL_MODIFIER, NEGATION_MODIFIER, DETERMINER, POSSESSION_MODIFIER, POSSESSIVE_MODIFIER, PREPOSITIONAL_MODIFIER, PHRASAL_VERB_PARTICLE, SEMANTIC_DEPENDENT, CONTROLLING_SUBJECT, AGENT, NUMBER_MODIFIER, PURPOSE_CLAUSE_MODIFIER};

  // the exhaustive list of conjunction relations
  private static final Map<String, GrammaticalRelation> conjs = new HashMap<String, GrammaticalRelation>();

  public static Collection<GrammaticalRelation> getConjs() { return conjs.values(); }

  /**
   *  The "conj" grammatical relation. Used to collapse conjunct relations.
   *  They will be turned into conj_word, where "word" is a conjunction.
   *
   *  @param conjunctionString The conjunction to make a GrammaticalRelation out of
   *  @return A grammatical relation for this conjunction
   */
  public static GrammaticalRelation getConj(String conjunctionString) {
    GrammaticalRelation result = conjs.get(conjunctionString);
    if (result == null) {
      result = new GrammaticalRelation("conj", "conj_collapsed", DEPENDENT, null , StringUtils.EMPTY_STRING_ARRAY,
                                       conjunctionString);
      conjs.put(conjunctionString, result);
    }
    return result;
  }

  // the exhaustive list of preposition relations
  private static final Map<String, GrammaticalRelation> preps = new HashMap<String, GrammaticalRelation>();
  private static final Map<String, GrammaticalRelation> prepsC = new HashMap<String, GrammaticalRelation>();


  public static Collection<GrammaticalRelation> getPreps() { return preps.values(); }

  /**
   *  The "prep" grammatical relation. Used to collapse prepositions.<p>
   *  They will be turned into prep_word, where "word" is a preposition
   *
   *  @param prepositionString The conjunction to make a GrammaticalRelation out of
   *  @return A grammatical relation for this conjunction
   */
  public static GrammaticalRelation getPrep(String prepositionString) {
    GrammaticalRelation result = preps.get(prepositionString);
     if (result == null) {
       result = new GrammaticalRelation("prep", "prep_collapsed", DEPENDENT, null , StringUtils.EMPTY_STRING_ARRAY,
                                        prepositionString);
       preps.put(prepositionString, result);
     }
     return result;
   }


  /**
   *  The "prepc" grammatical relation. Used to collapse prepositions.<p>
   *  They will be turned into prep_word, where "word" is a preposition
   *
   *  @param prepositionString The conjunction to make a GrammaticalRelation out of
   *  @return A grammatical relation for this conjunction
   */
  public static GrammaticalRelation getPrepC(String prepositionString) {
    GrammaticalRelation result = prepsC.get(prepositionString);
     if (result == null) {
       result = new GrammaticalRelation("prepc", "prepc_collapsed", DEPENDENT, null , StringUtils.EMPTY_STRING_ARRAY,
                                        prepositionString);
       prepsC.put(prepositionString, result);
     }
     return result;
   }


  /**
   * Returns the EnglishGrammaticalRelation having the given string
   * representation (e.g. "nsubj"), or null if no such is found.
   *
   * @param s The short name of the GrammaticalRelation
   * @return The EnglishGrammaticalRelation with that name
   */
  public static GrammaticalRelation valueOf(String s) {
    for (GrammaticalRelation reln : values) {
      if (reln.toString().equals(s)) return reln;
    }

    // modification NOTE: do not commit until go-ahead
    // If this is a collapsed relation (indicated by a "_" separating
    // the type and the dependent, instantiate a collapsed version.
    // Currently handcode against conjunctions and prepositions, but
    // should do this in a more robust fashion.
    String[] tuples = s.trim().split("_",2);
    if (tuples.length == 2) {
      String reln = tuples[0];
      String specific = tuples[1];
      if (reln.equals(PREPOSITIONAL_MODIFIER.getShortName())) {
        return getPrep(specific);
      } else if (reln.equals(CONJUNCT.getShortName())) {
       return getConj(specific);
      }
    }

    return null;
  }

  /**
   * Returns an EnglishGrammaticalRelation based on the argument.
   * It works if passed a GrammaticalRelation or the String
   * representation of one (e.g. "nsubj").  It returns <code>null</code>
   * for other classes or if no string match is found.
   *
   * @param o A GrammaticalRelation or String
   * @return The EnglishGrammaticalRelation with that name

   */
  public static GrammaticalRelation valueOf(Object o) {
    if (o instanceof GrammaticalRelation) {
      return (GrammaticalRelation) o;
    } else if (o instanceof String) {
      return valueOf((String) o);
    } else {
      return null;
    }
  }

  /** Prints out the English grammatical relations hierarchy.
   *  See <code>EnglishGrammaticalStructure</code> for a main method that
   *  will print the grammatical relations of a sentence or tree.
   *
   *  @param args Args are ignored.
   */
  public static void main(String[] args) {
    System.out.println(DEPENDENT.toPrettyString());
  }

}
