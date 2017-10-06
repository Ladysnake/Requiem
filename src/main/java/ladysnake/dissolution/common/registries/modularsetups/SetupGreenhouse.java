package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.api.DistillateStack;
import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.IDistillateHandler;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetupGreenhouse extends ModularMachineSetup {
    private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1),
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.CLOCHE, 1));
    public final List<Instance> allInstances = new ArrayList<>();

    private Map<Pair<Item, DistillateStack>, ItemStack> recipes;

    public SetupGreenhouse() {
        this.setRegistryName("setup_greenhouse");
        this.recipes = new HashMap<>();
        addRecipe(ModItems.BACA_SEEDS, new DistillateStack(DistillateTypes.CINNABARIS, 9), new ItemStack(ModItems.INSUBACA));
        addRecipe(ModItems.BACA_SEEDS, new DistillateStack(DistillateTypes.SULPURIS, 9), new ItemStack(ModItems.ACERBACA));
    }

    @Override
    public ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup() {
        return setup;
    }

    @Override
    public ISetupInstance getInstance(TileEntityModularMachine te) {
        return new Instance(te);
    }

    private void addRecipe(Item seed, DistillateStack essentia, ItemStack fruit) {
        this.recipes.put(Pair.of(seed, essentia), fruit);
    }

    public class Instance implements ISetupInstance {

        private TileEntityModularMachine tile;
        private InputItemHandler fruitInv;
        private IDistillateHandler essentiaInv;
        private int time;

        Instance(TileEntityModularMachine tile) {
            this.tile = tile;
            this.fruitInv = new InputItemHandler(recipes.entrySet().stream()
                    .flatMap(entry -> Stream.of(entry.getKey().getLeft(), entry.getValue().getItem()))
                    .collect(Collectors.toList()).toArray(new Item[0]));
            this.fruitInv.setMaxSize(1);
            this.essentiaInv = new CapabilityDistillateHandler.DefaultDistillateHandler(100);
        }

        @Override
        public void init() {
            tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.CONSUMER);
            allInstances.add(this);
        }

        @Override
        public void onRemoval() {
            tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.NONE);
            allInstances.remove(this);
        }

        @Override
        public void onInteract(EntityPlayer playerIn, EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {
            ItemStack stack = playerIn.getHeldItem(hand);
            if(recipes.keySet().stream().map(Pair::getLeft).anyMatch(stack.getItem()::equals) && this.fruitInv.getStackInSlot(0).isEmpty()) {
                this.fruitInv.insertItem(0, stack.splitStack(1), false);
            }
        }

        @Override
        public void onTick() {
            if(time++ % 20 == 0) {
                ItemStack fruit = this.fruitInv.extractItem(0, 1, false);
                if (tile.isPowered() && !fruit.isEmpty()) {
                    ItemStack result = recipes.get(
                            Pair.of(fruit.getItem(), essentiaInv.readContent(DistillateTypes.UNTYPED)));
                    if(result != null) {
                        fruit = result;
                        essentiaInv.extract(Integer.MAX_VALUE, DistillateTypes.UNTYPED);
                    }
                }
                this.fruitInv.insertItem(0, fruit, false);
            }
        }

        public BlockPos getPosition() {
            return this.tile.getPos();
        }

        @Override
        public ResourceLocation getPlugModel(EnumFacing facing, BlockCasing.EnumPartType part, ResourceLocation defaultModel) {
            return part == BlockCasing.EnumPartType.BOTTOM ? defaultModel : null;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
            return part == BlockCasing.EnumPartType.TOP &&
                    (capability == CapabilityDistillateHandler.CAPABILITY_ESSENTIA || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
            if(part == BlockCasing.EnumPartType.TOP) {
                if(capability == CapabilityDistillateHandler.CAPABILITY_ESSENTIA)
                    return CapabilityDistillateHandler.CAPABILITY_ESSENTIA.cast(this.essentiaInv);
                if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.fruitInv);
            }
            return null;
        }
    }
}
