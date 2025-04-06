import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class WordLengthAnalyzerApp {

    // --- Nested Class for File I/O Logic ---
    // (Reusing the structure from previous example)
    public static class FileIOService {
        public record FileData(String path, String content) {}
        private final String startingDirectoryPath;

        public FileIOService(String startingDirectoryPath) {
           this.startingDirectoryPath = startingDirectoryPath;
        }

        public Optional<FileData> selectAndReadFile() {
            JFileChooser fileChooser = new JFileChooser();
            // Set starting directory logic (same as before)
            if (startingDirectoryPath != null && !startingDirectoryPath.isEmpty()) {
                File documentsDir = new File(startingDirectoryPath);
                if (documentsDir.isDirectory()) {
                    fileChooser.setCurrentDirectory(documentsDir);
                     System.out.println(" HINT: File chooser starting in: " + startingDirectoryPath);
                } else {
                    System.out.println("⚠️ Warning: Specified directory not found: " + startingDirectoryPath);
                    System.out.println("       File chooser will open in the default location.");
                }
            } else { System.out.println(" HINT: No specific starting directory provided, using default."); }

            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("📜 Select Text File for Word Analysis 📜");
            fileChooser.setApproveButtonText("Analyze This File");

            System.out.println("❓ Please choose the text file you wish to analyze...");
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String chosenFilePath = selectedFile.getAbsolutePath();
                System.out.println("✔️ You selected file: " + chosenFilePath);
                try {
                    // Specify UTF-8 encoding for broader compatibility
                    String content = Files.readString(Paths.get(chosenFilePath), java.nio.charset.StandardCharsets.UTF_8);
                    System.out.println("📜 File successfully read!");
                    return Optional.of(new FileData(chosenFilePath, content));
                } catch (IOException e) {
                    System.err.println("❌ Error reading the selected file: " + e.getMessage()); return Optional.empty();
                } catch (Exception e) {
                     System.err.println("❌ An unexpected error occurred during file reading: " + e.getMessage()); e.printStackTrace(); return Optional.empty();
                }
            } else {
                System.out.println("🚫 No file selected, or operation cancelled."); return Optional.empty();
            }
        }
    } // --- End of nested FileIOService class ---


    // --- Nested Class for Word Processing Logic ---
    public static class WordProcessor {

        // Calculates the "effective" length, ignoring leading/trailing punctuation.
        public int calculateEffectiveLength(String wordToken) {
            if (wordToken == null || wordToken.isEmpty()) {
                return 0;
            }
            int start = 0;
            int end = wordToken.length() - 1;

            // Find first letter or digit
            while (start <= end && !Character.isLetterOrDigit(wordToken.charAt(start))) {
                start++;
            }

            // Find last letter or digit
            while (end >= start && !Character.isLetterOrDigit(wordToken.charAt(end))) {
                end--;
            }

            // If no letters/digits found, or only punctuation
            if (start > end) {
                return 0;
            }

            return (end - start) + 1;
        }

        public record AnalysisResult(
            Set<Integer> mostCommonLengths,
            int totalCommonCount,
            Map<Integer, Integer> lengthFrequencies,
            String originalText // Pass original text through
        ) {}

        // Analyzes the text to find most common word lengths.
        public AnalysisResult analyzeText(String text) {
            Map<Integer, Integer> lengthFrequencies = new HashMap<>();
            int totalWordsProcessed = 0; // For calculating totalCommonCount later

            // Split by whitespace to process word tokens
            String[] tokens = text.split("\\s+");

            for (String token : tokens) {
                if (token.isEmpty()) continue; // Skip empty strings from multiple spaces

                int effectiveLength = calculateEffectiveLength(token);
                if (effectiveLength > 0) { // Only count words with actual content
                   lengthFrequencies.put(effectiveLength, lengthFrequencies.getOrDefault(effectiveLength, 0) + 1);
                   totalWordsProcessed++; // Count words contributing to frequencies
                }
            }

            Set<Integer> mostCommonLengths = new HashSet<>();
            int totalCommonCount = 0;
            int maxFreq = 0;

            if (!lengthFrequencies.isEmpty()) {
                // Find the maximum frequency among lengths > 0
                 maxFreq = Collections.max(lengthFrequencies.values());

                // Collect all lengths that have this maximum frequency
                for (Map.Entry<Integer, Integer> entry : lengthFrequencies.entrySet()) {
                    if (entry.getValue() == maxFreq) {
                        mostCommonLengths.add(entry.getKey());
                        totalCommonCount += entry.getValue(); // Sum counts for all tied lengths
                    }
                }
            }

             // Provide feedback if no processable words are found
             if (totalWordsProcessed == 0) {
                 System.out.println("INFO: No processable words (containing letters/digits) found in the text.");
             } else if (mostCommonLengths.isEmpty()){
                  // This case shouldn't really happen if totalWordsProcessed > 0 and lengths > 0 are added
                  System.out.println("INFO: Processed words, but could not determine most common length.");
             }


            return new AnalysisResult(mostCommonLengths, totalCommonCount, lengthFrequencies, text);
        }
    } // --- End of nested WordProcessor class ---


     // --- Nested Class for Results Display Logic ---
    public static class HighlightingViewer {

        // Displays the results in a JFrame. HTML generation happens elsewhere.
        public void displayResults(String htmlContent, String filePath) {
            System.out.println("\n✨ Creating the results window...");

            SwingUtilities.invokeLater(() -> {
                JEditorPane editorPane = new JEditorPane();
                editorPane.setEditable(false);
                editorPane.setContentType("text/html; charset=UTF-8"); // Specify UTF-8 for HTML
                editorPane.setText(htmlContent);
                // Scroll to top after rendering
                SwingUtilities.invokeLater(() -> editorPane.setCaretPosition(0));

                JScrollPane scrollPane = new JScrollPane(editorPane);
                scrollPane.setPreferredSize(new Dimension(800, 600));

                JFrame frame = new JFrame("📊 Word Length Analysis Results: " + new File(filePath).getName());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(scrollPane);
                frame.pack();
                frame.setLocationRelativeTo(null); // Center on screen
                frame.setVisible(true);

                System.out.println("✅ Results window should be visible.");
            });
        }

        // Helper method to escape HTML characters - reused
        public static String escapeHtml(String text) {
             if (text == null) return "";
             // Basic escaping, sufficient for this use case
             return text.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&#39;");
        }

        // Generates the HTML with highlighting
        public String generateHighlightedHtml(String originalText, Set<Integer> commonLengths, int totalCommonCount, WordProcessor processor) {
             StringBuilder htmlBuilder = new StringBuilder();
             // Add charset meta tag for better character handling
             htmlBuilder.append("<html><head><meta charset='UTF-8'></head><body style='font-family: sans-serif; padding: 15px;'>");
             htmlBuilder.append("<h1>📊 Word Length Analysis Results</h1>");
             htmlBuilder.append("<hr>");

             // Use <pre> tag to preserve original whitespace and line breaks
             htmlBuilder.append("<h2>Highlighted Text (Words with length(s): ")
                        .append(commonLengths.isEmpty() ? "N/A" : commonLengths.stream().map(String::valueOf).collect(Collectors.joining(", ")))
                        .append(" are highlighted):</h2>");
             htmlBuilder.append("<div style='border: 1px solid #ccc; padding: 10px; background-color: #f9f9f9;'>");
             // Setting font explicitly in pre style helps consistency
             htmlBuilder.append("<pre style='white-space: pre-wrap; word-wrap: break-word; font-family: Consolas, \"Courier New\", monospace; font-size: 11pt;'>");

             // Process the text preserving whitespace for display
             // Split by lookahead/lookbehind to keep delimiters (whitespace)
             String[] parts = originalText.split("(?<=\\s)|(?=\\s)"); // Keep spaces as separate parts

             for (String part : parts) {
                 if (part == null || part.isEmpty()) continue;

                 // Check if the part is just whitespace
                 if (part.trim().isEmpty()) {
                     // It's whitespace, append escaped version (though spaces usually render fine)
                     htmlBuilder.append(escapeHtml(part));
                 } else {
                     // It's a potential word token
                     int effectiveLength = processor.calculateEffectiveLength(part);
                     boolean highlight = commonLengths.contains(effectiveLength);

                     if (highlight) {
                        htmlBuilder.append("<span style='background-color: yellow;'>");
                     }
                     // IMPORTANT: Escape the token *before* adding to HTML
                     htmlBuilder.append(escapeHtml(part));
                     if (highlight) {
                        htmlBuilder.append("</span>");
                     }
                 }
             }

             htmlBuilder.append("</pre>");
             htmlBuilder.append("</div>");
             htmlBuilder.append("<hr>");

             // Add Summary Information
             htmlBuilder.append("<h2>Summary:</h2>");
             if (commonLengths.isEmpty()) {
                 htmlBuilder.append("<p>No common word length found (or no processable words with length > 0).</p>");
             } else {
                 String lengthsStr = commonLengths.stream()
                                                  .sorted() // Display lengths in order
                                                  .map(String::valueOf)
                                                  .collect(Collectors.joining(", "));
                 htmlBuilder.append("<p>Most common word length(s): <b>").append(lengthsStr).append("</b></p>");
                 htmlBuilder.append("<p>Total count of words with this length(s): <b>").append(totalCommonCount).append("</b></p>");
             }

             htmlBuilder.append("</body></html>");
             return htmlBuilder.toString();
         }

    } // --- End of nested HighlightingViewer class ---


    // --- Main Application Method ---
    public static void main(String[] args) {
        System.out.println("🚀 Starting Word Length Analyzer 🚀");

        // File Selection
        // Ron, using the specific path you confirmed earlier.
        String specificDocumentsPath = "C:\\Users\\inouy\\OneDrive\\Documents";
        FileIOService fileService = new FileIOService(specificDocumentsPath);
        Optional<FileIOService.FileData> fileDataOptional = fileService.selectAndReadFile();

        if (fileDataOptional.isEmpty()) {
            System.out.println("🚫 Analysis aborted due to file selection issue.");
            return;
        }

        FileIOService.FileData fileData = fileDataOptional.get();
        String fileContent = fileData.content();
        String filePath = fileData.path();

        // Analysis
        System.out.println("\n⚙️ Analyzing text...");
        WordProcessor processor = new WordProcessor();
        WordProcessor.AnalysisResult analysisResult = processor.analyzeText(fileContent);
        System.out.println("📊 Analysis complete.");
        if (!analysisResult.mostCommonLengths().isEmpty()) {
            String lengthsStr = analysisResult.mostCommonLengths().stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
            System.out.println("   Most common length(s): " + lengthsStr);
            System.out.println("   Total count of common words: " + analysisResult.totalCommonCount());
        } // Console output already handled inside analyzeText if no words found


        // Prepare and Display Results
        HighlightingViewer viewer = new HighlightingViewer();
        // Pass the original text content for HTML generation
        String htmlOutput = viewer.generateHighlightedHtml(
            fileContent,
            analysisResult.mostCommonLengths(),
            analysisResult.totalCommonCount(),
            processor // Pass processor for calculating length during HTML generation
        );

        viewer.displayResults(htmlOutput, filePath);

        System.out.println("\n🎉 Word Length Analyzer finished. Check the results window! 🎉");
    }
}