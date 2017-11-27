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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SetupGreenhouse extends ModularMachineSetup {
    private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1),
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.CLOCHE, 1));
    public final List<Instance> allInstances = new ArrayList<>();

    private List<Recipe> recipes;

    public SetupGreenhouse() {
        this.setRegistryName("setup_greenhouse");
        this.recipes = new LinkedList<>();
        this.recipes.add(new Recipe(ModItems.BACA_SEEDS, new ItemStack(ModItems.SALERBACA),
                new DistillateStack(DistillateTypes.SULPURIS, 9), new DistillateStack(DistillateTypes.SALIS, 9)));
        addRecipe(ModItems.BACA_SEEDS, DistillateTypes.CINNABARIS, 9, new ItemStack(ModItems.INSUBACA));
        addRecipe(ModItems.BACA_SEEDS, DistillateTypes.SULPURIS, 9, new ItemStack(ModItems.ACERBACA));
        this.recipes.sort((r1, r2) -> Integer.compare(r2.distillates.size(), r1.distillates.size()));
    }

    @Override
    public ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup() {
        return setup;
    }

    @Override
    public ISetupInstance getInstance(TileEntityModularMachine te) {
        return new Instance(te);
    }

    private void addRecipe(Item seed, DistillateTypes distillateType, int distillateCount, ItemStack fruit) {
        this.recipes.add(new Recipe(seed, fruit, new DistillateStack(distillateType, distillateCount)));
    }

    public class Instance implements ISetupInstance {

        private TileEntityModularMachine tile;
        private InputItemHandler fruitInv;
        private IDistillateHandler essentiaInv;
        private int time;

        Instance(TileEntityModularMachine tile) {
            this.tile = tile;
            this.fruitInv = new InputItemHandler(recipes.stream()
                    .flatMap(entry -> Stream.of(entry.seeds, entry.result.getItem()))
                    .collect(Collectors.toList()).toArray(new Item[0]));
            this.fruitInv.setMaxSize(1);
            this.essentiaInv = new CapabilityDistillateHandler.DefaultDistillateHandler(100, 3);
        }

        @Override
        public void init() {
            tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.CONSUMER);
            recipes.stream().flatMap(recipe -> recipe.distillates.stream()).map(DistillateStack::getType).distinct().forEach(distillate -> this.essentiaInv.setSuction(distillate, 6));
            allInstances.add(this);
        }

        @Override
        public void onRemoval() {
            tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.NONE);
            allInstances.remove(this);
        }

        @Override
        public void onInteract(EntityPlayer playerIn, EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {
            if (playerIn.world.isRemote) return;
            ItemStack stack = playerIn.getHeldItem(hand);
            if (recipes.stream().map(r -> r.seeds).anyMatch(stack.getItem()::equals) && this.fruitInv.getStackInSlot(0).isEmpty()) {
                this.fruitInv.insertItem(0, stack.splitStack(1), false);
            } else if (stack.isEmpty()) {
                playerIn.addItemStackToInventory(this.fruitInv.extractItem(0, 1, false));
            }
        }

        @Override
        public void onTick() {
            if (time++ % 20 == 0) {
                ItemStack fruit = this.fruitInv.extractItem(0, 1, true);
                if (tile.isPowered() && !fruit.isEmpty()) {
                    recipes.stream()
                            .filter(recipe -> recipe.accepts(fruit.getItem(), essentiaInv)).findFirst()
                            .ifPresent(res -> {
                                this.fruitInv.extractItem(0, 1, false);
                                res.distillates.forEach(dis -> essentiaInv.extract(dis.getCount(), dis.getType()));
                                this.fruitInv.insertItem(0, res.getResult(), false);
                            });
                }
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
            return part == BlockCasing.EnumPartType.BOTTOM &&
                    (capability == CapabilityDistillateHandler.CAPABILITY_DISTILLATE || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
            if (part == BlockCasing.EnumPartType.BOTTOM) {
                if (capability == CapabilityDistillateHandler.CAPABILITY_DISTILLATE)
                    return CapabilityDistillateHandler.CAPABILITY_DISTILLATE.cast(this.essentiaInv);
                if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.fruitInv);
            }
            return null;
        }
    }

    private static class Recipe {

        private Item seeds;
        private List<DistillateStack> distillates;
        private ItemStack result;

        Recipe(Item seeds, ItemStack result, DistillateStack... distillates) {
            super();
            this.seeds = seeds;
            this.distillates = Arrays.asList(distillates);
            this.result = result;
        }

        boolean accepts(Item seeds, IDistillateHandler distillates) {
            return this.seeds == seeds &&
                    this.distillates.stream().allMatch(dis -> StreamSupport.stream(distillates.spliterator(), false).anyMatch(dis2 -> dis.getType() == dis2.getType() && dis.getCount() <= dis2.getCount()));
        }

        ItemStack getResult() {
            return result.copy();
        }
    }
}
