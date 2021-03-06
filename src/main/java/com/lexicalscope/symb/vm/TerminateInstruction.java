package com.lexicalscope.symb.vm;

import com.lexicalscope.symb.vm.instructions.TerminationException;

public class TerminateInstruction implements InstructionNode {
   @Override public void eval(final Vm vm, final StateImpl state) {
      throw new TerminationException(state);
   }

   @Override public InstructionNode next(final InstructionNode instruction) {
      throw new IllegalStateException("TERMINATE has no successor");
   }

   @Override public void jmpTarget(final InstructionNode instruction) {
      throw new UnsupportedOperationException();
   }

   @Override public InstructionNode next() {
      throw new IllegalStateException("TERMINATE has no successor");
   }

   @Override public InstructionNode jmpTarget() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(final Object obj) {
      return obj != null && obj.getClass().equals(this.getClass());
   }

   @Override
   public int hashCode() {
      return this.getClass().hashCode();
   }

   @Override
   public String toString() {
      return "TERMINATE";
   }
}
