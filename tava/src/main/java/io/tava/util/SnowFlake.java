package io.tava.util;

/**
 * twitter的snowflake算法 -- java实现
 *
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-01 13:56
 */
public final class SnowFlake {


    /**
     * 起始的时间戳，可以修改为服务第一次启动的时间
     * 一旦服务已经开始使用，起始时间戳就不应该改变
     */
    private final static long START_TIMESTAMP = 1630425600000L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;  //数据中心
    private final long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastTimestamp = -1L;//上一次时间戳


    /**
     * 通过单例模式来获取实例
     * 分布式部署服务时，数据节点标识和机器标识作为联合键必须唯一
     *
     * @param datacenterId 数据节点标识ID
     * @param machineId    机器标识ID
     */
    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return 新的id
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < this.lastTimestamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currentTimestamp == this.lastTimestamp) {
            //相同毫秒内，序列号自增
            this.sequence = (this.sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (this.sequence == 0L) {
                currentTimestamp = getNextMilliseconds();
            }
        } else {
            //不同毫秒内，序列号置为0
            this.sequence = 0L;
        }

        this.lastTimestamp = currentTimestamp;

        return (currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT //时间戳部分
                | this.datacenterId << DATACENTER_LEFT       //数据中心部分
                | this.machineId << MACHINE_LEFT             //机器标识部分
                | this.sequence;                             //序列号部分
    }

    private long getNextMilliseconds() {
        long currentTimestamp = System.currentTimeMillis();
        while (currentTimestamp <= this.lastTimestamp) {
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }

}
