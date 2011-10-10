package edu.stanford.nlp.ling;


/**
 * A <code>MapLabelFactory</code> is a factory that makes a
 * <code>Label</code> which is a <code>MapLabel</code>.
 *
 * @author Bill MacCartney
 */
public class MapLabelFactory implements LabelFactory {

  /**
   * Make a new label with this <code>String</code> as the value.
   *
   * @param str the string to use as a value
   * @return the newly created label
   */
  public Label newLabel(String str) {
    return new MapLabel(str);
  }

  /**
   * Make a new label with this <code>String</code> as the value.
   * This implementation ignores the options.
   *
   * @param str     the string to use as a value
   * @param options this argument is ignored
   * @return the newly created label
   */
  public Label newLabel(String str, int options) {
    return newLabel(str);
  }

  /**
   * Make a new label with this <code>String</code> as the value.
   *
   * @param str the string to use as a value
   * @return the newly created label
   */
  public Label newLabelFromString(String str) {
    return newLabel(str);
  }

  /**
   * Create a new <code>MapLabel</code> with the value of
   * <code>oldLabel</code> as its value.<p>
   * <p/>
   * If <code>oldLabel</code> is a {@link MapLabel
   * <code>MapLabel</code>}, the new label is identical to
   * <code>oldLabel</code> (but does not share storage).<p>
   * <p/>
   * If <code>oldLabel</code> is a {@link CategoryWordTag
   * <code>CategoryWordTag</code>}, the category, word, and tag are
   * stored in the map of the new label under the map keys {@link
   * MapLabel#CATEGORY_KEY <code>CATEGORY_KEY</code>}, {@link
   * MapLabel#HEAD_WORD_KEY <code>HEAD_WORD_KEY</code>}, and {@link
   * MapLabel#HEAD_TAG_KEY <code>HEAD_TAG_KEY</code>},
   * respectively.
   *
   * @param oldLabel the other label
   */
  public Label newLabel(Label oldLabel) {
    return new MapLabel(oldLabel);
  }

  /**
   * Just for testing.
   */
  public static void main(String[] args) {
    MapLabelFactory lf = new MapLabelFactory();
    System.out.println("new label from String: " + ((MapLabel) lf.newLabel("foo")).toString("value{map}"));
    System.out.println("new label from StringLabel: " + ((MapLabel) lf.newLabel(new StringLabel("foo"))).toString("value{map}"));
    CategoryWordTag cwt = new CategoryWordTag("cat", "word", "tag");
    MapLabel label = (MapLabel) lf.newLabel(cwt);
    System.out.println("new label from CategoryWordTag: " + label.toString("value{map}"));
    label.put("temp", "hot");
    System.out.println("new label from MapLabel: " + ((MapLabel) lf.newLabel(label)).toString("value{map}"));
  }

}

