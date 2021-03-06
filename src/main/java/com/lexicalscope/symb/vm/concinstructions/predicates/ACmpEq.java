package com.lexicalscope.symb.vm.concinstructions.predicates;

import com.lexicalscope.symb.vm.Heap;
import com.lexicalscope.symb.vm.Stack;
import com.lexicalscope.symb.vm.StackFrame;
import com.lexicalscope.symb.vm.Statics;
import com.lexicalscope.symb.vm.concinstructions.BranchPredicate;

public class ACmpEq implements BranchPredicate {
   @Override public Boolean eval(final StackFrame stackFrame, final Stack stack, final Heap heap, final Statics statics) {
      final Object value2 = stackFrame.pop();
      final Object value1 = stackFrame.pop();

      return value1.equals(value2);
   }

   @Override public String toString() {
      return "IF_ACMPEQ";
   }
}
