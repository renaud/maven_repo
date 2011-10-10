package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.MapLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;
import static edu.stanford.nlp.trees.EnglishGrammaticalRelations.*;
import static edu.stanford.nlp.trees.GrammaticalRelation.DEPENDENT;
import static edu.stanford.nlp.trees.GrammaticalRelation.KILL;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;

/**
 * A GrammaticalStructure for English.
 * <p/>
 * The parser should be run with the "-retainNPTmpSubcategories" option!
 * <b>Caveat emptor!</b> This is a work in progress.
 * Suggestions welcome.
 *
 * @author Bill MacCartney
 * @author Marie-Catherine de Marneffe
 */
public class EnglishGrammaticalStructure extends GrammaticalStructure {

  public static final String CONJ_MARKER = "conj_";
  public static final String DEFAULT_PARSER_FILE = "/u/nlp/data/lexparser/englishPCFG.ser.gz";

  private static final boolean DEBUG = false;

  /**
   * Construct a new <code>GrammaticalStructure</code> from an
   * existing parse tree.  The new <code>GrammaticalStructure</code>
   * has the same tree structure and label values as the given tree
   * (but no shared storage).  As part of construction, the parse tree
   * is analyzed using definitions from {@link GrammaticalRelation
   * <code>GrammaticalRelation</code>} to populate the new
   * <code>GrammaticalStructure</code> with as many labeled
   * grammatical relations as it can.
   *
   * @param t Parse tree to make grammatical structure from
   */
  public EnglishGrammaticalStructure(Tree t) {
    this(t, new PennTreebankLanguagePack().punctuationWordRejectFilter());
  }

  /**
   * Construct a new <code>GrammaticalStructure</code> from an
   * existing parse tree.  The new <code>GrammaticalStructure</code>
   * has the same tree structure and label values as the given tree
   * (but no shared storage).  As part of construction, the parse tree
   * is analyzed using definitions from {@link GrammaticalRelation
   * <code>GrammaticalRelation</code>} to populate the new
   * <code>GrammaticalStructure</code> with as many labeled
   * grammatical relations as it can.
   *
   * @param t Parse tree to make grammatical structure from
   * @param puncFilter Filter for punctuation words
   */
  public EnglishGrammaticalStructure(Tree t, Filter<String> puncFilter) {
    super((new CoordinationTransformer()).transformTree(t), EnglishGrammaticalRelations.values(), new SemanticHeadFinder(true), puncFilter);
  }


  /**
   * Tries to return a node representing the <code>SUBJECT</code> (whether
   * nominal or clausal) of the given node <code>t</code>.
   * Probably, node <code>t</code> should represent a clause or
   * verb phrase.
   *
   * @param t a node in this <code>GrammaticalStructure</code>
   * @return a node which is the subject of node
   *         <code>t</code>, or else <code>null</code>
   */
  public TreeGraphNode getSubject(TreeGraphNode t) {
    TreeGraphNode subj = getNodeInRelation(t, NOMINAL_SUBJECT);
    if (subj == null) {
      return getNodeInRelation(t, CLAUSAL_SUBJECT);
    } else {
      return subj;
    }
  }

  @Override
  protected void correctDependencies(Collection<TypedDependency> list) {
    correctSubjPassAndPoss(list);
  }

  private static void printListSorted(String title,
                                      Collection<TypedDependency> list) {
    List<TypedDependency> lis = new ArrayList<TypedDependency>(list);
    Collections.sort(lis);
    if (title != null) System.err.println(title);
    System.err.println(lis);
  }


  /**
   * Destructively modifies this <code>TypedDependencyList</code> by
   * collapsing two types of transitive pairs of dependencies:
   * <dl>
   * <dt>prepositional object dependencies: pobj</dt>
   * <dd>
   * <code>prep(cat, in)</code> and
   * <code>pobj(in, hat)</code> are collapsed to
   * <code>prep_in(cat, hat)</code>
   * </dd>
   * <dt>prepositional complement dependencies: pcomp</dt>
   * <dd>
   * <code>prep(heard, of)</code> and
   * <code>pcomp(of, attacking)</code> are collapsed to
   * <code>prepc_of(heard, attacking)</code>
   * </dd>
   * <dt>conjunct dependencies</dt>
   * <dd>
   * <code>cc(investors, and)</code> and
   * <code>conj(investors, regulators) are collapsed to
   * <code>conj_and(investors,regulators)</code>
   * </dd>
   * </dl>
   * It will also erase possesive dependencies
   * <code>possessive(Montezuma, 's)</code> will be erased
   * For relative clauses, it will collapse referent such as:
   * <dd>
   * <code>ref(man, that)</code> and
   * <code>dobj(love, that)</code> are collapsed to
   * <code>dobj(love, man)</code>
   * </dd>
   */
  @Override
  protected void collapseDependencies(Collection<TypedDependency> list,
                                      boolean CCprocess) {
    if (DEBUG) printListSorted("collapseDependencies: CCproc: " + CCprocess,
                               list);
    correctDependencies(list);
    if (DEBUG) printListSorted("After correctDependencies:", list);

    eraseMultiConj(list);
      if(DEBUG) printListSorted("After collapse multi conj:", list);

    collapseMWP(list);
    if (DEBUG) printListSorted("After collapseMWP:", list);

    collapseFlatMWP(list);
    if (DEBUG) printListSorted("After collapseFlatMWP:", list);

    collapsePrepAndPoss(list);
    if (DEBUG) printListSorted("After PrepAndPoss:", list);

    collapseConj(list);
    if (DEBUG) printListSorted("After conj:", list);

    if (CCprocess) {
      treatCC(list);
      if (DEBUG) printListSorted("After treatCC:", list);
    }

    collapseReferent(list);
    if (DEBUG) printListSorted("After collapse referent:", list);

    Collections.sort((List) list);
    if (DEBUG) printListSorted("After all collapse:", list);
  }

  // some hard coding to deal with CONJP: for now we deal with
  // as well as, not to mention, rather than, but rather, instead of, but not, but also
  protected GrammaticalRelation conjValue(Object conj) {
    String newConj = conj.toString().toLowerCase();
    if (conj.toString().equals("not") || conj.toString().equals("instead") || conj.toString().equals("rather")) {
      newConj = "negcc";
    } else if (conj.toString().equals("to") || conj.toString().equals("also") || conj.toString().contains("well")) {
      newConj = "and";
    }
    return EnglishGrammaticalRelations.getConj(newConj);
  }

  private void treatCC(Collection<TypedDependency> list) {

    // Construct a map from tree nodes to the set of typed
    // dependencies in which the node appears as dependent.
    Map<TreeGraphNode, Set<TypedDependency>> map = new HashMap<TreeGraphNode, Set<TypedDependency>>();
    // Construct a map of tree nodes being governor of a subject grammatical relation
    // to that relation
    Map<TreeGraphNode, TypedDependency> subjectMap = new HashMap<TreeGraphNode, TypedDependency>();

    for (TypedDependency typedDep : list) {
      if (!map.containsKey(typedDep.dep())) {
        map.put(typedDep.dep(), new TreeSet<TypedDependency>());
      }
      map.get(typedDep.dep()).add(typedDep);
      //look for subjects
      if(typedDep.reln().parent() == NOMINAL_SUBJECT || typedDep.reln().parent() == SUBJECT || typedDep.reln().parent() == CLAUSAL_SUBJECT) {
        if(!subjectMap.containsKey(typedDep.gov())) {
          subjectMap.put(typedDep.gov(), typedDep);
        }
      }
    }

    //System.err.println(map);
    //System.err.println(subjectMap);

    //create a new list of typed dependencies
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>(list);

    // find typed deps of form conj(gov,dep)
    for (TypedDependency td : list) {
      if (EnglishGrammaticalRelations.getConjs().contains(td.reln())) {
        TreeGraphNode gov = td.gov();
        TreeGraphNode dep = td.dep();
        // look at the dep in the conjunct
        Set<TypedDependency> gov_relations = map.get(gov);
        if (gov_relations != null) {
          for (TypedDependency td1 : gov_relations) {
            //System.err.println("gov rel " + td1);
            TreeGraphNode newGov = td1.gov();
            GrammaticalRelation newRel = td1.reln();
            newTypedDeps.add(new TypedDependency(newRel, newGov, dep));
          }
        }

        // propagate subjects
        // look at the gov in the conjunct: if it is has a subject relation,
        // the dep is a verb and the dep doesn't have a subject relation
        // then we want to add a subject relation for the dep.
        // (By testing for the dep to be a verb, we are going to miss subject of copular verbs! but
        // is it safe to relax this assumption?? i.e., just test for the subject part)
        if(subjectMap.containsKey(gov) && dep.parent().value().startsWith("VB") && !subjectMap.containsKey(dep)) {
          TypedDependency tdsubj = subjectMap.get(gov);
          newTypedDeps.add(new TypedDependency(tdsubj.reln(), dep, tdsubj.dep()));
        }
      }
    }
    list.clear();
    list.addAll(newTypedDeps);
  }

  private void collapseConj(Collection<TypedDependency> list) {
    // find typed deps of form cc(gov, dep)
    for (TypedDependency td : list) {
      if (td.reln() == COORDINATION) {
        TreeGraphNode gov = td.gov();
        GrammaticalRelation conj = conjValue(td.dep().value());

        // find other deps of that gov having reln "conj"
        for (TypedDependency td1 : list) {
          if (td1.reln() == CONJUNCT && td1.gov() == gov) {
            // and change "conj" to the actual (lexical) conjunction
            td1.setReln(conj);
          } else if (td1.reln() == COORDINATION) {
            conj = conjValue(td1.dep().value());
          }
        }
      }
    }
    // now remove typed dependencies with reln "cc"
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();
    for (TypedDependency td : list) {
      if (td.reln() != COORDINATION) {
        newTypedDeps.add(td);
      }
    }
    list.clear();
    list.addAll(newTypedDeps);
  }


  // this method will collapse a referent relation such as follows
  // e.g.: "The man that I love ... "
  // ref(man, that)
  // dobj(love, that)
  // -> dobj(love, man)
  private void collapseReferent(Collection<TypedDependency> list) {
    // find typded deps of form ref(gov, dep)
    // put them in a List of Pairs
    List<Pair<TreeGraphNode, TreeGraphNode>> refs = new ArrayList<Pair<TreeGraphNode, TreeGraphNode>>();
    for (TypedDependency td : list) {
      if (td.reln() == REFERENT) {
        Pair<TreeGraphNode, TreeGraphNode> ref = new Pair<TreeGraphNode, TreeGraphNode>(td.dep(), td.gov());
        refs.add(ref);
      }
    }

    for (Pair<TreeGraphNode, TreeGraphNode> ref : refs) {
      TreeGraphNode dep = ref.first(); // take the relative word
      TreeGraphNode ant = ref.second(); // take the antecedent
      for (TypedDependency td : list) {
        if (td.dep() == dep && td.reln() != RELATIVE) {
          td.setDep(ant);
        }
      }
    }

    // now remove typed dependencies with reln "ref"
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();
    for (TypedDependency td : list) {
      if (td.reln() != REFERENT) {
        newTypedDeps.add(td);
      }
    }
    list.clear();
    list.addAll(newTypedDeps);
  }


  /**
   *  This method corrects subjects of verbs for which we identified an auxpass, but
   *  din't identify the subject as passive.
   *  It also corrects the unretrieved possessive relations for PRP$ and WP$.
   *  @param list List of typedDependencies to work on
   */
  private void correctSubjPassAndPoss(Collection<TypedDependency> list) {
    //put in a list verbs having an auxpass
    List<TreeGraphNode> list_auxpass = new ArrayList<TreeGraphNode>();
    for (TypedDependency td : list) {
      if (td.reln() == AUX_PASSIVE_MODIFIER) {
        list_auxpass.add(td.gov());
      }
    }
    //correct nsubj
    for (TypedDependency td : list) {
      if (td.reln() == NOMINAL_SUBJECT && list_auxpass.contains(td.gov())) {
        td.setReln(NOMINAL_PASSIVE_SUBJECT);
      }
      if (td.reln() == CLAUSAL_SUBJECT && list_auxpass.contains(td.gov())) {
        td.setReln(CLAUSAL_PASSIVE_SUBJECT);
      }
      // correct unretrieved poss: dep relation in which the dependent is a
      // PRP$ or WP$
      // cdm jan 2006: couldn't we just recognize this in basic rules??
      String tag = td.dep().parent().value();
      if (td.reln() == DEPENDENT && (tag.equals("PRP$")||tag.equals("WP$"))) {
        td.setReln(POSSESSION_MODIFIER);
      }
    }
  }

  private void collapsePrepAndPoss(Collection<TypedDependency> list) {

    // Man oh man, how gnarly is the logic of this method.

    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();

    // Construct a map from tree nodes to the set of typed
    // dependencies in which the node appears as governor.
    // cdm: could use CollectionValuedMap here!
    Map<TreeGraphNode, SortedSet<TypedDependency>> map = new HashMap<TreeGraphNode, SortedSet<TypedDependency>>();
    for (TypedDependency typedDep : list) {
      if (!map.containsKey(typedDep.gov())) {
        map.put(typedDep.gov(), new TreeSet<TypedDependency>());
      }
      map.get(typedDep.gov()).add(typedDep);
    }
    //System.err.println("here's the map: " + map);

    // Do preposition conjunction interaction for
    // governor p NP and p NP case ... a lot of special code cdm jan 2006

    for (TypedDependency td1 : list) {
      boolean pobj = true; // default of collasping preposition is prep_

      if (td1.reln() != KILL) {
        TreeGraphNode td1Dep = td1.dep();
        String td1DepPOS = td1Dep.parent().value();
        SortedSet<TypedDependency> possibles = map.get(td1Dep);
        if (possibles == null) continue;
        // look for the "second half"
        TypedDependency prepDep = null;
        TypedDependency ccDep = null;
        TypedDependency conjDep = null;
        TypedDependency prep2Dep = null;
        TypedDependency prepOtherDep = null;

        Set<TypedDependency> otherDtrs = new TreeSet<TypedDependency>();

        // first look for the eventual conj(prep, prep)  (there might be several conj relations!!!)
        for (TypedDependency td2 : possibles) {
          if (td2.reln() == CONJUNCT) {
            TreeGraphNode td2Dep = td2.dep();
            String td2DepPOS = td2Dep.parent().value();
            if ((td2DepPOS.equals("IN") || td2DepPOS.equals("TO")) &&
                td2Dep.value().equals(td1Dep.value())) {
              // Same prepositions are conjuncts
              conjDep = td2;
              Set<TypedDependency> possibles2 = map.get(td2Dep);
              if (possibles2 == null) continue;
              for (TypedDependency td3 : possibles2) {
                TreeGraphNode td3Dep = td3.dep();
                String td3DepPOS = td3Dep.parent().value();
                // CDM Mar 2006: I put in disjunction here when I added in
                // PREPOSITIONAL_OBJECT.  If it catches all cases, we should
                // be able to delete the DEPENDENT disjunct
                // maybe better to delete the DEPENDENT disjunct want it creates problem with multiple prep (mcdm)
                if ((td3.reln() == PREPOSITIONAL_OBJECT || td3.reln() == PREPOSITIONAL_COMPLEMENT) && prep2Dep == null &&
                    ( ! (td3DepPOS.equals("RB") || td3DepPOS.equals("IN") ||
                         td3DepPOS.equals("TO")))) {
                  prep2Dep = td3;
                  if(td3.reln() == PREPOSITIONAL_COMPLEMENT) pobj = false;
                } else {
                  otherDtrs.add(td3);
                }
              }
            }
            else if ((td2DepPOS.equals("IN") || td2DepPOS.equals("TO"))) {
              //Different prepositions are conjuncts
              conjDep = td2;
              Set<TypedDependency> possibles2 = map.get(td2Dep);
              if (possibles2 == null) continue;
              for (TypedDependency td3 : possibles2) {
                TreeGraphNode td3Dep = td3.dep();
                String td3DepPOS = td3Dep.parent().value();
                if ((td3.reln() == PREPOSITIONAL_OBJECT || td3.reln() == PREPOSITIONAL_COMPLEMENT) && prepOtherDep == null &&
                    ( ! (td3DepPOS.equals("RB") || td3DepPOS.equals("IN") ||
                         td3DepPOS.equals("TO")))) {
                  prepOtherDep = td3;
                  if(td3.reln() == PREPOSITIONAL_COMPLEMENT) pobj = false;
                } else {
                  otherDtrs.add(td3);
                }
              }
            }
          }
        }//end td2:possibles

        if (conjDep != null) {// look for the other parts
          int index = conjDep.dep().index();
          for (TypedDependency td2 : possibles) {
            // we look for the cc linked to this conjDep
            // the cc dep must have an index smaller than the dep of conjDep
            if (td2.reln() == COORDINATION && td2.dep().index() < index) {
              ccDep = td2;
            } else {
              TreeGraphNode td2Dep = td2.dep();
              String td2DepPOS = td2Dep.parent().value();
              if ((td1.reln() == PREPOSITIONAL_MODIFIER || td1.reln() == RELATIVE)
                   && (td2.reln() == DEPENDENT || td2.reln() == PREPOSITIONAL_OBJECT || td2.reln() == PREPOSITIONAL_COMPLEMENT)
                   && (td1DepPOS.equals("IN") || td1DepPOS.equals("TO") || td1DepPOS.equals("VBG"))
                   && prepDep == null
                   && (!(td2DepPOS.equals("RB") || td2DepPOS.equals("IN") || td2DepPOS.equals("TO")))
                   && td2.dep().index() < index) {  // same index trick, in case we have multiple deps
                prepDep = td2;
                if (td2.reln() == PREPOSITIONAL_COMPLEMENT) {
                  pobj = false;
                }
              } else if (td2 != conjDep){ // don't want to add the conjDep again!
                otherDtrs.add(td2);
              }

            }
          }
        }

        if (DEBUG && ccDep != null) {
          System.err.println("!!!!!");
          System.err.println("td1: " + td1);
          System.err.println("Kids of td1 are: " + possibles);
          System.err.println("prepDep: " + prepDep);
          System.err.println("ccDep: " + ccDep);
          System.err.println("conjDep: " + conjDep);
          System.err.println("prep2Dep: " + prep2Dep);
          System.err.println("prepOtherDep: " + prepOtherDep);
          System.err.println("otherDtrs: " + otherDtrs);
        }

        // check if we have the same prepositions in the conjunction
        if (prepDep != null && ccDep != null && conjDep != null &&
            prep2Dep != null) {
          // OK, we have a conjunction over identical PPs
          // handling the case of an "agent": the governor of a "by" preposition must have an "auxpass" dependency
          // if it is the case, the "agent" variable becomes true
          boolean agent = false;
          if (td1Dep.value().equals("by")) {
            // look if we have an auxpass
            Set<TypedDependency> aux_pass_poss = map.get(td1.gov());
            if (aux_pass_poss != null) {
              for (TypedDependency td_pass : aux_pass_poss) {
                if (td_pass.reln() == AUX_PASSIVE_MODIFIER) {
                  agent = true;
                }
              }
            }
          }

          TypedDependency tdNew;
          if (agent) {
            tdNew = new TypedDependency(AGENT, td1.gov(), prepDep.dep());
            agent = false;
          } else {
            // for prepositions, use the preposition
            GrammaticalRelation reln;
            if (td1.reln() == RELATIVE) {
              reln = RELATIVE;
            } else {
              // for pobj: we collaspe into "prep"
              if(pobj)
                reln = EnglishGrammaticalRelations.getPrep(td1Dep.value().toLowerCase());
              // for pcomp: we collapse into "prepc"
              else
                reln = EnglishGrammaticalRelations.getPrepC(td1Dep.value().toLowerCase());
            }
            tdNew = new TypedDependency(reln, td1.gov(), prepDep.dep());
          }
          newTypedDeps.add(tdNew);
          TypedDependency tdNew2 = new TypedDependency(conjValue(ccDep.dep().value()),
                                                       prepDep.dep(), prep2Dep.dep());
          newTypedDeps.add(tdNew2);
          if (DEBUG) System.err.println("ConjPP adding: " + tdNew + " " + tdNew2);
          td1.setReln(KILL);        // remember these are "used up"
          prepDep.setReln(KILL);
          ccDep.setReln(KILL);
          conjDep.setReln(KILL);
          prep2Dep.setReln(KILL);

          // promote dtrs that would be orphaned
          for (TypedDependency otd : otherDtrs) {
            // special treatment for prepositions: the original relation is
            // likely to be a "dep" and we want this to be a "prep"
            if(otd.dep().parent().value().equals("IN")) {
              otd.setReln(PREPOSITIONAL_MODIFIER);
            }
            otd.setGov(td1.gov());
          }
        }// end same prepositions


        // check if we have two different prepositions in the conjunction
        // in this case we need to add a node
        // "Bill jumped over the fence and through the hoop"
        // prep_over(jumped, fence)
        // conj_and(jumped, jumped)
        // prep_through(jumped, hoop)
        if (prepDep != null && ccDep != null && conjDep != null &&
            prepOtherDep != null) {
          // OK, we have a conjunction over different PPs
          // we create a new node;
          // in order to make a distinction between the original node and its copy
          // we add a "copy" entry in the MapLabel
          // existence of copy key is checked at printing (toString method of TypedDependency)
          TreeGraphNode copy = new TreeGraphNode(td1.gov(), td1.gov().parent);
          MapLabel label = new MapLabel(td1.gov().label());
          label.put("copy", "true");
          copy.setLabel(label);

          // handling the case of an "agent": the governor of a "by" preposition must have an "auxpass" dependency
          // if it is the case, the "agent" variable becomes true
          boolean agent = false;
          if (td1Dep.value().equals("by")) {
            // look if we have an auxpass
            Set<TypedDependency> aux_pass_poss = map.get(td1.gov());
            if (aux_pass_poss != null) {
              for (TypedDependency td_pass : aux_pass_poss) {
                if (td_pass.reln() == AUX_PASSIVE_MODIFIER) {
                  agent = true;
                }
              }
            }
          }

          TypedDependency tdNew;
          if (agent) {
            tdNew = new TypedDependency(AGENT, td1.gov(), prepDep.dep());
            agent = false;
          } else {
            // for prepositions, use the preposition
            GrammaticalRelation reln;
            if (td1.reln() == RELATIVE) {
              reln = RELATIVE;
            } else {
              // for pobj: we collaspe into "prep"
              if(pobj)
                reln = EnglishGrammaticalRelations.getPrep(td1Dep.value().toLowerCase());
              // for pcomp: we collapse into "prepc"
              else
                reln = EnglishGrammaticalRelations.getPrepC(td1Dep.value().toLowerCase());
            }
            tdNew = new TypedDependency(reln, td1.gov(), prepDep.dep());
          }
          newTypedDeps.add(tdNew);
          // so far we added the first prep grammatical relation

          //now we add the conjunction relation between td1.gov and the copy
          // the copy has the same label as td1.gov() but is another TreeGraphNode
          TypedDependency tdNew2 = new TypedDependency(conjValue(ccDep.dep().value()),
                                                       td1.gov(), copy);
          newTypedDeps.add(tdNew2);

          // now we still need to add the second prep grammatical relation
          // between the copy and the dependent of the prepOtherDep node
          TypedDependency tdNew3;
          // relation: for prepositions, use the preposition
          GrammaticalRelation reln;
          if (td1.reln() == RELATIVE) {
            reln = RELATIVE;
          } else {
            // for pobj: we collaspe into "prep"
            if (pobj) {
              reln = EnglishGrammaticalRelations.getPrep(prepOtherDep.gov().value().toLowerCase());
            }
            // for pcomp: we collapse into "prepc"
            else {
              reln = EnglishGrammaticalRelations.getPrepC(prepOtherDep.gov().value().toLowerCase());
            }

          }
          tdNew3 = new TypedDependency(reln, copy, prepOtherDep.dep());
          newTypedDeps.add(tdNew3);

          if (DEBUG) System.err.println("ConjPP adding: " + tdNew + " " + tdNew2 + " " + tdNew3);
          td1.setReln(KILL);        // remember these are "used up"
          prepDep.setReln(KILL);
          ccDep.setReln(KILL);
          conjDep.setReln(KILL);
          prepOtherDep.setReln(KILL);

          // promote dtrs that would be orphaned
          for (TypedDependency otd : otherDtrs) {
            // special treatment for prepositions: the original relation is
            // likely to be a "dep" and we want this to be a "prep"
            if(otd.dep().parent().value().equals("IN")) {
              otd.setReln(PREPOSITIONAL_MODIFIER);
            }
            otd.setGov(td1.gov());
          }
        }//end different prepositions
      }
    } // for TypedDependency td1 : list

    // below here is the single preposition/possessor basic case!
    for (TypedDependency td1 : list) {
      boolean agent = false;
      boolean pobj = true; // default for prep relation is prep_

      if (td1.reln() != KILL) {
        TreeGraphNode td1Dep = td1.dep();
        String td1DepPOS = td1Dep.parent().value();
        // find all other typedDeps having our dep as gov
        Set<TypedDependency> possibles = map.get(td1Dep);

        if (possibles != null) {

          // look for the "second half"
          for (TypedDependency td2 : possibles) {
            if (td2.reln() != COORDINATION && td2.reln() != CONJUNCT) {

              TreeGraphNode td2Dep = td2.dep();
              String td2DepPOS = td2Dep.parent().value();
              // CDM Mar 2006: I put in disjunction here when I added in
              // PREPOSITIONAL_OBJECT.  If it catches all cases, we should
              // be able to delete the DEPENDENT disjunct
              // maybe better to delete the DEPENDENT disjunct because it creates problem with multiple prep (mcdm)
              // td1.reln() == PREPOSITIONAL_COMPLEMENT: to add the collasping of pcomp too
              if ((/*td1.reln() == DEPENDENT ||*/ td1.reln() == PREPOSITIONAL_MODIFIER || td1.reln() == RELATIVE) &&  (td2.reln() == PREPOSITIONAL_OBJECT || td2.reln() == PREPOSITIONAL_COMPLEMENT) && (td1DepPOS.equals("IN") || td1DepPOS.equals("TO") || td1DepPOS.equals("VBG"))
                   && ( !(td2DepPOS.equals("RB") || td2DepPOS.equals("IN") || td2DepPOS.equals("TO")))
                   && !isConjWithNoPrep(td2.gov(), possibles)) { // we don't collapse preposition conjoined with a non-preposition
                                                                 // to avoid disconnected constituents
                // OK, we have a pair td1, td2 to collapse to td3

                // check whether we are in a pcomp case:
                if (td2.reln() == PREPOSITIONAL_COMPLEMENT) {
                  pobj = false;
                }

                // handling the case of an "agent": the governor of a "by" preposition must have an "auxpass" dependency
                // if it is the case, the "agent" variable becomes true
                if (td1Dep.value().equals("by")) {
                  // look if we have an auxpass
                  Set<TypedDependency> aux_pass_poss = map.get(td1.gov());
                  if (aux_pass_poss != null) {
                    for (TypedDependency td_pass : aux_pass_poss) {
                      if (td_pass.reln() == AUX_PASSIVE_MODIFIER) {
                        agent = true;
                      }
                    }
                  }
                }

                TypedDependency td3;
                if (agent) {
                  td3 = new TypedDependency(AGENT, td1.gov(), td2.dep());
                  agent = false;
                } else {
                  // for prepositions, use the preposition
                  GrammaticalRelation reln;
                  if (td1.reln() == RELATIVE) {
                    reln = RELATIVE;
                  } else {
                    if(pobj) //if we are in a pobj case, we collapse to "prep"
                     reln = EnglishGrammaticalRelations.getPrep(td1Dep.value().toLowerCase());
                    else // we are in a pcomp case, we collapse to "prepc"
                     reln = EnglishGrammaticalRelations.getPrepC(td1Dep.value().toLowerCase());
                  }
                  td3 = new TypedDependency(reln, td1.gov(), td2.dep());
                }
                if (DEBUG) System.err.println("PP adding: " + td3 + " deleting: " + td1 + " " + td2);
                newTypedDeps.add(td3);
                td1.setReln(KILL);        // remember these are "used up"
                td2.setReln(KILL);        // remember these are "used up"
              }
            }
          }

          // Now we need to see if there any TDs that will be "orphaned"
          // by this collapse.  Example: if we have:
          //   dep(drew, on)
          //   dep(on, book)
          //   dep(on, right)
          // the first two will be collapsed to on(drew, book), but then
          // the third one will be orphaned, since its governor no
          // longer appears.  So, change its governor to 'drew'.
          if (td1.reln() == KILL) {
            for (TypedDependency td2 : possibles) {
              if (td2.reln() != KILL && td2.reln() != COORDINATION && td2.reln() != CONJUNCT) {
                //System.err.println("td1 & td2: " + td1 + " & " + td2);
                td2.setGov(td1.gov());
              }
            }
          }
        }
      }
    }

    // now copy remaining unkilled TDs from here to new
    // POSSESSIVE_MODIFIERS should normally have no kids, as possessor is
    // already linked to possessee, but only delete it if there are really
    // no other kids
    for (TypedDependency td : list) {
      boolean keep = true;
      if (td.reln() == KILL) {
        keep = false;
      } else if (td.reln() == POSSESSIVE_MODIFIER) {
        keep = false;
        TreeGraphNode dep = td.dep();
        // search for it
        for (TypedDependency typedD : list) {
          if (typedD.gov().equals(dep)) {
            keep = true;
            break;
          }
        }
      }
      if (keep) {
        newTypedDeps.add(td);
      }
    } // for TypedDependency td

    list.clear();                            // forget all (esp. killed) TDs
    list.addAll(newTypedDeps);
  } // end collapsePrepAndPoss()


  // used by collapseMultiwordPreps(), collapseMWP(), collapseFlatMWP() KEPT IN ALPHABETICAL ORDER
  private static String[][] MULTIWORD_PREPS = {{"according", "to"}, {"across", "from"}, // may not work (across parsed as particle)
                                               {"ahead", "of"}, // may not work (ahead parsed as adverb)
                                               {"along", "with"}, {"due", "to"}, {"alongside", "of"}, {"apart", "from"}, {"as", "of"}, {"as", "to"}, {"away", "from"}, {"based", "on"}, {"because", "of"}, {"close", "by"}, {"close", "to"}, {"due", "to"}, {"compared", "to"}, {"compared", "with"}, {"depending", "on"}, {"followed", "by"}, {"inside", "of"}, {"instead", "of"}, {"next", "to"}, {"near", "to"}, {"out", "of"}, {"outside", "of"}, {"prior", "to"}, {"together", "with"}};

  /**
   * Given a list of typedDependencies, returns true if the node "node" is the governor of a conj relation
   * with a dependent which is not a preposition
   * @param node  a node in this GrammaticalStructure
   * @param list  a list of typedDependencies
   * @return true if node is the governor of a conj relation in the list with the dep not being a preposition
   */
  private boolean isConjWithNoPrep(TreeGraphNode node, Collection<TypedDependency> list) {
    for(TypedDependency td : list) {
      if(td.gov() == node && td.reln() == CONJUNCT) {
        // we have a conjunct
        // check the POS of the dependent
        String tdDepPOS = td.dep().parent().value();
        if( ! (tdDepPOS.equals("IN") || tdDepPOS.equals("TO"))) return true;
      }
    }
    return false;

  }


  // mcdm -- this method actually doesn't work: there is no guarantee that we are going to
  // collapse two parts of a multi_word preposition that belong together
  // the method is only looking at string value and not at the actual node !!!
  private void collapseMultiwordPreps(Collection<TypedDependency> list) {
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();

    for (String[] mwp : MULTIWORD_PREPS) {
      // find typed deps of form rel(gov, mwp[0])
      for (TypedDependency td1 : list) {
        if (td1.dep().value().equalsIgnoreCase(mwp[0])) {
          // find other deps of that dep having reln prep_mwp[1]
          for (TypedDependency td2 : list) {
            if ((td2.gov() == td1.dep() || td2.gov() == td1.gov()) && td2.reln().getSpecific() != null && td2.reln().getSpecific().equals(mwp[1])) {
              if(td2.reln().getShortName().startsWith("prep_")) {
                GrammaticalRelation gr = EnglishGrammaticalRelations.getPrep(mwp[0] + "_" + mwp[1]);
                newTypedDeps.add(new TypedDependency(gr, td1.gov(), td2.dep()));
              }
              else if(td2.reln().getShortName().startsWith("prepc_")) {
                GrammaticalRelation gr = EnglishGrammaticalRelations.getPrepC(mwp[0] + "_" + mwp[1]);
                newTypedDeps.add(new TypedDependency(gr, td1.gov(), td2.dep()));
              }
              td1.setReln(KILL);
              td2.setReln(KILL);
            }
          }
        }
      }
    }

    // now remove typed dependencies with reln "kill"
    for (TypedDependency td : list) {
      if (td.reln() != KILL) {
        newTypedDeps.add(td);
      }
    }
    list.clear();
    list.addAll(newTypedDeps);
  }


  /**
   * Collapse multi-words preposition of the following format:
   * prep(gov, mwp[0])
   * dep(mpw[0],mwp[1])
   * pobj(mwp[1], compl)
   * -> prep_mwp[0]_mwp[1](gov, compl)
   *
   * The collapsing has to be done at once in order to know exaclty which node is the gov and the dep of
   * the multi-word preposition.
   * This method replaces the old "collapsedMultiWordPreps"
   * otherwise this can lead to problems: removing a non-multiword "to" preposition for example!!!
   *
   * @param list list of typedDependencies to work on
   */
  private void collapseMWP(Collection<TypedDependency> list) {
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();

    for (String[] mwp : MULTIWORD_PREPS) {

      TreeGraphNode mwp0 = null;
      TreeGraphNode mwp1 = null;
      TreeGraphNode governor = null;

      TypedDependency prep = null;
      TypedDependency dep = null;
      TypedDependency pobj = null;

      // first find the multi_preposition: dep(mpw[0], mwp[1])
      // the two words should be next to another in the sentence (difference of indexes = 1)
      for (TypedDependency td : list) {
        if (td.gov().value().equalsIgnoreCase(mwp[0]) && td.dep().value().equalsIgnoreCase(mwp[1])
            && Math.abs(td.gov().index()-td.dep().index()) == 1) {
          mwp0 = td.gov();
          mwp1 = td.dep();
          dep = td;
        }
      }

      // now search for prep(gov, mwp0)
      for (TypedDependency td1 : list) {
        if (td1.dep() == mwp0 && td1.reln() == PREPOSITIONAL_MODIFIER) {//we found prep(gov, mwp0)
          prep = td1;
          governor = prep.gov();
        }
      }

      // search for the complement: pobj|pcomp(mwp1,X)
      for (TypedDependency td2 : list) {
        if (td2.gov() == mwp1 && td2.reln() == PREPOSITIONAL_OBJECT) {
          pobj = td2;
          // create the new gr relation
          GrammaticalRelation gr = EnglishGrammaticalRelations.getPrep(mwp[0] + "_" + mwp[1]);
          newTypedDeps.add(new TypedDependency(gr, governor, pobj.dep()));
        }
        if (td2.gov() == mwp1 && td2.reln() == PREPOSITIONAL_COMPLEMENT) {
          pobj = td2;
          // create the new gr relation
          GrammaticalRelation gr = EnglishGrammaticalRelations.getPrepC(mwp[0] + "_" + mwp[1]);
          newTypedDeps.add(new TypedDependency(gr, governor, pobj.dep()));
        }
      }

      // only if we found the three parts, set to KILL and remove
      if (prep != null && dep != null && pobj != null) {
        prep.setReln(KILL);
        dep.setReln(KILL);
        pobj.setReln(KILL);

        // now remove typed dependencies with reln "kill"
        for (TypedDependency td1 : list) {
          if (td1.reln() != KILL) {
            newTypedDeps.add(td1);
          }
        }
        list.clear();
        list.addAll(newTypedDeps);
      }
    }
  }


  /**
   * Collapse multi-words preposition of the following format, which comes from flat annotation.
   * This handles e.g., "because of" (PP (IN because) (IN of) ...)
   *
   * prep(gov, mwp[1])
   * dep(mpw[1], mwp[0])
   * pobj(mwp[1], compl)
   * -> prep_mwp[0]_mwp[1](gov, compl)
   *
   *
   * @param list list of typedDependencies to work on
   */
  private void collapseFlatMWP(Collection<TypedDependency> list) {
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();


    for (String[] mwp : MULTIWORD_PREPS) {

      TreeGraphNode mwp1 = null;
      TreeGraphNode governor = null;

      TypedDependency prep = null;
      TypedDependency dep = null;
      TypedDependency pobj = null;

      // first find the multi_preposition: dep(mpw[1], mwp[0])
      for (TypedDependency td : list) {
        if (td.gov().value().equalsIgnoreCase(mwp[1]) && td.dep().value().equalsIgnoreCase(mwp[0])
            && Math.abs(td.gov().index()-td.dep().index()) == 1) {
          mwp1 = td.gov();
          dep = td;
        }
      }

      // now search for prep(gov, mwp1)
      for (TypedDependency td1 : list) {
        if (td1.dep() == mwp1 && td1.reln() == PREPOSITIONAL_MODIFIER) {//we found prep(gov, mwp1)
          prep = td1;
          governor = prep.gov();
        }
      }

      // search for the complement: pobj|pcomp(mwp1,X)
      for (TypedDependency td2 : list) {
        if (td2.gov() == mwp1 && td2.reln() == PREPOSITIONAL_OBJECT) {
          pobj = td2;
          // create the new gr relation
          GrammaticalRelation gr = EnglishGrammaticalRelations.getPrep(mwp[0] + "_" + mwp[1]);
          newTypedDeps.add(new TypedDependency(gr, governor, pobj.dep()));
        }
        if (td2.gov() == mwp1 && td2.reln() == PREPOSITIONAL_COMPLEMENT) {
          pobj = td2;
          // create the new gr relation
          GrammaticalRelation gr = EnglishGrammaticalRelations.getPrepC(mwp[0] + "_" + mwp[1]);
          newTypedDeps.add(new TypedDependency(gr, governor, pobj.dep()));
        }
      }

      // only if we found the three parts, set to KILL and remove
      if (prep != null && dep != null && pobj != null) {
        prep.setReln(KILL);
        dep.setReln(KILL);
        pobj.setReln(KILL);

        // now remove typed dependencies with reln "kill"
        // and promote eventual orphans
        for (TypedDependency td1 : list) {
          if (td1.reln() != KILL) {
            if(td1.gov() == mwp1) {
              td1.setGov(governor);
            }
            newTypedDeps.add(td1);
          }
        }
        list.clear();
        list.addAll(newTypedDeps);
      }
    }
  }



  /**
   * This method gets rid of multi-words in conjunction to avoid having them
   * creating disconnected constituents
   * e.g., "bread-1 as-2 well-3 as-4 cheese-5" will be turned into conj_and(bread, cheese)
   * and then dep(well-3, as-2) and dep(well-3, as-4) cannot be attached to the graph,
   * these dependencies are erased
   *
   */
  private void eraseMultiConj(Collection<TypedDependency> list) {
    Collection<TypedDependency> newTypedDeps = new ArrayList<TypedDependency>();

    //find typed deps of form cc(gov, x)
    for (TypedDependency td1 : list) {
      if (td1.reln() == COORDINATION) {
        TreeGraphNode x = td1.dep();
        //find typed deps of form dep(x,y) and kill them
        for (TypedDependency td2 : list) {
          if (td2.gov().equals(x) && td2.reln() == DEPENDENT) {
            td2.setReln(KILL);
          }
        }
      }
    }

    // now remove typed dependencies with reln "kill"
    for (TypedDependency td : list) {
      if (td.reln() != KILL) {
        newTypedDeps.add(td);
      }
    }
    list.clear();
    list.addAll(newTypedDeps);
  }


  /**
   * Given sentences or trees, output the typed dependencies.<p/>
   *
   * By default, the method outputs the collapsed typed dependencies.
   * The input can be given as plain text (one sentence by line) using the option -sentFile,
   * or as trees using the option -treeFile.
   * <p/>
   *
   * The following options can be used to specify the types of dependencies wanted: <br/>
   * -collapsed   collapsed dependencies (by default) <br/>
   * -basic       non-collapsed dependencies <br/>
   * -CCprocessed collapsed dependenices and conjunctions processed
   *             (dependenices are added for each conjunct) <br/>
   *
   * The -test option is used for debugging: it prints the grammatical structure, as well as
   * the basic, collapsed and CCprocessed dependencies. It also checks the connectivity of the collapsed dependencies.
   * If the collapsed dependencies list doesn't constitute a connected graph, it prints the possible offending nodes
   * (one of them is the real root of the graph).<p/>
   *
   * Usage: <br/> <code>java edu.stanford.nlp.trees.EnglishGrammaticalStructure [-treeFile FILE | -sentFile FILE] <br>
   *      [-collapsed -basic -CCprocessed -test]</code>
   */
  public static void main(String[] args) {

    // System.out.print("GrammaticalRelations under DEPENDENT:");
    // System.out.println(DEPENDENT.toPrettyString());

    Treebank tb = new MemoryTreebank();
    Properties props = StringUtils.argsToProperties(args);
    String treeFileName = props.getProperty("treeFile");
    String sentFileName = props.getProperty("sentFile");
    String fileName = null;

    if (sentFileName == null && treeFileName == null) {
      try {
        System.err.println("Usage: java EnglishGrammaticalStructure [options]* [-sentFile file|-treeFile file] [-testGraph]");
        System.err.println("  options: -basic, -collapsed [the default], -CCprocessed, -parseTree, -test; -parserFile file");
        TreeReader tr = new PennTreeReader(new StringReader("((S (NP (NNP Sam)) (VP (VBD died) (NP-TMP (NN today)))))"), new LabeledScoredTreeFactory());
        tb.add(tr.readTree());
      } catch (Exception e) {
        System.err.println("Horrible error: " + e);
        e.printStackTrace();
      }
    } else if (treeFileName != null) {
        fileName = treeFileName;
        tb.loadPath(treeFileName);
    } else {
      fileName = sentFileName;
      String[] opts = new String[]{"-retainNPTmpSubcategories"};
      String parserFile = props.getProperty("parserFile");
      if (parserFile == null || "".equals(parserFile)) {
        parserFile = DEFAULT_PARSER_FILE;
      }
      LexicalizedParser lp = new LexicalizedParser(parserFile);
      lp.setOptionFlags(opts);
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(sentFileName));
      } catch (FileNotFoundException e) {
        System.err.println("Cannot find " + sentFileName);
        System.exit(1);
      }
      try {
        System.err.println("Processing sentence file " + sentFileName);
        for (String line; (line = reader.readLine()) != null; ) {
          // System.out.println("Processing sentence: "  + line);
          PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader(line));
          List<Word> words = ptb.tokenize();
          lp.parse(words);
          Tree parseTree = lp.getBestParse();
          tb.add(parseTree);
        }
        reader.close();
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("IOexception reading key file " + sentFileName);
        System.exit(1);
      }
    }


    // treats the output according to the options passed
    boolean basic = props.getProperty("basic") != null;
    boolean collapsed = props.getProperty("collapsed") != null;
    boolean CCprocessed = props.getProperty("CCprocessed") != null;
    boolean parseTree = props.getProperty("parseTree") != null;
    boolean test = props.getProperty("test") != null;

    System.err.println("Printing trees and the typed dependencies for file " + fileName);
    for (Tree t : tb) {

      GrammaticalStructure gs = new EnglishGrammaticalStructure(t);

      if (test) { // print the grammatical structure, the basic, collapsed and CCprocessed

        System.out.println("============= parse tree =======================");
        t.pennPrint();
        System.out.println();

        System.out.println("------------- GrammaticalStructure -------------");
        System.out.println(gs);

        System.out.println("------------- basic dependencies ---------------");
        System.out.println(StringUtils.join(gs.typedDependencies(true), "\n"));

        System.out.println("------------- collapsed dependencies -----------");
        System.out.println(StringUtils.join(gs.typedDependenciesCollapsed(true), "\n"));

        System.out.println("------------- CCprocessed dependencies --------");
        System.out.println(StringUtils.join(gs.typedDependenciesCCprocessed(true), "\n"));

        System.out.println("-----------------------------------------------");
        //connectivity test
        boolean connected = gs.isConnected(gs.typedDependenciesCollapsed(true));
        System.out.println("collapsed dependencies form a connected graph: " + connected);
        if (!connected) {
          System.out.println("possible offending nodes: " + gs.getRoots(gs.typedDependenciesCollapsed(true)));
        }

        // cdm May 2006: this used to make and test the SemanticGraph, but
        // I've moved that code to SemanticGraph to delink things.
      } // end of "test" output

      else {
        if (parseTree) {
          System.out.println("============= parse tree =======================");
          t.pennPrint();
          System.out.println();
        }

        if (basic) {
          if (collapsed || CCprocessed) System.out.println("------------- basic dependencies ---------------");
          System.out.println(StringUtils.join(gs.typedDependencies(true), "\n"));
          System.out.println();
        }
        if (collapsed) {
          if (basic || CCprocessed) System.out.println("----------- collapsed dependencies -----------");
          System.out.println(StringUtils.join(gs.typedDependenciesCollapsed(true), "\n"));
          System.out.println();

        }
        if (CCprocessed) {
          if (basic || collapsed) System.out.println("---------- CCprocessed dependencies ----------");
          System.out.println(StringUtils.join(gs.typedDependenciesCCprocessed(true), "\n"));
          System.out.println();

        }
        if (!basic & !collapsed & !CCprocessed) {
          // System.out.println("----------- collapsed dependencies -----------");
          System.out.println(StringUtils.join(gs.typedDependenciesCollapsed(true), "\n"));
          System.out.println();
        }

      }

      //SemanticGraph sg = SemanticGraph.allTypedDependenciesCollapsed(t, true);
      //System.out.println(sg);

    }// end for
  } // end main

}

