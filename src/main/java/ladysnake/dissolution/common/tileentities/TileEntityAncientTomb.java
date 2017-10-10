package ladysnake.dissolution.common.tileentities;

import java.util.Random;

import ladysnake.dissolution.api.Soul;
import ladysnake.dissolution.api.SoulTypes;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityAncientTomb extends TileEntityLockableLoot implements ITickable {
	
    private NonNullList<ItemStack> chestContents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
    private Random rand;
	private int timeUntilSpawn;
	
	public TileEntityAncientTomb() {
		this.rand = new Random();
		this.timeUntilSpawn = rand.nextInt(2400);
		this.setLootTable(new ResourceLocation(Reference.MOD_ID, "ancient_tomb"), rand.nextLong());
	}

	@Override
	public void update() {
        if (!this.world.isRemote && this.world.getEntitiesWithinAABB(AbstractSoul.class, new AxisAlignedBB(this.pos).grow(50)).size() < 10) {
			if(timeUntilSpawn-- <= 0) {
				BlockPos spawnPos = this.pos.add(rand.nextGaussian(), Math.abs(rand.nextGaussian()), rand.nextGaussian());
				if(world.isAirBlock(spawnPos)) {
					EntityFleetingSoul soul = new EntityFleetingSoul(world, spawnPos.getX()+0.5, spawnPos.getY()+0.5, spawnPos.getZ()+0.5, new Soul(SoulTypes.BENIGN));
					world.spawnEntity(soul);
					this.timeUntilSpawn = world.rand.nextInt(2400);
				} else {
					this.timeUntilSpawn = world.rand.nextInt(400);
				}
			}
        }
	}

	@Override
	public int getSizeInventory() {
		return chestContents.size();
	}

	@Override
	public boolean isEmpty() {
		return chestContents.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "dissolution.container.ancient_tomb";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerChest(playerInventory, this, playerIn);
	}

	@Override
	public String getGuiID() {
		return "dissolution:ancient_tomb";
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.chestContents;
	}
	
	public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.chestContents = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

        if (!this.checkLootAndRead(compound))
        {
            ItemStackHelper.loadAllItems(compound, this.chestContents);
        }

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (!this.checkLootAndWrite(compound))
        {
            ItemStackHelper.saveAllItems(compound, this.chestContents);
        }

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }

        return compound;
    }

}
