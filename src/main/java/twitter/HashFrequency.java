package twitter;

public class HashFrequency implements Comparable<HashFrequency> {

    public String hashtag;
    public Integer estimatedFrequency;
    public Integer maxPossibleFreqError;

    public HashFrequency(String hashtag, Integer estimatedFrequency, Integer maxPossibleFreqError) {
        this.hashtag = hashtag;
        this.estimatedFrequency = estimatedFrequency;
        this.maxPossibleFreqError = maxPossibleFreqError;
    }

    public void incrementEstimatedFrequency() {
        this.estimatedFrequency++;
    }

    public Integer possibleCount() {
        return this.estimatedFrequency + this.maxPossibleFreqError;
    }

    @Override
    public String toString() {
        return String.format("{hashtag=%s, estimatedFrequency=%d, maxPossibleFreqError=%d}",
                this.hashtag, this.estimatedFrequency, this.maxPossibleFreqError);
    }

    @Override
    public int compareTo(HashFrequency other) {
        if (this.estimatedFrequency > other.estimatedFrequency) {
            return -1;
        } else if (this.estimatedFrequency < other.estimatedFrequency) {
            return 1;
        } else {
            return 0;
        }
    }
}
