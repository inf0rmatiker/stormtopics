import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.*;

public class ReportBolt extends BaseRichBolt {

    private HashMap<String, Integer> counts = null;

    @Override
    public void prepare(Map<String, Object> config, TopologyContext context, OutputCollector collector) {
        this.counts = new HashMap<>();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // not used
    }

    @Override
    public void execute(Tuple input) {
        String word = input.getStringByField("word");
        Integer count = input.getIntegerByField("count");
        this.counts.put(word, count);
    }

    @Override
    public void cleanup() {
        System.err.println("------------ FINAL COUNTS ---------------");
        List<String> keys = new ArrayList<>();
        keys.addAll(this.counts.keySet());
        Collections.sort(keys);
        for (String key: keys) {
            System.out.println(key + " : " + this.counts.get(key));
        }
    }
}
