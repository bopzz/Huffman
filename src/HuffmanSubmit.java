import java.io.*;
import java.util.*;

public class HuffmanSubmit implements Huffman {
    public static final int MAX = 256; //Max of ASCII values

    public static class HuffmanNode implements Comparable<HuffmanNode> { //Class to create nodes for Huffman tree
        public int freq;
        public char data;
        public HuffmanNode left, right;

        public HuffmanNode(int freq, char data, HuffmanNode left, HuffmanNode right) { //Constructor for HuffmanNode
            this.freq = freq;
            this.data = data;
            this.left = left;
            this.right = right;
        }

        public boolean checkLeaf() { //Method to check if the node is a leaf
            return left == null && right == null;
        }

        @Override
        public int compareTo(HuffmanNode n) { //Method to compare frequencies
            return this.freq - n.freq;
        }
    }

    public static HuffmanNode buildTree(int[] freq) { //Method to build Huffman tree using PriorityQueue
        PriorityQueue<HuffmanNode> pQueue = new PriorityQueue<>();
        for (char data = 0; data < MAX; data++) {
            if (freq[data] > 0) {
                pQueue.add(new HuffmanNode(freq[data], data, null, null));
            }
        }
        if (pQueue.size() == 1) {
            pQueue.add(new HuffmanNode(1, '\0', null, null));
        }
        while (pQueue.size() > 1) {
            HuffmanNode left = pQueue.poll();
            HuffmanNode right = pQueue.poll();
            pQueue.add(new HuffmanNode(left.freq + right.freq, '\0', left, right));
        }
        return pQueue.poll();
    }

    public static void assign(String[] s, HuffmanNode n, String def) { //Method to assign values to left nodes and right nodes
        if (!n.checkLeaf()) {
            assign(s, n.left, def + "0");
            assign(s, n.right, def + "1");
        } else
            s[n.data] = def;
    }

    public void encode(String inputFile, String outputFile, String freqFile) { //Method to encode the input file and create a frequency file
        BinaryIn in = new BinaryIn(inputFile);
        BinaryOut out = new BinaryOut(outputFile);

        String input = in.readString();
        char[] store = input.toCharArray();
        int[] freq = new int[MAX];
        String[] s = new String[MAX];
        for (int i = 0; i < store.length; i++) {
            freq[store[i]]++;
        }

        HuffmanNode root = buildTree(freq);
        assign(s, root, "");

        try {
            FileWriter writer = new FileWriter(freqFile);
            for (int i = 0; i < MAX; i++) {
                if (freq[i] != 0) {
                    String stringRep = Integer.toBinaryString(i);
                    while (stringRep.length() < 8) {
                        stringRep = "0" + stringRep;
                    }
                    writer.write(stringRep + ":" + freq[i] + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < store.length; i++) {
            String bin = s[store[i]];
            for (int j = 0; j < bin.length(); j++) {
                if (bin.charAt(j) == '0')
                    out.write(false);
                else
                    out.write(true);
            }
        }
        out.flush();
        out.close();
    }

    public void decode(String inputFile, String outputFile, String freqFile) { //Method to decode the encoded file using the freuency file
        int[] freq = new int[MAX];
        String[] s = new String[MAX];
        int length = 0;
        try {
            File file = new File(freqFile);
            Scanner scnr = new Scanner(file);
            while (scnr.hasNextLine()) {
                String line = scnr.nextLine();
                if (line.equals("")) {
                    break;
                } else {
                    String[] lineSplit = line.split(":");
                    int data = Integer.parseInt(lineSplit[0], 2);
                    int frequency = Integer.parseInt(lineSplit[1]);
                    length += frequency;
                    freq[data] = frequency;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Invalid frequency file");
            e.printStackTrace();
        }

        HuffmanNode root = buildTree(freq);
        assign(s, root, "");

        BinaryIn in = new BinaryIn(inputFile);
        BinaryOut out = new BinaryOut(outputFile);

        for (int i = 0; i < length; i++) {
            HuffmanNode n = root;
            while (!n.checkLeaf()) {
                if (in.readBoolean())
                    n = n.right;
                else
                    n = n.left;
            }
            out.write(n.data, 8);
        }
        out.flush();
        out.close();
    }

    public static void main(String[] args) { //Main method to invoke the encode and decode methods
        Huffman huffman = new HuffmanSubmit();
        huffman.encode("ur.jpg", "ur.enc", "freq.txt");
        huffman.decode("ur.enc", "ur_dec.jpg", "freq.txt");
        huffman.encode("alice30.txt", "alice30.enc", "freq2.txt");
        huffman.decode("alice30.enc", "alice30_dec.txt", "freq2.txt");
        // After decoding, both ur.jpg and ur_dec.jpg should be the same.
        // On linux and mac, you can use `diff' command to check if they are the same.
    }
}