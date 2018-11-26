package ladysnake.dissolution.common.handlers;

import ladylib.compat.EnhancedBusSubscriber;
import ladylib.reflection.Getter;
import ladylib.reflection.LLReflectionHelper;
import ladylib.reflection.Setter;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@EnhancedBusSubscriber(Ref.MOD_ID)
public class PossessionEventHandler {
    private static final Getter<EntityPlayerSP, Double> getLastReportedPosX =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_175172_bI", double.class);
    private static final Setter<EntityPlayerSP, Double> setLastReportedPosX =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_175172_bI", double.class);
    private static final Getter<EntityPlayerSP, Double> getLastReportedPosY =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_175166_bJ", double.class);
    private static final Setter<EntityPlayerSP, Double> setLastReportedPosY =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_175166_bJ", double.class);
    private static final Getter<EntityPlayerSP, Double> getLastReportedPosZ =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_175167_bK", double.class);
    private static final Setter<EntityPlayerSP, Double> setLastReportedPosZ =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_175167_bK", double.class);
    private static final Getter<EntityPlayerSP, Float> getLastReportedYaw =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_175164_bL", float.class);
    private static final Setter<EntityPlayerSP, Float> setLastReportedYaw =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_175164_bL", float.class);
    private static final Getter<EntityPlayerSP, Float> getLastReportedPitch =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_175165_bM", float.class);
    private static final Setter<EntityPlayerSP, Float> setLastReportedPitch =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_175165_bM", float.class);
    private static final Getter<EntityPlayerSP, Integer> getPositionUpdateTicks =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_175168_bP", int.class);
    private static final Setter<EntityPlayerSP, Integer> setPositionUpdateTicks =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_175168_bP", int.class);
    private static final Getter<EntityPlayerSP, Boolean> getPrevOnGround =
            LLReflectionHelper.findGetter(EntityPlayerSP.class, "field_184841_cd", boolean.class);
    private static final Setter<EntityPlayerSP, Boolean> setPrevOnGround =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_184841_cd", boolean.class);
    private static final Setter<EntityPlayerSP, Boolean> autoJumpEnabled =
            LLReflectionHelper.findSetter(EntityPlayerSP.class, "field_189811_cr", boolean.class);

    @SubscribeEvent
    public void onTickPlayerTick(TickEvent.PlayerTickEvent event) {
        // Tick a bunch of possession
        if (event.phase != TickEvent.Phase.START) return;
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        IPossessable possessed = playerCorp.getPossessed();
        World world = event.player.world;
        if (possessed != null) {
            possessed.updatePossessing();
            if (world.isRemote) {
                possessed.possessTickClient();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        // Saves the player's possessed entity and removes it, so that it doesn't wander around
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        Entity possessed = playerCorp.getPossessed();
        World world = event.player.world;
        if (possessed != null && !world.isRemote) {
            if (playerCorp instanceof CapabilityIncorporealHandler.DefaultIncorporealHandler) {
                ((CapabilityIncorporealHandler.DefaultIncorporealHandler) playerCorp).setSerializedPossessedEntity(possessed.serializeNBT());
                world.removeEntity(possessed);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        CapabilityIncorporealHandler.getHandler(event.getEntity()).ifPresent(handler -> {
            if (handler instanceof CapabilityIncorporealHandler.DefaultIncorporealHandler) {
                NBTTagCompound serializedPossessedEntity = ((CapabilityIncorporealHandler.DefaultIncorporealHandler) handler).getSerializedPossessedEntity();
                if (serializedPossessedEntity != null) {
                    Entity host = EntityList.createEntityFromNBT(serializedPossessedEntity, event.getWorld());
                    event.getWorld().spawnEntity(host);
                    if (host instanceof EntityLivingBase && host instanceof IPossessable) {
                        handler.setPossessed((EntityLivingBase & IPossessable) host);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient()) {
            // Manually send position information
            EntityPlayerSP self = (EntityPlayerSP) event.player;
            AxisAlignedBB axisalignedbb = self.getEntityBoundingBox();
            double d0 = self.posX - getLastReportedPosX.invoke(self);
            double d1 = axisalignedbb.minY - getLastReportedPosY.invoke(self);
            double d2 = self.posZ - getLastReportedPosZ.invoke(self);
            double d3 = (double) (self.rotationYaw - getLastReportedYaw.invoke(self));
            double d4 = (double) (self.rotationPitch - getLastReportedPitch.invoke(self));
            // ++positionUpdateTicks
            setPositionUpdateTicks.invoke(self, getPositionUpdateTicks.invoke(self) + 1);
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || getPositionUpdateTicks.invoke(self) >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            if (self.isRiding()) {
                self.connection.sendPacket(new CPacketPlayer.PositionRotation(self.motionX, -999.0D, self.motionZ, self.rotationYaw, self.rotationPitch, self.onGround));
                flag2 = false;
            } else if (flag2 && flag3) {
                self.connection.sendPacket(new CPacketPlayer.PositionRotation(self.posX, axisalignedbb.minY, self.posZ, self.rotationYaw, self.rotationPitch, self.onGround));
            } else if (flag2) {
                self.connection.sendPacket(new CPacketPlayer.Position(self.posX, axisalignedbb.minY, self.posZ, self.onGround));
            } else if (flag3) {
                self.connection.sendPacket(new CPacketPlayer.Rotation(self.rotationYaw, self.rotationPitch, self.onGround));
            } else if (getPrevOnGround.invoke(self) != self.onGround) {
                self.connection.sendPacket(new CPacketPlayer(self.onGround));
            }

            if (flag2) {
                setLastReportedPosX.invoke(self, self.posX);
                setLastReportedPosY.invoke(self, self.posY);
                setLastReportedPosZ.invoke(self, self.posZ);
                setPositionUpdateTicks.invoke(self, 0);
            }

            if (flag3) {
                setLastReportedYaw.invoke(self, self.rotationYaw);
                setLastReportedPitch.invoke(self, self.rotationPitch);
            }

            setPrevOnGround.invoke(self, self.onGround);
            autoJumpEnabled.set(self, Minecraft.getMinecraft().gameSettings.autoJump);
        }
    }
}
