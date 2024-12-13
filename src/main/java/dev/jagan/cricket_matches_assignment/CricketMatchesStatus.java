package dev.jagan.cricket_matches_assignment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CricketMatchesStatus {

    // API endpoint and credentials
    private static final String API_URL = "https://api.cuvora.com/car/partner/cricket-data";
    private static final String API_KEY = "test-creds@2320";

    public static JSONArray fetchCricketMatchData() throws Exception {
        // Create URL connection
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request method and headers
        connection.setRequestMethod("GET");
        connection.setRequestProperty("apiKey", API_KEY);

        // Check response code
        int responseCode = connection.getResponseCode();
        System.out.println("API Response Code: " + responseCode);

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray matchData = jsonResponse.getJSONArray("data");

        // Print total matches retrieved
        System.out.println("Total Matches Retrieved: " + matchData.length());

        return matchData;

    }

    public static Object[] findHighestScore(JSONArray matchData) {
        int highestScore = 0;
        String highestScoreTeam = "";

        // Iterate through matches
        for (int i = 0; i < matchData.length(); i++) {
            JSONObject match = matchData.getJSONObject(i);

            // Only consider matches with results
            if ("Result".equalsIgnoreCase(match.optString("ms", ""))) {
                // Get scores for both teams
                int team1Score = parseScore(match.optString("t1s", "0"));
                int team2Score = parseScore(match.optString("t2s", "0"));

                // Find highest score of team 1
                if (team1Score > highestScore) {
                    highestScore = team1Score;
                    highestScoreTeam = match.optString("t1", "");
                }

                // Find highest score of team 2
                if (team2Score > highestScore) {
                    highestScore = team2Score;
                    highestScoreTeam = match.optString("t2", "");
                }
            }
        }

        return new Object[]{highestScore, highestScoreTeam};
    }

    private static int parseScore(String score) {
        try {
            // Remove everything after "(" (overs information)
            if (score.contains("(")) {
                score = score.split("\\(")[0];
            }
            // Extract only the part before "/" (main score)
            if (score.contains("/")) {
                score = score.split("/")[0];
            }
            // Parse the numeric score
            return Integer.parseInt(score.trim());
        } catch (Exception e) {
            System.err.println("Error parsing score: " + score);
            return 0;
        }
    }

    public static int countHighScoringMatches(JSONArray matchData) {
        int highScoringMatchCount = 0;

        // Iterate through matches
        for (int i = 0; i < matchData.length(); i++) {
            JSONObject match = matchData.getJSONObject(i);

            // Only consider matches with results
            if ("Result".equalsIgnoreCase(match.optString("ms", ""))) {
                // Get scores for both teams
                int team1Score = parseScore(match.optString("t1s", "0"));
                int team2Score = parseScore(match.optString("t2s", "0"));

                // Check if total score is 300 or more
                if (team1Score + team2Score >= 300) {
                    highScoringMatchCount++;
                }
            }
        }

        return highScoringMatchCount;
    }

    public static void main(String[] args) {
        try {
            // Fetch match data
            JSONArray matchData = fetchCricketMatchData();

            // Find the highest score
            Object[] highestScoreResult = findHighestScore(matchData);
            int highestScore = (int) highestScoreResult[0];
            String highestScoreTeam = (String) highestScoreResult[1];

            // count high scoring matches
            int highScoringMatchCount = countHighScoringMatches(matchData);

            // Print results
            System.out.println("\nMatches Status: ");
            System.out.println("Highest Score: " + highestScore );
            System.out.println("Team Name is: " + highestScoreTeam);
            System.out.println("Number of Matches with total 300 Plus Score: " + highScoringMatchCount);
        } catch (Exception e) {
            // Handle any errors in fetching or processing data
            System.err.println("Error processing cricket match data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
