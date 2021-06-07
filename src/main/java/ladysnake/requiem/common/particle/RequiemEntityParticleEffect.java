package ladysnake.requiem.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

public class RequiemEntityParticleEffect implements ParticleEffect {
   public static final Factory<RequiemEntityParticleEffect> PARAMETERS_FACTORY = new Factory<>() {
       @Override
       public RequiemEntityParticleEffect read(ParticleType<RequiemEntityParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
           stringReader.expect(' ');
           BlockState blockState = (new BlockArgumentParser(stringReader, false)).parse(false).getBlockState();
           stringReader.expect(' ');
           int targetEntityId = stringReader.readInt();
           return new RequiemEntityParticleEffect(particleType, blockState, targetEntityId);
       }

       @Override
       public RequiemEntityParticleEffect read(ParticleType<RequiemEntityParticleEffect> particleType, PacketByteBuf buf) {
           return new RequiemEntityParticleEffect(particleType, Block.STATE_IDS.get(buf.readVarInt()), buf.readVarInt());
       }
   };
   private final ParticleType<RequiemEntityParticleEffect> type;
   private final BlockState blockState;
   private final int targetEntityId;

   public static Codec<RequiemEntityParticleEffect> codec(ParticleType<RequiemEntityParticleEffect> particleType) {
      return RecordCodecBuilder.create(instance -> instance.group(
          BlockState.CODEC.fieldOf("state").forGetter(RequiemEntityParticleEffect::getBlockState),
          Codec.INT.fieldOf("entityId").forGetter(RequiemEntityParticleEffect::getTargetEntityId)
      ).apply(instance, (state, id) -> new RequiemEntityParticleEffect(particleType, state, id)));
   }

   public RequiemEntityParticleEffect(ParticleType<RequiemEntityParticleEffect> type, BlockState blockState, int targetEntityId) {
      this.type = type;
      this.blockState = blockState;
       this.targetEntityId = targetEntityId;
   }

   @Override
   public void write(PacketByteBuf buf) {
      buf.writeVarInt(Block.STATE_IDS.getRawId(this.blockState));
      buf.writeVarInt(targetEntityId);
   }

   @Override
   public String asString() {
       return "%s %s %d".formatted(Registry.PARTICLE_TYPE.getId(this.getType()), BlockArgumentParser.stringifyBlockState(this.blockState), this.targetEntityId);
   }

   @Override
   public ParticleType<RequiemEntityParticleEffect> getType() {
      return this.type;
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

    public int getTargetEntityId() {
        return targetEntityId;
    }
}
