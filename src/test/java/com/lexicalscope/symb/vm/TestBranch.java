package com.lexicalscope.symb.vm;

import static com.lexicalscope.symb.vm.Vm.concreteVm;
import static com.lexicalscope.symb.vm.matchers.StateMatchers.normalTerminiationWithResult;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.lexicalscope.symb.vm.classloader.MethodInfo;
import com.lexicalscope.symb.vm.symbinstructions.SymbInstructionFactory;
import com.lexicalscope.symb.vm.symbinstructions.symbols.ConstSymbol;
import com.lexicalscope.symb.vm.symbinstructions.symbols.MultiplySymbol;
import com.lexicalscope.symb.vm.symbinstructions.symbols.Symbol;

public class TestBranch {
	MethodInfo absMethod = new MethodInfo(
			"com/lexicalscope/symb/vm/StaticAbsMethod", "abs", "(I)I");

	@Test
	public void concExecuteLeftBranch() {
		final Vm vm = concreteVm(absMethod, -2);
		assertThat(vm.execute(), normalTerminiationWithResult(2));
	}

	@Test
	public void concExecuteRightBranch() {
	   final Vm vm = concreteVm(absMethod, 2);
      assertThat(vm.execute(), normalTerminiationWithResult(2));
	}

	@Test
	public void symbExecuteBothBranches() {
		final SymbInstructionFactory instructionFactory = new SymbInstructionFactory();
		final Symbol symbol1 = instructionFactory.symbol();

		final Vm vm = Vm.vm(instructionFactory, absMethod, symbol1);
		assertThat(vm.execute(), normalTerminiationWithResult(new MultiplySymbol(symbol1, new ConstSymbol(-1))));
	}
}
