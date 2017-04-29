package ladysnake.tartaros.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemScytheIron extends ItemSword {
	
	protected static Map<Material, Float> effectiveBlocks = new HashMap<Material, Float>();
	
	static {
		effectiveBlocks.put(Material.PLANTS, 2.0f);
		effectiveBlocks.put(Material.VINE, 2.0f);
		effectiveBlocks.put(Material.LEAVES, 1.5f);
	}

	public ItemScytheIron() {
		super(ToolMaterial.IRON);
		this.setUnlocalizedName(Reference.Items.SCYTHE_IRON.getUnlocalizedName());
        this.setRegistryName(Reference.Items.SCYTHE_IRON.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
        this.setMaxStackSize(1);
        this.setMaxDamage(50);
	}
	
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		return !entityLiving.getHeldItemOffhand().isEmpty();
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		stack.damageItem(1, attacker);
		return !attacker.getHeldItemOffhand().isEmpty();
	}
	
	@Override
	public float getDamageVsEntity() {
		return super.getDamageVsEntity();
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        return !player.getHeldItemOffhand().isEmpty();
    }
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add("Two-handed");
	}
	
	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		Material material = state.getMaterial();
        return effectiveBlocks.get(material) == null ? 0.8F : effectiveBlocks.get(material);
	}
	
	@Override
	public int getItemEnchantability()
    {
        return super.getItemEnchantability();
    }
	
	
}
