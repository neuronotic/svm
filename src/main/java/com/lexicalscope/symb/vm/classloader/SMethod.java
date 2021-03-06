package com.lexicalscope.symb.vm.classloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.lexicalscope.symb.vm.Instruction;
import com.lexicalscope.symb.vm.InstructionInternalNode;
import com.lexicalscope.symb.vm.InstructionNode;
import com.lexicalscope.symb.vm.instructions.Instructions;
import com.lexicalscope.symb.vm.instructions.Instructions.InstructionSink;

public class SMethod {
   private final SClassLoader classLoader;
   private final SMethodName methodName;
	private final MethodNode method;
	private final Instructions instructions;

   private InstructionNode entryPoint;
   private int maxLocals;
   private int maxStack;

	public SMethod(
	      final SClassLoader classLoader,
	      final SMethodName methodName,
	      final Instructions instructions,
	      final MethodNode method) {
		this.classLoader = classLoader;
      this.methodName = methodName;
      this.instructions = instructions;
		this.method = method;
	}

	public int maxLocals() {
	   link();
		return maxLocals;
	}

	public int maxStack() {
	   link();
		return maxStack;
	}

	public InstructionNode entry() {
	   link();
		return entryPoint;
	}

	private void link() {
	   if(entryPoint != null) return;

	   if((method.access & Opcodes.ACC_NATIVE) != 0) {
	      linkNativeMethod();
	   } else {
	      linkJavaMethod();
	   }
   }

   private void linkNativeMethod() {
      final MethodBody resolved = classLoader.resolveNative(methodName);

      maxLocals = resolved.maxLocals();
      maxStack = resolved.maxStack();
      entryPoint = resolved.entryPoint();
   }

   private void linkJavaMethod() {
      final List<AbstractInsnNode> unlinked = new ArrayList<>();
	   final Map<AbstractInsnNode, InstructionNode> linked = new LinkedHashMap<>();
	   final InstructionNode[] prev = new InstructionNode[1];

	   final InstructionSink instructionSink = new InstructionSink() {
         @Override public void nextInstruction(final AbstractInsnNode asmInstruction, final Instruction instruction) {
            final InstructionNode node = new InstructionInternalNode(instruction);
            for (final AbstractInsnNode unlinkedInstruction : unlinked) {
               linked.put(unlinkedInstruction, node);
            }
            unlinked.clear();
            linked.put(asmInstruction, node);
            if(prev[0] != null) prev[0].next(node);
            prev[0] = node;
         }

         @Override public void noInstruction(final AbstractInsnNode abstractInsnNode) {
            unlinked.add(abstractInsnNode);
         }
	   };

	   AbstractInsnNode asmInstruction = getEntryPoint();
	   while(asmInstruction != null) {
	      instructions.instructionFor(asmInstruction, instructionSink);
	      asmInstruction = asmInstruction.getNext();
	   }

	   for (final Entry<AbstractInsnNode, InstructionNode> entry : linked.entrySet()) {
         if(entry.getKey() instanceof JumpInsnNode) {
            final JumpInsnNode asmJumpInstruction = (JumpInsnNode) entry.getKey();
            final AbstractInsnNode asmInstructionAfterTargetLabel = asmJumpInstruction.label.getNext();
            final InstructionNode jmpTarget = linked.get(asmInstructionAfterTargetLabel);

            assert asmInstructionAfterTargetLabel != null;
            assert jmpTarget != null : asmInstructionAfterTargetLabel;

            entry.getValue().jmpTarget(jmpTarget);
         }
      }

      maxLocals = method.maxLocals;
      maxStack = method.maxStack;
	   entryPoint = linked.values().iterator().next();
   }

   private AbstractInsnNode getEntryPoint() {
      return method.instructions.get(0);
   }

   public int argSize() {
		return Type.getMethodType(method.desc).getArgumentsAndReturnSizes() >> 2;
	}
}
