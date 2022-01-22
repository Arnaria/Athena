package corviolis.athena.util;

import java.util.Comparator;
import java.util.HashMap;

public class XpComparator implements Comparator<HashMap<String, Object>> {

    @Override
    public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
        if ((int) o1.get("xp") > (int) o2.get("xp")) return 1;
        return 0;
    }
}
