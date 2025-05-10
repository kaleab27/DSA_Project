import java.io.File;

/**
 * Represents a version control repository.
 *
 * This class provides functionality for initializing a repository and managing files within it.
 * It simulates basic repository operations, including creating the repository structure and
 * tracking files to be added.
 */
public class Repository {
    private final String path;

    /**
     * Constructs a new {@code Repository} instance with the specified path.
     *
     * @param path The path to the directory where the repository is located.
     */
    public Repository(String path) {
        this.path = path;
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
     * If the file is found at the specified path, it simulates adding the file to the repository.
     * If not, it notifies the user that the specified file does not exist.
     *
     * @param fileName The name of the file to be added.
     */
    public void addFile(String fileName) {
        File file = new File(path + "/" + fileName);
        if (file.exists()) {
            System.out.println("File added to repository: " + fileName);
        } else {
            System.out.println("File not found: " + fileName);
        }
    }
}