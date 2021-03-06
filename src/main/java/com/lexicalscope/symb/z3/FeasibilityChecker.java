package com.lexicalscope.symb.z3;

import java.io.Closeable;
import java.util.HashMap;

import com.lexicalscope.symb.vm.symbinstructions.Pc;
import com.lexicalscope.symb.vm.symbinstructions.symbols.Symbol;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class FeasibilityChecker implements Closeable {
   // TODO[tim]: use z3 stack for efficency
   private final Context ctx;

   public FeasibilityChecker() {
      try {
         Context.ToggleWarningMessages(true);
      } catch (final Z3Exception e) {
         throw new RuntimeException("could not enable warning messages", e);
      }
      //Log.open("test.log");

      final HashMap<String, String> cfg = new HashMap<String, String>();
      cfg.put("model", "true");
      try {
         ctx = new Context(cfg);
      } catch (final Z3Exception e) {
         throw new RuntimeException("could not create context", e);
      }
   }

   public boolean checkZ3IsWorking() throws Z3Exception  {
      final IntExpr x = ctx.mkIntConst("x");
      final IntExpr y = ctx.mkIntConst("y");
      final IntExpr one = ctx.mkInt(1);
      final IntExpr two = ctx.mkInt(2);

      final ArithExpr y_plus_one = ctx.mkAdd(y, one);

      final BoolExpr c1 = ctx.mkLt(x, y_plus_one);
      final BoolExpr c2 = ctx.mkGt(x, two);

      final BoolExpr q = ctx.mkAnd(c1, c2);

      return check(q);
   }

   private boolean check(final BoolExpr expr) {
      try {
         final Solver s = ctx.mkSolver();
         try {
            s.add(expr);
            return s.check().equals(Status.SATISFIABLE);
         } finally {
            s.dispose();
         }
      } catch (final Z3Exception e) {
         throw new RuntimeException("unable to check satisfiablility", e);
      }
   }

   public boolean check(final Pc pc) {
      try {
         return check(pc.accept(new PcToZ3(ctx)));
      } catch (final Z3Exception e) {
         throw new RuntimeException("could not map PC to Z3", e);
      }
   }

   /**
    * Be kind, rewind.
    */
   @Override
   public void close() {
      ctx.dispose();
   }

   public int simplifyBv32Expr(final Symbol symbol) {
      try {
         // problem with overflow handling
         // http://stackoverflow.com/questions/20383866/z3-modeling-java-twos-complement-overflow-and-underflow-in-z3-bit-vector-addit
         return (int) ((BitVecNum) symbol.accept(new SymbolToExpr(ctx)).simplify()).getLong();
      } catch (final Z3Exception e) {
         throw new RuntimeException("unable to simplify " + symbol, e);
      }
   }

//   This uses a bit blasting tactic...
//
//   public int simplifyBv32Expr(final Symbol symbol) {
//      try {
//         final Solver s = ctx.mkSolver();
//         try {
//            final Tactic simplify = ctx.mkTactic("simplify");
//            final Tactic solveEquations = ctx.mkTactic("solve-eqs");
//            final Tactic bitBlast = ctx.mkTactic("bit-blast");
//            final Tactic propositional = ctx.mkTactic("sat");
//
//            final Tactic tactic = ctx.parAndThen(simplify, ctx.parAndThen(solveEquations, ctx.parAndThen(bitBlast, propositional)));
//
//            final Goal goal = ctx.mkGoal(true, false, false);
//            goal.add(ctx.mkEq(ctx.mkBVConst("__res", 32), symbol.accept(new SymbolToExpr(ctx))));
//
//
//            final ApplyResult ar = tactic.apply(goal);
//
//            for (final BoolExpr e : ar.getSubgoals()[0].getFormulas())
//                s.add(e);
//            final Status q = s.check();
//            System.out.println("Solver says: " + q);
//            System.out.println("Model: \n" + s.getModel());
//            System.out.println("Converted Model: \n"
//                    + ar.convertModel(0, s.getModel()));
//
//            final Expr unsimple = symbol.accept(new SymbolToExpr(ctx));
//            System.out.println("!!!!! " + unsimple);
//            final Expr simplified = unsimple.simplify().simplify();
//            System.out.println("!!!!! " + simplified);
//            return 7;
//         } finally {
//            s.dispose();
//         }
//      } catch (final Z3Exception e) {
//         throw new RuntimeException("unable to chec satisfiablility", e);
//      }
//   }
}
