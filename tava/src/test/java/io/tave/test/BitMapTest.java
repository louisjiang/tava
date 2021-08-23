package io.tave.test;

import io.tava.util.BitMap;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-08-16 09:53
 */
public class BitMapTest {

    public static void main(String[] args) {

        BitMap bitMap = new BitMap(8);
        bitMap.set(8);
        System.out.println(bitMap.get(8));
        bitMap.set(16);
        System.out.println(bitMap.get(16));


    }

}
