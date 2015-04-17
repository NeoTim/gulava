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
package musubi.processor;

/**
 * Constants containing fully-qualified names of classes of concern to this processor.
 */
public class ClassNames {
  public static final String MAKE_LOGIC_VALUE = "musubi.annotation.MakeLogicValue";

  public static final String MAKE_GOAL_FACTORY = "musubi.annotation.MakeGoalFactory";

  public static final String MAKE_PREDICATES = "musubi.annotation.MakePredicates";

  public static final String DELAYED_GOAL = "musubi.DelayedGoal";

  public static final String LOGIC_VALUE = "musubi.LogicValue";

  public static final String GOAL = "musubi.Goal";

  public static final String GOALS = "musubi.Goals";

  public static final String STREAM = "musubi.Stream";

  public static final String SUBST = "musubi.Subst";

  public static final String REPLACER = "musubi.Replacer";

  public static final String VAR = "musubi.Var";

  private ClassNames() {}
}
