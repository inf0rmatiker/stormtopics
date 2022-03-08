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

import java.util.HashMap;
import java.util.Map;

public class TwitterCountBolt extends BaseRichBolt {

    private static final Logger log = LogManager.getLogger(TwitterCountBolt.class.getSimpleName());

    private final double EPSILON = 0.2;
    private final int BUCKET_CAPACITY = (int)(1.0/EPSILON);
    private final double THRESHOLD = 0.002;

    private OutputCollector collector;
    private int bucket = 1;
    private int totalCount = 0;
    private Map<String, HashFrequency> hashFrequencies = new HashMap<>();

    @Override
    public void prepare(Map<String, Object> config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag", "count"));
    }

    @Override
    public void execute(Tuple input) {

        // Increment N on every incoming value
        this.totalCount++;

        // If the bucket is full (N mod w == 0), prune D and move to next bucket number
        if (bucketIsFull()) {
            prune();
            this.bucket++;
        }

        String hashtagValue = input.getStringByField("hashtag");


        if (this.hashFrequencies.containsKey(hashtagValue)) {

            // We've already seen this hashtag; increment frequency
            HashFrequency hashFrequency = this.hashFrequencies.get(hashtagValue);
            hashFrequency.incrementEstimatedFrequency();
            log.info("Updated hashtag={} with frequency of {}", hashtagValue, hashFrequency.estimatedFrequency);

        } else {

            // Seeing this hashtag for the first time; create a new entry
            this.hashFrequencies.put(hashtagValue, new HashFrequency(
                    hashtagValue, 1, this.bucket - 1
            ));
            log.info("Added hashtag={} with frequency of 1", hashtagValue);

        }
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

    private boolean bucketIsFull() {
        return this.totalCount > 0 && this.totalCount % this.BUCKET_CAPACITY == 0;
    }


}
