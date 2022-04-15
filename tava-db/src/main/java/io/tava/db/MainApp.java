package io.tava.db;

import com.typesafe.config.ConfigFactory;
import io.tava.configuration.Configuration;
import io.tava.db.segment.SegmentMap;
import io.tava.serialization.kryo.KryoSerialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainApp {

    public static void main(String[] args) throws IOException {

        Map<String, Object> values = new HashMap<>();
        values.put("path", "data");
        RocksdbDatabase rocksdbDatabase = new RocksdbDatabase(new Configuration(ConfigFactory.parseMap(values)), new KryoSerialization());
        SegmentMap<String, String> segmentMap = rocksdbDatabase.newSegmentMap("token-holder", 16);
        for (int i = 0; i < 100000; i++) {
            segmentMap.put("key" + i, "value" + i);
            segmentMap = segmentMap.remap();
        }
    }

}
