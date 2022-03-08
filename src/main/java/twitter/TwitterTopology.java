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
        log.info(sb.toString());
    }

    public static void main(String[] args) {
        printArgs(args);

        log.info("Running main()...");

        if (args.length != 2) {
            log.error("Usage example: storm jar <path_to_jar> twitter.TwitterTopology parallel /s/chopin/g/cacaleb/results.txt");
            log.error("Please supply <topology_type> and <results_file> as arguments");
            System.exit(1);
        }

        // Defaults for parallelism
        boolean isParallel = false;
        int maxTaskParallelism = 1;
        int countBoltExecutors = 1;
        int countBoltTasks = 1;
        String resultsFile = args[1];

        if (args[0].equals("parallel")) {
            isParallel = true;
            maxTaskParallelism = 4;
            countBoltExecutors = 4;
            countBoltTasks = 8;
        }

        TwitterSpout twitterSpout = new TwitterSpout();
        TwitterCountBolt twitterCountBolt = new TwitterCountBolt();
        TwitterReportBolt twitterReportBolt = new TwitterReportBolt(resultsFile);

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(TWITTER_SPOUT_ID, twitterSpout);

        // parallelismHint = How many executors (threads) to spawn per component.
        builder.setBolt(TWITTER_COUNT_BOLT_ID, twitterCountBolt, countBoltExecutors)
                .setNumTasks(countBoltTasks) // How many tasks to create per component. Gets run by an executor thread.
                .fieldsGrouping(TWITTER_SPOUT_ID, new Fields("window_timestamp", "hashtag"));

        // parallelismHint = How many executors (threads) to spawn per component.
        builder.setBolt(TWITTER_REPORT_BOLT_ID, twitterReportBolt, 1)
                .setNumTasks(1) // How many tasks to create per component. Gets run by an executor thread.
                .fieldsGrouping(TWITTER_COUNT_BOLT_ID, new Fields("window_timestamp", "hashtag", "count", "error"));

        StormTopology topology = builder.createTopology();
        Config config = new Config();

        try {
            config.setDebug(true);

            // Max number of threads allowed per JVM worker process
            config.setMaxTaskParallelism(maxTaskParallelism);

            // How many worker processes (JVMs) to create for the topology across machines in the cluster.
            config.setNumWorkers(3);

            config.setMessageTimeoutSecs(12);
            StormSubmitter.submitTopology(TOPOLOGY_NAME, config, topology);
        } catch (Exception e) {
            log.error("Caught Exception! " + e.getMessage());
        }
    }

}
