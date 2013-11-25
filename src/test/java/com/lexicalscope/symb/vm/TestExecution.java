package com.lexicalscope.symb.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.lexicalscope.symb.vm.instructions.Terminate;
import com.lexicalscope.symb.vm.instructions.ops.StackFrameOp;
import com.lexicalscope.symb.vm.matchers.StateMatchers;
import com.lexicalscope.symb.vm.stackFrameOps.PopOperand;

public class TestExecution {
   @Test public void executeEmptyMainMethod()  {
      final State initial = State.initial("com/lexicalscope/symb/vm/EmptyStaticMethod", "main", "()V");

      assertNormalTerminiation(new Vm().execute(initial));
   }

   @Test public void executeStaticAddMethod()  {
      final State initial = State.initial("com/lexicalscope/symb/vm/StaticAddMethod", "add", "(II)I");
      initial.op(new StackFrameOp<Void>() {
		@Override
		public Void eval(final StackFrame stackFrame) {
			stackFrame.loadConst(1);
			stackFrame.loadConst(2);
			return null;
		}
	  });
      final State result = new Vm().execute(initial);

      assertThat(result.peekOperand(), equalTo((Object) 3));
      assertThat(result, StateMatchers.operandEqual(3));


      result.op(new PopOperand());
      assertNormalTerminiation(result);
   }

   private void assertNormalTerminiation(final State result) {
      assertThat(result.stack(), equalTo(new Stack(new Terminate())));
   }
}
