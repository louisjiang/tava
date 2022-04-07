package io.tava.util;

import io.tava.util.builder.ArrayListBuilder;
import io.tava.util.builder.CollectionBuilder;

import java.util.AbstractList;
import java.util.Objects;
import java.util.RandomAccess;

public class Arrays {

    public static <E> List<E> asList(E[] array) {
        return new Arrays.ArrayList<E>(array);
    }

    static class ArrayList<E> extends AbstractList<E> implements RandomAccess, List<E> {

        private E[] array;

        ArrayList(E[] array) {
            this.array = Objects.requireNonNull(array);
        }

        @Override
        public E get(int index) {
            if (index >= size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            }
            return array[index];
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
            return (CollectionBuilder<E0, C0>) new ArrayListBuilder<E0>();
        }
    }

}
