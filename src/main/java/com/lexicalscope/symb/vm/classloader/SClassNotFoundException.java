package com.lexicalscope.symb.vm.classloader;

public class SClassNotFoundException extends RuntimeException {
   public SClassNotFoundException(final String name) {
      super(name);
   }
}
