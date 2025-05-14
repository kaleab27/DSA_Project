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

        // Store the actual file contents before creating the tree hash
        HashMap<String, String> commitFiles = new HashMap<>();
        for (var entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            commitFiles.put(fileName, fileHash);
            
            // Ensure file content is stored
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
        
        // Store the snapshot with the actual file hashes
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
                    
                    // Restore each file from the snapshot
                    for (var entry : snapshot.entrySet()) {
                        String fileName = entry.getKey();
                        String fileHash = entry.getValue();
                        String content = fileContents.get(fileHash);
                        
                        if (content != null) {
                            try {
                                // Write the content back to the actual file
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
    
    private void saveHEAD() {
        try {
            if (currentCommit != null) {
                Files.writeString(Paths.get(path + "/.git/HEAD"), currentCommit.getHash());
            }
        } catch (IOException e) {
            System.err.println("Error saving HEAD: " + e.getMessage());
        }
    }
    
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
    
    private static class RestoredCommit extends Commit {
        private final String originalHash;
        private final Instant originalTimestamp;
        private final String originalTreeHash;
        private final String originalParentHash;
        private final String originalAuthor;
        private final String originalMessage;
    
        public RestoredCommit(String hash, String treeHash, String parentHash, String author, Instant timestamp, String message) {
            super(treeHash, parentHash, author, message);
            this.originalHash = hash;
            this.originalTimestamp = timestamp;
            this.originalTreeHash = treeHash;
            this.originalParentHash = parentHash;
            this.originalAuthor = author;
            this.originalMessage = message;
        }
    
        @Override
        public String getHash() {
            return originalHash;
        }
    
        @Override
        public Instant getTimestamp() {
            return originalTimestamp;
        }
        
        @Override
        public String getTreeHash() {
            return originalTreeHash;
        }
        
        @Override
        public String getParentHash() {
            return originalParentHash;
        }
        
        @Override
        public String getAuthor() {
            return originalAuthor;
        }
        
        @Override
        public String getMessage() {
            return originalMessage;
        }
    }
}