import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class scratch_test {
    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(Files.newBufferedReader(Paths.get("data", "users.properties"), StandardCharsets.UTF_8));
            System.out.println("LOADED USERS:");
            for (String key : props.stringPropertyNames()) {
                System.out.println("Key: [" + key + "], Value: [" + props.getProperty(key) + "]");
            }
            System.out.println("Checking 'sath': " + props.getProperty("sath"));
            System.out.println("Checking 'nitin': " + props.getProperty("nitin"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
