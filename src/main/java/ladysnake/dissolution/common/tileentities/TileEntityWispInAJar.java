package ladysnake.dissolution.common.tileentities;

import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo", striprefs = true)
public class TileEntityWispInAJar extends TileEntity implements ILightProvider {

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public Light provideLight() {
        return Light.builder().pos(this.pos).radius(5).color(0.5f, 0.5f, 0.8f).build();
    }
}
