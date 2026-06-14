
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class CodePlagiarismChecker extends JFrame {

    private JTextArea code1Area, code2Area, resultArea;
    private File file1, file2;

    private ArrayList<String> tokens1 = new ArrayList<>();
    private ArrayList<String> tokens2 = new ArrayList<>();

    private HashSet<String> set1 = new HashSet<>();
    private HashSet<String> set2 = new HashSet<>();

    private LinkedList<String> history = new LinkedList<>();
    private Queue<String> recentComparisons = new ArrayDeque<>();

    private TreeSet<String> sortedKeywords = new TreeSet<>();
    private HashMap<String, Integer> frequencyMap = new HashMap<>();

    public CodePlagiarismChecker() {
        setTitle("Code Plagiarism Checker - DSA Project");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panels (Code Inputs)
        JPanel topPanel = new JPanel(new GridLayout(1, 2));

        code1Area = new JTextArea();
        code2Area = new JTextArea();

        topPanel.add(new JScrollPane(code1Area));
        topPanel.add(new JScrollPane(code2Area));

        add(topPanel, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();

        JButton load1 = new JButton("Load File 1");
        JButton load2 = new JButton("Load File 2");
        JButton check = new JButton("Check Similarity");
        JButton save = new JButton("Save Report");
        JButton clear = new JButton("Clear");

        buttonPanel.add(load1);
        buttonPanel.add(load2);
        buttonPanel.add(check);
        buttonPanel.add(save);
        buttonPanel.add(clear);

        add(buttonPanel, BorderLayout.CENTER);

        // Result Area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Actions
        load1.addActionListener(e -> loadFile(1));
        load2.addActionListener(e -> loadFile(2));
        clear.addActionListener(e -> clearAll());
        check.addActionListener(e -> analyze());
        save.addActionListener(e -> saveReport());
    }

    private void loadFile(int which) {
        JFileChooser chooser = new JFileChooser();
        int r = chooser.showOpenDialog(this);

        if (r == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(file.toPath()));

                if (which == 1) {
                    file1 = file;
                    code1Area.setText(content);
                } else {
                    file2 = file;
                    code2Area.setText(content);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "File Error!");
            }
        }
    }

    private void clearAll() {
        code1Area.setText("");
        code2Area.setText("");
        resultArea.setText("");

        tokens1.clear();
        tokens2.clear();
        set1.clear();
        set2.clear();
        frequencyMap.clear();
        sortedKeywords.clear();
    }

    private void analyze() {
        tokens1 = tokenize(code1Area.getText());
        tokens2 = tokenize(code2Area.getText());

        set1 = new HashSet<>(tokens1);
        set2 = new HashSet<>(tokens2);

        HashSet<String> common = new HashSet<>(set1);
        common.retainAll(set2);

        HashSet<String> union = new HashSet<>(set1);
        union.addAll(set2);

        double similarity = union.size() == 0 ? 0 :
                ((double) common.size() / union.size()) * 100;

        buildFrequency(tokens1);
        buildFrequency(tokens2);

        sortedKeywords.addAll(set1);
        sortedKeywords.addAll(set2);

        boolean balanced1 = checkBrackets(code1Area.getText());
        boolean balanced2 = checkBrackets(code2Area.getText());

        StringBuilder sb = new StringBuilder();

        sb.append("=== PLAGIARISM REPORT ===\n");
        sb.append("Similarity: ").append(String.format("%.2f", similarity)).append("%\n\n");

        sb.append("Common Tokens: ").append(common).append("\n\n");
        sb.append("Unique Tokens (File1): ").append(set1).append("\n");
        sb.append("Unique Tokens (File2): ").append(set2).append("\n\n");

        sb.append("Sorted Keywords: ").append(sortedKeywords).append("\n\n");

        sb.append("Bracket Check File1: ").append(balanced1 ? "Balanced" : "Not Balanced").append("\n");
        sb.append("Bracket Check File2: ").append(balanced2 ? "Balanced" : "Not Balanced").append("\n\n");

        sb.append("Token Frequency:\n");
        for (String key : frequencyMap.keySet()) {
            sb.append(key).append(" -> ").append(frequencyMap.get(key)).append("\n");
        }

        history.add("Compared files | Similarity: " + similarity);
        recentComparisons.add("Comparison done at " + new Date());

        if (recentComparisons.size() > 5) recentComparisons.poll();

        sb.append("\nRecent Comparison History:\n");
        for (String h : recentComparisons) {
            sb.append(h).append("\n");
        }

        resultArea.setText(sb.toString());
    }

    private ArrayList<String> tokenize(String text) {
        ArrayList<String> list = new ArrayList<>();
        String[] parts = text.split("[^a-zA-Z0-9_]+");

        for (String p : parts) {
            if (p.length() > 0) {
                list.add(p);
            }
        }
        return list;
    }

   
    private void buildFrequency(List<String> tokens) {
        for (String t : tokens) {
            frequencyMap.put(t, frequencyMap.getOrDefault(t, 0) + 1);
        }
    }

    private boolean checkBrackets(String text) {
        Stack<Character> stack = new Stack<>();

        for (char c : text.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else if (c == ')' || c == '}' || c == ']') {
                if (stack.isEmpty()) return false;

                char top = stack.pop();
                if (!isMatch(top, c)) return false;
            }
        }
        return stack.isEmpty();
    }

    private boolean isMatch(char open, char close) {
        return (open == '(' && close == ')') ||
               (open == '{' && close == '}') ||
               (open == '[' && close == ']');
    }

    private void saveReport() {
        try {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                Files.write(file.toPath(), resultArea.getText().getBytes());
                JOptionPane.showMessageDialog(this, "Report Saved!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save Error!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CodePlagiarismChecker().setVisible(true));
    }
}
