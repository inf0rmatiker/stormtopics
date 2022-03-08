package twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;

public class TwitterReportBolt extends BaseRichBolt {

    public TwitterReportBolt(String resultsFile) {
        this.resultsFile = resultsFile;
    }

    private static final Logger log = LogManager.getLogger(TwitterReportBolt.class.getSimpleName());

    private PriorityQueue<HashFrequency> globalResults;
    private List<HashFrequency> currentWindowResults;
    private FileWriter fileWriter;
    private Long currentWindow;
    private String resultsFile;

    @Override
    public void prepare(Map<String, Object> config, TopologyContext context, OutputCollector collector) {
        this.currentWindow = 0L;
        this.currentWindowResults = new ArrayList<>();
        this.globalResults = new PriorityQueue<>();

        try {
            File fileHandle = new File(this.resultsFile);
            if (fileHandle.createNewFile()) {
                log.info("File created: {}", fileHandle.getName());
            } else {
                log.info("File already exists: {}", fileHandle.getName());
            }

            this.fileWriter = new FileWriter(fileHandle, true);
        } catch (IOException e) {
            log.error("Unable to open/create file");
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // Not emitting anything, so not used
    }

    @Override
    public void execute(Tuple input) {
        Long windowTimestamp = input.getLongByField("window_timestamp");

        if (this.currentWindow == 0L) {
            this.currentWindow = windowTimestamp;
        }

        if (windowTimestamp > this.currentWindow) {
            writeWindowResultsToFile();
            resetWindowResultsForNewWindow(windowTimestamp);
        }

        log.info("Received final count for hashtag={}", input.getStringByField("hashtag"));

        HashFrequency hashFrequency = new HashFrequency(
                this.currentWindow,
                input.getStringByField("hashtag"),
                input.getIntegerByField("count"),
                input.getIntegerByField("error")
        );

        this.currentWindowResults.add(hashFrequency);
        this.globalResults.offer(hashFrequency);
    }

    @Override
    public void cleanup() {
        try {
            this.fileWriter.write("\n ------- Global results of stream -------\n\n");
            for (int i = 0; i < 100 && i < this.globalResults.size(); i++) {
                HashFrequency nextTopHashtag = this.globalResults.poll();
                this.fileWriter.write(String.format("hashFrequency=%s\n", nextTopHashtag));
            }
            this.fileWriter.write("\n ----------------------------------------\n");
            this.fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWindowResultsToFile() {
        Collections.sort(this.currentWindowResults); // Sort by hashtag frequencies, descending

        try {
            // Log top 100 hashtags for window
            this.fileWriter.write(String.format("\n> Results for window %d:\n\n", this.currentWindow));
            for (int i = 0; i < 100 && i < this.currentWindowResults.size(); i++) {
                HashFrequency hashFrequency = this.currentWindowResults.get(i);
                this.fileWriter.write(String.format("hashFrequency=%s\n", hashFrequency));
            }
            this.fileWriter.flush();
        } catch (IOException e) {
            log.error("Caught IOException when writing window results to file");
        }
    }

    private void resetWindowResultsForNewWindow(Long windowTimestamp) {
        this.currentWindowResults = new ArrayList<>();
        setCurrentWindow(windowTimestamp);
    }

    private void setCurrentWindow(Long windowTimestamp) {
        this.currentWindow = windowTimestamp;
    }
}
