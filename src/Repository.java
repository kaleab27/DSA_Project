import java.io.File;

public class Repository {
    private final String path;

    public Repository(String path) {
        this.path = path;
    }

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
    public void addFile(String fileName) {
        File file = new File(path + "/" + fileName);
        if (file.exists()) {
            System.out.println("File added to repository: " + fileName);
        } else {
            System.out.println("File not found: " + fileName);
        }
    }
}