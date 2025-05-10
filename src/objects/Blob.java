package objects;

/**
 * Represents a Blob object in a Git-like implementation.
 * A Blob is used to store file contents in the repository.
 */
public class Blob extends Object {
    /**
     * Creates a Blob object with the given content.
     *
     * @param content The content of the blob (file content).
     */
    public Blob(String content) {
        super("blob", content);
    }

    @Override
    public String toString() {
        return "Blob{" +
                "hash='" + getHash() + '\'' +
                ", content='" + getContent() + '\'' +
                '}';
    }
}