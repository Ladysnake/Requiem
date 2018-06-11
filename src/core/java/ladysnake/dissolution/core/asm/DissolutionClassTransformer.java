package ladysnake.dissolution.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;

public class DissolutionClassTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }
}
