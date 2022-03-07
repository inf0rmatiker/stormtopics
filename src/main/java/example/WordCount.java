package example;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

public class WordCount extends BaseRichBolt {

    private OutputCollector collector;
    private HashMap<String, Integer> counts = null;

    @Override
    public void prepare(Map<String, Object> config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.counts = new HashMap<>();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

    @Override
    public void execute(Tuple input) {
        String word = input.getStringByField("word");
        Integer count = this.counts.get(word);
        if (count == null) {
            count = 0;
        }
        count++;
        this.counts.put(word, count);
        this.collector.emit(new Values(word, count));
    }
}
