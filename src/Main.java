import java.util.Objects;

import static utils.Hasher.computeSHA1;

public class Main {
    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println(workingDirectory);
        Repository repo = new Repository(workingDirectory);
        String command = args[0];
        if (Objects.equals(command, "init")) {
            repo.init();
        } else if(Objects.equals(command, "add")) {
            //  Add more logic to handle wild characters and multiple files
            repo.addFile(args[1]);
        }

        // Test the Hasher
        String input = "Hello, Git Clone!";
        String hash = computeSHA1(input);
        System.out.println("Input: " + input);
        System.out.println("SHA-1 Hash: " + hash);
    }
}