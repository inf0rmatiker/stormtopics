package twitter;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;

public class TwitterSpout extends BaseRichSpout {

    private static final String API_KEY = "KpHRi90CRut4JE3ilESpfZsxd";
    private static final String API_KEY_SECRET = "00pCIw58twUpHUwIk1UMuS0stY5NiIvnt1V3xrJAE6FBBKCpF7";
    private static final String BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAGF5ZwEAAAAA5%2B1SSbC2OdOagBBVOdDxZXXQmDs%3DsKeljnM4RkPxvUSY4rNRhK5I3dVwKo8UT44WhE1evRbOKv6VOB";
    private static final String ACCESS_TOKEN = "1379146026523582464-4loRoAVBJ9WD7HTPFScmbrl7IOxvUh";
    private static final String ACCESS_TOKEN_SECRET = "tN9LeVbQ2YEF1c4q8Or8vbYcacNSoTiCVuFWM8MySecGH";

    private TwitterStream twitterStream;
    private SpoutOutputCollector collector;
    private StatusListener statusListener;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }

    @Override
    public void open(Map<String, Object> config, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        this.statusListener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                System.out.println(status.getUser().getName() + " : " + status.getText());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            @Override
            public void onScrubGeo(long l, long l1) {}

            @Override
            public void onStallWarning(StallWarning stallWarning) {}

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setJSONStoreEnabled(true)
                .setDebugEnabled(true)
                .setOAuthConsumerKey(API_KEY)
                .setOAuthConsumerSecret(API_KEY_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        this.twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        this.twitterStream.addListener(this.statusListener);
        this.twitterStream.sample();
    }

    @Override
    public void nextTuple() {

    }

}
