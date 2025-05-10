package objects;

import utils.Hasher;

public class Object {
    private final String type;    // type => blob, tree, commit, tag
    private final String content;
    private final String hash;

    /**
     * Constructor to create an Object (Git Object).
     *
     * @param type    The type of the object (e.g., "blob", "tree", "commit").
     * @param content The content of the object.
     */
    public Object(String type, String content) {
        this.type = type;
        this.content = content;
        this.hash = computeHash();
    }

    /**
     * Get the type of the object.
     *
     * @return The type (e.g., "blob", "tree", "commit").
     */
    public String getType() {
        return type;
    }

    /**
     * Get the content of the object.
     *
     * @return The raw content of the object.
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the hash of the object.
     *
     * @return The unique hash of the object.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Computes the hash for the object by combining:
     * - Type
     * - Size of the content
     * - Content itself
     *
     * This mimics Git's behavior of hashing objects.
     *
     * @return The computed hash of the object.
     */
    private String computeHash() {
        // Format: "<type> <size>\0<content>"
        String objectData = type + " " + content.length() + "\0" + content;
        return Hasher.computeSHA1(objectData);
    }

    @Override
    public String toString() {
        return "GitObject{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}