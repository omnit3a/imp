package org.imp.jvm;

import org.imp.jvm.types.ImpType;
import org.javatuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {
    public static <A, B, O> Stream<O> zipMap(List<? extends A> a, List<? extends B> b, BiFunction<A, B, ? extends O> lambda) throws ArrayIndexOutOfBoundsException {
        var l = new ArrayList<O>();
        if (a.size() == b.size()) {
            var i1 = a.iterator();
            var i2 = b.iterator();
            while (i1.hasNext() && i2.hasNext()) {
                var o = lambda.apply(i1.next(), i2.next());
                l.add(o);
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("Can't zip two Lists with differing number of elements.");
        }
        return l.stream();
    }

    public static <A, B, O> void zip(List<? extends A> a, List<? extends B> b, BiConsumer<A, ? super B> lambda) throws ArrayIndexOutOfBoundsException {
        if (a.size() == b.size()) {
            var i1 = a.iterator();
            var i2 = b.iterator();
            while (i1.hasNext() && i2.hasNext()) {
                lambda.accept(i1.next(), i2.next());
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("Can't zip two Lists with differing number of elements.");
        }
    }

    public static void exit(String message, int code) {
        System.err.println(message);
        System.exit(code);
    }

    @SafeVarargs
    public static <T> List<T> list(T... i) {
        return Arrays.asList(i);
    }

    /**
     * Type-safe method to generate a Set based on varargs.
     *
     * @param varargs supplied to set
     * @param <T>     type parameter
     * @return new Set
     */
    @SafeVarargs
    public static <T> Set<T> set(T... varargs) {
        return Set.of(varargs);
    }

    @SafeVarargs
    public static <T> Map<T, Long> countedSet(T... varargs) {
        return Arrays.stream(varargs).collect(Collectors.groupingBy(s -> s,
                Collectors.counting()));
    }


    public static String parameterString(List<Pair<String, ImpType>> parameters) {
        return parameters.stream().map(p -> p.getValue0() + " " + p.getValue1()).collect(Collectors.joining(", "));
    }

    public static void println(Object o) {
        System.out.println(o);
    }

    /**
     * Find class on classpath.
     *
     * @param externalName descriptor
     */
    public static Optional<Class<?>> getClass(String externalName) {
        try {
            Class<?> c = Class.forName(externalName);
            return Optional.of(c);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }


    /**
     * Join a list in normal language similar to JavaScript's Intl.ListFormat class.
     *
     * @return ex. Something, Another Object, Penultimate Object and Final Object.
     */
    public static <T> String joinConjunction(Collection<T> collection) {
        var strList = collection.stream().map(Objects::toString).toList();
        if (strList.size() == 0) return "";
        if (strList.size() == 1) return strList.get(0);
        if (strList.size() == 2) return String.join(" and ", strList);
        return String.join(", ", strList.subList(0, strList.size() - 1)) + " and " + strList.get(strList.size() - 1);
    }


    public static Class<?> convert(Class<?> c) {
        if (c == Integer.class) return int.class;
        if (c == Float.class) return float.class;
        if (c == Boolean.class) return boolean.class;

        return null;
    }

    // According to https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3
    public static String getMethodDescriptor(Collection<Pair<String, ImpType>> parameters, ImpType returnType) {
        String parametersDescriptor = parameters.stream()
                .map(parameter -> parameter.getValue1().getDescriptor())
                .collect(Collectors.joining("", "(", ")"));
        String returnDescriptor = returnType.getDescriptor();
        return parametersDescriptor + returnDescriptor;
    }
}
