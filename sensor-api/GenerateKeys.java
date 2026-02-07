import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048, SecureRandom.getInstanceStrong());
        KeyPair kp = kpg.generateKeyPair();
        
        // Save public key
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/app.pub")) {
            fos.write(kp.getPublic().getEncoded());
        }
        
        // Save private key
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/app.key")) {
            fos.write(kp.getPrivate().getEncoded());
        }
        
        System.out.println("Keys generated successfully!");
        System.out.println("Public key: src/main/resources/app.pub");
        System.out.println("Private key: src/main/resources/app.key");
    }
}
