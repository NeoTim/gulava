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
package gulava.processor;

import static gulava.Goals.UNIT;
import static gulava.Goals.conj;
import static gulava.Goals.same;

import gulava.Cons;
import gulava.DelayedGoal;
import gulava.Goal;
import gulava.Goals;
import gulava.Var;
import gulava.annotation.MakePredicates;
import gulava.testing.LogicAsserter;
import gulava.util.Count;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class MakePredicatesFunctionalTest {
  private static final Var A = new Var();
  private static final Var B = new Var();

  @MakePredicates
  public static abstract class IsCons {
    public abstract Goal isCons(Object item);

    final Goal isCons_impl(Cons<?, ?> item) {
      return Goals.UNIT;
    }
  }

  @MakePredicates
  public static abstract class MultiplePredicates {
    public abstract Goal even(Object count);

    final Goal even_baseCase(Void count) {
      return Goals.UNIT;
    }

    final Goal even_iterate(Count<Count<?>> count) {
      return even(count.oneLess().oneLess());
    }

    // Note that the argument name is different from even, just to show that it can be.
    public abstract Goal odd(Object fooCount);

    final Goal odd_baseCase(Count<Void> fooCount) {
      return Goals.UNIT;
    }

    final Goal odd_iterate(Count<Count<?>> fooCount) {
      // TODO: This is a StackOverflowError if there are infinitely many results and we don't use
      // DelayedGoal. Figure out why.
      return new DelayedGoal(odd(fooCount.oneLess().oneLess()));
    }
  }

  @Test
  public void testSimplePredicateIsCons() {
    IsCons isCons = new MakePredicates_MakePredicatesFunctionalTest_IsCons();
    new LogicAsserter()
        .stream(isCons.isCons(new Var()))
        .workUnits(1)
        .startSubst()
        .test();

    new LogicAsserter()
        .stream(isCons.isCons(0))
        .workUnits(0)
        .test();
  }

  @Test
  public void testMultiplePredicatesInOneClass() {
    MultiplePredicates predicates =
        new MakePredicates_MakePredicatesFunctionalTest_MultiplePredicates();
    new LogicAsserter()
        .stream(predicates.even(null))
        .workUnits(1)
        .finishes(true)
        .startSubst()
        .test();

    new LogicAsserter()
        .stream(predicates.odd(A))
        .addRequestedVar(A)
        .workUnits(9)
        .finishes(false)
        .startSubst()
        .put(A, Count.fromInt(1))
        .startSubst()
        .put(A, Count.fromInt(3))
        .startSubst()
        .put(A, Count.fromInt(5))
        .startSubst()
        .put(A, Count.fromInt(7))
        .startSubst()
        .put(A, Count.fromInt(9))
        .test();
  }

  @MakePredicates
  public static abstract class OverloadOnArity {
    public abstract Goal sum(Object a, Object b, Object total);

    final Goal sum_baseCase(Void a, Object b, Object total) {
      return same(b, total);
    }

    final Goal sum_iterate(Count<?> a, Object b, Count<?> total) {
      return sum(a.oneLess(), b, total.oneLess());
    }

    public abstract Goal sum(Object a, Object b, Object c, Object total);

    // Note that this need not be a separate method from the abstract one, but
    // we make it this way to exercise overloads on arity on predicate name.
    final Goal sum_impl(Object a, Object b, Object c, Object total) {
      Var ab = new Var();
      return conj(
          sum(a, b, ab),
          sum(ab, c, total));
    }
  }

  @Test
  public void overloadOnArity() {
    OverloadOnArity overload = new MakePredicates_MakePredicatesFunctionalTest_OverloadOnArity();

    new LogicAsserter()
        .stream(overload.sum(Count.fromInt(5), Count.fromInt(4), A))
        .addRequestedVar(A)
        .workUnits(1)
        .startSubst()
        .put(A, Count.fromInt(9))
        .test();

    new LogicAsserter()
        .stream(overload.sum(Count.fromInt(5), Count.fromInt(4), Count.fromInt(10), A))
        .addRequestedVar(A)
        .workUnits(1)
        .startSubst()
        .put(A, Count.fromInt(19))
        .test();
  }

  @MakePredicates
  public static abstract class HasNonDefaultConstructor {
    private final Cons<?, ?> domain;

    HasNonDefaultConstructor(List<?> domain) {
      this.domain = Cons.list(domain);
    }

    public final Goal inDomain(Object o) {
      return member(o, domain);
    }

    public abstract Goal member(Object o, Object seq);

    final Goal member_select(Object o, Cons<?, ?> seq) {
      return same(o, seq.car());
    }

    final Goal member_skip(Object o, Cons<?, ?> seq) {
      return member(o, seq.cdr());
    }
  }

  @MakePredicates
  public static abstract class HasDefaultAndNonDefaultConstructor {
    final int arg;

    HasDefaultAndNonDefaultConstructor(int arg) {
      this.arg = arg;
    }

    HasDefaultAndNonDefaultConstructor() {
      this(1000);
    }

    public abstract Goal unit(Object o);

    final Goal unit_impl(Object o) {
      return UNIT;
    }
  }

  @Test
  public void hasNonDefaultConstructor() {
    new LogicAsserter()
        .stream(
            new MakePredicates_MakePredicatesFunctionalTest_HasNonDefaultConstructor(
                Arrays.asList(1, 2, 3))
            .inDomain(A))
        .addRequestedVar(A)
        .startSubst()
        .put(A, 1)
        .startSubst()
        .put(A, 2)
        .startSubst()
        .put(A, 3)
        .test();
  }

  @Test
  public void hasDefaultAndNonDefaultConstructor() {
    Assert.assertEquals(1000,
        new MakePredicates_MakePredicatesFunctionalTest_HasDefaultAndNonDefaultConstructor().arg);
    Assert.assertEquals(42,
        new MakePredicates_MakePredicatesFunctionalTest_HasDefaultAndNonDefaultConstructor(42).arg);
  }

  @Test
  public void testToString() {
    HasNonDefaultConstructor nonDefaultConstructor =
        new MakePredicates_MakePredicatesFunctionalTest_HasNonDefaultConstructor(
            Arrays.asList(1, 2, 3));
    Assert.assertEquals("member(" + A + ", [1,2,3])", nonDefaultConstructor.inDomain(A).toString());

    OverloadOnArity overload = new MakePredicates_MakePredicatesFunctionalTest_OverloadOnArity();
    Assert.assertEquals("sum(" + A + ", " + B + ", Count{10})",
        overload.sum(A, B, Count.fromInt(10)).toString());

    IsCons isCons = new MakePredicates_MakePredicatesFunctionalTest_IsCons();
    Assert.assertEquals("isCons(" + A + ")", isCons.isCons(A).toString());
  }

  @MakePredicates
  public static abstract class ClausesThatReturnVoid {
    public abstract Goal isList(Object list);

    final void isList_end(Void list) {}

    final Goal isList_iterate(Cons<?, ?> list) {
      return isList(list.cdr());
    }

    public abstract Goal sameLength(Object list1, Object list2);

    final void sameLength_end(Void list1, Void list2) {}

    final Goal sameLength_iterate(Cons<?, ?> list1, Cons<?, ?> list2) {
      return sameLength(list1.cdr(), list2.cdr());
    }
  }

  @Test
  public void clausesThatReturnVoid() {
    ClausesThatReturnVoid clauses =
        new MakePredicates_MakePredicatesFunctionalTest_ClausesThatReturnVoid();
    new LogicAsserter()
        .stream(clauses.isList(Cons.s(1, 2, 3)))
        .workUnits(1)
        .finishes(true)
        .startSubst()
        .test();

    new LogicAsserter()
        .stream(clauses.sameLength(Cons.s(1, 2), Cons.s(4, 5)))
        .workUnits(1)
        .finishes(true)
        .startSubst()
        .test();
  }

  @MakePredicates
  public static abstract class PredicateWithZeroArity {
    public abstract Goal thePredicate();

    final Goal thePredicate_aEqualsB() {
      return same(A, B);
    }

    final Goal thePredicate_bIs42() {
      return same(B, 42);
    }
  }

  @Test
  public void predicateWithZeroArity() {
    PredicateWithZeroArity clauses =
        new MakePredicates_MakePredicatesFunctionalTest_PredicateWithZeroArity();
    new LogicAsserter()
        .stream(clauses.thePredicate())
        .addRequestedVar(A, B)
        .startSubst()
        .put(A, B)
        .startSubst()
        .put(B, 42)
        .test();
  }

  @MakePredicates
  public static abstract class PublicNonFinalClause {
    public abstract Goal thePredicate(Object a);

    public Goal thePredicate_foo(Object a) {
      return same(a, A);
    }
  }

  @Test
  public void publicNonFinalClause() {
    PublicNonFinalClause clauses =
        new MakePredicates_MakePredicatesFunctionalTest_PublicNonFinalClause();
    new LogicAsserter()
        .stream(clauses.thePredicate(B))
        .addRequestedVar(B)
        .startSubst()
        .put(B, A)
        .test();
  }
}
