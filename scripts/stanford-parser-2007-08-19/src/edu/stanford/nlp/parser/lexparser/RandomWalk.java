package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.stats.Counter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class RandomWalk {

  private Map model = new HashMap();

  private Map hiddenToSeen = new HashMap();
  private Map seenToHidden = new HashMap();

  private static final double LAMBDA = 0.01;

  /**
   * Uses the initialized values
   */
  public double score(Object hidden, Object seen) {
    return ((Counter) model.get(hidden)).getNormalizedCount(seen);
  }

  /* score with flexible number of steps */
  public double score(Object hidden, Object seen, int steps) {
    double total = 0;
    for (int i = 0; i <= steps; i++) {
      total += Math.pow(LAMBDA, steps) * step(hidden, seen, steps);
    }
    return total;
  }

  /* returns probability of hidden -> seen with <code>steps</code>
   * random walk steps */
  public double step(Object hidden, Object seen, int steps) {
    if (steps < 1) {
      return ((Counter) hiddenToSeen.get(hidden)).getNormalizedCount(seen);
    } else {
      double total = 0;
      for (Iterator i = seenToHidden.keySet().iterator(); i.hasNext();) {
        Object seen1 = i.next();
        for (Iterator j = hiddenToSeen.keySet().iterator(); j.hasNext();) {
          Object hidden1 = j.next();
          double subtotal = ((Counter) hiddenToSeen.get(hidden)).getNormalizedCount(seen1) * ((Counter) seenToHidden.get(seen1)).getNormalizedCount(hidden1);
          subtotal += score(hidden1, seen, steps - 1);
          total += subtotal;
        }
      }
      return total;
    }
  }


  public void train(Collection data) {
    for (Iterator i = data.iterator(); i.hasNext();) {
      Pair p = (Pair) i.next();
      Object seen = p.first();
      Object hidden = p.second();
      if (!hiddenToSeen.keySet().contains(hidden)) {
        hiddenToSeen.put(hidden, new Counter());
      }
      ((Counter) hiddenToSeen.get(hidden)).incrementCount(seen);

      if (!seenToHidden.keySet().contains(seen)) {
        seenToHidden.put(seen, new Counter());
      }
      ((Counter) seenToHidden.get(seen)).incrementCount(hidden);
    }
  }

  /**
   * builds a random walk model with n steps.
   *
   * @arg data A collection of seen/hidden event <code>Pair</code>s
   */
  public RandomWalk(Collection data, int steps) {
    Map m = new HashMap();
    train(data);
    for (Iterator i = seenToHidden.keySet().iterator(); i.hasNext();) {
      Object seen = i.next();
      if (!model.containsKey(seen)) {
        model.put(seen, new Counter());
      }
      for (Iterator j = hiddenToSeen.keySet().iterator(); j.hasNext();) {
        Object hidden = j.next();
        ((Counter) model.get(seen)).setCount(hidden, score(seen, hidden, steps));
      }
    }
  }

}
