package corviolis.athena.interfaces;

public interface PlayerEntityInf {
    void setKingdomId(String kingdomId);
    String getKingdomId();
    void setKingship(boolean isKing);
    boolean isKing();
    void allowToEditIn(String kingdomId);
    void removeAllowedToEditIn(String kingdomId);
    boolean allowedToEditIn(String kingdomId);
}
