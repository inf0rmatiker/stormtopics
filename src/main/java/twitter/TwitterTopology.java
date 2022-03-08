package twitter;

import example.RandomSentenceSpout;
import example.ReportBolt;
import example.SplitSentence;
import example.WordCount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterTopology {

    private static final Logger log = LogManager.getLogger(TwitterTopology.class.getSimpleName());

    private static final String TWITTER_SPOUT_ID = "twitter-spout";
    private static final String TWITTER_COUNT_BOLT_ID = "twitter-count-bolt";
    private static final String TWITTER_REPORT_BOLT_ID = "twitter-report-bolt";
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
        TwitterCountBolt twitterCountBolt = new TwitterCountBolt();
        TwitterReportBolt twitterReportBolt = new TwitterReportBolt();

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(TWITTER_SPOUT_ID, twitterSpout);
        builder.setBolt(TWITTER_COUNT_BOLT_ID, twitterCountBolt)
                .fieldsGrouping(TWITTER_SPOUT_ID, new Fields("window_timestamp", "hashtag"));
        builder.setBolt(TWITTER_REPORT_BOLT_ID, twitterReportBolt)
                .fieldsGrouping(TWITTER_COUNT_BOLT_ID, new Fields("window_timestamp", "hashtag", "count", "error"));

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
