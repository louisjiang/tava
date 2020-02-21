package io.tava.util.builder;

import io.tava.util.ArrayList;

public final class ArrayListBuilder<E> implements CollectionBuilder<E, ArrayList<E>> {

    private final ArrayList<E> list = new ArrayList<>();

    @Override
    public ArrayList<E> build() {
        return list;
    }

}