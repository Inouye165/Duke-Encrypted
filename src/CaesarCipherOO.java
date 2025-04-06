// CaesarCipherOO.java

// Package-private interface (no public modifier)
interface Cipher {
    String encrypt(String input);
    String decrypt(String input);
}

public class CaesarCipherOO implements Cipher {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final int key; // stored as state

    // Constructor: name matches the class name
    public CaesarCipherOO(int key) {
        // Normalize key to ensure it's within 0-25
        this.key = key % ALPHABET.length();
    }

    @Override
    public String encrypt(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                int idx = ALPHABET.indexOf(c);
                if (idx != -1) {
                    int shiftedIndex = (idx + key) % ALPHABET.length();
                    encrypted.append(ALPHABET.charAt(shiftedIndex));
                } else {
                    encrypted.append(c);
                }
            } else {
                encrypted.append(c);
            }
        }
        return encrypted.toString();
    }

    @Override
    public String decrypt(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                int idx = ALPHABET.indexOf(c);
                if (idx != -1) {
                    int shiftedIndex = (idx - key + ALPHABET.length()) % ALPHABET.length();
                    decrypted.append(ALPHABET.charAt(shiftedIndex));
                } else {
                    decrypted.append(c);
                }
            } else {
                decrypted.append(c);
            }
        }
        return decrypted.toString();
    }

    @Override
    public String toString() {
        return "CaesarCipherOO with key: " + key;
    }

    // Example usage:
    public static void main(String[] args) {
        CaesarCipherOO cipher = new CaesarCipherOO(3);
        String original = "HELLO WORLD";
        String encrypted = cipher.encrypt(original);
        String decrypted = cipher.decrypt(encrypted);
        System.out.println("Original:  " + original);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
    }
}
