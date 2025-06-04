import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * DreamAnalyzer reads and analyzes user-provided dream journal entries, and outputs an analysis.
 * <p>
 * It detects positive and negative emotional terms, as well as whether or not a dream is lucid. It then assigns scores, and ranks the dreams by those scores. 
 * <p>
 */

public class DreamAnalyzer
{
    //instance variables for the DreamAnalyzer class, including reference lists for terms that indicate good, bad, and lucid dreams
    private ArrayList<DreamEntry> dreamEntries;
    private String[] posTerms = {"happy", "free", "peaceful", "love", "bright", "joy", "safe", "flying", "floating", "light",
    "calm", "laugh", "smile", "hug", "sunlight", "explore", "beautiful", "dance", "play", "fun",
    "gentle", "rainbow", "glow", "comfort", "positive", "success", "victory", "celebrate", "relief", "breeze"};
    private String[] negTerms = { "sad", "fear", "dark", "trapped", "falling", "lost", "alone", "angry", "cry", "storm", 
    "chase", "hide", "scream", "hurt", "pain", "fail", "cold", "drown", "anxious", "nightmare",
    "monster", "bleed", "broken", "die", "death", "scared", "panic", "freeze", "shadow"};
    private String[] lucidTerms = {"i knew i was dreaming", "i realized i was dreaming", "i could control the dream", "became aware i was dreaming", "conscious while dreaming"};
    
    /**
     * Constructor to initialize the list of dream entries.
     */
    
    public DreamAnalyzer() {
        dreamEntries = new ArrayList<>();
    }
    
    /**
     * Reads a text file of a dream journal and creates and populates dream entries with their dates and content.
     * Detects whether dreams are lucid based on reference phrases
     */
    
    public void readDreams(String path) throws IOException {
        File file = new File(path);
        Scanner scan = new Scanner(file);
        
        String date = null;
        StringBuilder dreamText = new StringBuilder();
        //loop to read file line by line, checks for dates and whether or not dream is lucid
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.matches("\\d{4}-\\d{2}-\\d{2}")) {
                if (date != null) {
                    String dreamContent = dreamText.toString();
                    boolean isLucid = false;
                    String lowerContent = dreamContent.toLowerCase();
                    for (String clue : lucidTerms) {
                        if (lowerContent.contains(clue)) {
                            isLucid = true;
                            break;
                        }
                    }
                    DreamEntry entry = isLucid ? new LucidDreamEntry(date, dreamContent) : new DreamEntry(date, dreamContent);
                    entry.analyze(posTerms, negTerms);
                    dreamEntries.add(entry);
                }
                date = line;
                dreamText.setLength(0);
            }
            else {
                dreamText.append(line).append(" ");
            }
        }
        //reads and analyzes final dream
        if (date != null) {
            String dreamContent = dreamText.toString();
            boolean isLucid = false;
                    String lowerContent = dreamContent.toLowerCase();
                    for (String clue : lucidTerms) {
                        if (lowerContent.contains(clue)) {
                            isLucid = true;
                            break;
                        }
                    }
                    DreamEntry entry = isLucid ? new LucidDreamEntry(date, dreamContent) : new DreamEntry(date, dreamContent);
            entry.analyze(posTerms, negTerms);
            dreamEntries.add(entry);
        }
        scan.close();
    }
    
    /**
     * Method to start the sorting.
     */
    public void sortDreamsByScore() {
        selectionSort(0);
    }
    
    /** 
     * Sorts dreamEntries by score using a recursive selection sort algorithm.
     */
    private void selectionSort(int start){
        if (start >= dreamEntries.size() - 1) {
            return;
        }
        
        int maxIndex = findMaxIndex(start, start);
        DreamEntry temp = dreamEntries.get(start);
        dreamEntries.set(start, dreamEntries.get(maxIndex));
        dreamEntries.set(maxIndex, temp);

        selectionSort(start + 1);
    }
    
    /**
     * Uses recursion to find the index of the dreamEntries entry with the highest score.
     */
    
    private int findMaxIndex(int current, int maxIndex) {
        if (current >= dreamEntries.size()) {
            return maxIndex;
        }
        
        if (dreamEntries.get(current).getScore() > dreamEntries.get(maxIndex).getScore()) {
            maxIndex = current;
        }
        
        return findMaxIndex(current + 1, maxIndex);
    }
    
    /**
     * Writes the results of the dream analysis to an output file.
     */
    
    public void writeAnalysis(String outputPath) throws IOException {
        FileWriter writer = new FileWriter(outputPath);
        int totalScore = 0;
        
        for (DreamEntry entry : dreamEntries) {
            writer.write(entry.toString() + "\n");
            totalScore += entry.getScore();
        }
        
        writer.write("\nTotal Dreams Analyzed: " + dreamEntries.size() + "\n");
        writer.write("Average Dream Score: " + (dreamEntries.size() > 0 ? (double) totalScore / dreamEntries.size() : 0) + "\n");
        writer.write("Most Common Emotion: " + mostCommonEmotion() + "\n");
        writer.close();
    }
    
    /**
     * Uses recursion to count the number of dreams with a negative score.
     */
    
    public int numBadDreams(int index) {
        if (index >= dreamEntries.size()) {
            return 0;
        }
        return (dreamEntries.get(index).getScore() < 0 ? 1 : 0) + numBadDreams(index + 1);
    }
    
    /**
     * Determines the most common emotion (tag) of all provided dreams.
     */
    private String mostCommonEmotion() {
        //HashMap usage provided by ChatGPT
        Map<String, Integer> emotionCount = new HashMap<>();
        for (DreamEntry entry : dreamEntries) {
            for (String tag : entry.getTags()) {
                emotionCount.put(tag, emotionCount.getOrDefault(tag, 0) + 1);
            }
        }
        //stream and Map.Entry usage provided by ChatGPT
        return emotionCount.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("None");
    }
    
    /**
     * Main method: runs the major aspects of the program
     */
    public static void main(String[] args) throws IOException {
        DreamAnalyzer analyzer = new DreamAnalyzer();
        analyzer.readDreams("/Users/angelakaempfen/Desktop/dreams.txt");
        analyzer.sortDreamsByScore();
        analyzer.writeAnalysis("/Users/angelakaempfen/Desktop/analysis.txt");
    }
}

/** 
 * Class that represents a dream entry, including the date, text, score and emotion tags.
 */

class DreamEntry {
    private String date;
    private String text;
    protected int score;
    private ArrayList<String> tags;
    
    public DreamEntry(String date, String text) {
        this.date = date;
        this.text = text;
        this.score = 0;
        this.tags = new ArrayList<>();
    }
    
    /**
     * Analyzes dream text to assign a positive or negative score and emotion tags.
     */
    public void analyze(String[] posTerms, String[] negTerms) {
        String lower = text.toLowerCase(); 
        for (String word : posTerms) {
            if (lower.contains(word)) {
                score ++;
                tags.add(word);
            }
        }
        for (String word : negTerms) {
            if (lower.contains(word)) {
                score --;
                tags.add(word);
            }
        }
    }
    
    public int getScore() {
        return score;
    }
    
    public ArrayList<String> getTags() {
        return tags;
    }
    
    public String toString() {
        return "[" + date + "] Score: " + score + " | Tags: " + String.join(", ", tags);
    }
}

/**
 * A subclass of the DreamEntry class that represents a lucid dream, and adds a score boost of 2.
 */
class LucidDreamEntry extends DreamEntry {
    
    public LucidDreamEntry(String date, String text) {
        super(date, text);
        getTags().add("lucid");
    }
    
    /**
     * Overrides the analyze method to add the bonus score for lucid dreams.
     */
    @Override
    public void analyze(String[] posTerms, String[] negTerms) {
        super.analyze(posTerms,negTerms);
        super.score += 3;
    }
}