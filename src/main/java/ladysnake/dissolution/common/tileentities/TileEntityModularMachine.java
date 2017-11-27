package ladysnake.dissolution.common.tileentities;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.common.blocks.alchemysystem.AbstractPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.init.ModModularSetups;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.items.ItemPlug;
import ladysnake.dissolution.common.registries.modularsetups.ISetupInstance;
import ladysnake.dissolution.common.registries.modularsetups.SetupResonantCoil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class TileEntityModularMachine extends TileEntity implements ITickable {

    private Set<ItemAlchemyModule.AlchemyModule> installedModules;
    private Map<BlockCasing.EnumPartType, Map<EnumFacing, Boolean>> plugs;
    private ISetupInstance currentSetup = null;
    private PowerConsumption powerConsumption;
    private boolean powered = false;

    public TileEntityModularMachine() {
        this.installedModules = new HashSet<>();
        this.plugs = new HashMap<>();
        for (BlockCasing.EnumPartType part : BlockCasing.EnumPartType.values()) {
            Map<EnumFacing, Boolean> facings = new HashMap<>();
            for (EnumFacing facing : EnumFacing.values()) {
                facings.put(facing, false);
            }
            this.plugs.put(part, facings);
        }
    }

    @Override
    public void update() {
        if (currentSetup != null)
            currentSetup.onTick();
    }

    /**
     * Adjusts the given facing to take the block's rotation into account
     * Should be used by setups trying to interact with the world
     *
     * @param face a facing relative to the machine's rotation
     * @return the corresponding in-world face using the casing's rotation
     */
    public EnumFacing adjustFaceOut(EnumFacing face) {
        if (face.getAxis() == EnumFacing.Axis.Y)
            return face;
        switch (world.getBlockState(pos).getValue(BlockCasing.FACING)) {
            case WEST:
                face = face.rotateY();
            case SOUTH:
                face = face.rotateY();
            case EAST:
                face = face.rotateY();
            default:
        }
        return face;
    }

    /**
     * Performs the opposite operation of {@link #adjustFaceOut}
     *
     * @param face an absolute facing given by the world
     * @return the corresponding relative facing
     */
    public EnumFacing adjustFaceIn(EnumFacing face) {
        if (face.getAxis() == EnumFacing.Axis.Y)
            return face;
        switch (world.getBlockState(pos).getValue(BlockCasing.FACING)) {
            case WEST:
                face = face.rotateYCCW();
            case SOUTH:
                face = face.rotateYCCW();
            case EAST:
                face = face.rotateYCCW();
            default:
        }
        return face;
    }

    /**
     * Attempts to output an ItemStack into one or more containers
     *
     * @param stack the stack to output through this machine
     * @return what could not be inserted
     */
    public ItemStack tryOutput(ItemStack stack, BlockCasing.EnumPartType part) {
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (isPlugAttached(facing, part))
                stack = tryOutput(stack, part, facing);
            if (stack.isEmpty())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    /**
     * Attempts to output an ItemStack into a container
     *
     * @param stack the stack to output through this machine
     * @param face  the face interacting with the world, not taking into account the casing's rotation
     * @return what could not be inserted
     */
    public ItemStack tryOutput(ItemStack stack, BlockCasing.EnumPartType part, EnumFacing face) {
        TileEntity te = this.getWorld().getTileEntity(getPos().offset(face));
        if (te != null) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    face.getOpposite());
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    if ((stack = handler.insertItem(i, stack, false)).isEmpty())
                        return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    public Map<EnumFacing, TileEntity> getAdjacentTEs(BlockCasing.EnumPartType part, BiPredicate<EnumFacing, TileEntity> condition) {
        Map<EnumFacing, TileEntity> ret = new HashMap<>();
        BlockPos pos = part == BlockCasing.EnumPartType.BOTTOM ? this.pos : this.pos.up();
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te != null && condition.test(facing, te))
                ret.put(facing, te);
        }
        return ret;
    }

    private boolean addModule(ItemAlchemyModule.AlchemyModule module) {
        boolean added = installedModules.stream().allMatch(mod -> (mod.getType().isCompatible(module.getType())))
                && installedModules.add(module);
        if (added) {
            this.verifySetup();
            SetupResonantCoil.THREADPOOL.submit(() -> AbstractPowerConductor.updatePowerCore(world, pos));
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
            this.markDirty();
        }
        return added;
    }

    private ItemStack removeModule() {
        Iterator<ItemAlchemyModule.AlchemyModule> iterator = installedModules.iterator();
        ItemStack ret = ItemStack.EMPTY;
        if (iterator.hasNext()) {
            ret = new ItemStack(iterator.next().toItem());
            iterator.remove();
            this.verifySetup();
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
            this.markDirty();
        }
        return ret;
    }

    public void interact(EntityPlayer playerIn, EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItemMainhand();
        if (stack.getItem() instanceof ItemAlchemyModule) {
            if (addModule(((ItemAlchemyModule) stack.getItem()).toModule(part, this)) && !playerIn.isCreative()) {
                stack.shrink(1);
            }
        } else if (stack.isEmpty() && playerIn.isSneaking()) {
            playerIn.addItemStackToInventory(removeModule());
        } else if (stack.getItem() instanceof ItemPlug && this.currentSetup != null) {
            if (!this.setPlug(facing, part, true)) {
                this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
                this.markDirty();
                if (!playerIn.isCreative())
                    stack.shrink(1);
            }
        } else if (this.currentSetup != null)
            this.currentSetup.onInteract(playerIn, hand, part, adjustFaceIn(facing), hitX, hitY, hitZ);
    }

    public boolean setPlug(EnumFacing facing, BlockCasing.EnumPartType part, boolean b) {
        return this.plugs.get(part).put(facing, b);
    }

    public boolean isPlugAttached(EnumFacing facing, BlockCasing.EnumPartType part) {
        return this.plugs.get(part).get(facing);
    }

    public ResourceLocation getPlugModel(EnumFacing facing, BlockCasing.EnumPartType part) {
        if (isPlugAttached(facing, part)) {
            ResourceLocation plug;
            TileEntity neighbour = this.world.getTileEntity((part == BlockCasing.EnumPartType.TOP
                    ? this.getPos().up() : this.getPos()).offset(facing));
            if (neighbour instanceof TileEntityChest)
                plug = BlockCasing.PLUG_CHEST;
            else if (neighbour instanceof TileEntityHopper)
                plug = BlockCasing.PLUG_HOPPER;
            else
                plug = BlockCasing.PLUG;
            return this.currentSetup != null ? this.currentSetup.getPlugModel(adjustFaceIn(facing), part, plug) : null;
        }
        return null;
    }

    public Set<ItemAlchemyModule.AlchemyModule> getInstalledModules() {
        return ImmutableSet.copyOf(this.installedModules);
    }

    @SideOnly(Side.CLIENT)
    public Set<ResourceLocation> getModelsForRender() {
        Set<ResourceLocation> ret = this.installedModules.stream().map(ItemAlchemyModule.AlchemyModule::getModel)
                .collect(Collectors.toSet());
        if (currentSetup != null)
            currentSetup.addModelsForRender(ret);
        return ret;
    }

    public ISetupInstance getCurrentSetup() {
        return this.currentSetup;
    }

    public void dropContent() {
        if (this.currentSetup != null)
            this.currentSetup.onRemoval();
        if (!this.world.isRemote) {
            for (ItemAlchemyModule.AlchemyModule module : installedModules) {
                world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
                        new ItemStack(module.toItem())));
            }
            world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
                    new ItemStack(ModItems.PLUG, (int) this.plugs.values().stream().flatMap(e -> e.values().stream())
                            .filter(Boolean::booleanValue).count())));
        }
        this.installedModules.clear();
    }

    private void verifySetup() {
        Set<ItemAlchemyModule.AlchemyModule> modules = getInstalledModules();
        ISetupInstance newSetup = ModModularSetups.REGISTRY.getValues().stream()
                .filter(setup -> setup.isValidSetup(modules)).map(setup -> setup.getInstance(this))
                .findAny().orElse(null);
        if ((newSetup != null && currentSetup != null && newSetup.getClass() != currentSetup.getClass())
                || (newSetup == null ^ currentSetup == null)) {
            if (currentSetup != null) currentSetup.onRemoval();
            if (newSetup != null) newSetup.init();
            currentSetup = newSetup;
        }
    }

    public PowerConsumption getPowerConsumption() {
        return powerConsumption;
    }

    public void setPowerConsumption(PowerConsumption powerConsumption) {
        this.powerConsumption = powerConsumption;
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return hasCapability(capability, facing, BlockCasing.EnumPartType.BOTTOM);
    }

    public boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
        return (isPlugAttached(facing, part)
                && this.currentSetup != null
                && this.currentSetup.hasCapability(capability, adjustFaceIn(facing), part))
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return getCapability(capability, facing, BlockCasing.EnumPartType.BOTTOM);
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
        T setupCap = this.currentSetup != null && isPlugAttached(facing, part)
                ? this.currentSetup.getCapability(capability, adjustFaceIn(facing), part)
                : null;
        return setupCap == null ? (super.getCapability(capability, facing)) : setupCap;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBTBasic(nbt);

        return new SPacketUpdateTileEntity(getPos(), 0, nbt);

    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (this.world.isRemote)
            this.readFromNBTBasic(pkt.getNbtCompound());
    }

    @Override
    public @Nonnull
    NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBTBasic(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        readFromNBTBasic(tag);
    }

    private void readFromNBTBasic(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList modules = compound.getTagList("modules", 10);
        for (NBTBase mod : modules) {
            AlchemyModuleTypes type = AlchemyModuleTypes.valueOf(((NBTTagCompound) mod).getString("type"));
            if (type != null)
                this.installedModules.add(type.readNBT((NBTTagCompound) mod));
        }
        NBTTagCompound plugsNBT = compound.getCompoundTag("plugs");
        this.plugs.forEach((key, value) -> value.replaceAll((f, b) -> plugsNBT.getCompoundTag(key.name()).getBoolean(f.name())));
        verifySetup();
        this.setPowered(compound.getBoolean("powered"));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        readFromNBTBasic(compound);
        if (this.currentSetup != null)
            this.currentSetup.readFromNBT(compound.getCompoundTag("currentSetup"));
    }

    private void writeToNBTBasic(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList modules = new NBTTagList();
        for (ItemAlchemyModule.AlchemyModule mod : installedModules) {
            if (mod != null) {
                modules.appendTag(mod.toNBT());
            }
        }
        compound.setTag("modules", modules);
        NBTTagCompound plugs = new NBTTagCompound();
        for (Map.Entry<BlockCasing.EnumPartType, Map<EnumFacing, Boolean>> part : this.plugs.entrySet()) {
            NBTTagCompound facings = new NBTTagCompound();
            for (Map.Entry<EnumFacing, Boolean> side : part.getValue().entrySet()) {
                facings.setBoolean(side.getKey().name(), side.getValue());
            }
            plugs.setTag(part.getKey().name(), facings);
        }
        compound.setTag("plugs", plugs);
        compound.setBoolean("powered", isPowered());
    }

    @Override
    public @Nonnull
    NBTTagCompound writeToNBT(NBTTagCompound compound) {
        writeToNBTBasic(compound);
        if (this.currentSetup != null)
            compound.setTag("currentSetup", currentSetup.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock() || oldState.withProperty(IPowerConductor.POWERED, newState.getValue(IPowerConductor.POWERED)) != newState;
    }

}
