package io.tava.util;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-08-16 09:23
 */
public class BitMap {

    private byte[] bits;
    private int bitLength;

    public BitMap() {
        this(1024);
    }

    public BitMap(int maxBitIndex) {
        generate(maxBitIndex);
    }


    /**
     * 根据最大的"bit位"的index值生成一个byte数组
     *
     * @param maxBitIndex max bit index
     */
    public void generate(int maxBitIndex) {
        if (maxBitIndex < 0) {
            throw new NegativeArraySizeException("maxBitIndex < 0: " + maxBitIndex);
        }
        this.bits = new byte[(maxBitIndex >> 3) + 1];
        this.bitLength = bits.length << 3;
    }


    /**
     * 将对应"bit位"的值设置为“1”
     *
     * @param bitIndex bit index
     */
    public void set(int bitIndex) {
        check(bits, bitIndex);
        if (bitIndex >= bitLength) {
            ensureCapacity(bitIndex);
        }
        bits[bitIndex >> 3] |= (0x80 >> (bitIndex & 7));
    }


    /**
     * 将对应"bit位"的值设置为"0"
     *
     * @param bitIndex bit index
     */
    public void clear(int bitIndex) {
        check(bits, bitIndex);
        if (bitIndex >= bitLength) {
            return;
        }
        bits[bitIndex >> 3] &= ~(0x80 >> (bitIndex & 7));
    }


    /**
     * 获取对应"bit位"的值
     *
     * @param index 0:false 1:true
     */
    public boolean get(int index) {
        check(bits, index);
        if (index >= bitLength) {
            return false;
        }
        return (bits[index >> 3] & (0x80 >> (index & 7))) != 0;
    }


    public byte[] bits() {
        return this.bits;
    }

    private void check(byte[] bits, int index) {
        if (bits == null) {
            throw new NullPointerException("bits is null");
        }
        if (index < 0) {
            throw new NegativeArraySizeException("index < 0: " + index);
        }
    }

    private void ensureCapacity(int bitIndex) {
        int maxBitIndex = bitIndex + bitIndex / 10;
        generate(maxBitIndex);
    }

}
