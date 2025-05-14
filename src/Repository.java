import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Instant;

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
    private final String indexPath;

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
        this.indexPath = path + "/.git/index";

        loadIndex();
        loadCommits();
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
                saveIndex();
                
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

        HashMap<String, String> commitFiles = new HashMap<>();
        for (var entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            commitFiles.put(fileName, fileHash);

            if (!fileContents.containsKey(fileHash)) {
                try {
                    String content = Files.readString(Paths.get(path + "/" + fileName));
                    fileContents.put(fileHash, content);
                } catch (IOException e) {
                    System.err.println("Error reading file content: " + fileName);
                }
            }
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

        commitSnapshots.put(newCommit.getHash(), new HashMap<>(commitFiles));
        commitHistory.add(newCommit);
        currentCommit = newCommit;

        trackedFiles.clear();
        saveIndex();
        saveCommits();

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
        System.out.println("Repository status:");
        
        if (!trackedFiles.isEmpty()) {
            System.out.println("Staged files:");
            for (String fileName : trackedFiles.keySet()) {
                System.out.println("    " + fileName);
            }
        } else {
            System.out.println("No files staged for commit");
        }
        
        if (currentCommit != null) {
            System.out.println("Current commit: " + currentCommit.getHash());
        } else {
            System.out.println("No commits yet");
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

                    for (var entry : snapshot.entrySet()) {
                        String fileName = entry.getKey();
                        String fileHash = entry.getValue();
                        String content = fileContents.get(fileHash);
                        
                        if (content != null) {
                            try {
                                Files.writeString(Paths.get(path + "/" + fileName), content);
                                trackedFiles.put(fileName, fileHash);
                                System.out.println("Restored file: " + fileName);
                            } catch (IOException e) {
                                System.err.println("Error restoring file " + fileName + ": " + e.getMessage());
                            }
                        } else {
                            System.err.println("Content not found for file: " + fileName + " (hash: " + fileHash + ")");
                        }
                    }
                    
                    // Clear the stage after checkout
                    trackedFiles.clear();
                    saveIndex();
                    saveHEAD();
                }
                
                System.out.println("Checked out commit " + commitHash);
                System.out.println("Repository is now at commit:");
                System.out.println(commit);
                
                return;
            }
        }

        System.out.println("Commit with hash " + commitHash + " not found.");
    }

    /**
     * Saves the current state of the index file.
     *
     * This method serializes the tracked files along with their corresponding file hashes
     * and writes the data to the index file to persist the current state.
     */
    private void saveIndex() {
        try {
            Properties props = new Properties();
            for (var entry : trackedFiles.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            File gitDir = new File(path + "/.git");
            if (!gitDir.exists()) {
                gitDir.mkdirs();
            }
            props.store(new FileOutputStream(indexPath), "Staged files");
        } catch (IOException e) {
            System.err.println("Error saving index: " + e.getMessage());
        }
    }

    /**
     * Loads the index from the saved state.
     *
     * This method reconstructs the tracked files by reading from the index file
     * and initializing the `trackedFiles` map with their previous state.
     */
    private void loadIndex() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(indexPath));
            for (String key : props.stringPropertyNames()) {
                trackedFiles.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
        }
    }

    /**
     * Saves all commits into a persistent storage file.
     *
     * This method serializes all commits from the commit history and writes them to
     * a storage file for later retrieval.
     */
    private void saveCommits() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Commit commit : commitHistory) {
                sb.append(commit.getHash()).append("|")
                  .append(commit.getTreeHash()).append("|")
                  .append(commit.getParentHash() != null ? commit.getParentHash() : "null").append("|")
                  .append(commit.getAuthor()).append("|")
                  .append(commit.getTimestamp()).append("|")
                  .append(commit.getMessage().replace("\n", "\\n")).append("|");
                
                HashMap<String, String> snapshot = commitSnapshots.get(commit.getHash());
                if (snapshot != null) {
                    for (var entry : snapshot.entrySet()) {
                        sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
                    }
                }
                sb.append("\n");
            }

            Files.writeString(Paths.get(path + "/.git/commits"), sb.toString());
            
            StringBuilder fileContentsSb = new StringBuilder();
            for (var entry : fileContents.entrySet()) {
                fileContentsSb.append(entry.getKey()).append("|")
                            .append(entry.getValue().replace("\n", "\\n")).append("\n");
            }
            Files.writeString(Paths.get(path + "/.git/contents"), fileContentsSb.toString());
            
            saveHEAD();
        } catch (IOException e) {
            System.err.println("Error saving commits: " + e.getMessage());
        }
    }

    /**
     * Loads commits from the storage file.
     *
     * This method reads the serialized commit objects and initializes the commit history
     * in the repository using the corresponding `Commit` object instances.
     */
    private void loadCommits() {
        try {
            try {
                String contentsData = Files.readString(Paths.get(path + "/.git/contents"));
                String[] lines = contentsData.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split("\\|", 2);
                        if (parts.length == 2) {
                            fileContents.put(parts[0], parts[1].replace("\\n", "\n"));
                        }
                    }
                }
            } catch (IOException e) {
            }
            
            String content = Files.readString(Paths.get(path + "/.git/commits"));
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        String hash = parts[0];
                        String treeHash = parts[1];
                        String parentHash = parts[2].equals("null") ? null : parts[2];
                        String author = parts[3];
                        Instant timestamp = Instant.parse(parts[4]);
                        String message = parts[5].replace("\\n", "\n");
                        
                        Commit commit = new RestoredCommit(hash, treeHash, parentHash, author, timestamp, message);
                        commitHistory.add(commit);
                        
                        HashMap<String, String> snapshot = new HashMap<>();
                        if (parts.length > 6 && !parts[6].trim().isEmpty()) {
                            String[] fileEntries = parts[6].split(";");
                            for (String entry : fileEntries) {
                                if (!entry.trim().isEmpty()) {
                                    String[] fileData = entry.split("=", 2);
                                    if (fileData.length == 2) {
                                        snapshot.put(fileData[0], fileData[1]);
                                    }
                                }
                            }
                        }
                        commitSnapshots.put(hash, snapshot);
                    }
                }
            }
            
            loadHEAD();
        } catch (IOException e) {
        }
    }

    /**
     * Saves the current state of the HEAD pointer.
     *
     * The HEAD pointer identifies the current commit. This method serializes the
     * current HEAD's position and writes it to a file to ensure that future operations
     * know the latest commit state.
     */
    private void saveHEAD() {
        try {
            if (currentCommit != null) {
                Files.writeString(Paths.get(path + "/.git/HEAD"), currentCommit.getHash());
            }
        } catch (IOException e) {
            System.err.println("Error saving HEAD: " + e.getMessage());
        }
    }

    /**
     * Loads the state of the HEAD pointer.
     *
     * This method reads the stored HEAD pointer data, initializing the repository to
     * point to the corresponding `Commit` object identified as the current HEAD.
     */
    private void loadHEAD() {
        try {
            String headHash = Files.readString(Paths.get(path + "/.git/HEAD")).trim();
            for (Commit commit : commitHistory) {
                if (commit.getHash().equals(headHash)) {
                    currentCommit = commit;
                    break;
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     * A subclass of {@link Commit} that represents a commit restored from persistent storage.
     *
     * This class extends the functionality of the parent `Commit` class to include additional
     * metadata that was preserved when the commit was serialized. It allows for detailed
     * reconstruction of the commit history with its original state.
     */
    private static class RestoredCommit extends Commit {
        private final String originalHash;
        private final Instant originalTimestamp;
        private final String originalTreeHash;
        private final String originalParentHash;
        private final String originalAuthor;
        private final String originalMessage;

        /**
         * Constructs a new {@code RestoredCommit} instance with its original metadata.
         *
         * @param originalHash        The original hash of the restored commit.
         * @param originalTimestamp   The original timestamp of the restored commit.
         * @param originalTreeHash    The tree hash representing the file snapshot of the restored commit.
         * @param originalParentHash  The hash of the parent commit (or null if this is the first commit).
         * @param originalMessage     The original commit message.
         * @param author              The author of this commit.
         */
        public RestoredCommit(String hash, String treeHash, String parentHash, String author, Instant timestamp, String message) {
            super(treeHash, parentHash, author, message);
            this.originalHash = hash;
            this.originalTimestamp = timestamp;
            this.originalTreeHash = treeHash;
            this.originalParentHash = parentHash;
            this.originalAuthor = author;
            this.originalMessage = message;
        }

        /**
         * Returns the original hash of this commit.
         *
         * @return The hash as a string.
         */
        @Override
        public String getHash() {
            return originalHash;
        }

        /**
         * Returns the original timestamp when the commit was made.
         *
         * @return The timestamp as an {@link Instant} object.
         */
        @Override
        public Instant getTimestamp() {
            return originalTimestamp;
        }

        /**
         * Returns the original tree hash that represents the state of the repository for this commit.
         *
         * @return The tree hash as a string.
         */
        @Override
        public String getTreeHash() {
            return originalTreeHash;
        }

        /**
         * Returns the original parent hash of this commit.
         *
         * @return The hash of the parent commit, or null if this was the first commit.
         */
        @Override
        public String getParentHash() {
            return originalParentHash;
        }

        /**
         * Returns the original author of the commit.
         *
         * @return The author as a string.
         */
        @Override
        public String getAuthor() {
            return originalAuthor;
        }

        /**
         * Returns the original commit message.
         *
         * @return The commit message as a string.
         */
        @Override
        public String getMessage() {
            return originalMessage;
        }
    }
}