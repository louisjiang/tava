package io.tave.test;

import io.tava.lang.Option;
import io.tava.lang.Tuples;
import io.tava.lang.Tuple2;
import io.tava.util.HashSet;
import io.tava.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SetTest {

    Set<String> set = new HashSet<>();

    @BeforeEach
    public void before() {
        set.add("a");
        set.add("b");
        set.add("c");
        set.add("d");
        set.add("e");
    }

    @Test
    public void testFilter() {
        Set<String> expected = new HashSet<>();
        expected.add("a");
        Set<String> actual = set.filter(item -> item.equals("a"));
        Assertions.assertIterableEquals(expected, actual);

    }

    @Test
    public void testFilterNot() {
        Set<String> expected = new HashSet<>();
        expected.add("b");
        expected.add("c");
        expected.add("d");
        expected.add("e");

        Set<String> actual = set.filterNot(item -> item.equals("a"));

        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testTake() {
        Set<String> expected = new HashSet<>();
        expected.add("a");
        expected.add("b");
        expected.add("c");

        Set<String> actual = set.take(3);

        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testTakeRight() {
        Set<String> expected = new HashSet<>();
        expected.add("c");
        expected.add("d");
        expected.add("e");

        Set<String> actual = set.takeRight(3);


        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testTakeWhile() {
        Set<String> actual = set.takeWhile(item -> item.equals("a"));
        Set<String> expected = new HashSet<>();
        expected.add("a");
        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testDrop() {
        Set<String> actual = set.drop(2);
        Set<String> expected = new HashSet<>();
        expected.add("c");
        expected.add("d");
        expected.add("e");
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testDropRight() {
        Set<String> actual = set.dropRight(2);
        Set<String> expected = new HashSet<>();
        expected.add("a");
        expected.add("b");
        expected.add("c");
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testDropWhile() {
        Set<String> actual = this.set.dropWhile(s -> s.equals("a"));
        Set<String> expected = new HashSet<>();
        expected.add("b");
        expected.add("c");
        expected.add("d");
        expected.add("e");
        Assertions.assertIterableEquals(expected, actual);
    }


    @Test
    public void testSlice() {
        Set<String> expected = new HashSet<>();
        expected.add("c");
        expected.add("d");
        expected.add("e");

        Set<String> actual = this.set.slice(2, 5);

        Assertions.assertIterableEquals(expected, actual);

    }

    @Test
    public void testMap() {
        Set<String> actual = this.set.map(s -> s + " " + s);

        Set<String> expected = new HashSet<>();

        expected.add("a a");
        expected.add("b b");
        expected.add("c c");
        expected.add("d d");
        expected.add("e e");

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testMapWithIndex() {
        Set<String> actual = this.set.mapWithIndex((index, s) -> s + " " + index);
        Set<String> expected = new HashSet<>();

        expected.add("a 0");
        expected.add("b 1");
        expected.add("c 2");
        expected.add("d 3");
        expected.add("e 4");

        Assertions.assertIterableEquals(expected, actual);


    }

    @Test
    public void testFlatMap() {
        Set<String> actual = this.set.flatMap(s -> {
            Set<String> list = new HashSet<>();
            list.add(s);
            list.add(s + " " + s);
            return list;
        });
        Set<String> expected = new HashSet<>();

        expected.add("a");
        expected.add("a a");
        expected.add("b");
        expected.add("b b");
        expected.add("c");
        expected.add("c c");
        expected.add("d");
        expected.add("d d");
        expected.add("e");
        expected.add("e e");

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void testZipWithIndex() {
        Set<Tuple2<String, Integer>> actual = this.set.zipWithIndex();
        Set<Tuple2<String, Integer>> expected = new HashSet<>();
        expected.add(Tuples.of("a", 0));
        expected.add(Tuples.of("b", 1));
        expected.add(Tuples.of("c", 2));
        expected.add(Tuples.of("d", 3));
        expected.add(Tuples.of("e", 4));

        Assertions.assertIterableEquals(expected, actual);

    }


    @Test
    public void testSpan() {
        Tuple2<? extends Set<String>, ? extends Set<String>> actual = this.set.span(s -> s.equals("a"));

        Set<String> list1 = new HashSet<>();
        Set<String> list2 = new HashSet<>();

        list1.add("a");

        list2.add("b");
        list2.add("c");
        list2.add("d");
        list2.add("e");
        Assertions.assertEquals(Tuples.of(list1, list2), actual);

    }

    @Test
    public void testSplitAt() {
        Tuple2<? extends Set<String>, ? extends Set<String>> actual = this.set.splitAt(3);
        Set<String> list1 = new HashSet<>();
        Set<String> list2 = new HashSet<>();
        list1.add("a");
        list1.add("b");
        list1.add("c");

        list2.add("d");
        list2.add("e");

        Assertions.assertEquals(Tuples.of(list1, list2), actual);
    }

    @Test
    public void testForall() {
        boolean a = this.set.forall(s -> s.length() == 1);
        Assertions.assertTrue(a);
    }

    @Test
    public void testExists() {
        boolean a = this.set.exists(s -> s.equals("a"));
        Assertions.assertTrue(a);
    }

    @Test
    public void testCount() {
        int count = this.set.count(s -> s.equals("a"));
        Assertions.assertEquals(count, 1);
    }

    @Test
    public void testFind() {
        Option<String> s1 = this.set.find(s -> s == "b");
        Assertions.assertEquals(s1.get(), "b");
    }


    @Test
    public void testFoldLeft() {
        StringBuilder stringBuilder = this.set.foldLeft(new StringBuilder(), StringBuilder::append);
        Assertions.assertEquals(stringBuilder.toString(), "abcde");
    }


    @Test
    public void testFoldRight() {
        String expected = this.set.foldRight(new StringBuilder(), StringBuilder::append).toString();
        Assertions.assertEquals(expected, "edcba");
    }

    @Test
    public void testMkString() {
        Assertions.assertEquals(this.set.mkString("[", ",", "]"), "[a,b,c,d,e]");
        Assertions.assertEquals(this.set.mkString(","), "a,b,c,d,e");
        Assertions.assertEquals(this.set.mkString(), "abcde");
    }

}
