package corviolis.athena.interfaces;

public interface PlayerEntityInf {
    void setKingdomId(String kingdomId);
    String getKingdomId();
    void setKingship(boolean isKing);
    boolean isKing();
    void allowToEditIn(String kingdomId);
    void removeAllowedToEditIn(String kingdomId);
    boolean allowedToEditIn(String kingdomId);
    void addWin();
    void addLoss();
    int getWins();
    int getLosses();
    void addStreak();
    void resetStreak();
    int getStreak();
    boolean canDuelForXp();
}
