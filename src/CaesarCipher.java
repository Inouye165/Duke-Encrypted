import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
// Imports needed for File Chooser
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class CaesarCipher {

    // --- encrypt method remains the same ---
    public String encrypt(String input, int key) {
        StringBuilder encrypted = new StringBuilder(input);
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int effectiveKey = key % 26;
        if (effectiveKey < 0) {
            effectiveKey += 26;
        }
        String shiftedAlphabet = alphabet.substring(effectiveKey) + alphabet.substring(0, effectiveKey);

        for (int i = 0; i < encrypted.length(); i++) {
            char currChar = encrypted.charAt(i);
            boolean isLower = Character.isLowerCase(currChar);
            char upperChar = Character.toUpperCase(currChar);
            int idx = alphabet.indexOf(upperChar);

            if (idx != -1) {
                char newUpperChar = shiftedAlphabet.charAt(idx);
                char newChar = isLower ? Character.toLowerCase(newUpperChar) : newUpperChar;
                encrypted.setCharAt(i, newChar);
            }
        }
        return encrypted.toString();
    }


    // --- Main method UPDATED to use the specific OneDrive Documents path ---
    public static void main(String[] args) {
        System.out.println("🧙‍♂️✨ Welcome to the Magical Caesar Cipher Machine! ✨🧙‍♀️");

        int encryptionKey = 17;
        String originalMessage = "";
        String chosenFilePath = "";

        try {
            JFileChooser fileChooser = new JFileChooser();

            // **Set Default Directory to the SPECIFIC OneDrive path**
            // Using the exact path you provided. Remember double backslashes in Java Strings.
            String specificDocumentsPath = "C:\\Users\\inouy\\OneDrive\\Documents";
            File documentsDir = new File(specificDocumentsPath);

            // Check if the specific directory exists before setting it
            if (documentsDir.isDirectory()) {
                fileChooser.setCurrentDirectory(documentsDir);
                System.out.println(" HINT: File chooser starting in: " + specificDocumentsPath);
            } else {
                // If the specific path doesn't exist, print a warning and use default
                System.out.println("⚠️ Warning: Specified OneDrive Documents directory not found: " + specificDocumentsPath);
                System.out.println("         File chooser will open in the default location.");
                // JFileChooser will use its own default (often user home or last used)
            }

            // Filter for .txt files only
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Scrolls (*.txt)", "txt");
            fileChooser.setFileFilter(filter);

            // Customize the Dialog
            fileChooser.setDialogTitle("📜 Choose Your Secret Scroll (.txt) 📜");
            fileChooser.setApproveButtonText("Select This Scroll!");
            fileChooser.setApproveButtonToolTipText("Confirm selection of this file for cipher magic!");

            // Show the dialog
            System.out.println("❓ Please choose the text scroll you wish to process...");
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                chosenFilePath = selectedFile.getAbsolutePath();
                System.out.println("✔️ You selected scroll: " + chosenFilePath);

                originalMessage = Files.readString(Paths.get(chosenFilePath));
                System.out.println("📜 Scroll successfully read!");

            } else {
                System.out.println("🚫 No scroll selected, or you cancelled. Aborting the magic.");
                System.exit(0);
            }

        } catch (IOException e) {
            System.err.println("❌ Oh dear! Trouble reading the selected scroll: " + e.getMessage());
            System.err.println("Please ensure the file exists and you have permission to read it.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ An unexpected error occurred during file selection: " + e.getMessage());
            System.exit(1);
        }

        // --- Encryption ---
        System.out.println("\n🔑 Applying the secret key (" + encryptionKey + ") for encryption...");
        CaesarCipher cipher = new CaesarCipher();
        String encryptedMessage = cipher.encrypt(originalMessage, encryptionKey);
        System.out.println("🔒 Encryption complete!");

        // --- Decryption ---
        int decryptionKey = 26 - encryptionKey;
        System.out.println("\n🔑 Applying the reverse key (" + decryptionKey + ") for decryption...");
        String decryptedMessage = cipher.encrypt(encryptedMessage, decryptionKey);
        System.out.println("🔓 Decryption complete!");

        // --- Display Results ---
        System.out.println("\n\n--- ✨ The Grand Reveal! ✨ ---");
        System.out.println("\n📜 Original Message from the Scroll:\n--------------------------------------\n" + originalMessage);
        System.out.println("\n🔒 Encrypted Message (Shhh!):\n--------------------------------------\n" + encryptedMessage);
        System.out.println("\n🔓 Decrypted Message (Back to normal!):\n--------------------------------------\n" + decryptedMessage);
        System.out.println("\n--- Magic accomplished! ---");
    }
}