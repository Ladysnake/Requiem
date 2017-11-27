package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.api.DistillateStack;
import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.IDistillateHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.HashMap;
import java.util.Map;

public class SetupCrystallizer extends ModularMachineSetup {

    private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, 1),
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.CRYSTALLIZER, 1),
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1));
    private final Map<DistillateStack, Item> conversions;

    public SetupCrystallizer() {
        this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "crystallizer"));
        this.conversions = new HashMap<>();
        addConversion(DistillateTypes.CINNABARIS, ModItems.CINNABAR);
        addConversion(DistillateTypes.SULPURIS, ModItems.SULFUR);
        addConversion(DistillateTypes.SALIS, ModItems.HALITE);
    }

    private void addConversion(DistillateTypes essentia, Item out) {
        conversions.put(new DistillateStack(essentia, 9), out);
    }

    @Override
    public ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup() {
        return setup;
    }

    @Override
    public ISetupInstance getInstance(TileEntityModularMachine te) {
        return new Instance(te);
    }

    public class Instance implements ISetupInstance {

        private TileEntityModularMachine tile;
        private IDistillateHandler distillateHandler;
        private OutputItemHandler oreOutput;
        private int progressTicks;

        Instance(TileEntityModularMachine tile) {
            super();
            this.tile = tile;
            if (tile.hasWorld())
                tile.getWorld().markBlockRangeForRenderUpdate(tile.getPos().add(1, 0, 1), tile.getPos().add(-1, 0, -1));
            this.distillateHandler = new CapabilityDistillateHandler.DefaultDistillateHandler(99, 4);
            this.distillateHandler.setSuction(DistillateTypes.UNTYPED, 10);
            this.oreOutput = new OutputItemHandler();
        }

        @Override
        public void init() {
            tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.CONSUMER);
        }

        @Override
        public void onRemoval() {
            tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.NONE);
        }

        @Override
        public void onTick() {
            if (!tile.getWorld().isRemote && this.progressTicks++ % 20 == 0) {
                if (this.oreOutput.getStackInSlot(0).getCount() < this.oreOutput.getSlotLimit(0)) {
                    for (DistillateStack distillateStack : distillateHandler) {
                        if (distillateStack.getCount() >= 9) {
                            DistillateStack in = this.distillateHandler.extract(9, distillateStack.getType());
                            this.oreOutput.insertItemInternal(0, new ItemStack(conversions.get(in)), false);
                            break;
                        }
                    }
                }
                this.oreOutput.insertItemInternal(0,
                        tile.tryOutput(this.oreOutput.extractItem(0, 10, false), BlockCasing.EnumPartType.BOTTOM),
                        false);
            }
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return part == BlockCasing.EnumPartType.BOTTOM && facing != EnumFacing.EAST;
            }
            return capability == CapabilityDistillateHandler.CAPABILITY_DISTILLATE && part == EnumPartType.TOP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                if (part == BlockCasing.EnumPartType.BOTTOM && facing != EnumFacing.EAST)
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.oreOutput);
            }
            if (capability == CapabilityDistillateHandler.CAPABILITY_DISTILLATE) {
                if (part == BlockCasing.EnumPartType.TOP)
                    return CapabilityDistillateHandler.CAPABILITY_DISTILLATE.cast(this.distillateHandler);
            }
            return null;
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.oreOutput, EnumFacing.WEST, compound.getTag("output"));
            //noinspection ConstantConditions
            CapabilityDistillateHandler.CAPABILITY_DISTILLATE.getStorage().readNBT(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, this.distillateHandler, EnumFacing.NORTH, compound.getTag("distillateHandler"));
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setTag("output", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.oreOutput, EnumFacing.WEST));
            compound.setTag("distillateHandler", CapabilityDistillateHandler.CAPABILITY_DISTILLATE.getStorage().writeNBT(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, distillateHandler, EnumFacing.NORTH));
            return compound;
        }

    }

}
