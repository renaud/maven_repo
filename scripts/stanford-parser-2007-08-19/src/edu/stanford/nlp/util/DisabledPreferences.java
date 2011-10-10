package edu.stanford.nlp.util;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * A do-nothing Preferences implementation so that we can avoid the hassles
 * of the JVM Preference implementations.
 * Taken from: http://www.allaboutbalance.com/disableprefs/index.html
 *
 * @author Robert Slifka
 * @version 2003/03/24
 */
public class DisabledPreferences extends AbstractPreferences {

  public DisabledPreferences() {
    super(null, "");
  }

  protected void putSpi(String key, String value) {

  }

  protected String getSpi(String key) {
    return null;
  }

  protected void removeSpi(String key) {

  }

  protected void removeNodeSpi() throws BackingStoreException {

  }

  protected String[] keysSpi() throws BackingStoreException {
    return new String[0];
  }

  protected String[] childrenNamesSpi() throws BackingStoreException {
    return new String[0];
  }

  protected AbstractPreferences childSpi(String name) {
    return null;
  }

  protected void syncSpi() throws BackingStoreException {

  }

  protected void flushSpi() throws BackingStoreException {

  }
}
