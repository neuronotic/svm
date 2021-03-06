package com.lexicalscope.symb.vm;

import com.lexicalscope.symb.vm.classloader.SClass;

public class DuplicateClassDefinitionException extends RuntimeException {
   public DuplicateClassDefinitionException(final SClass klass) {
      super(klass.name());
   }
}
