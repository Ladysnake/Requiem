package ladysnake.dissolution.client.models.blocks;

import java.util.Set;

import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyModulePresent implements IUnlistedProperty<Set> {

	@Override
	public String getName() {
		return "AlchemyModules";
	}

	@Override
	public boolean isValid(Set value) {
		return true;
	}

	@Override
	public Class<Set> getType() {
		return Set.class;
	}

	@Override
	public String valueToString(Set value) {
		return "[Alchemy Module: " + value + ", " + value + "]";
	}

}
