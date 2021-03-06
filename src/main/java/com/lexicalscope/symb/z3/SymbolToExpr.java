package com.lexicalscope.symb.z3;

import com.lexicalscope.symb.vm.symbinstructions.symbols.Symbol;
import com.lexicalscope.symb.vm.symbinstructions.symbols.SymbolVisitor;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public class SymbolToExpr implements SymbolVisitor<Expr, Z3Exception> {
   private final Context ctx;

   public SymbolToExpr(final Context ctx) {
      this.ctx = ctx;
   }

   @Override
   public BitVecExpr add(final Symbol left, final Symbol right) throws Z3Exception {
      return ctx.mkBVAdd((BitVecExpr) left.accept(this), (BitVecExpr) right.accept(this));
   }

   @Override
   public BitVecExpr sub(final Symbol left, final Symbol right) throws Z3Exception {
      return ctx.mkBVSub((BitVecExpr) left.accept(this), (BitVecExpr) right.accept(this));
   }

   @Override
   public BitVecExpr mul(final Symbol left, final Symbol right) throws Z3Exception {
      return ctx.mkBVMul((BitVecExpr) left.accept(this), (BitVecExpr) right.accept(this));
   }

   @Override
   public BitVecExpr constant(final int val) throws Z3Exception {
      return ctx.mkBV(val, 32);
   }

   @Override
   public Expr ge(final Symbol val) throws Z3Exception {
      return ctx.mkBVSGE((BitVecExpr) val.accept(this), constant(0));
   }

   @Override
   public Expr not(final Symbol val) throws Z3Exception {
      return ctx.mkNot((BoolExpr) val.accept(this));
   }

   @Override
   public Expr intSymbol(final int name) throws Z3Exception {
      return ctx.mkBVConst("i" + name, 32);
   }
}
