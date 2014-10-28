package fr.lipn.sts.basic;

public class Levenshtein {
    private static int minimum(int a, int b, int c) {
            return Math.min(Math.min(a, b), c);
    }

    private static int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
            int[][] distance = new int[str1.length() + 1][str2.length() + 1];

            for (int i = 0; i <= str1.length(); i++)
                    distance[i][0] = i;
            for (int j = 1; j <= str2.length(); j++)
                    distance[0][j] = j;

            for (int i = 1; i <= str1.length(); i++)
                    for (int j = 1; j <= str2.length(); j++)
                            distance[i][j] = minimum(
                                            distance[i - 1][j] + 1,
                                            distance[i][j - 1] + 1,
                                            distance[i - 1][j - 1]
                                                            + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                                                            : 1));

            return distance[str1.length()][str2.length()];
    }
    
    public static float characterBasedSimilarity(CharSequence str1, CharSequence str2){
    	int len = Math.max(str1.length(), str2.length());
    	int d = computeLevenshteinDistance(str1, str2);
    	return (1.0f-(float)d/(float)len);
    }
    
    /**
     * This method uses a simple regular expression ("[\\w]+") to transform a string into an array of strings
     * @param seq1
     * @param seq2
     * @return
     */
    public static float wordBasedSimilarity(String seq1, String seq2){
    	String [] s1 = seq1.split("[\\w]+");
    	String [] s2 = seq2.split("[\\w]+");
    	
    	int len = Math.max(s1.length, s2.length);
    	int d = computeWordLevelDistance(s1, s2);
    	return (1.0f-(float)d/(float)len);
    }
    
    public static float wordBasedSimilarity(String [] s1, String [] s2){
    	int len = Math.max(s1.length, s2.length);
    	int d = computeWordLevelDistance(s1, s2);
    	return (1.0f-(float)d/(float)len);
    }
    
    private static int computeWordLevelDistance(String [] s1, String [] s2) {
    	int[][] distance = new int[s1.length + 1][s2.length + 1];

        for (int i = 0; i <= s1.length; i++)
                distance[i][0] = i;
        for (int j = 1; j <= s2.length; j++)
                distance[0][j] = j;

        for (int i = 1; i <= s1.length; i++)
                for (int j = 1; j <= s2.length; j++)
                        distance[i][j] = minimum(
                                        distance[i - 1][j] + 1,
                                        distance[i][j - 1] + 1,
                                        distance[i - 1][j - 1]
                                                        + ((s1[i - 1] == s2[j - 1]) ? 0
                                                                        : 1));

        return distance[s1.length][s2.length];
    }
}