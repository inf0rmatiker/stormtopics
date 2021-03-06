package example;

import example.RandomSentenceSpout;
import example.ReportBolt;
import example.SplitSentence;
import example.WordCount;
import org.apache.storm.Config;
//import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class WordCountTopology {

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

        RandomSentenceSpout spout = new RandomSentenceSpout();
        SplitSentence splitBolt = new SplitSentence();
        WordCount countBolt = new WordCount();
        ReportBolt reportBolt = new ReportBolt();
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(SENTENCE_SPOUT_ID, spout);

        // SentenceSpout --> SplitSentenceBolt
        builder.setBolt(SPLIT_BOLT_ID, splitBolt).shuffleGrouping(SENTENCE_SPOUT_ID);
        builder.setBolt(COUNT_BOLT_ID, countBolt).fieldsGrouping(SPLIT_BOLT_ID, new Fields("word"));

        // WordCountBolt --> example.ReportBolt
        builder.setBolt(REPORT_BOLT_ID, reportBolt).globalGrouping(COUNT_BOLT_ID);
        StormTopology topology = builder.createTopology();

        Config config = new Config();
        System.out.println("Got here");
        try {

            if (is_remote) {
                System.out.println("is_remote=True");
                config.setDebug(true);
                config.setMaxTaskParallelism(1);
                config.setNumWorkers(4);
                config.setMessageTimeoutSecs(12);
                StormSubmitter.submitTopology(TOPOLOGY_NAME, config, topology);
            }
//            else {
//                LocalCluster cluster = new LocalCluster();
//                cluster.submitTopology(TOPOLOGY_NAME, config, topology);
//                Thread.sleep(10000);
//                cluster.killTopology(TOPOLOGY_NAME);
//                cluster.shutdown();
//            }

        } catch (Exception e) {
            System.err.println("Caught Exception! " + e.getMessage());
        }
    }

}
