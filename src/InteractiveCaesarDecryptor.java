import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors; // Keep imports tidy
import java.util.Set;             // Keep imports tidy


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;


// Renamed slightly for clarity, but functionality is the interactive decryptor with auto-guess
public class InteractiveCaesarDecryptor {

    private static final int ALPHABET_SIZE = 26;
    private static final String ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int E_INDEX = ALPHABET_UPPER.indexOf('E'); // Index of 'E'

    // Store original encrypted text and file path for access by UI updates
    private static String originalEncryptedText = "";
    private static String sourceFilePath = "";

    // --- Nested Frequency Analyzer Logic ---
    // (Copied from CaesarBreakerApp)
    public static class FrequencyAnalyzer {
        public Map<Character, Integer> countLetterFrequencies(String text) { Map<Character,Integer> f=new HashMap<>();if(text==null)return f;for(char c:text.toUpperCase().toCharArray()){if(c>='A'&&c<='Z'){f.put(c,f.getOrDefault(c,0)+1);}}return f;}
        public Optional<Character> findMostFrequentLetter(Map<Character,Integer> f){if(f==null||f.isEmpty()){return Optional.empty();}int max=0;char most='\0';List<Character> keys=new ArrayList<>(f.keySet());Collections.sort(keys);for(char l:keys){int freq=f.get(l);if(freq>max){max=freq;most=l;}}return(most=='\0')?Optional.empty():Optional.of(most);}
        public OptionalInt calculateKeyFromFreqChar(Optional<Character> mostOpt){if(mostOpt.isEmpty()){return OptionalInt.empty();}char m=mostOpt.get();int fIdx=ALPHABET_UPPER.indexOf(m);if(fIdx==-1)return OptionalInt.empty();int key=(fIdx-E_INDEX+ALPHABET_SIZE)%ALPHABET_SIZE;return OptionalInt.of(key);}
        public record EvenOddStrings(String evenChars, String oddChars){}
        public EvenOddStrings separateEvenOddLetters(String text){StringBuilder e=new StringBuilder();StringBuilder o=new StringBuilder();if(text==null)return new EvenOddStrings("","");for(int i=0;i<text.length();i++){char c=text.charAt(i);if(Character.isLetter(c)){if(i%2==0){e.append(c);}else{o.append(c);}}}return new EvenOddStrings(e.toString(),o.toString());}
    }

    // --- Nested Single-Key Caesar Cipher Logic ---
    // (No changes needed)
    public static class CaesarCipher {
        private static final String ALPHABET_UPPER="ABCDEFGHIJKLMNOPQRSTUVWXYZ";private static final String ALPHABET_LOWER=ALPHABET_UPPER.toLowerCase();private final String shiftedAlphabetUpper;private final String shiftedAlphabetLower;private final int mainKey;
        public CaesarCipher(int key){this.mainKey=Math.floorMod(key,ALPHABET_SIZE);this.shiftedAlphabetUpper=ALPHABET_UPPER.substring(mainKey)+ALPHABET_UPPER.substring(0,mainKey);this.shiftedAlphabetLower=ALPHABET_LOWER.substring(mainKey)+ALPHABET_LOWER.substring(0,mainKey);}
        public String encrypt(String input){Objects.requireNonNull(input);StringBuilder e=new StringBuilder(input);for(int i=0;i<e.length();i++){char c=e.charAt(i);int idxU=ALPHABET_UPPER.indexOf(c);if(idxU!=-1){e.setCharAt(i,shiftedAlphabetUpper.charAt(idxU));continue;}int idxL=ALPHABET_LOWER.indexOf(c);if(idxL!=-1){e.setCharAt(i,shiftedAlphabetLower.charAt(idxL));}}return e.toString();}
        public String decrypt(String input){Objects.requireNonNull(input);int dk=Math.floorMod(ALPHABET_SIZE-this.mainKey,ALPHABET_SIZE);CaesarCipher d=new CaesarCipher(dk);return d.encrypt(input);}
        public int getKey(){return mainKey;}@Override public String toString(){return"CaesarCipher(key="+mainKey+")";}
    }

    // --- Nested Dual-Key Caesar Cipher Logic ---
    // (No changes needed)
    public static class CaesarCipherTwoKeys {
        private static final String ALPHABET_UPPER="ABCDEFGHIJKLMNOPQRSTUVWXYZ";private static final String ALPHABET_LOWER=ALPHABET_UPPER.toLowerCase();private final String shiftedAlphabetUpper1;private final String shiftedAlphabetLower1;private final String shiftedAlphabetUpper2;private final String shiftedAlphabetLower2;private final int key1;private final int key2;
        public CaesarCipherTwoKeys(int k1,int k2){this.key1=Math.floorMod(k1,ALPHABET_SIZE);this.key2=Math.floorMod(k2,ALPHABET_SIZE);this.shiftedAlphabetUpper1=ALPHABET_UPPER.substring(this.key1)+ALPHABET_UPPER.substring(0,this.key1);this.shiftedAlphabetLower1=shiftedAlphabetUpper1.toLowerCase();this.shiftedAlphabetUpper2=ALPHABET_UPPER.substring(this.key2)+ALPHABET_UPPER.substring(0,this.key2);this.shiftedAlphabetLower2=shiftedAlphabetUpper2.toLowerCase();}
        public String encrypt(String input){Objects.requireNonNull(input);StringBuilder e=new StringBuilder(input);for(int i=0;i<e.length();i++){char c=e.charAt(i);boolean even=(i%2==0);String sU=even?shiftedAlphabetUpper1:shiftedAlphabetUpper2;String sL=even?shiftedAlphabetLower1:shiftedAlphabetLower2;int idxU=ALPHABET_UPPER.indexOf(c);if(idxU!=-1){e.setCharAt(i,sU.charAt(idxU));continue;}int idxL=ALPHABET_LOWER.indexOf(c);if(idxL!=-1){e.setCharAt(i,sL.charAt(idxL));}}return e.toString();}
        public String decrypt(String input){Objects.requireNonNull(input);int dk1=Math.floorMod(ALPHABET_SIZE-this.key1,ALPHABET_SIZE);int dk2=Math.floorMod(ALPHABET_SIZE-this.key2,ALPHABET_SIZE);CaesarCipherTwoKeys d=new CaesarCipherTwoKeys(dk1,dk2);return d.encrypt(input);}
        public int getKey1(){return key1;}public int getKey2(){return key2;}@Override public String toString(){return"CaesarCipherTwoKeys(k1="+key1+", k2="+key2+")";}
    }

    // --- Nested File I/O Service ---
    // (No changes needed here)
     public static class FileIOService {
        public record FileData(String path, String content) {}
        private final String startingDirectoryPath;
        public FileIOService(String startPath){this.startingDirectoryPath=startPath;}
        public Optional<FileData> selectAndReadFile(){
            JFileChooser fc=new JFileChooser();if(startingDirectoryPath!=null&&!startingDirectoryPath.isEmpty()){File d=new File(startingDirectoryPath);if(d.isDirectory()){fc.setCurrentDirectory(d);System.out.println(" HINT: Starting in: "+startingDirectoryPath);}else{System.out.println("⚠️ Warning: Dir not found.");}}
            FileNameExtensionFilter f=new FileNameExtensionFilter("Encrypted Text Files (*.txt)","txt");fc.setFileFilter(f);
            fc.setDialogTitle("📜 Select ENCRYPTED File (.txt) 📜");fc.setApproveButtonText("Load File");System.out.println("❓ Please choose the ENCRYPTED text file...");
            int r=fc.showOpenDialog(null);if(r==JFileChooser.APPROVE_OPTION){File sf=fc.getSelectedFile();String fp=sf.getAbsolutePath();System.out.println("✔️ Selected: "+fp);try{String c=Files.readString(Paths.get(fp),StandardCharsets.UTF_8);System.out.println("📜 File read!");return Optional.of(new FileData(fp,c));}catch(IOException e){System.err.println("❌ Error reading file: "+e.getMessage());return Optional.empty();}catch(Exception e){System.err.println("❌ Unexpected file read error: "+e.getMessage());e.printStackTrace();return Optional.empty();}}else{System.out.println("🚫 No file selected/cancelled.");return Optional.empty();}
        }
    }

    // --- Main Application Method ---
    public static void main(String[] args) {
        System.out.println("🚀 Starting Interactive Caesar Decryptor with Auto-Guess... 🚀");

        // 1. Select Encrypted File
        // Ron, using the specific path you confirmed earlier.
        String specificDocumentsPath = "C:\\Users\\inouy\\OneDrive\\Documents";
        FileIOService fileService = new FileIOService(specificDocumentsPath);
        Optional<FileIOService.FileData> fileDataOptional = fileService.selectAndReadFile();

        if (fileDataOptional.isEmpty()) {
            System.out.println("🚫 Decryption aborted: No file selected.");
            return; // Exit if no file
        }

        FileIOService.FileData fileData = fileDataOptional.get();
        originalEncryptedText = fileData.content(); // Store globally
        sourceFilePath = fileData.path();           // Store globally

        if (originalEncryptedText == null || originalEncryptedText.trim().isEmpty()) {
             System.out.println("🚫 Decryption aborted: Selected file is empty.");
             JOptionPane.showMessageDialog(null, "The selected file appears to be empty.", "File Error", JOptionPane.WARNING_MESSAGE);
            return; // Exit if file empty
        }

        // 2. Perform Frequency Analysis to Guess Initial Keys
        System.out.println("\n⚙️ Analyzing ciphertext for key guesses...");
        FrequencyAnalyzer analyzer = new FrequencyAnalyzer();

        // Single key guess
        Map<Character, Integer> freqsSingle = analyzer.countLetterFrequencies(originalEncryptedText);
        Optional<Character> freqSingleOpt = analyzer.findMostFrequentLetter(freqsSingle);
        OptionalInt keySingleGuessOpt = analyzer.calculateKeyFromFreqChar(freqSingleOpt);

        // Dual key guess
        FrequencyAnalyzer.EvenOddStrings separated = analyzer.separateEvenOddLetters(originalEncryptedText);
        Map<Character, Integer> freqsEven = analyzer.countLetterFrequencies(separated.evenChars());
        Optional<Character> freqEvenOpt = analyzer.findMostFrequentLetter(freqsEven);
        OptionalInt key1GuessOpt = analyzer.calculateKeyFromFreqChar(freqEvenOpt); // Key 1 guess from Even

        Map<Character, Integer> freqsOdd = analyzer.countLetterFrequencies(separated.oddChars());
        Optional<Character> freqOddOpt = analyzer.findMostFrequentLetter(freqsOdd);
        OptionalInt key2GuessOpt = analyzer.calculateKeyFromFreqChar(freqOddOpt); // Key 2 guess from Odd

        // Determine initial keys for the interactive window (default to 0 if guess failed)
        int initialKey1 = key1GuessOpt.orElse(0);
        int initialKey2 = key2GuessOpt.orElse(0);

        System.out.println("📊 Analysis complete.");
        System.out.println("  Most frequent letter (overall): " + freqSingleOpt.map(String::valueOf).orElse("N/A") + " -> Guessed Single Key: " + keySingleGuessOpt.stream().mapToObj(String::valueOf).findFirst().orElse("N/A"));
        System.out.println("  Most frequent EVEN idx letter: " + freqEvenOpt.map(String::valueOf).orElse("N/A") + " -> Guessed Key 1: " + key1GuessOpt.stream().mapToObj(String::valueOf).findFirst().orElse("N/A"));
        System.out.println("  Most frequent ODD idx letter: " + freqOddOpt.map(String::valueOf).orElse("N/A") + " -> Guessed Key 2: " + key2GuessOpt.stream().mapToObj(String::valueOf).findFirst().orElse("N/A"));


        // 3. Setup and Display UI on EDT
        final Optional<Character> finalFreqSingleOpt = freqSingleOpt; // Make effectively final for lambda
        final OptionalInt finalKeySingleGuessOpt = keySingleGuessOpt;
        final Optional<Character> finalFreqEvenOpt = freqEvenOpt;
        final OptionalInt finalKey1GuessOpt = key1GuessOpt;
        final Optional<Character> finalFreqOddOpt = freqOddOpt;
        final OptionalInt finalKey2GuessOpt = key2GuessOpt;

        SwingUtilities.invokeLater(() -> {
            // Pass analysis results to input window
            createAndShowInputWindow(originalEncryptedText, sourceFilePath,
                                     finalFreqSingleOpt, finalKeySingleGuessOpt,
                                     finalFreqEvenOpt, finalKey1GuessOpt,
                                     finalFreqOddOpt, finalKey2GuessOpt);
            // Pass guessed keys as initial values to decryption window
            createAndShowDecryptionWindow(initialKey1, initialKey2);
        });

         System.out.println("\n🎉 Decryptor launched with auto-guessed keys. Check the windows! 🎉");
    }

    // --- UI Creation Methods ---

    // UPDATED: Creates the window showing input ciphertext and analysis results
    private static void createAndShowInputWindow(String encryptedText, String filePath,
                                                  Optional<Character> freqSingleOpt, OptionalInt keySingleGuessOpt,
                                                  Optional<Character> freqEvenOpt, OptionalInt key1GuessOpt,
                                                  Optional<Character> freqOddOpt, OptionalInt key2GuessOpt) {
        JFrame inputFrame = new JFrame("🔒 Encrypted Input & Analysis"); // Updated Title
        inputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inputFrame.setSize(700, 500); // Wider
        inputFrame.setLocation(50, 50);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Info Panel for File and Analysis Results
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2)); // Vertical layout
        infoPanel.setBorder(BorderFactory.createTitledBorder("File & Analysis Results (Assuming 'E')"));
        infoPanel.add(new JLabel("Source File: " + new File(filePath).getName()));
        infoPanel.add(new JSeparator());
        infoPanel.add(new JLabel("Overall Most Freq Char: " + freqSingleOpt.map(String::valueOf).orElse("N/A")
                                + " -> Guessed Single Key: " + keySingleGuessOpt.stream().mapToObj(String::valueOf).findFirst().orElse("N/A")));
        infoPanel.add(new JSeparator());
        infoPanel.add(new JLabel("Even Idx Most Freq Char: " + freqEvenOpt.map(String::valueOf).orElse("N/A")
                                + " -> Guessed Key 1: " + key1GuessOpt.stream().mapToObj(String::valueOf).findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Odd Idx Most Freq Char: " + freqOddOpt.map(String::valueOf).orElse("N/A")
                                + " -> Guessed Key 2: " + key2GuessOpt.stream().mapToObj(String::valueOf).findFirst().orElse("N/A")));

        contentPanel.add(infoPanel, BorderLayout.NORTH);

        // Text Area for Encrypted Text
        JTextArea encryptedTextArea = new JTextArea(encryptedText);
        encryptedTextArea.setEditable(false);
        encryptedTextArea.setLineWrap(true);
        encryptedTextArea.setWrapStyleWord(true);
        encryptedTextArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(encryptedTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Encrypted Text Content"));

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        inputFrame.add(contentPanel);
        inputFrame.setVisible(true);
    }

    // Creates the interactive decryption window (Signature unchanged, logic unchanged)
    private static void createAndShowDecryptionWindow(int initialKey1, int initialKey2) {
        JFrame decryptFrame = new JFrame("🔓 Interactive Decryptor");
        decryptFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        decryptFrame.setSize(700, 550);
        decryptFrame.setLocation(780, 50); // Position next to input window more reliably

        // --- Components ---
        JTextField key1Input = new JTextField(String.valueOf(initialKey1), 5); // Initialized with guess
        JTextField key2Input = new JTextField(String.valueOf(initialKey2), 5); // Initialized with guess
        JButton updateButton = new JButton("Update Decryption");

        JTextArea decryptedSingleArea = new JTextArea();
        decryptedSingleArea.setEditable(false);decryptedSingleArea.setLineWrap(true);decryptedSingleArea.setWrapStyleWord(true);decryptedSingleArea.setFont(new java.awt.Font("Consolas",java.awt.Font.PLAIN,12));
        JScrollPane singleScrollPane=new JScrollPane(decryptedSingleArea);singleScrollPane.setBorder(BorderFactory.createTitledBorder("Decrypted with Single-Key (Using Key 1)"));singleScrollPane.setPreferredSize(new Dimension(300,150));

        JTextArea decryptedDualArea = new JTextArea();
        decryptedDualArea.setEditable(false);decryptedDualArea.setLineWrap(true);decryptedDualArea.setWrapStyleWord(true);decryptedDualArea.setFont(new java.awt.Font("Consolas",java.awt.Font.PLAIN,12));
        JScrollPane dualScrollPane=new JScrollPane(decryptedDualArea);dualScrollPane.setBorder(BorderFactory.createTitledBorder("Decrypted with Dual-Key (Using Key 1 & Key 2)"));dualScrollPane.setPreferredSize(new Dimension(300,150));


        // --- Layout ---
        JPanel controlPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,10,5));controlPanel.add(new JLabel("Key 1:"));controlPanel.add(key1Input);controlPanel.add(new JLabel("Key 2:"));controlPanel.add(key2Input);controlPanel.add(updateButton);controlPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel resultsPanel=new JPanel(new GridLayout(1,2,10,10));resultsPanel.add(singleScrollPane);resultsPanel.add(dualScrollPane);resultsPanel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel mainPanel=new JPanel(new BorderLayout(10,10));mainPanel.add(controlPanel,BorderLayout.NORTH);mainPanel.add(resultsPanel,BorderLayout.CENTER);
        decryptFrame.add(mainPanel);

         // --- Action Listener for Update Button --- (Unchanged logic)
        updateButton.addActionListener(e -> { // Use Lambda for ActionListener
            try {
                int k1 = Integer.parseInt(key1Input.getText().trim());
                int k2 = Integer.parseInt(key2Input.getText().trim());
                System.out.println("Updating decryption with Key1=" + k1 + ", Key2=" + k2);
                CaesarCipher cs = new CaesarCipher(k1);
                String decSingle = cs.decrypt(originalEncryptedText);
                decryptedSingleArea.setText(decSingle);
                decryptedSingleArea.setCaretPosition(0);
                 CaesarCipherTwoKeys cdk = new CaesarCipherTwoKeys(k1, k2);
                String decDual = cdk.decrypt(originalEncryptedText);
                decryptedDualArea.setText(decDual);
                decryptedDualArea.setCaretPosition(0);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(decryptFrame,"Invalid key format. Please enter integers only.","Key Error",JOptionPane.ERROR_MESSAGE); System.err.println("Key parsing error: "+ex.getMessage());
            } catch (Exception ex) {
                 JOptionPane.showMessageDialog(decryptFrame,"An unexpected error occurred during decryption update:\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); System.err.println("Decryption update error: "+ex); ex.printStackTrace();
            }
        });

        // --- Perform Initial Decryption Using Guessed Keys ---
        Runnable initialDecrypt = () -> {
             try {
                int k1 = Integer.parseInt(key1Input.getText().trim()); // Use initial values from text fields
                int k2 = Integer.parseInt(key2Input.getText().trim());
                CaesarCipher cs = new CaesarCipher(k1);
                decryptedSingleArea.setText(cs.decrypt(originalEncryptedText));
                decryptedSingleArea.setCaretPosition(0);
                CaesarCipherTwoKeys cdk = new CaesarCipherTwoKeys(k1, k2);
                decryptedDualArea.setText(cdk.decrypt(originalEncryptedText));
                decryptedDualArea.setCaretPosition(0);
                System.out.println("Initial decryption displayed using guessed keys: Key1=" + k1 + ", Key2=" + k2);
             } catch (Exception ex) {
                 System.err.println("Error during initial decryption display: " + ex.getMessage());
                 decryptedSingleArea.setText("Error during initial decryption.");
                 decryptedDualArea.setText("Error during initial decryption.");
             }
        };
        initialDecrypt.run();

        decryptFrame.setVisible(true);
    }


    // --- Helper method to prompt for TWO keys --- (REMOVED - No longer needed for initial input)
    /*
    private static int[] promptForTwoKeys() throws NumberFormatException, IllegalStateException {
        // ... (code removed as it's not called) ...
    }
    */
}
// ============================================================
// END OF COMPLETE InteractiveCaesarDecryptor.java FILE CONTENT (with Auto-Guess)
// ============================================================