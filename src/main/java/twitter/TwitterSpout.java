package twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;

import org.apache.storm.tuple.Values;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class TwitterSpout extends BaseRichSpout {

    private static final Logger log = LogManager.getLogger(TwitterSpout.class.getSimpleName());

    private static final String API_KEY = "KpHRi90CRut4JE3ilESpfZsxd";
    private static final String API_KEY_SECRET = "00pCIw58twUpHUwIk1UMuS0stY5NiIvnt1V3xrJAE6FBBKCpF7";
    private static final String BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAGF5ZwEAAAAA5%2B1SSbC2OdOagBBVOdDxZXXQmDs%3DsKeljnM4RkPxvUSY4rNRhK5I3dVwKo8UT44WhE1evRbOKv6VOB";
    private static final String ACCESS_TOKEN = "1379146026523582464-4loRoAVBJ9WD7HTPFScmbrl7IOxvUh";
    private static final String ACCESS_TOKEN_SECRET = "tN9LeVbQ2YEF1c4q8Or8vbYcacNSoTiCVuFWM8MySecGH";

    private static final short SECOND = 1000;
    private static final int WINDOW_SECONDS = 10 * SECOND;

    private TwitterStream twitterStream;
    private SpoutOutputCollector collector;
    private StatusListener statusListener;
    private LinkedBlockingQueue<String> hashtagQueue;
    private Long timestamp;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("window_timestamp", "hashtag"));
    }

    @Override
    public void open(Map<String, Object> config, TopologyContext context, SpoutOutputCollector collector) {
        this.timestamp = System.currentTimeMillis();
        this.collector = collector;
        this.hashtagQueue = new LinkedBlockingQueue<>();
        log.info("open() called for timestamp={}", this.timestamp.toString());

        this.statusListener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                if (status.getHashtagEntities().length > 0) {
                    StringBuilder sb = new StringBuilder("[");
                    HashtagEntity[] hashtagEntities = status.getHashtagEntities();
                    for (HashtagEntity hashtagEntity: hashtagEntities) {
                        boolean isAscii = hashtagEntity.getText().matches("\\A\\p{ASCII}*\\z");
                        if (isAscii) {
                            hashtagQueue.add(hashtagEntity.getText());
                            sb.append(String.format(" %s ", hashtagEntity.getText()));
                        }
                    }
                    sb.append("]");
                    log.info("Tweet with hashtags: {}", sb.toString());
                }
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
        this.twitterStream.filter(new FilterQuery().language("en"));
        this.twitterStream.sample();
    }

    @Override
    public void nextTuple() {
        // Check current time and update window if needed
        if (System.currentTimeMillis() >= this.timestamp + WINDOW_SECONDS) {
            moveToNextWindow();
        }

        // Emit all queued hashtags
        while (!this.hashtagQueue.isEmpty()) {
            collector.emit(new Values(this.timestamp, hashtagQueue.poll()));
        }
    }

    @Override
    public void close() {
        this.twitterStream.shutdown();
    }

    private synchronized void moveToNextWindow() {
        this.timestamp += WINDOW_SECONDS;
        log.info("Moved to next window, timestamp={}", this.timestamp);
    }

}
