/*
 *  Copyright (c) 2015 Dmitry Neverov and Google
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
package musubi;

import static musubi.Goals.conj;
import static musubi.Goals.disj;
import static musubi.Goals.same;

import java.util.Arrays;

// TODO: Refactor this into test cases and proper documentation.
public class MusubiDemo {
  static void print(Stream s, int n, Var... requestedVars) {
    while (n-- >= 0) {
      SolveStep solve = s.solve();
      if (solve == null) {
        System.out.println("()");
        break;
      }
      if (solve.subst() != null) {
        System.out.println(new View.Builder()
            .setSubst(solve.subst())
            .addRequestedVar(requestedVars)
            .build());
      }
      s = solve.rest();
    }
  }

  static Goal reverseo(Object a, Object b) {
    return reverseo(a, b, null);
  }

  static Goal reverseo(final Object a, final Object b, final Object bTail) {
    return new Goal() {
      @Override
      public Stream run(Subst s) {
        Var aFirst = new Var();
        Var aRest = new Var();

        return disj(
            conj(
                same(a, null),
                same(b, bTail)),
            conj(
                same(a, new Cons(aFirst, aRest)),
                new DelayedGoal(reverseo(aRest, b, new Cons(aFirst, bTail)))))
            .run(s);
      }
    };
  }

  public static void main(String... args) {
    Var x = new Var();
    Var y = new Var();

    System.out.println("\nexample 5");
    Var a = new Var();
    Var b = new Var();
    Goal g = conj(
        same(x, y),
        same(x, Cons.list(Arrays.asList(2, 3, 4))),
        same(a, new Cons(42, x)),
        same(b, new Cons(43, y)));
    print(g.run(Subst.EMPTY), 10, a, b);

    System.out.println("\nexample 6");
    g = reverseo(Cons.list(Arrays.asList(4, 5, 6)), x);
    print(g.run(Subst.EMPTY), 10, x);

    System.out.println("\nexample 9");
    g = reverseo(x, y);
    print(g.run(Subst.EMPTY), 10, x, y);

    System.out.println("\nexample 10");
    g = same(x, x);
    print(g.run(Subst.EMPTY), 10, x, x);

    System.out.println("\nexample 11");
    g = conj(
        same(x, 42),
        same(y, Cons.list(Arrays.asList(5, 7, 9))),
        same(a, new Cons(x, y)));
    print(g.run(Subst.EMPTY), 10, a);
  }
}
