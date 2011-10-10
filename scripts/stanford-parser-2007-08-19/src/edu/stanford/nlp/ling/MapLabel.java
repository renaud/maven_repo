package edu.stanford.nlp.ling;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.MapFactory;

import java.io.File;
import java.util.*;


/**
 * An <code>AbstractMapLabel</code> implementation which uses Object equality
 * and hashcode. This is useful when one wants to refer to token instances,
 * such as to particular nodes in a tree.
 * <p/>
 * As standardly used, a key of a MapLabel is always a String, but the value
 * can be an arbitrary object.
 *
 * @author Bill MacCartney
 * @author Teg Grenager
 */
public class MapLabel extends AbstractMapLabel {

  private static String printOptions = "value-index";

  // CONSTRUCTORS

  /**
   * Create a new <code>MapLabel</code> with null value.
   */
  public MapLabel() {
    this((String) null);
  }

  /**
   * Create a new <code>MapLabel</code> with null value.
   */
  public MapLabel(MapFactory mapFactory) {
    this((String) null, mapFactory);
  }

  /**
   * Create a new <code>MapLabel</code> with the given value.
   *
   * @param value the value of the new label
   */
  public MapLabel(String value) {
    setValue(value);
  }

  /**
   * Create a new <code>MapLabel</code> with the given value.
   *
   * @param value the value of the new label
   */
  public MapLabel(String value, MapFactory mapFactory) {
    super(mapFactory);
    setValue(value);
  }

  /**
   * Create a new <code>MapLabel</code> with the value of another
   * <code>Label</code> as its value.
   * <p/>
   * If the argument label is a <code>MapLabel</code>, the new
   * label is identical to the argument label (but does not share
   * storage). If the argument is a MapLabel, it just copies the map, but
   * doesn't make copies of the map's contents.
   * <p/>
   * If the argument label implements HasWord and/or HasTag, such as
   * a {@link CategoryWordTag
   * <code>CategoryWordTag</code>}, the category, word, and tag are
   * stored in the map of the new label under the map keys {@link
   * MapLabel#CATEGORY_KEY <code>CATEGORY_KEY</code>}, {@link
   * MapLabel#WORD_KEY <code>WORD_KEY</code>}, and {@link
   * MapLabel#TAG_KEY <code>TAG_KEY</code>},
   * respectively.
   *
   * @param label the other label
   */
  public MapLabel(Label label) {
    this(label, MapFactory.HASH_MAP_FACTORY);
  }

  /**
   * Create a new <code>MapLabel</code> with the value of another
   * <code>Label</code> as its value.
   * <p/>
   * If the argument label is a <code>MapLabel</code>, the new
   * label is identical to the argument label (but does not share
   * storage). If the argument is a MapLabel, it just copies the map, but
   * doesn't make copies of the map's contents.
   * <p/>
   * If the argument label implements HasWord and/or HasTag, such as
   * a {@link CategoryWordTag
   * <code>CategoryWordTag</code>}, the category, word, and tag are
   * stored in the map of the new label under the map keys {@link
   * MapLabel#CATEGORY_KEY <code>CATEGORY_KEY</code>}, {@link
   * MapLabel#WORD_KEY <code>WORD_KEY</code>}, and {@link
   * MapLabel#TAG_KEY <code>TAG_KEY</code>},
   * respectively.
   *
   * @param label the other label
   */
  public MapLabel(Label label, MapFactory mapFactory) {
    super(mapFactory);
    if (label instanceof AbstractMapLabel) {
      AbstractMapLabel ml = (AbstractMapLabel) label;
      if (mapFactory==null) {
        this.mapFactory = ml.mapFactory;
        map = mapFactory.newMap(initCapacity);
      }
      map.putAll(ml.map());
    } else {
      map = mapFactory.newMap(initCapacity);
      if (label instanceof HasCategory) {
        map.put(CATEGORY_KEY, ((HasCategory) label).category());
      }
      if (label instanceof HasTag) {
        setTag(((HasTag) label).tag());
      }
      if (label instanceof HasWord) {
        map.put(WORD_KEY, ((HasWord) label).word());
      }
    }
    setValue(label.value());
  }

  // LABELFACTORY IMPLEMENTATION

  // extra class guarantees correct lazy loading (Bloch p.194)
  private static class LabelFactoryHolder {
    static final LabelFactory lf = new MapLabelFactory();
  }


  /**
   * Return a factory for <code>MapLabel</code>s.
   * The factory returned is always the same one (a singleton).
   *
   * @return the label factory
   */
  public LabelFactory labelFactory() {
    return LabelFactoryHolder.lf;
  }

  /**
   * Return a factory for <code>MapLabel</code>s.
   * The factory returned is always the same one (a singleton).
   *
   * @return the label factory
   */
  public static LabelFactory factory() {
    return LabelFactoryHolder.lf;
  }

  /**
   * Return a <code>String</code> containing the value (and index,
   * if any) of this label.  This is equivalent to
   * toString("value-index").
   */
  public String toString() {
    return toString(printOptions);
  }

  /**
   * Returns a formatted string representing this label.  The
   * desired format is passed in as a <code>String</code>.
   * Currently supported formats include:
   * <ul>
   * <li>"value": just prints the value</li>
   * <li>"{map}": prints the complete map</li>
   * <li>"value{map}": prints the value followed by the contained
   * map (less the map entry containing key {@link
   * MapLabel#CATEGORY_KEY <code>CATEGORY_KEY</code>})</li>
   * <li>"value-index": extracts a value and an integer index from
   * the contained map using keys  {@link
   * MapLabel#CATEGORY_KEY <code>INDEX_KEY</code>},
   * respectively, and prints them with a hyphen in between</li>
   * <li>"value-index{map}": a combination of the above; the index is
   * displayed first and then not shown in the map that is displayed</li>
   * <li>"word": Just the value of HEAD_WORD_KEY in the map</li>
   * </ul>
   * <p/>
   * Map is printed in alphabetical order of keys.
   */
  public String toString(String format) {
    StringBuilder buf = new StringBuilder();
    if (format.equals("value")) {
      buf.append(value());
    } else if (format.equals("{map}")) {
      Map map2 = new TreeMap(asStringComparator);
      map2.putAll(map);
      buf.append(map2);
    } else if (format.equals("value{map}")) {
      buf.append(value());
      Map map2 = new TreeMap(asStringComparator);
      map2.putAll(map);
      map2.remove(VALUE_KEY);
      buf.append(map2);
    } else if (format.equals("value-index")) {
      buf.append(value());
      Object index = map.get(INDEX_KEY);
      if (index != null && index instanceof Integer) {
        buf.append("-").append(((Integer) index).intValue());
      }
    } else if (format.equals("value-index{map}")) {
      buf.append(value());
      Object index = map.get(INDEX_KEY);
      if (index != null && index instanceof Integer) {
        buf.append("-").append(((Integer) index).intValue());
      }
      Map map2 = new TreeMap(asStringComparator);
      map2.putAll(map);
      map2.remove(INDEX_KEY);
      map2.remove(VALUE_KEY);
      if (!map2.isEmpty()) {
        buf.append(map2);
      }
    } else if (format.equals("word")) {
      buf.append(word());
    }
    return buf.toString();
  }

  public static void setPrintOptions(String po) {
    printOptions = po;
  }

  private static final Comparator asStringComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      return o1.toString().compareTo(o2.toString());
    }
  };

  /**
   * For testing only.
   */
  public static void main(String[] args) {
    System.out.println("Testing different constructors");
    System.out.println("  MapLabel from zero-arg constructor:      " + new MapLabel());
    System.out.println("  MapLabel from String constructor:        " + new MapLabel("StringValue"));
    System.out.println("  MapLabel from Label constructor:         " + new MapLabel(new StringLabel("StringLabelValue")));
    System.out.println("  MapLabel from MapLabel constructor:      " + new MapLabel(new MapLabel("MapLabelValue")));
    MapLabel label = new MapLabel(new CategoryWordTag("cat", "word", "tag"));
    System.out.println("MapLabel from CategoryWordTag constructor: " + label);
    System.out.println("That same label in value{map} format:      " + label.toString("value{map}"));
    label.setIndex(666);
    System.out.println("Add index key 666 to that label:           " + label.toString("value{map}"));
    System.out.println("That label in {map} format:                " + label.toString("{map}"));
    System.out.println("That label in word format:                 " + label.toString("word"));
    System.out.println("That label in value-index format:          " + label.toString("value-index"));
    System.out.println("That label in value-index{map} format:     " + label.toString("value-index{map}"));

    int oldValue = label.index();
    label.setIndex(777);
    System.out.println("Changed the index from " + oldValue + ":                " + label.toString("value-index"));
    Object oldVal = label.put(INDEX_KEY, "sixsixsix");
    System.out.println("Changed the index from " + oldVal + " to string:      " + label.toString("value-index"));
    System.out.println("That label in value{map} format:           " + label.toString("value{map}"));
    label.map.remove(INDEX_KEY);
    System.out.println("Removed index key:                         " + label.toString("value-index"));
    System.out.println("That label in value{map} format:           " + label.toString("value{map}"));

    oldVal = label.put("foo", "bar");
    System.out.println("Changed foo from " + oldVal + ":                     " + label.toString("value-index{map}"));
    label.put("self", label);
    System.out.println("Add map entry with self as value:          " + label.toString("value-index{map}"));
    label.setHeadWord(new CategoryWordTag("cat", "rose", "tag"));
    System.out.println("Setting headWord to rose:                  " + label.toString("value{map}"));

    System.out.println("Testing serialization...");
    try {
      File testFile = IOUtils.writeObjectToTempFile(label, "testfile");
      MapLabel newLabel = (MapLabel) edu.stanford.nlp.io.IOUtils.readObjectFromFile(testFile);
      System.out.println("New label:                                 " + newLabel.toString("value-index{map}"));
    } catch (Exception e) {
      System.err.println(e);
    }

    System.out.println("Done.");
    //MapLabelFactory lf = (MapLabelFactory) factory();
  }

  private static final long serialVersionUID = 1289283452485202162L;;

}

