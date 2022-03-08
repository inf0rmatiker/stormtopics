package twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TwitterCountBolt extends BaseRichBolt {

    private static final Logger log = LogManager.getLogger(TwitterCountBolt.class.getSimpleName());

    private final double EPSILON = 0.002; // 1/0.0002 = 5,000 entries
    private final int BUCKET_CAPACITY = (int)(1.0/EPSILON);
    private final double THRESHOLD = 0.002;

    private OutputCollector collector;
    private Integer bucket;
    private Integer totalCount;
    private ConcurrentMap<String, HashFrequency> hashFrequencies;
    private Long windowTimestamp;

    @Override
    public void prepare(Map<String, Object> config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.hashFrequencies = new ConcurrentHashMap<>();
        this.windowTimestamp = 0L;
        this.bucket = 1;
        this.totalCount = 0;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("window_timestamp", "hashtag", "count", "error"));
    }

    @Override
    public void execute(Tuple input) {

        String hashtagValue = input.getStringByField("hashtag");
        Long windowTimestamp = input.getLongByField("window_timestamp");

        // Initialize windowTimestamp if not already done
        if (this.windowTimestamp == 0L) {
            setWindowTimestamp(windowTimestamp);
            log.info("Initialized windowTimestamp for the first time to {}", this.windowTimestamp);
        } else if (!windowTimestamp.equals(this.windowTimestamp)) {
            if (windowTimestamp > this.windowTimestamp) {
                emitAllAndReset();
                setWindowTimestamp(windowTimestamp);
                log.info("Updated windowTimestamp to {}", this.windowTimestamp);
            } else {
                log.warn("Received tuple for previous window; discarding");
            }
        }

        // Increment N on every incoming value
        this.totalCount++;

        // If the bucket is full (N mod w == 0), prune D and move to next bucket number
        if (bucketIsFull()) {
            prune();
            this.bucket++;
        }

        if (this.hashFrequencies.containsKey(hashtagValue)) {

            // We've already seen this hashtag; increment frequency
            HashFrequency hashFrequency = this.hashFrequencies.get(hashtagValue);
            hashFrequency.incrementEstimatedFrequency();
            log.info("Updated hashtag={} with frequency of {}", hashtagValue, hashFrequency.estimatedFrequency);

        } else {

            // Seeing this hashtag for the first time; create a new entry
            this.hashFrequencies.put(hashtagValue, new HashFrequency(
                    this.windowTimestamp, hashtagValue, 1, this.bucket - 1
            ));
            log.info("Added hashtag={} with frequency of 1", hashtagValue);

        }
    }

    @Override
    public void cleanup() {
        log.info("cleanup() invoked, emitting final counts for windowTimestamp={}", this.windowTimestamp);
        emitAllAndReset();
    }

    private void emitAllAndReset() {
        log.info("Emitting all bucket values and resetting...");
        for (String hashtag: this.hashFrequencies.keySet()) {
            HashFrequency hashFrequency = this.hashFrequencies.get(hashtag);
            collector.emit(
                    new Values(
                            this.windowTimestamp,
                            hashtag,
                            hashFrequency.estimatedFrequency,
                            hashFrequency.maxPossibleFreqError
                    )
            );
        }

        this.bucket = 1;
        this.totalCount = 0;
        this.hashFrequencies = new ConcurrentHashMap<>();
    }

    private void prune() {
        log.info("Bucket {} is full ({} total items), pruning...", this.bucket, this.totalCount);
        for (String hashtag: this.hashFrequencies.keySet()) {
            HashFrequency hashFrequency = this.hashFrequencies.get(hashtag);
            if (hashFrequency.possibleCount() <= this.bucket) {
                log.info("possibleCount({})={}, bucket={}, deleting", hashtag, hashFrequency.possibleCount(),
                        this.bucket);
                this.hashFrequencies.remove(hashtag);
            } else {
                log.info("possibleCount({})={}, bucket={}, keeping", hashtag, hashFrequency.possibleCount(),
                        this.bucket);
            }
        }
    }

    private synchronized void setWindowTimestamp(Long windowTimestamp) {
        this.windowTimestamp = windowTimestamp;
    }

    private boolean bucketIsFull() {
        return this.totalCount > 0 && this.totalCount % this.BUCKET_CAPACITY == 0;
    }

}
