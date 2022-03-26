package org.imp.jvm;

import org.imp.jvm.domain.Identifier;
import org.imp.jvm.types.ImpType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    /**
     * Is Object o an instance of one of the following classes?
     */
    public static boolean instanceOfOne(Object o, Class<?>... clazz) {
        for (Class<?> c : clazz) {
            if (c.isInstance(o)) {
                return true;
            }
        }
        return false;
    }

    public static String parameterString(List<? extends Identifier> parameters) {
        return parameters.stream().map(p -> p.name + " " + p.type).collect(Collectors.joining(", "));
    }

    public static String parameterString(String[] names, ImpType[] types) {
        return zipMap(Arrays.asList(names), Arrays.asList(types), (name, type) -> name + " " + type).collect(Collectors.joining(", "));
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
}
