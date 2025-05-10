package objects;

import java.time.Instant;

/**
 * Represents a Commit object in a Git-like system.
 * Each commit points to a tree (representing the snapshot of the file
 * system), the parent commit (for history), and contains metadata such
 * as the author, timestamp, and commit message.
 */
public class Commit extends Object {
    private final String treeHash;
    private final String parentHash;  // null if it is the first commit
    private final String author;
    private final Instant timestamp;
    private final String message;

    /**
     * Constructor to create a Commit object.
     *
     * @param treeHash    The hash of the root tree object this commit refers to.
     * @param parentHash  The hash of the parent commit (can be null for the first commit).
     * @param author      The author of this commit.
     * @param message     A message describing this commit.
     */
    public Commit(String treeHash, String parentHash, String author, String message) {
        super("commit", buildContent(treeHash, parentHash, author, Instant.now(), message));
        this.treeHash = treeHash;
        this.parentHash = parentHash;
        this.author = author;
        this.timestamp = Instant.now();
        this.message = message;
    }

    /**
     * Helper method to construct the content of a commit object.
     *
     * @param treeHash    The hash of the tree object.
     * @param parentHash  The hash of the parent commit, or null if this is the first commit.
     * @param author      The author of the commit.
     * @param timestamp   The timestamp of the commit.
     * @param message     The commit message.
     * @return The formatted content for the commit.
     */
    private static String buildContent(String treeHash, String parentHash, String author, Instant timestamp, String message) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("tree ").append(treeHash).append("\n");
        if (parentHash != null) {
            contentBuilder.append("parent ").append(parentHash).append("\n");
        }
        contentBuilder.append("author ").append(author).append("\n");
        contentBuilder.append("date ").append(timestamp.toString()).append("\n\n");
        contentBuilder.append(message).append("\n");
        return contentBuilder.toString();
    }

    /**
     * Returns the hash of the tree object associated with this commit.
     *
     * @return The tree hash.
     */
    public String getTreeHash() {
        return treeHash;
    }

    /**
     * Returns the hash of the parent commit.
     *
     * @return The parent commit hash or null for the first commit.
     */
    public String getParentHash() {
        return parentHash;
    }

    /**
     * Returns the author of the commit.
     *
     * @return The author name.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the timestamp of the commit.
     *
     * @return The commit timestamp.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the commit message.
     *
     * @return The message describing this commit.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "hash='" + getHash() + '\'' +
                ", treeHash='" + treeHash + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", author='" + author + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}