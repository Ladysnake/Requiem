package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.EssentiaTypes;
import ladysnake.dissolution.api.IEssentiaHandler;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.capabilities.CapabilityEssentiaHandler;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetupGreenhouse extends ModularMachineSetup {
    private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, 1),
            new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.CLOCHE, 1));

    private Map<Pair<Item, EssentiaStack>, ItemStack> recipes;

    public SetupGreenhouse() {
        this.setRegistryName("setup_greenhouse");
        addRecipe(ModItems.BACA_SEEDS, new EssentiaStack(EssentiaTypes.CINNABARIS, 9), new ItemStack(ModItems.INSUBACA));
        addRecipe(ModItems.BACA_SEEDS, new EssentiaStack(EssentiaTypes.SULPURIS, 9), new ItemStack(ModItems.ACERBACA));
    }

    @Override
    public ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup() {
        return setup;
    }

    @Override
    public ISetupInstance getInstance(TileEntityModularMachine te) {
        return new Instance(te);
    }

    public void addRecipe(Item seed, EssentiaStack essentia, ItemStack fruit) {
        this.recipes.put(Pair.of(seed, essentia), fruit);
    }

    class Instance implements ISetupInstance {

        private TileEntityModularMachine tile;
        private InputItemHandler fruitInv;
        private IEssentiaHandler essentiaInv;
        private int time;

        Instance(TileEntityModularMachine tile) {
            this.tile = tile;
            this.fruitInv = new InputItemHandler(recipes.entrySet().stream()
                    .flatMap(entry -> Stream.of(entry.getKey().getLeft(), entry.getValue().getItem()))
                    .collect(Collectors.toList()).toArray(new Item[0]));
            this.fruitInv.setMaxSize(1);
            this.essentiaInv = new CapabilityEssentiaHandler.DefaultEssentiaHandler(100);
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
                            Pair.of(fruit.getItem(), essentiaInv.readContent(EssentiaTypes.UNTYPED)));
                    if(result != null) {
                        fruit = result;
                        essentiaInv.extract(Integer.MAX_VALUE, EssentiaTypes.UNTYPED);
                    }
                }
                this.fruitInv.insertItem(0, fruit, false);
            }
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
            return part == BlockCasing.EnumPartType.TOP &&
                    (capability == CapabilityEssentiaHandler.CAPABILITY_ESSENTIA || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
            if(part == BlockCasing.EnumPartType.TOP && tile.isPlugAttached(facing, part)) {
                if(capability == CapabilityEssentiaHandler.CAPABILITY_ESSENTIA)
                    return CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.cast(this.essentiaInv);
                if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.fruitInv);
            }
            return null;
        }
    }
}
