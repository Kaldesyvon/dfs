package dfs.lockservice;

public class Pair {

    private final String ownerId;
    private final long sequence;

    public Pair(String ownerId, long sequence) {
        this.ownerId = ownerId;
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
