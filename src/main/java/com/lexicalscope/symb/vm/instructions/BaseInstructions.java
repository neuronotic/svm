package com.lexicalscope.symb.vm.instructions;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.lexicalscope.symb.vm.Heap;
import com.lexicalscope.symb.vm.HeapVop;
import com.lexicalscope.symb.vm.Instruction;
import com.lexicalscope.symb.vm.Stack;
import com.lexicalscope.symb.vm.StackFrame;
import com.lexicalscope.symb.vm.State;
import com.lexicalscope.symb.vm.Vm;
import com.lexicalscope.symb.vm.classloader.SClassLoader;
import com.lexicalscope.symb.vm.classloader.SMethod;
import com.lexicalscope.symb.vm.concinstructions.StateTransformer;
import com.lexicalscope.symb.vm.instructions.ops.BinaryOp;
import com.lexicalscope.symb.vm.instructions.ops.BinaryOperator;
import com.lexicalscope.symb.vm.instructions.ops.DupOp;
import com.lexicalscope.symb.vm.instructions.ops.Load;
import com.lexicalscope.symb.vm.instructions.ops.StackOp;
import com.lexicalscope.symb.vm.instructions.ops.Store;
import com.lexicalscope.symb.vm.instructions.transformers.StackFrameTransformer;

public final class BaseInstructions implements Instructions {
	private final InstructionFactory instructionFactory;

	public BaseInstructions(final InstructionFactory instructionFactory) {
		this.instructionFactory = instructionFactory;
	}

	@Override
	public Instruction instructionFor(final AbstractInsnNode abstractInsnNode) {
		if (abstractInsnNode == null)
			return new Terminate();

		switch (abstractInsnNode.getType()) {
		case AbstractInsnNode.LABEL:
			return new Label((LabelNode) abstractInsnNode);
		case AbstractInsnNode.LINE:
			return new LineNumber((LineNumberNode) abstractInsnNode);
		case AbstractInsnNode.FRAME:
			return new Frame((FrameNode) abstractInsnNode);
		case AbstractInsnNode.VAR_INSN:
			final VarInsnNode varInsnNode = (VarInsnNode) abstractInsnNode;
			switch (abstractInsnNode.getOpcode()) {
			case Opcodes.ILOAD:
			case Opcodes.ALOAD:
			   return load(varInsnNode);
			case Opcodes.ISTORE:
		   case Opcodes.ASTORE:
		      return store(varInsnNode);
         }
			break;
		case AbstractInsnNode.FIELD_INSN:
		   final FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNode;
         switch (abstractInsnNode.getOpcode()) {
            case Opcodes.PUTFIELD:
               return new LinearInstruction(abstractInsnNode, new StateTransformer() {
                  @Override
                  public void transform(final State state, final Instruction nextInstruction) {
                     state.op(new HeapVop(){
                        @Override
                        public void eval(final StackFrame stackFrame, final Heap heap) {
                           stackFrame.advance(nextInstruction);

                           final Object val = stackFrame.pop();
                           final Object obj = stackFrame.pop();

                           heap.put(obj, fieldKey(fieldInsnNode), val);
                        }});
                  }

                  @Override
                  public String toString() {
                     return "PUTFIELD " + fieldKey(fieldInsnNode);
                  }
               });
            case Opcodes.GETFIELD:
               return new LinearInstruction(abstractInsnNode, new StateTransformer() {
                  @Override
                  public void transform(final State state, final Instruction nextInstruction) {
                     state.op(new HeapVop(){
                        @Override
                        public void eval(final StackFrame stackFrame, final Heap heap) {
                           stackFrame.advance(nextInstruction);

                           final Object obj = stackFrame.pop();

                           stackFrame.push(heap.get(obj, fieldKey(fieldInsnNode)));
                        }});
                  }

                  @Override
                  public String toString() {
                     return "GETFIELD " + fieldKey(fieldInsnNode);
                  }
               });
         }
         break;
		case AbstractInsnNode.INSN:
		   final InsnNode insnNode = (InsnNode) abstractInsnNode;
         switch (abstractInsnNode.getOpcode()) {
			case Opcodes.RETURN:
				return new Return(0);
			case Opcodes.IRETURN:
				return new Return(1);
			case Opcodes.IADD:
				return binaryOp(abstractInsnNode, instructionFactory.iaddOperation());
			case Opcodes.IMUL:
				return binaryOp(abstractInsnNode, instructionFactory.imulOperation());
        case Opcodes.ISUB:
            return binaryOp(abstractInsnNode, instructionFactory.isubOperation());
			case Opcodes.DUP:
				return new LinearInstruction(insnNode,
						new StackFrameTransformer(new DupOp()));
			case Opcodes.ICONST_M1:
			   return iconst(insnNode, -1);
			case Opcodes.ICONST_0:
            return iconst(insnNode, 0);
			case Opcodes.ICONST_1:
            return iconst(insnNode, 1);
			case Opcodes.ICONST_2:
            return iconst(insnNode, 2);
			case Opcodes.ICONST_3:
            return iconst(insnNode, 3);
			case Opcodes.ICONST_4:
            return iconst(insnNode, 4);
			case Opcodes.ICONST_5:
            return iconst(insnNode, 5);
			}
			break;
		case AbstractInsnNode.INT_INSN:
         final IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNode;
         switch (abstractInsnNode.getOpcode()) {
            case Opcodes.BIPUSH:
               return iconst(intInsnNode, intInsnNode.operand);
         }
         break;
		case AbstractInsnNode.TYPE_INSN:
         final TypeInsnNode typeInsnNode = (TypeInsnNode) abstractInsnNode;
         switch (abstractInsnNode.getOpcode()) {
         case Opcodes.NEW:
            return new LinearInstruction(abstractInsnNode, new StateTransformer() {
               @Override
               public void transform(final State state, final Instruction nextInstruction) {
                  state.op(new HeapVop(){
                     @Override
                     public void eval(final StackFrame stackFrame, final Heap heap) {
                        stackFrame.advance(nextInstruction);
                        stackFrame.push(heap.newObject());
                     }});
               }

               @Override
               public String toString() {
                  return String.format("NEW %s", typeInsnNode.desc);
               }
            });
         }
			break;
		case AbstractInsnNode.JUMP_INSN:
			final JumpInsnNode jumpInsnNode = (JumpInsnNode) abstractInsnNode;
			switch (jumpInsnNode.getOpcode()) {
			case Opcodes.IFGE:
				return instructionFactory.branchIfge(jumpInsnNode);
			}
			break;
		case AbstractInsnNode.METHOD_INSN:
		   final MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
		   switch (abstractInsnNode.getOpcode()) {
		   case Opcodes.INVOKESPECIAL:
		      return new Instruction() {
               @Override
               public void eval(final SClassLoader cl, final Vm vm, final State state) {
                  final SMethod targetMethod = cl.loadMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);

                  state.op(new StackOp<Void>(){
                  @Override
                  public Void eval(final Stack stack) {
                     stack.pushFrame(cl.instructionFor(methodInsnNode.getNext()), targetMethod, targetMethod.argSize());
                     return null;
                  }});
               }

               @Override
               public String toString() {
                  return String.format("INVOKESPECIAL %s", methodInsnNode.desc);
               }
            };
		   case Opcodes.INVOKEVIRTUAL:
            return new Instruction() {
               @Override
               public void eval(final SClassLoader cl, final Vm vm, final State state) {
                  final SMethod targetMethod = cl.loadMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);

                  // TODO[tim]: resolve overridden methods

                  state.op(new StackOp<Void>(){
                  @Override
                  public Void eval(final Stack stack) {
                     stack.pushFrame(cl.instructionFor(methodInsnNode.getNext()), targetMethod, targetMethod.argSize());
                     return null;
                  }});
               }

               @Override
               public String toString() {
                  return String.format("INVOKESPECIAL %s", methodInsnNode.desc);
               }
            };
		   }
		   break;
		}

		return new UnsupportedInstruction(abstractInsnNode);
	}

   private LinearInstruction store(final VarInsnNode varInsnNode) {
      return new LinearInstruction(varInsnNode,
         new StackFrameTransformer(new Store(varInsnNode.var)));
   }

   private Instruction iconst(final AbstractInsnNode insnNode, final int constVal) {
      return nullary(insnNode, instructionFactory.iconst(constVal));
   }

   private LinearInstruction nullary(final AbstractInsnNode insnNode, final NullaryOperator nullary) {
      return new LinearInstruction(insnNode,
            new StackFrameTransformer(new NullaryOp(
                  nullary)));
   }

   private LinearInstruction load(final VarInsnNode varInsnNode) {
      return new LinearInstruction(varInsnNode,
      		new StackFrameTransformer(new Load(varInsnNode.var)));
   }

	private LinearInstruction binaryOp(final AbstractInsnNode abstractInsnNode,
			final BinaryOperator addOperation) {
		return new LinearInstruction(abstractInsnNode,
				new StackFrameTransformer(new BinaryOp(
						addOperation)));
	}

   private static String fieldKey(final FieldInsnNode fieldInsnNode) {
      return String.format("%s.%s:%s", fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc);
   }
}
