/*
 *  Copyright (c) 2015 The Gulava Authors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package gulava;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A view of a substitution. This may or may not be the same as a raw substitution. A raw
 * substitution often has unused variables, and/or variables that only appear once and ought to be
 * expanded.
 */
public final class View {
  /**
   * Builder of {@code View} instances.
   */
  public static final class Builder {
    private Set<Var> requestedVars = new HashSet<>();
    private Subst subst;

    public Builder addRequestedVar(Var... requestedVars) {
      return addAllRequestedVars(Arrays.asList(requestedVars));
    }

    public Builder addAllRequestedVars(Iterable<? extends Var> requestedVars) {
      for (Var var : requestedVars) {
        this.requestedVars.add(var);
      }
      return this;
    }

    public Builder setSubst(Subst subst) {
      if (this.subst != null) {
        throw new IllegalStateException();
      }
      this.subst = subst;
      return this;
    }

    /**
     * Builds a view with variables that only appear once out of all
     * substitutions replaced with their mapping, and their mapping removed. Also, removes variables
     * from the substitution mapping that are not used.
     */
    public View build() {
      View view = new View(subst);

      while (true) {
        View simpler = view.simplify(requestedVars);

        if (simpler.map.size() == view.map.size()) {
          return simpler;
        }

        view = simpler;
      }
    }
  }

  private final Map<Object, Object> map;

  private View(Map<Object, Object> map) {
    this.map = map;
  }

  /**
   * Returns a map representation of this view, which is a mapping of variables to values.
   */
  public Map<Object, Object> map() {
    return map;
  }

  private static void countOccurrences(Map<Var, int[]> occurrences, Object value) {
    if (value instanceof Var) {
      if (!occurrences.containsKey(value)) {
        occurrences.put((Var) value, new int[] {1});
      } else {
        occurrences.get(value)[0]++;
      }
    } else if (value instanceof LogicValue) {
      for (Object subValue : ((LogicValue) value).asMap().values()) {
        countOccurrences(occurrences, subValue);
      }
    }
  }

  private static final class ReplacerImpl implements Replacer {
    private final Map<Var, int[]> occurrences;
    private final Map<Object, Object> map;

    ReplacerImpl(Map<Var, int[]> occurrences, Map<Object, Object> map) {
      this.occurrences = occurrences;
      this.map = map;
    }

    @Override
    public Object replace(Object original) {
      if (original instanceof Var) {
        int[] thisCount = occurrences.get(original);
        if ((thisCount != null) && (thisCount[0] == 1) && map.containsKey(original)) {
          return replace(map.get(original));
        }
      } else if (original instanceof LogicValue) {
        return ((LogicValue) original).replace(this);
      }
      return original;
    }
  }

  private View simplify(Set<Var> requestedVars) {
    Map<Var, int[]> occurrences = new HashMap<>();

    for (Var requestedVar : requestedVars) {
      occurrences.put(requestedVar, new int[] {1});
    }

    for (Object value : map.values()) {
      countOccurrences(occurrences, value);
    }

    Map<Object, Object> newMap = new HashMap<>();
    for (Object var : map.keySet()) {
      if (!occurrences.containsKey(var)
          || ((occurrences.get(var)[0] == 1) && !requestedVars.contains(var))) {
        continue;
      }

      newMap.put(var, new ReplacerImpl(occurrences, map).replace(map.get(var)));
    }

    return new View(newMap);
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
