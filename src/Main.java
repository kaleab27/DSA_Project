import objects.Commit;
import objects.Object;

import static utils.Hasher.computeSHA1;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a command");
            return;
        }
        
        String workingDirectory = System.getProperty("user.dir");
        Repository repo = new Repository(workingDirectory);
        String command = args[0];
        
        switch (command) {
            case "init":
                repo.init();
                break;
            case "add":
                if (args.length < 2) {
                    System.out.println("Please specify a file to add");
                    return;
                }
                repo.addFile(args[1]);
                break;
            case "commit":
                if (args.length < 3) {
                    System.out.println("Usage: commit \"<message>\" \"<author>\"");
                    return;
                }
                repo.commit(args[1], args[2]);
                break;
            case "log":
                repo.log();
                break;
            case "status":
                repo.status();
                break;
            case "remove":
                if (args.length < 2) {
                    System.out.println("Please specify a file to remove");
                    return;
                }
                repo.removeFile(args[1]);
                break;
            case "checkout":
                if (args.length < 2) {
                    System.out.println("Please specify a commit hash");
                    return;
                }
                repo.checkout(args[1]);
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
        
        // // Test the Hasher
        // String input = "Hello, Git Clone!";
        // String hash = computeSHA1(input);
        // System.out.println("Input: " + input);
        // System.out.println("SHA-1 Hash: " + hash);

        // // Test the Git Object
        // Object blobObject = new Object("blob", "Hello, Git!");
        // System.out.println("Type: " + blobObject.getType());
        // System.out.println("Content: " + blobObject.getContent());
        // System.out.println("Hash: " + blobObject.getHash());
        // System.out.println("Object: " + blobObject);

        // // Test the Commit Git Object
        // String treeHash = "abc123treehash";
        // String parentHash = null;
        // Commit commit = new Commit(treeHash, parentHash, "John Doe <john@example.com>", "Initial commit");
        // System.out.println(commit);
    }
}