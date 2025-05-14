import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

import static utils.Hasher.computeSHA1;
import objects.Commit;

/**
 * Represents a version control repository.
 *
 * This class provides functionality for initializing a repository and managing files within it.
 * It simulates basic repository operations, including creating the repository structure and
 * tracking files to be added.
 */
public class Repository {
    private final String path;
    private final HashMap<String, String> trackedFiles;
    private final LinkedList<Commit> commitHistory;
    private Commit currentCommit;
    private final HashMap<String, String> fileContents;
    private final HashMap<String, HashMap<String, String>> commitSnapshots;

    /**
     * Constructs a new {@code Repository} instance with the specified path.
     *
     * @param path The path to the directory where the repository is located.
     */
    public Repository(String path) {
        this.path = path;
        this.trackedFiles = new HashMap<>();
        this.commitHistory = new LinkedList<>();
        this.currentCommit = null;
        this.fileContents = new HashMap<>();
        this.commitSnapshots = new HashMap<>();
    }

    /**
     * Initializes the repository by creating a `.git` directory at the specified path.
     * If the repository already exists, it notifies the user.
     */
    public void init() {
        File gitDir = new File(path + "/.git");
        if (!gitDir.exists()) {
            if (gitDir.mkdirs()) {
                System.out.println("Initialized empty Git repository in " + gitDir.getPath());
            }
        } else {
            System.out.println("Repository already exists.");
        }
    }

    /**
     * Adds a file to the repository.
     * If the file is found at the specified path, it reads the content, computes its hash,
     * and adds it to the tracking system.
     *
     * @param fileName The name of the file to be added.
     */
    public void addFile(String fileName) {
        File file = new File(path + "/" + fileName);
        if (file.exists()) {
            try {
                String content = Files.readString(Paths.get(file.getPath()));
                
                String fileHash = computeSHA1(content);
                
                fileContents.put(fileHash, content);
                
                trackedFiles.put(fileName, fileHash);
                
                System.out.println("File added to repository: " + fileName);
            } catch (IOException e) {
                System.out.println("Error reading file: " + fileName + " - " + e.getMessage());
            }
        } else {
            System.out.println("File not found: " + fileName);
        }
    }

    /**
     * Commits the current changes by creating a new Commit object.
     * The commit includes all tracked files and their hashes.
     *
     * @param message The commit message describing the changes.
     * @param author  The author of this commit.
     */
    public void commit(String message, String author) {
        if (trackedFiles.isEmpty()) {
            System.out.println("No files to commit.");
            return;
        }

        StringBuilder treeBuilder = new StringBuilder();
        for (var entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            treeBuilder.append(fileName).append(fileHash);
        }
        String treeHash = computeSHA1(treeBuilder.toString());

        String parentHash = currentCommit != null ? currentCommit.getHash() : null;

        Commit newCommit = new Commit(treeHash, parentHash, author, message);
        
        commitSnapshots.put(newCommit.getHash(), new HashMap<>(trackedFiles));
        
        commitHistory.add(newCommit);
        currentCommit = newCommit;

        System.out.println("Commit successful!");
        System.out.println("Commit details:");
        System.out.println(newCommit);
    }

    /**
     * Prints the commit history, starting from the latest commit and moving backward.
     */
    public void log() {
        if (commitHistory.isEmpty()) {
            System.out.println("No commits yet.");
            return;
        }

        System.out.println("Commit History:");
        for (Commit commit : commitHistory) {
            System.out.println("Commit " + commit.getHash());
            System.out.println("Tree: " + commit.getTreeHash());
            System.out.println("Parent: " + (commit.getParentHash() != null ? commit.getParentHash() : "None"));
            System.out.println("Author: " + commit.getAuthor());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println("\n    " + commit.getMessage());
            System.out.println();
        }
    }

    /**
     * Displays the current state of the repository, including tracked files and their hashes
     * based on the current commit's tree hash.
     */
    public void status() {
        if (currentCommit == null) {
            System.out.println("No commits yet. Nothing to display.");
            return;
        }

        System.out.println("Current commit: " + currentCommit.getHash());
        System.out.println("Files tracked in this commit:");
        if (trackedFiles.isEmpty()) {
            System.out.println("    (No files tracked)");
            return;
        }

        for (String fileName : trackedFiles.keySet()) {
            System.out.println("    " + fileName + " -> " + trackedFiles.get(fileName));
        }
    }

    /**
     * Removes a file from the repository's tracking system.
     * This does not delete the file itself but stops tracking it,
     * which will be reflected in subsequent commits.
     *
     * @param fileName The name of the file to remove.
     */
    public void removeFile(String fileName) {
        if (trackedFiles.containsKey(fileName)) {
            trackedFiles.remove(fileName);
            System.out.println("File removed from tracking: " + fileName);
        } else {
            System.out.println("File not found in tracking: " + fileName);
        }
    }

    /**
     * Reverts the repository to the specified commit ID.
     * All files are restored to the state represented by the commit's tree hash.
     *
     * @param commitHash The hash of the commit to revert to.
     */
    public void checkout(String commitHash) {
        for (Commit commit : commitHistory) {
            if (commit.getHash().equals(commitHash)) {
                currentCommit = commit;
                
                HashMap<String, String> snapshot = commitSnapshots.get(commitHash);
                if (snapshot != null) {
                    trackedFiles.clear();
                    trackedFiles.putAll(snapshot);
                }
                
                System.out.println("Checked out commit " + commitHash);
                System.out.println("Repository is now at commit:");
                System.out.println(commit);
                
                return;
            }
        }

        System.out.println("Commit with hash " + commitHash + " not found.");
    }
}