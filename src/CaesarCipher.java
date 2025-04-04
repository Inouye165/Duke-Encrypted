import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
// Imports needed for File Chooser AND the styled output window
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
// import javax.swing.JTextArea; // No longer using JTextArea
import javax.swing.JEditorPane; // Using JEditorPane for styled text
import javax.swing.JScrollPane;
import java.awt.Dimension;
// import java.awt.Font; // Font setting will be done via HTML/CSS
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

    /**
     * Basic HTML escaping for displaying text safely within HTML content.
     * Replaces critical characters and newlines.
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        // Basic escaping for characters that interfere with HTML interpretation
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;")
                   // Convert newlines to HTML line breaks for display
                   .replace("\n", "<br>");
    }


    // --- Main method modified for Styled Window Output ---
    public static void main(String[] args) {
        System.out.println("🧙‍♂️✨ Welcome to the Magical Caesar Cipher Machine! ✨🧙‍♀️");

        int encryptionKey = 17;
        String originalMessage = "";
        String chosenFilePath = "";

        try {
            JFileChooser fileChooser = new JFileChooser();
            String specificDocumentsPath = "C:\\Users\\inouy\\OneDrive\\Documents";
            File documentsDir = new File(specificDocumentsPath);

            if (documentsDir.isDirectory()) {
                fileChooser.setCurrentDirectory(documentsDir);
                 System.out.println(" HINT: File chooser starting in: " + specificDocumentsPath);
            } else {
                System.out.println("⚠️ Warning: Specified OneDrive Documents directory not found: " + specificDocumentsPath);
                System.out.println("         File chooser will open in the default location.");
            }

            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Scrolls (*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("📜 Choose Your Secret Scroll (.txt) 📜");
            fileChooser.setApproveButtonText("Select This Scroll!");
            fileChooser.setApproveButtonToolTipText("Confirm selection of this file for cipher magic!");

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


        // --- Display Results in a Styled Window ---
        System.out.println("\n✨ Conjuring the styled results window...");

        // 1. Create an HTML string with formatting and colors
        // Using inline CSS for basic styling. Choose your favorite colors!
        String resultsHTML = "<html>" +
            "<body style='font-family: sans-serif; padding: 15px; background-color: #f0f8ff;'>" + // Light blue background

            "<h1 style='color: #483d8b; text-align: center;'>✨ The Grand Reveal! ✨</h1>" + // Dark slate blue title
            "<hr style='border-top: 1px dashed #483d8b;'>" +

            "<h2 style='color: #2f4f4f;'>📜 Original Message:</h2>" + // Dark slate gray subtitle
            "<div style='border: 1px solid #778899; padding: 10px; margin-bottom: 15px; background-color: #ffffff; font-family: Consolas, monospace;'>" +
            escapeHtml(originalMessage) + // Use escapeHtml to handle special chars and newlines
            "</div>" +

            "<h2 style='color: #b22222;'>🔒 Encrypted Message (Shhh!):</h2>" + // Firebrick red subtitle
            "<div style='border: 1px solid #cd5c5c; padding: 10px; margin-bottom: 15px; background-color: #fffafa; font-family: Consolas, monospace; color: #b22222;'>" +
            escapeHtml(encryptedMessage) + // Escape the message content
            "</div>" +

            "<h2 style='color: #228b22;'>🔓 Decrypted Message (Back to normal!):</h2>" + // Forest green subtitle
            "<div style='border: 1px solid #90ee90; padding: 10px; margin-bottom: 15px; background-color: #f0fff0; font-family: Consolas, monospace; color: #228b22;'>" +
            escapeHtml(decryptedMessage) + // Escape the message content
            "</div>" +

            "<hr style='border-top: 1px dashed #483d8b;'>" +
            "<p style='text-align: center; font-style: italic; color: #483d8b;'>--- Magic accomplished! ---</p>" +

            "</body></html>";


        // 2. Create and show the results window using JEditorPane
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Use JEditorPane for HTML content
            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.setContentType("text/html"); // Set content type to HTML
            editorPane.setText(resultsHTML);        // Set the HTML content
            editorPane.setCaretPosition(0); // Scroll to top

            // Wrap it in a scroll pane
            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setPreferredSize(new Dimension(750, 550)); // Adjusted size slightly

            // Create the window (JFrame)
            JFrame frame = new JFrame("✨ Formatted Cipher Results ✨"); // Updated Title
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(scrollPane);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        System.out.println("✅ Styled results window launched! Check the separate window for output.");
    }
}