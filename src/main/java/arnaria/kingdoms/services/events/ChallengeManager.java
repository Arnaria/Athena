package arnaria.kingdoms.services.events;

import com.opencsv.CSVReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChallengeManager {

    private static final File folder = new File(FabricLoader.getInstance().getConfigDir() + "/challenges");
    private static final ArrayList<List<String[]>> challenges = new ArrayList<>();

    public static void init() {
        try {
            if (!folder.exists()) folder.mkdir();

            String[] fileNames = folder.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    CSVReader csvReader = new CSVReader(new FileReader(folder.getPath() + "/" + fileName));

                    ArrayList<String[]> parsedChallenges = new ArrayList<>();
                    int tier = 0;

                    int count = 0;
                    for (String[] challenge : csvReader.readAll()) {
                        if (count == 0) tier = Integer.parseInt(challenge[0]);
                        else parsedChallenges.add(challenge);
                        count++;
                    }

                    challenges.set(tier, parsedChallenges);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getMaxTier() {
        return challenges.size();
    }

    public static ArrayList<String> getChallengeIds(int tier) {
        ArrayList<String> challengeIds = new ArrayList<>();
        for (String[] challenge : challenges.get(tier)) {
            challengeIds.add(challenge[0]);
        }
        return challengeIds;
    }

    public static String[] getChallenge(int tier, String challengeId) {
        for (String[] challenge : challenges.get(tier)) {
            if (challenge[0].equals(challengeId)) return challenge;
        }
        return null;
    }
}