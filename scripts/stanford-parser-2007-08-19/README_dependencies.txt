MAJOR CHANGES IN ENGLISH TYPED DEPENDENCIES CODE -- AUGUST 2007

--------------------------------------
0. Original dependencies scheme
--------------------------------------

For an overview of the original typed dependencies scheme, please look
at:

  Marie-Catherine de Marneffe, Bill MacCartney, and Christopher D.
  Manning. 2006. Generating Typed Dependency Parses from Phrase
  Structure Parses. 5th International Conference on Language Resources
  and Evaluation (LREC 2006).
  http://nlp.stanford.edu/~manning/papers/LREC_2.pdf

There is also some documentation in the Javadoc, especially for the
EnglishGrammaticalRelations class.

--------------------------------------
I. New typed dependencies
--------------------------------------

* csujbpass: 
  Clausal subjects of passive verbs are now distinguished.

* pcomp/prepc_:
  Clausal complements of prepositions are differentiated from NP complements
  (tagged "pobj"). In the collapsing phase, "pcomp" are turned into "prepc_"
  relation, instead of "prep_".
  "They heard about you missing classes"
       --> prep(heard, about)
           pcomp(about, missing)
       --> prepc_about(heard, missing)
	
* quantmod:
  The quantifier phrase modifier is an element modifying the head of a QP.
  "About 200 people" --> quantmod(200, About)

  To deal with the flat annotation of some QP constituents, more structure
  is injected in the trees, adding XS constituents:
  (NP (QP (XS more than) 30) years)
        quantmod(30, than)
        advmod(than, more)
        num(years, 30)

* measure: 
  The "measure" relation has been introduced to capture measure-phrase
  modifying an ADJP or ADVP:
  "The director is 65 years old" --> measure(old, years)
	

--------------------------------------	
II. Broader coverage of some relations
--------------------------------------

* Prepositional complements of adjectives and verbs are now captured 
  by the "prep/prepc" relations.

* Reduced relative clauses are identified by the "rcmod" relation.

* Some temporal relations are identified even if the tree doesn't contain
  a -TMP tag. However if trees are produced using the Stanford parser it's
  more accurate to run the parser with the option to retain -TMP tags

* In the CCprocessed method (which propagates relations on conjuncts),
  subjects are now propagated too.
  For the sentence "He escaped and fled across the state line to Illinois",
  we will thus get:
        nsubj(escaped-2, He-1)
        conj_and(escaped-2, fled-4)
        nusbj(fled-4, He-4)


--------------------------------------	
III. Bugs fixed 
--------------------------------------

* Disjoint subgraphs when collapsing prepositions which are conjuncts
  (e.g., "Bill jumped over the fence and through the hoop") have been 
  fixed. A copy of one node is required to avoid disjoint subgraphs 
  when collapsing. On the example above, this gives:
        prep_over(jumped-2, fence-5)
        prep_through(jumped-2', hoop-9)
        conj_and(jumped-2, jumped-2')

  Copy nodes are indicated by a ' after their index in the string format
  of the typed dependencies. In the xml format an attribute "copy" gets 
  the value "yes".

* Typed dependencies are now deterministic.

* Words containing an hyphen are correctly printed in the tree and in the
  typed dependencies.

* More consistency between "partmod" and "xcomp": "xcomp" has been 
  restricted to TO-clause complement.
