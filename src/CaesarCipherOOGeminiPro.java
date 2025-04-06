// ============================================================
// START OF COMPLETE CaesarCipherOOGeminiPro.java FILE CONTENT (Combined Encrypt/Decrypt Demo)
// ============================================================
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.Objects;
import java.util.Optional;


public class CaesarCipherOOGeminiPro {

    private static final int ALPHABET_SIZE = 26;

    // --- Nested Class for Single-Key Cipher Logic ---
    // No changes needed in CaesarCipher class
    public static class CaesarCipher {
        private static final String ALPHABET_UPPER="ABCDEFGHIJKLMNOPQRSTUVWXYZ";private static final String ALPHABET_LOWER=ALPHABET_UPPER.toLowerCase();private final String shiftedAlphabetUpper;private final String shiftedAlphabetLower;private final int mainKey;
        public CaesarCipher(int key){this.mainKey=Math.floorMod(key,ALPHABET_SIZE);this.shiftedAlphabetUpper=ALPHABET_UPPER.substring(mainKey)+ALPHABET_UPPER.substring(0,mainKey);this.shiftedAlphabetLower=ALPHABET_LOWER.substring(mainKey)+ALPHABET_LOWER.substring(0,mainKey);}
        public String encrypt(String input){Objects.requireNonNull(input,"Input string cannot be null.");StringBuilder e=new StringBuilder(input);for(int i=0;i<e.length();i++){char c=e.charAt(i);int idxU=ALPHABET_UPPER.indexOf(c);if(idxU!=-1){e.setCharAt(i,shiftedAlphabetUpper.charAt(idxU));continue;}int idxL=ALPHABET_LOWER.indexOf(c);if(idxL!=-1){e.setCharAt(i,shiftedAlphabetLower.charAt(idxL));}}return e.toString();}
        public String decrypt(String input){Objects.requireNonNull(input,"Input string cannot be null.");int dk=Math.floorMod(ALPHABET_SIZE-this.mainKey,ALPHABET_SIZE);CaesarCipher d=new CaesarCipher(dk);return d.encrypt(input);}
        @Override public String toString(){return"CaesarCipher object with key: "+mainKey;}public int getKey(){return mainKey;}
    } // --- End of nested CaesarCipher class ---

    // --- Nested Class for Dual-Key Cipher Logic ---
    // No changes needed in CaesarCipherTwoKeys class
    public static class CaesarCipherTwoKeys {
        private static final String ALPHABET_UPPER="ABCDEFGHIJKLMNOPQRSTUVWXYZ";private static final String ALPHABET_LOWER=ALPHABET_UPPER.toLowerCase();private final String shiftedAlphabetUpper1;private final String shiftedAlphabetLower1;private final String shiftedAlphabetUpper2;private final String shiftedAlphabetLower2;private final int key1;private final int key2;
        public CaesarCipherTwoKeys(int key1,int key2){this.key1=Math.floorMod(key1,ALPHABET_SIZE);this.key2=Math.floorMod(key2,ALPHABET_SIZE);this.shiftedAlphabetUpper1=ALPHABET_UPPER.substring(this.key1)+ALPHABET_UPPER.substring(0,this.key1);this.shiftedAlphabetLower1=shiftedAlphabetUpper1.toLowerCase();this.shiftedAlphabetUpper2=ALPHABET_UPPER.substring(this.key2)+ALPHABET_UPPER.substring(0,this.key2);this.shiftedAlphabetLower2=shiftedAlphabetUpper2.toLowerCase();}
        public String encrypt(String input){Objects.requireNonNull(input,"Input string cannot be null.");StringBuilder e=new StringBuilder(input);for(int i=0;i<e.length();i++){char c=e.charAt(i);boolean even=(i%2==0);String sU=even?shiftedAlphabetUpper1:shiftedAlphabetUpper2;String sL=even?shiftedAlphabetLower1:shiftedAlphabetLower2;int idxU=ALPHABET_UPPER.indexOf(c);if(idxU!=-1){e.setCharAt(i,sU.charAt(idxU));continue;}int idxL=ALPHABET_LOWER.indexOf(c);if(idxL!=-1){e.setCharAt(i,sL.charAt(idxL));}}return e.toString();}
        public String decrypt(String input){Objects.requireNonNull(input,"Input string cannot be null.");int dk1=Math.floorMod(ALPHABET_SIZE-this.key1,ALPHABET_SIZE);int dk2=Math.floorMod(ALPHABET_SIZE-this.key2,ALPHABET_SIZE);CaesarCipherTwoKeys d=new CaesarCipherTwoKeys(dk1,dk2);return d.encrypt(input);}
        @Override public String toString(){return"CaesarCipherTwoKeys object with key1: "+key1+", key2: "+key2;}public int getKey1(){return key1;}public int getKey2(){return key2;}
    } // --- End of nested CaesarCipherTwoKeys class ---


    // --- Nested Class for File I/O Logic ---
    // No changes needed in FileIOService class, but update dialog prompt
    public static class FileIOService {
        public record FileData(String path, String content) {}
        private final String startingDirectoryPath;
        public FileIOService(String startingDirectoryPath) { this.startingDirectoryPath = startingDirectoryPath; }
        public Optional<FileData> selectAndReadFile() {
           JFileChooser fc=new JFileChooser();if(startingDirectoryPath!=null&&!startingDirectoryPath.isEmpty()){File d=new File(startingDirectoryPath);if(d.isDirectory()){fc.setCurrentDirectory(d);System.out.println(" HINT: File chooser starting in: "+startingDirectoryPath);}else{System.out.println("⚠️ Warning: Dir not found: "+startingDirectoryPath+"\n Opening in default.");}}else{System.out.println(" HINT: No starting dir.");}
           FileNameExtensionFilter f=new FileNameExtensionFilter("Text Files (*.txt)","txt");fc.setFileFilter(f);
           // *** Updated Dialog Title ***
           fc.setDialogTitle("📜 Select ORIGINAL Plaintext File (.txt) 📜");
           fc.setApproveButtonText("Process This File");System.out.println("❓ Please choose the ORIGINAL plaintext file...");
           int r=fc.showOpenDialog(null);
           if(r==JFileChooser.APPROVE_OPTION){File sf=fc.getSelectedFile();String fp=sf.getAbsolutePath();System.out.println("✔️ You selected file: "+fp);try{String c=Files.readString(Paths.get(fp),java.nio.charset.StandardCharsets.UTF_8);System.out.println("📜 File successfully read!");return Optional.of(new FileData(fp,c));}catch(IOException e){System.err.println("❌ Error reading file: "+e.getMessage());return Optional.empty();}catch(Exception e){System.err.println("❌ Unexpected file reading error: "+e.getMessage());e.printStackTrace();return Optional.empty();}}else{System.out.println("🚫 No file selected/cancelled.");return Optional.empty();}
        }
    } // --- End of nested FileIOService class ---


    // --- Nested Class for Results Display Logic ---
    // *** UPDATED for Combined Encrypt/Decrypt Demo ***
    public static class ResultsViewer {

        // UPDATED Method Signature to include all 5 text versions
        public void displayResults(String originalMessage,
                                   String encryptedSingle,
                                   String encryptedDual, // This is the intermediate ciphertext
                                   String decryptedSingleFromDual,
                                   String decryptedDualFromDual,
                                   int key1, int key2,
                                   String filePath) {
            System.out.println("\n✨ Conjuring the combined results window...");

            String resultsHTML = buildHtmlContent(originalMessage, encryptedSingle, encryptedDual,
                                                  decryptedSingleFromDual, decryptedDualFromDual,
                                                  key1, key2, filePath);

            SwingUtilities.invokeLater(() -> {
                JEditorPane editorPane = new JEditorPane();
                editorPane.setEditable(false);
                editorPane.setContentType("text/html; charset=UTF-8");
                editorPane.setText(resultsHTML);
                SwingUtilities.invokeLater(() -> editorPane.setCaretPosition(0));
                JScrollPane scrollPane = new JScrollPane(editorPane);
                // Make window potentially taller to fit more content
                scrollPane.setPreferredSize(new Dimension(800, 750));
                JFrame frame = new JFrame("✨ Combined Cipher Results ✨"); // Updated title
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(scrollPane);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                System.out.println("✅ Combined results window should be visible now!");
            });
        }

        // UPDATED HTML builder for combined results
        private String buildHtmlContent(String originalMessage,
                                        String encryptedSingle,
                                        String encryptedDual,
                                        String decryptedSingleFromDual,
                                        String decryptedDualFromDual,
                                        int key1, int key2,
                                        String filePath) {
             return "<html><head><meta charset='UTF-8'></head><body style='font-family: sans-serif; padding: 15px; background-color: #f0f8ff;'>"
                 + "<h1 style='color: #483d8b; text-align: center;'>✨ Combined Cipher Demonstration ✨</h1><hr style='border-top: 1px dashed #483d8b;'>"

                 // 1. Original Plaintext Section
                 + "<h2 style='color: #2f4f4f;'>📜 Original Plaintext from: " + escapeHtml(filePath) + "</h2>"
                 + "<div style='border: 1px solid #778899; padding: 10px; margin-bottom: 15px; background-color: #ffffff; font-family: Consolas, monospace; white-space: pre-wrap; word-wrap: break-word;'>" + escapeHtml(originalMessage) + "</div>"

                 // 2. Single-Key Encryption Section
                 + "<h2 style='color: #b22222;'>🔑 Encrypted with Single-Key (Key: " + key1 + ")</h2>"
                 + "<div style='border: 1px solid #cd5c5c; padding: 10px; margin-bottom: 15px; background-color: #fffafa; font-family: Consolas, monospace; color: #b22222; white-space: pre-wrap; word-wrap: break-word;'>" + escapeHtml(encryptedSingle) + "</div>"

                 // 3. Dual-Key Encryption Section (Intermediate Ciphertext)
                 + "<h2 style='color: #191970;'>🔑🔑 Encrypted with Dual-Key (Key1: " + key1 + ", Key2: " + key2 + ")</h2>"
                 + "<div style='border: 1px solid #4682b4; padding: 10px; margin-bottom: 15px; background-color: #f0f8ff; font-family: Consolas, monospace; color: #191970; white-space: pre-wrap; word-wrap: break-word;'>" + escapeHtml(encryptedDual) + "</div>"

                 + "<hr style='border-top: 1px dashed #483d8b;'>" // Separator for decryption attempts
                 + "<h2 style='text-align: center; color: #555;'>Attempting Decryption of the Dual-Key Ciphertext Above...</h2>"

                 // 4. Decryption of Intermediate using Single-Key
                 + "<h2 style='color: #ff8c00;'>🔓 Decrypted using Single-Key (Key: " + key1 + ")</h2>"
                 + "<p style='margin-left: 10px; font-style: italic; color: #777;'>(Attempting to decrypt the Dual-Key text with only Key 1)</p>"
                 + "<div style='border: 1px solid #ffcc80; padding: 10px; margin-bottom: 15px; background-color: #fff8e1; font-family: Consolas, monospace; color: #e65100; white-space: pre-wrap; word-wrap: break-word;'>" + escapeHtml(decryptedSingleFromDual) + "</div>"

                 // 5. Decryption of Intermediate using Dual-Key
                 + "<h2 style='color: #2e8b57;'>🔓 Decrypted using Dual-Key (Key1: " + key1 + ", Key2: " + key2 + ")</h2>"
                 + "<p style='margin-left: 10px; font-style: italic; color: #777;'>(Attempting to decrypt the Dual-Key text with both correct keys - should match original)</p>"
                 + "<div style='border: 1px solid #3cb371; padding: 10px; margin-bottom: 15px; background-color: #f5fff5; font-family: Consolas, monospace; color: #2e8b57; white-space: pre-wrap; word-wrap: break-word;'>" + escapeHtml(decryptedDualFromDual) + "</div>"

                 + "<hr style='border-top: 1px dashed #483d8b;'><p style='text-align: center; font-style: italic; color: #483d8b;'>--- End of Demonstration ---</p>"
                 + "</body></html>";
        }
        // escapeHtml remains the same
        private static String escapeHtml(String text) {
             if(text==null)return"";return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
        }
    } // --- End of nested ResultsViewer class ---


    // --- Test Runner for Single Key Cipher ---
    // Remains the same
    private static void runCaesarCipherTests() {
        System.out.println("\n--- Running Basic Caesar Cipher Tests ---");int p=0,f=0;
        String o1="HelloWorld";int k1=3;String e1="KhoorZruog";CaesarCipher c1=new CaesarCipher(k1);String n1=c1.encrypt(o1);String d1=c1.decrypt(n1);if(e1.equals(n1)&&o1.equals(d1)){System.out.println("Test 1 PASS");p++;}else{System.err.println("Test 1 FAIL");f++;}
        String o2="Zoo";int k2=5;String e2="Ett";CaesarCipher c2=new CaesarCipher(k2);String n2=c2.encrypt(o2);String d2=c2.decrypt(n2);if(e2.equals(n2)&&o2.equals(d2)){System.out.println("Test 2 PASS");p++;}else{System.err.println("Test 2 FAIL");f++;}
        String o3="Hello, World 123!";int k3=15;String e3="Wtaad, Ldgas 123!";CaesarCipher c3=new CaesarCipher(k3);String n3=c3.encrypt(o3);String d3=c3.decrypt(n3);if(e3.equals(n3)&&o3.equals(d3)){System.out.println("Test 3 PASS");p++;}else{System.err.println("Test 3 FAIL");f++;}
        String o4="NoChange";int k4=0;String e4="NoChange";CaesarCipher c4=new CaesarCipher(k4);String n4=c4.encrypt(o4);String d4=c4.decrypt(n4);if(e4.equals(n4)&&o4.equals(d4)){System.out.println("Test 4 PASS");p++;}else{System.err.println("Test 4 FAIL");f++;}
        String o5="";int k5=10;String e5="";CaesarCipher c5=new CaesarCipher(k5);String n5=c5.encrypt(o5);String d5=c5.decrypt(n5);if(e5.equals(n5)&&o5.equals(d5)){System.out.println("Test 5 PASS");p++;}else{System.err.println("Test 5 FAIL");f++;}
        System.out.println("--- Single Test Summary: P:"+p+" F:"+f+" ---\n");if(f>0){System.err.println("! SINGLE TEST FAILURES !");}
    }

    // --- Test Runner for Dual Key Cipher ---
    // Remains the same
    private static void runCaesarCipherTwoKeysTests() {
        System.out.println("\n--- Running Dual-Key Caesar Cipher Tests ---");int p=0,f=0;
        String o1="abcdef";int k11=1,k21=3;String e1="bedgfi";CaesarCipherTwoKeys c1=new CaesarCipherTwoKeys(k11,k21);String n1=c1.encrypt(o1);String d1=c1.decrypt(n1);if(e1.equals(n1)&&o1.equals(d1)){System.out.println("Test 1 Dual PASS");p++;}else{System.err.println("Test 1 Dual FAIL");f++;}
        String o2="XYZxyz";int k12=5,k22=2;String e2="CAEzdb";CaesarCipherTwoKeys c2=new CaesarCipherTwoKeys(k12,k22);String n2=c2.encrypt(o2);String d2=c2.decrypt(n2);if(e2.equals(n2)&&o2.equals(d2)){System.out.println("Test 2 Dual PASS");p++;}else{System.err.println("Test 2 Dual FAIL");f++;}
        String o3="Hello, World 1!";int k13=10,k23=20;String e3="Ryvfy, Qylvx 1!";CaesarCipherTwoKeys c3=new CaesarCipherTwoKeys(k13,k23);String n3=c3.encrypt(o3);String d3=c3.decrypt(n3);if(e3.equals(n3)&&o3.equals(d3)){System.out.println("Test 3 Dual PASS");p++;}else{System.err.println("Test 3 Dual FAIL");f++;}
        String o4="Test";int k14=7,k24=7;String e4="Alza";CaesarCipherTwoKeys c4=new CaesarCipherTwoKeys(k14,k24);String n4=c4.encrypt(o4);String d4=c4.decrypt(n4);CaesarCipher cs=new CaesarCipher(k14);String sn=cs.encrypt(o4);if(e4.equals(n4)&&o4.equals(d4)&&sn.equals(n4)){System.out.println("Test 4 Dual PASS");p++;}else{System.err.println("Test 4 Dual FAIL");f++;}
        String o5="ZeroKeys";int k15=0,k25=0;String e5="ZeroKeys";CaesarCipherTwoKeys c5=new CaesarCipherTwoKeys(k15,k25);String n5=c5.encrypt(o5);String d5=c5.decrypt(n5);if(e5.equals(n5)&&o5.equals(d5)){System.out.println("Test 5 Dual PASS");p++;}else{System.err.println("Test 5 Dual FAIL");f++;}
        System.out.println("--- Dual Test Summary: P:"+p+" F:"+f+" ---\n");if(f>0){System.err.println("! DUAL TEST FAILURES !");}
    }


    // --- Main Method (Combined Encrypt -> Decrypt Demo) ---
    public static void main(String[] args) {

        // Run embedded tests first
        runCaesarCipherTests();
        runCaesarCipherTwoKeysTests();

        System.out.println("🧙‍♂️✨ Welcome to the Combined Caesar Cipher Demo! ✨🧙‍♀️"); // Updated welcome

        // Prompt for TWO keys
        int key1, key2;
        try {
            int[] keys = promptForTwoKeys();
            key1 = keys[0];
            key2 = keys[1];
        } catch (NumberFormatException e) {
             System.err.println("❌ Invalid key format. Please enter integers.");
             JOptionPane.showMessageDialog(null, "Invalid key format. Please enter integers.", "Error", JOptionPane.ERROR_MESSAGE); return;
        } catch (IllegalStateException e) {
            System.out.println("🚫 Key input cancelled. Aborting the magic."); return;
        }

        // File Selection for *PLAINTEXT*
        String specificDocumentsPath = "C:\\Users\\inouy\\OneDrive\\Documents";
        FileIOService fileService = new FileIOService(specificDocumentsPath);
        Optional<FileIOService.FileData> fileDataOptional = fileService.selectAndReadFile();

        if (fileDataOptional.isEmpty()) {
            System.out.println("🚫 File selection failed or was cancelled. Aborting."); return;
        }

        FileIOService.FileData fileData = fileDataOptional.get();
        String originalMessage = fileData.content(); // This is the plaintext
        String chosenFilePath = fileData.path();

        try {
            // --- Phase 1: Encryption ---
            System.out.println("\n--- Phase 1: Encrypting Original Plaintext ---");

            // Single-Key Encryption
            System.out.println("Creating Single-Key Cipher (Key: " + key1 + ")");
            CaesarCipher cipherSingle = new CaesarCipher(key1);
            System.out.println("Encrypting with Single-Key...");
            String encryptedSingle = cipherSingle.encrypt(originalMessage);
            System.out.println("Single-Key Encryption complete.");

            // Dual-Key Encryption
            System.out.println("Creating Dual-Key Cipher (Keys: " + key1 + ", " + key2 + ")");
            CaesarCipherTwoKeys cipherDual = new CaesarCipherTwoKeys(key1, key2);
            System.out.println("Encrypting with Dual-Key...");
            String encryptedDual = cipherDual.encrypt(originalMessage); // This becomes intermediate ciphertext
            System.out.println("Dual-Key Encryption complete.");


            // --- Phase 2: Decryption of the Dual-Key Ciphertext ---
            System.out.println("\n--- Phase 2: Decrypting the Dual-Key Encrypted Text ---");
            String intermediateCiphertext = encryptedDual; // For clarity

            // Decrypt Intermediate using Single-Key (Key 1)
            System.out.println("Decrypting intermediate text using Single-Key (Key: " + key1 + ")...");
            // No need to create a new cipherSingle object if keys haven't changed
            String decryptedSingleFromDual = cipherSingle.decrypt(intermediateCiphertext);
            System.out.println("Single-Key Decryption attempt complete.");

            // Decrypt Intermediate using Dual-Key (Keys 1 & 2)
            System.out.println("Decrypting intermediate text using Dual-Key (Keys: " + key1 + ", " + key2 + ")...");
            // No need to create a new cipherDual object if keys haven't changed
            String decryptedDualFromDual = cipherDual.decrypt(intermediateCiphertext);
            System.out.println("Dual-Key Decryption attempt complete (should match original).");


            // --- Display ALL Results ---
            ResultsViewer viewer = new ResultsViewer();
            viewer.displayResults(
                originalMessage,         // 1. Original
                encryptedSingle,         // 2. Encrypted w/ Single-Key
                encryptedDual,           // 3. Encrypted w/ Dual-Key (Intermediate)
                decryptedSingleFromDual, // 4. Decryption of (3) w/ Single-Key
                decryptedDualFromDual,   // 5. Decryption of (3) w/ Dual-Key
                key1, key2,              // The keys used
                chosenFilePath
            );

        } catch (NullPointerException e) {
             System.err.println("❌ Error: Text content seems to be null.");
             e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ An unexpected error occurred during processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to prompt for TWO keys (No changes needed)
    private static int[] promptForTwoKeys() throws NumberFormatException, IllegalStateException {
        JTextField k1f=new JTextField(5);JTextField k2f=new JTextField(5);JPanel p=new JPanel(new GridLayout(0,2,5,5));p.add(new JLabel("Enter Key 1 (for 1st, 3rd,... char):"));p.add(k1f);p.add(new JLabel("Enter Key 2 (for 2nd, 4th,... char):"));p.add(k2f);
        int r=JOptionPane.showConfirmDialog(null, p,"🔑🔑 Enter Cipher Keys",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE); // Updated title
        if(r==JOptionPane.OK_OPTION){try{int k1=Integer.parseInt(k1f.getText().trim());int k2=Integer.parseInt(k2f.getText().trim());return new int[]{k1,k2};}catch(NumberFormatException e){throw new NumberFormatException("Invalid integer key format.");}}else{throw new IllegalStateException("User cancelled key input.");}
    }
}
// ============================================================
// END OF COMPLETE CaesarCipherOOGeminiPro.java FILE CONTENT (Combined Encrypt/Decrypt Demo)
// ============================================================