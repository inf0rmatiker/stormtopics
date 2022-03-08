package twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitterReportBolt extends BaseRichBolt {

    private static final Logger log = LogManager.getLogger(TwitterReportBolt.class.getSimpleName());

    private Map<Long, List<HashFrequency>> allWindowResults;

    @Override
    public void prepare(Map<String, Object> config, TopologyContext context, OutputCollector collector) {
        this.allWindowResults = new HashMap<>();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // Not emitting anything, so not used
    }

    @Override
    public void execute(Tuple input) {
        Long windowTimestamp = input.getLongByField("window_timestamp");
        List<HashFrequency> windowFrequencies;
        log.info("Received final count for window={}, hashtag={}", windowTimestamp, input.getStringByField("hashtag"));

        // Get or create List of HashFrequencies for window
        if (this.allWindowResults.containsKey(windowTimestamp)) {
            windowFrequencies = this.allWindowResults.get(windowTimestamp);
        } else {
            windowFrequencies = new ArrayList<>();
            this.allWindowResults.put(windowTimestamp, windowFrequencies);
        }

        windowFrequencies.add(new HashFrequency(
                input.getStringByField("hashtag"),
                input.getIntegerByField("count"),
                input.getIntegerByField("error")
        ));
    }

    @Override
    public void cleanup() {
        log.info("------------ FINAL COUNTS ---------------");
        List<Long> windowKeys = new ArrayList<>(this.allWindowResults.keySet());
        Collections.sort(windowKeys);
        for (Long windowKey: windowKeys) {
            List<HashFrequency> hashFrequenciesForWindow = this.allWindowResults.get(windowKey);
            for (HashFrequency hashFrequency: hashFrequenciesForWindow) {
                log.info("window={}, hashFrequency={}", windowKey, hashFrequency.toString());
            }
        }
        log.info("------------------------------------------");
    }
}
