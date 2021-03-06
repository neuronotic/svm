package com.lexicalscope.symb.z3;

import org.junit.Rule;
import org.junit.Test;

import com.lexicalscope.junit.junitautocloseable.AutoCloseRule;
import com.microsoft.z3.Z3Exception;

public class TestFeasibilityChecker {
   @Rule public AutoCloseRule autoCloseRule = new AutoCloseRule();
   private final FeasibilityChecker feasbilityChecker = new FeasibilityChecker();

   @Test
   public void testZ3IsWorking() throws Z3Exception {
      feasbilityChecker.checkZ3IsWorking();
   }
}
