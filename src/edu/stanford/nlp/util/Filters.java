package edu.stanford.nlp.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

/**
 * Static class with some useful {@link java.util.function.Predicate} implementations and utility methods for working
 * with {@code Predicates}.
 * <p>
 * Contains filters that always accept or reject, filters that accept or reject an Object if it's in a given
 * Collection, as well as several composite filters. Contains methods for creating a new Filter that is the AND/OR of
 * two Filters, or the NOT of a Filter. Finally, you can filter an Object[] through a Filter to return a new
 * {@code Object[]} with only the accepted values, or {@link #retainAll(java.util.Collection,
 * java.util.function.Predicate)} elements in a Collection that pass a filter.
 *
 * @author Christopher Manning
 * @version 1.0
 */
public class Filters {

  /**
   * Nothing to instantiate
   */
  private Filters() {
  }

  /**
   * The acceptFilter accepts everything.
   */
  public static <T> Predicate<T> acceptFilter() {
    return new CategoricalFilter<>(true);
  }

  /**
   * The rejectFilter accepts nothing.
   */
  public static <T> Predicate<T> rejectFilter() {
    return new CategoricalFilter<>(false);
  }

  private static final class CategoricalFilter<T> implements Predicate<T>, Serializable {

    private final boolean judgment;

    protected CategoricalFilter(boolean judgment) {
      this.judgment = judgment;
    }

    /**
     * Checks if the given object passes the filter.
     *
     * @param obj an object to test
     */
    @Override
    public boolean test(T obj) {
      return judgment;
    }

    @Override
    public String toString() {
      return "CategoricalFilter(" + judgment + ')';
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (!(other instanceof CategoricalFilter)) {
        return false;
      }
      return ((CategoricalFilter) other).judgment == this.judgment;
    }

    // Automatically generated by Eclipse
    private static final long serialVersionUID = 7501774666726883656L;
  }


  /**
   * The collectionAcceptFilter accepts a certain collection.
   */
  public static <E> Predicate<E> collectionAcceptFilter(E[] objs) {
    return new CollectionAcceptFilter<>(Arrays.asList(objs), true);
  }

  /**
   * The collectionAcceptFilter accepts a certain collection.
   */
  public static <E> Predicate<E> collectionAcceptFilter(Collection<E> objs) {
    return new CollectionAcceptFilter<>(objs, true);
  }

  /**
   * The collectionRejectFilter rejects a certain collection.
   */
  public static <E> Predicate<E> collectionRejectFilter(E[] objs) {
    return new CollectionAcceptFilter<>(Arrays.asList(objs), false);
  }

  /**
   * The collectionRejectFilter rejects a certain collection.
   */
  public static <E> Predicate<E> collectionRejectFilter(Collection<E> objs) {
    return new CollectionAcceptFilter<>(objs, false);
  }

  private static final class CollectionAcceptFilter<E> implements Predicate<E>, Serializable {

    private final Collection<E> args;
    private final boolean judgment;

    protected CollectionAcceptFilter(Collection<E> c, boolean judgment) {
      this.args = Generics.newHashSet(c);
      this.judgment = judgment;
    }

    /**
     * Checks if the given object passes the filter.
     *
     * @param obj an object to test
     */
    @Override
    public boolean test(E obj) {
      if (args.contains(obj)) {
        return judgment;
      }
      return !judgment;
    }

    @Override
    public String toString() {
      return "(" + judgment + ':' + args + ')';
    }

    private static final long serialVersionUID = -8870550963937943540L;

  } // end class CollectionAcceptFilter

  /**
   * Filter that accepts only when both filters accept (AND).
   */
  public static <E> Predicate<E> andFilter(Predicate<E> f1, Predicate<E> f2) {
    return (new CombinedFilter<>(f1, f2, true));
  }

  /**
   * Filter that accepts when either filter accepts (OR).
   */
  public static <E> Predicate<E> orFilter(Predicate<E> f1, Predicate<E> f2) {
    return (new CombinedFilter<>(f1, f2, false));
  }

  /**
   * Conjunction or disjunction of two filters.
   */
  private static class CombinedFilter<E> implements Predicate<E>, Serializable {
    private final Predicate<E> f1, f2;
    private final boolean conjunction; // and vs. or

    public CombinedFilter(Predicate<E> f1, Predicate<E> f2, boolean conjunction) {
      this.f1 = f1;
      this.f2 = f2;
      this.conjunction = conjunction;
    }

    @Override
    public boolean test(E o) {
      if (conjunction) {
        return (f1.test(o) && f2.test(o));
      }
      return (f1.test(o) || f2.test(o));
    }

    // Automatically generated by Eclipse
    private static final long serialVersionUID = -2988241258905198687L;
  }

  /**
   * Disjunction of a list of filters.
   */
  public static class DisjFilter<T> implements Predicate<T>, Serializable {
    private final List<Predicate<T>> filters;

    public DisjFilter(List<Predicate<T>> filters) {
      this.filters = filters;
    }

    @SafeVarargs
    public DisjFilter(Predicate<T>... filters) {
      this.filters = new ArrayList<>();
      this.filters.addAll(Arrays.asList(filters));
    }

    public void addFilter(Predicate<T> filter) {
      filters.add(filter);
    }

    @Override
    public boolean test(T obj) {
      for (Predicate<T> f:filters) {
        if (f.test(obj)) return true;
      }
      return false;
    }

    private static final long serialVersionUID = 1L;
  }

  /**
   * Conjunction of a list of filters.
   */
  public static class ConjFilter<T> implements Predicate<T>, Serializable {
    private final List<Predicate<T>> filters;

    public ConjFilter(List<Predicate<T>> filters) {
      this.filters = filters;
    }

    @SafeVarargs
    public ConjFilter(Predicate<T>... filters) {
      this.filters = new ArrayList<>();
      this.filters.addAll(Arrays.asList(filters));
    }

    public void addFilter(Predicate<T> filter) {
      filters.add(filter);
    }

    @Override
    public boolean test(T obj) {
      for (Predicate<T> f:filters) {
        if (!f.test(obj)) return false;
      }
      return true;
    }

    private static final long serialVersionUID = 1L;
  }

  /**
   * Filter that does the opposite of given filter (NOT).
   */
  public static <E> Predicate<E> notFilter(Predicate<E> filter) {
    return (new NegatedFilter<>(filter));
  }

  /**
   * Filter that's either negated or normal as specified.
   */
  public static <E> Predicate<E> switchedFilter(Predicate<E> filter, boolean negated) {
    return new NegatedFilter<>(filter, negated);
  }

  /**
   * Negation of a filter.
   */
  private static class NegatedFilter<E> implements Predicate<E>, Serializable {
    private final Predicate<E> filter;
    private final boolean negated;

    public NegatedFilter(Predicate<E> filter, boolean negated) {
      this.filter = filter;
      this.negated = negated;
    }

    public NegatedFilter(Predicate<E> filter) {
      this(filter, true);
    }

    @Override
    public boolean test(E o) {
      return (negated ^ filter.test(o)); // xor
    }

    public String toString() {
      return "NOT(" + filter.toString() + ')';
    }

    // Automatically generated by Eclipse
    private static final long serialVersionUID = -1599556783677718177L;
  }

  /**
   * A filter that accepts a random fraction of the input it sees.
   */
  public static class RandomFilter<E> implements Predicate<E>, Serializable {
    private static final long serialVersionUID = -4885773582960355425L;
    private final Random random;
    private final double fraction;

    public RandomFilter() {
      this(0.1, new Random());
    }

    public RandomFilter(double fraction) {
      this(fraction, new Random());
    }

    public RandomFilter(double fraction, Random random) {
      this.fraction = fraction;
      this.random = random;
    }

    @Override
    public boolean test(E o) {
      return (random.nextDouble() < fraction);
    }
  }

  /**
   * Applies the given filter to each of the given elements, and returns the
   * array of elements that were accepted. The runtime type of the returned
   * array is the same as the passed in array.
   */
  @SuppressWarnings("unchecked")
  public static <E> E[] filter(E[] elems, Predicate<E> filter) {
    List<E> filtered = new ArrayList<>();
    for (E elem: elems) {
      if (filter.test(elem)) {
        filtered.add(elem);
      }
    }
    return (filtered.toArray((E[]) Array.newInstance(elems.getClass().getComponentType(), filtered.size())));
  }

  /**
   * Removes all elements in the given Collection that aren't accepted by the given Filter.
   */
  public static <E> void retainAll(Collection<E> elems, Predicate<? super E> filter) {
    for (Iterator<E> iter = elems.iterator(); iter.hasNext();) {
      E elem = iter.next();
      if ( ! filter.test(elem)) {
        iter.remove();
      }
    }
  }

}