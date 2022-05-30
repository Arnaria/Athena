package corviolis.athena.interfaces;


import net.minecraft.entity.Entity;

public interface ServerPlayerEntityInf {
    void trackEntity(Entity entity);
    void stopTrackingEntity();
}
