package edu.stanford.nlp.trees;

import edu.stanford.nlp.util.FilePathProcessor;
import edu.stanford.nlp.util.FileProcessor;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.ling.HasIndex;

import java.io.*;
import java.util.*;

/**
 * A <code>MemoryTreebank</code> object stores a corpus of examples with
 * given tree structures in memory (as a <code>List</code>).
 *
 * @author Christopher Manning
 * @version 2004/09/01
 */
public final class MemoryTreebank extends Treebank implements FileProcessor, List<Tree> {

  private static final boolean PRINT_FILENAMES = false;


  /**
   * The collection of parse trees.
   */
  private List<Tree> parseTrees;


  /**
   * Create a new tree bank.
   * The trees are made with a <code>LabeledScoredTreeReaderFactory</code>.
   * <p/>
   * <i>Compatibility note: Until Sep 2004, this used to create a Treebank
   * with a SimpleTreeReaderFactory, but this was changed as the old
   * default wasn't very useful, especially to naive users.</i>
   */
  public MemoryTreebank() {
    this(new LabeledScoredTreeReaderFactory());
  }

  /**
   * Create a new tree bank, set the encoding for file access
   *
   * @param encoding the encoding to use for file access.
   */
  public MemoryTreebank(String encoding) {
    this(new LabeledScoredTreeReaderFactory(), encoding);
  }

  /**
   * Create a new tree bank.
   *
   * @param trf the factory class to be called to create a new
   *            <code>TreeReader</code>
   */
  public MemoryTreebank(TreeReaderFactory trf) {
    super(trf);
    parseTrees = new ArrayList<Tree>();
  }


  /**
   * Create a new tree bank.
   *
   * @param trf      the factory class to be called to create a new
   *                 <code>TreeReader</code>
   * @param encoding the encoding to use for file access.
   */
  public MemoryTreebank(TreeReaderFactory trf, String encoding) {
    super(trf, encoding);
    parseTrees = new ArrayList<Tree>();
  }

  /**
   * Create a new tree bank.  The list of trees passed in is simply placed
   * in the Treebank.  It is not copied.
   *
   * @param trees    The trees to put in the Treebank.
   * @param trf      the factory class to be called to create a new
   *                 <code>TreeReader</code>
   * @param encoding the encoding to use for file access.
   */
  public MemoryTreebank(List<Tree> trees, TreeReaderFactory trf, String encoding) {
    super(trf, encoding);
    parseTrees = trees;
  }

  /**
   * Create a new Treebank.
   *
   * @param initialCapacity The initial size of the underlying Collection,
   *                        (if a Collection-based storage mechanism is being provided)
   */
  public MemoryTreebank(int initialCapacity) {
    this(initialCapacity, new SimpleTreeReaderFactory());
  }


  /**
   * Create a new tree bank.
   *
   * @param initialCapacity The initial size of the underlying Collection
   * @param trf             the factory class to be called to create a new
   *                        <code>TreeReader</code>
   */
  public MemoryTreebank(int initialCapacity, TreeReaderFactory trf) {
    super(initialCapacity, trf);
    parseTrees = new ArrayList<Tree>(initialCapacity);
  }


  /**
   * Empty a <code>Treebank</code>.
   */
  public void clear() {
    parseTrees.clear();
  }


  /**
   * Load trees from given directory.
   *
   * @param path file or directory to load from
   * @param filt a FilenameFilter of files to load
   */
  public void loadPath(File path, FileFilter filt) {
    FilePathProcessor.processPath(path, filt, this);
  }


  /**
   * Load a collection of parse trees from the file of given name.
   * Each tree may optionally be encased in parens to allow for Penn
   * Treebank style trees.
   * This methods implements the <code>FileProcessor</code> interface.
   *
   * @param file file to load a tree from
   */
  public void processFile(File file) {
    TreeReader tr = null;
    Tree pt;

    try {
      // maybe print file name to stdout to get some feedback
      if (PRINT_FILENAMES) {
        System.err.println(file);
      }
      // could throw an IO exception if can't open for reading
      tr = treeReaderFactory().newTreeReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding())));
      int sentIndex=0;
      while ((pt = tr.readTree()) != null) {
        if (pt.label() instanceof HasIndex) { // so we can trace where this tree came from
          HasIndex hi = (HasIndex) pt.label();
          hi.setDocID(file.getName());
          hi.setSentIndex(sentIndex);
        }
        parseTrees.add(pt);
        sentIndex++;
      }
    } catch (IOException e) {
      System.err.println("loadTree IO Exception: " + e + " in file " + file);
    } finally {
      try {
        if (tr != null) {
          tr.close();  // important: closes file even if error!
        }
      } catch (IOException e) {
      }
    }
  }


  /**
   * Load a collection of parse trees from a Reader.
   * Each tree may optionally be encased in parens to allow for Penn
   * Treebank style trees.
   *
   * @param r The reader to read trees from.  (If you want it buffered,
   *    you should already have buffered it!)
   */
  public void load(Reader r) {
    load(r, null);
  }

  /**
   * Load a collection of parse trees from a Reader.
   * Each tree may optionally be encased in parens to allow for Penn
   * Treebank style trees.
   *
   * @param r The reader to read trees from.  (If you want it buffered,
   *    you should already have buffered it!)
   * @param id An ID for where these files come from (arbitrary, but
   *    something like a filename.  Can be <code>null</code> for none.
   */
  public void load(Reader r, String id) {
    TreeReader tr = null;
    Tree pt;

    try {
      // could throw an IO exception?
      tr = treeReaderFactory().newTreeReader(r);
      int sentIndex=0;
      while ((pt = tr.readTree()) != null) {
        if (pt.label() instanceof HasIndex) { // so we can trace where this tree came from
          HasIndex hi = (HasIndex) pt.label();
          if (id != null) {
            hi.setDocID(id);
          }
          hi.setSentIndex(sentIndex);
        }
        parseTrees.add(pt);
        sentIndex++;
      }
    } catch (IOException e) {
      System.err.println("load IO Exception: " + e);
    }
  }


  /**
   * Get a tree by index from the Treebank.
   * This operation isn't in the <code>Treebank</code> feature set, and
   * so is only available with a <code>MemoryTreebank</code>, but is
   * useful in allowing the latter to be used as a <code>List</code>.
   *
   * @param i The integer (counting from 0) index of the tree
   * @return A tree
   */
  public Tree get(int i) {
    return parseTrees.get(i);
  }


  /**
   * Apply the TreeVisitor tp to all trees in the Treebank.
   *
   * @param tp A class that implements the TreeVisitor interface
   */
  public void apply(TreeVisitor tp) {
    for (int i = 0, size = parseTrees.size(); i < size; i++) {
      tp.visitTree(parseTrees.get(i));
    }
    // or could do as Iterator but slower
    // Iterator iter = parseTrees.iterator();
    // while (iter.hasNext()) {
    //    tp.visitTree((Tree) iter.next());
    // }
  }


  /**
   * Return an Iterator over Trees in the Treebank.
   *
   * @return The iterator
   */
  public Iterator<Tree> iterator() {
    return parseTrees.iterator();
  }


  /**
   * Returns the size of the Treebank.
   * Provides a more efficient implementation than the one for a
   * generic <code>Treebank</code>
   *
   * @return the number of trees in the Treebank
   */
  public int size() {
    return parseTrees.size();
  }


  // Extra stuff to implement List interface

  public void add(int index, Tree element) {
    parseTrees.add(index, element);
  }

  public boolean add(Tree element) {
    return parseTrees.add(element);
  }


  public boolean addAll(int index, Collection<? extends Tree> c) {
    return parseTrees.addAll(index, c);
  }

  public int indexOf(Object o) {
    return parseTrees.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return parseTrees.lastIndexOf(o);
  }

  public Tree remove(int index) {
    return parseTrees.remove(index);
  }

  public Tree set(int index, Tree element) {
    return parseTrees.set(index, element);
  }

  public ListIterator<Tree> listIterator() {
    return parseTrees.listIterator();
  }

  public ListIterator<Tree> listIterator(int index) {
    return parseTrees.listIterator(index);
  }

  public List<Tree> subList(int fromIndex, int toIndex) {
    return parseTrees.subList(fromIndex, toIndex);
  }

    /**
   * Return a MemoryTreebank where each
   * Tree in the current treebank has been transformed using the
   * TreeTransformer.  This Treebank is unchanged (assuming that the
   * TreeTransformer correctly doesn't change input Trees.
   *
   * @param treeTrans The TreeTransformer to use
   */
  public Treebank transform(TreeTransformer treeTrans) {
    Treebank mtb = new MemoryTreebank(size(), treeReaderFactory());
    for (Iterator it = iterator(); it.hasNext();) {
      Tree t = (Tree) it.next();
      mtb.add(treeTrans.transformTree(t));
    }
    return mtb;
  }

  /**
   * Loads treebank grammar from first argument and prints it.
   * Just a demonstration of functionality. <br>
   * <code>usage: java MemoryTreebank treebankFilesPath</code>
   *
   * @param args array of command-line arguments
   */
  public static void main(String[] args) {
    Timing.startTime();
    Treebank treebank = new MemoryTreebank(new TreeReaderFactory() {
      public TreeReader newTreeReader(Reader in) {
        return new PennTreeReader(in, new LabeledScoredTreeFactory());
      }
    });
    treebank.loadPath(args[0]);
    Timing.endTime();
    System.out.println(treebank);
  }

}
