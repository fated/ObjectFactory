package com.amazon.df.object.util;

public final class Throwables {

    /**
     * Google java sneaky throws.
     * Allows you to throw a checked exception without declaring it.  Exploits a
     * gap in the language specification when combining generics with checked exceptions.
     * Exists solely so you don't have to artifically wrap an exception in yet another
     * exception simply to get it past the compiler.
     */
    public static RuntimeException sneakyThrow(Throwable ex) {
        return unsafeCastAndRethrow(ex);
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> X unsafeCastAndRethrow(Throwable ex) throws X {
        throw (X) ex;
    }

}
