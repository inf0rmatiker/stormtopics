package twitter;

import example.RandomSentenceSpout;
import example.ReportBolt;
import example.SplitSentence;
import example.WordCount;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterTopology {


    private static final String SENTENCE_SPOUT_ID = "sentence-spout";
    private static final String SPLIT_BOLT_ID = "split-bolt";
    private static final String COUNT_BOLT_ID = "count-bolt";
    private static final String REPORT_BOLT_ID = "report-bolt";
    private static final String TOPOLOGY_NAME = "word-count-topology";

    public static void printArgs(String[] args) {
        StringBuilder sb = new StringBuilder("Args:");
        for (int i = 0; i < args.length; i++) {
            sb.append(String.format("arg[%d]=%s ", i, args[i]));
        }
        System.out.println(sb);
    }

    public static void main(String[] args) {
        printArgs(args);

        System.out.println("Running main()");
        boolean is_remote = false;
        if (args.length == 1) {
            if (args[0].equals("remote")) {
                is_remote = true;
            }
        }


    }

}
