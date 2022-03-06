import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class RandomSentenceSpout extends BaseRichSpout {

    private SpoutOutputCollector collector;
    private int index = 0;
    private final String[] sentences = {
            "my dog has fleas",
            "i like cold beverages",
            "the dog ate my homework",
            "don't have a truck",
            "i don't think i like fleas"
    };

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }

    @Override
    public void open(Map<String, Object> config, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void nextTuple() {
        System.out.println("Emitting sentence " + sentences[index]);
        this.collector.emit(new Values(this.sentences[this.index]));
        this.index++;
        if (this.index >= this.sentences.length) {
            this.index = 0;
        }

        Utils.sleep(1000);
    }

}
