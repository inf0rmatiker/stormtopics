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

    private static final String TWITTER_SPOUT_ID = "twitter-spout";
    private static final String SPLIT_BOLT_ID = "split-bolt";
    private static final String COUNT_BOLT_ID = "count-bolt";
    private static final String REPORT_BOLT_ID = "report-bolt";
    private static final String TOPOLOGY_NAME = "twitter-topology";

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

        TwitterSpout twitterSpout = new TwitterSpout();
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(TWITTER_SPOUT_ID, twitterSpout);
        StormTopology topology = builder.createTopology();

        Config config = new Config();

        try {

            if (is_remote) {
                System.out.println("is_remote=True");
                config.setDebug(true);
                config.setMaxTaskParallelism(1);
                config.setNumWorkers(1);
                config.setMessageTimeoutSecs(12);
                StormSubmitter.submitTopology(TOPOLOGY_NAME, config, topology);
            }
        } catch (Exception e) {
            System.err.println("Caught Exception! " + e.getMessage());
        }
    }

}
