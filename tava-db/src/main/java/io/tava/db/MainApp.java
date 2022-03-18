package io.tava.db;

import com.typesafe.config.ConfigFactory;
import io.tava.configuration.Configuration;
import io.tava.db.segment.SegmentList;
import io.tava.db.segment.SegmentMap;
import io.tava.serialization.kryo.KryoSerialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp {

    public static void main(String[] args) {

        Map<String, Object> values = new HashMap<>();
        values.put("path", "D:\\gome\\tava\\data");
        RocksdbDatabase rocksdbDatabase = new RocksdbDatabase(new Configuration(ConfigFactory.parseMap(values)), new KryoSerialization());
        SegmentMap<String, String> segmentMap = rocksdbDatabase.newMap("testMap", 10);
        for (int i = 0; i < 10; i++) {
            segmentMap.put("key" + i, "value" + i);
        }

        SegmentList<String> segmentList = rocksdbDatabase.newList("test", 3);
        segmentList.clear();

        for (int i = 0; i < 10; i++) {
            segmentList.add("value" + i);
        }
        segmentList.commit();
        segmentList.remove("value8");
        segmentList.commit();
        List<String> list1 = segmentList.toList();
        List<String> l = new ArrayList<>();
        l.add("value11");
        l.add("value22");
        segmentList.addAll(2, l);
        segmentList.commit();
        List<String> list2 = segmentList.toList();
        l.add("v");
        segmentList.removeAll(l);
        segmentList.commit();
        List<String> list3 = segmentList.toList();
        segmentList.remove(3);
        segmentList.commit();
        List<String> list4 = segmentList.toList();
        System.exit(-1);
    }

}
