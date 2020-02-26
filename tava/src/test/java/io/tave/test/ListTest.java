package io.tave.test;

import io.tava.lang.Option;
import io.tava.lang.Tuples;
import io.tava.lang.Tuple2;
import io.tava.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ListTest {

    List<String> list = new ArrayList<>();

    @BeforeEach
    public void before() {
        list.add("a");
        list.add("a");
        list.add("b");
        list.add("a");
        list.add("c");
        list.add("d");
        list.add("a");
        list.add("e");
        list.add("e");
    }

    @Test
    public void testIntersect() {

        List<Integer> l = new ArrayList<>();
        l.add(1);
        Integer max = l.max((o1, o2) -> o1 - o2);
        Integer min = l.min((o1, o2) -> o1 - o2);

        List<String> list1 = new ArrayList<>();
        list1.add("a");
        list1.add("a");
        list1.add("b");
        list1.add("vb");

        list1.groupBy(item -> item).map((key, values) -> Tuples.of(key, values.size()));


        Collection<String> intersect = list.intersect(list1);
        Collection<String> intersect1 = list1.intersect(list);


        List<String> a = new ArrayList<>();
        a.add("a");
        a.add("a");
        a.add("b");

        Assertions.assertIterableEquals(intersect, a);
        Assertions.assertIterableEquals(intersect1, a);


    }

    @Test
    public void testDiff() {
        List<String> list1 = new ArrayList<>();
        list1.add("a");
        list1.add("a");
        list1.add("b");
        list1.add("vb");

        Collection<String> diff = list.diff(list1);
        List<String> a = new ArrayList<>();
        a.add("a");
        a.add("c");
        a.add("d");
        a.add("a");
        a.add("e");
        a.add("e");
        Assertions.assertIterableEquals(diff, a);

        Collection<String> diff1 = list1.diff(list);
        List<String> a1 = new ArrayList<>();
        a1.add("vb");

        Assertions.assertIterableEquals(diff1, a1);


    }


    @Test
    public void testFilter() {
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("a");
        expected.add("a");
        expected.add("a");

        List<String> actual = list.filter(item -> item.equals("a"));

        Assertions.assertIterableEquals(expected, actual);

    }

    @Test
    public void testFilterNot() {
        List<String> expected = new ArrayList<>();
        expected.add("b");
        expected.add("c");
        expected.add("d");
        expected.add("e");
        expected.add("e");

        List<String> actual = list.filterNot(item -> item.equals("a"));

        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testTake() {
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("a");
        expected.add("b");

        List<String> actual = list.take(3);

        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testTakeRight() {
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("e");
        expected.add("e");

        List<String> actual = list.takeRight(3);

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testTakeWhile() {
        List<String> actual = list.takeWhile(item -> item.equals("a"));
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("a");
        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testDrop() {
        List<String> actual = list.drop(2);
        List<String> expected = new ArrayList<>();
        expected.add("b");
        expected.add("a");
        expected.add("c");
        expected.add("d");
        expected.add("a");
        expected.add("e");
        expected.add("e");
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testDropRight() {
        List<String> actual = list.dropRight(2);
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("a");
        expected.add("b");
        expected.add("a");
        expected.add("c");
        expected.add("d");
        expected.add("a");
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testDropWhile() {
        List<String> actual = this.list.dropWhile(s -> s.equals("a"));
        List<String> expected = new ArrayList<>();
        expected.add("b");
        expected.add("a");
        expected.add("c");
        expected.add("d");
        expected.add("a");
        expected.add("e");
        expected.add("e");
        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testSlice() {
        List<String> expected = new ArrayList<>();
        expected.add("b");
        expected.add("a");
        expected.add("c");

        List<String> actual = this.list.slice(2, 5);

        Assertions.assertIterableEquals(expected, actual);

    }

    @Test
    public void testMap() {
        List<String> actual = this.list.map(s -> s + " " + s);

        List<String> expected = new ArrayList<>();

        expected.add("a a");
        expected.add("a a");
        expected.add("b b");
        expected.add("a a");
        expected.add("c c");
        expected.add("d d");
        expected.add("a a");
        expected.add("e e");
        expected.add("e e");

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testMapWithIndex() {
        List<String> actual = this.list.mapWithIndex((index, s) -> s + " " + index);
        List<String> expected = new ArrayList<>();

        expected.add("a 0");
        expected.add("a 1");
        expected.add("b 2");
        expected.add("a 3");
        expected.add("c 4");
        expected.add("d 5");
        expected.add("a 6");
        expected.add("e 7");
        expected.add("e 8");

        Assertions.assertIterableEquals(expected, actual);


    }

    @Test
    public void testFlatMap() {
        List<String> actual = this.list.flatMap(s -> {
            List<String> list = new ArrayList<>();
            list.add(s);
            list.add(s + " " + s);
            return list;
        });
        List<String> expected = new ArrayList<>();

        expected.add("a");
        expected.add("a a");
        expected.add("a");
        expected.add("a a");
        expected.add("b");
        expected.add("b b");
        expected.add("a");
        expected.add("a a");
        expected.add("c");
        expected.add("c c");
        expected.add("d");
        expected.add("d d");
        expected.add("a");
        expected.add("a a");
        expected.add("e");
        expected.add("e e");
        expected.add("e");
        expected.add("e e");

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testZipWithIndex() {
        List<Tuple2<String, Integer>> actual = this.list.zipWithIndex();
        List<Tuple2<String, Integer>> expected = new ArrayList<>();
        expected.add(Tuples.of("a", 0));
        expected.add(Tuples.of("a", 1));
        expected.add(Tuples.of("b", 2));
        expected.add(Tuples.of("a", 3));
        expected.add(Tuples.of("c", 4));
        expected.add(Tuples.of("d", 5));
        expected.add(Tuples.of("a", 6));
        expected.add(Tuples.of("e", 7));
        expected.add(Tuples.of("e", 8));

        Assertions.assertIterableEquals(expected, actual);

    }


    @Test
    public void testSpan() {
        Tuple2<? extends List<String>, ? extends List<String>> actual = this.list.span(s -> s.equals("a"));

        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();

        list1.add("a");
        list1.add("a");
        list2.add("b");
        list2.add("a");
        list2.add("c");
        list2.add("d");
        list2.add("a");
        list2.add("e");
        list2.add("e");
        Assertions.assertEquals(Tuples.of(list1, list2), actual);

    }

    @Test
    public void testSplitAt() {
        Tuple2<? extends List<String>, ? extends List<String>> actual = this.list.splitAt(3);
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("a");
        list1.add("a");
        list1.add("b");
        list2.add("a");
        list2.add("c");
        list2.add("d");
        list2.add("a");
        list2.add("e");
        list2.add("e");

        Assertions.assertEquals(Tuples.of(list1, list2), actual);
    }

    @Test
    public void testForall() {
        boolean a = this.list.forall(s -> s.length() == 1);
        Assertions.assertTrue(a);
    }

    @Test
    public void testExists() {
        boolean a = this.list.exists(s -> s.equals("a"));
        Assertions.assertTrue(a);
    }

    @Test
    public void testCount() {
        int count = this.list.count(s -> s.equals("a"));
        Assertions.assertEquals(count, 4);
    }

    @Test
    public void testFind() {
        Option<String> s1 = this.list.find(s -> s == "b");
        Assertions.assertEquals(s1.get(), "b");
    }


    @Test
    public void testFoldLeft() {
        Map<Integer, ? extends List<String>> integerMap = this.list.groupBy(String::length);

        StringBuilder stringBuilder = this.list.foldLeft(new StringBuilder(), StringBuilder::append);
        Assertions.assertEquals(stringBuilder.toString(), "aabacdaee");
    }


    @Test
    public void testFoldRight() {
        String expected = this.list.foldRight(new StringBuilder(), StringBuilder::append).toString();
        Assertions.assertEquals(expected, "eeadcabaa");
    }

    @Test
    public void testMkString() {
        Assertions.assertEquals(this.list.mkString("[", ",", "]"), "[a,a,b,a,c,d,a,e,e]");
        Assertions.assertEquals(this.list.mkString(","), "a,a,b,a,c,d,a,e,e");
        Assertions.assertEquals(this.list.mkString(), "aabacdaee");
    }

    @Test
    public void testReverse() {

        List<String> reverse = this.list.reverse();
        List<String> expected = new ArrayList<>();

        this.list.add("a");
        this.list.add("a");
        this.list.add("b");
        this.list.add("a");
        this.list.add("c");
        this.list.add("d");
        this.list.add("a");
        this.list.add("e");
        this.list.add("e");

        expected.add("e");
        expected.add("e");
        expected.add("a");
        expected.add("d");
        expected.add("c");
        expected.add("a");
        expected.add("b");
        expected.add("a");
        expected.add("a");

        Assertions.assertIterableEquals(reverse, expected);
    }


}
