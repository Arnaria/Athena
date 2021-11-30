package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.ScoreboardInf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin implements ScoreboardInf {

    @Shadow @Final private Map<String, Team> teams;
    @Shadow @Final private Map<String, Team> teamsByPlayer;
    @Shadow public abstract void updateRemovedTeam(Team team);

    public void clearTeams() {
        teams.values().forEach(this::updateRemovedTeam);
        teams.clear();
        teamsByPlayer.clear();
    }
}
