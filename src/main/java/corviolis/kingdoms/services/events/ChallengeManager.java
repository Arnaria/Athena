package corviolis.kingdoms.services.events;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChallengeManager {

    private static final File folder = new File(FabricLoader.getInstance().getConfigDir() + "/challenges");
    private static final HashMap<Integer, ArrayList<Challenge>> challenges = new HashMap<>();

    public static void init() {
        try {
            if (!folder.exists()) folder.mkdir();

            String[] fileNames = folder.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    CSVReader csvReader = new CSVReader(new FileReader(folder.getPath() + "/" + fileName));

                    int tier = 0;
                    ArrayList<Challenge> parsedChallenges = new ArrayList<>();

                    int count = 0;
                    for (String[] c : csvReader.readAll()) {
                        if (count == 0) tier = Integer.parseInt(c[0]);
                        else {
                            Challenge challenge = new Challenge(c[0], c[1], Integer.parseInt(c[2]));
                            parsedChallenges.add(challenge);
                        }
                        count++;
                    }
                    challenges.put(tier, parsedChallenges);
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public static int getMaxTier() {
        return challenges.size();
    }

    public static ArrayList<String> getChallengeIds(int tier) {
        ArrayList<String> challengeIds = new ArrayList<>();
        for (Challenge challenge : challenges.get(tier)) {
            challengeIds.add(challenge.challengeId());
        }
        return challengeIds;
    }

    public static Challenge getChallenge(String challengeId) {
        for (ArrayList<Challenge> tier : challenges.values()) {
            for (Challenge challenge : tier) {
                if (challenge.challengeId().equals(challengeId)) return challenge;
            }
        }
       return null;
    }
}