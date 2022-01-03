/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;

public class RemnantTradeOffer extends TradeOffer {
    private final TradeOffer vanillaOffer, demonOffer;
    private final boolean exorcism;
    boolean demonCustomer;

    public static RemnantTradeOffer fromNbt(NbtCompound compound) {
        TradeOffer vanillaOffer = new TradeOffer(compound.getCompound("vanilla_offer"));
        TradeOffer demonOffer = new TradeOffer(compound.getCompound("demon_offer"));
        boolean exorcism = compound.getBoolean("exorcism");
        RemnantTradeOffer offer = new RemnantTradeOffer(vanillaOffer, demonOffer, exorcism);
        // Need this specifically to sync trades in singleplayer
        if (compound.getBoolean("demon_customer")) offer.demonCustomer = true;
        return offer;
    }

    public RemnantTradeOffer(TradeOffer vanillaOffer, TradeOffer demonOffer, boolean exorcism) {
        super(vanillaOffer.getOriginalFirstBuyItem(), vanillaOffer.getSecondBuyItem(), vanillaOffer.getSellItem(), vanillaOffer.getUses(), vanillaOffer.getMaxUses(), vanillaOffer.getMerchantExperience(), vanillaOffer.getPriceMultiplier(), vanillaOffer.getDemandBonus());
        this.vanillaOffer = vanillaOffer;
        this.demonOffer = demonOffer;
        this.exorcism = exorcism;
    }

    private TradeOffer getDelegate() {
        return this.demonCustomer ? this.demonOffer : this.vanillaOffer;
    }

    public boolean isExorcism() {
        return this.demonCustomer && this.exorcism;
    }

    @Override
    public ItemStack getOriginalFirstBuyItem() {
        return getDelegate().getOriginalFirstBuyItem();
    }

    @Override
    public ItemStack getAdjustedFirstBuyItem() {
        return getDelegate().getAdjustedFirstBuyItem();
    }

    @Override
    public ItemStack getSecondBuyItem() {
        return getDelegate().getSecondBuyItem();
    }

    @Override
    public ItemStack getSellItem() {
        return getDelegate().getSellItem();
    }

    @Override
    public void updateDemandBonus() {
        getDelegate().updateDemandBonus();
    }

    @Override
    public ItemStack copySellItem() {
        return getDelegate().copySellItem();
    }

    @Override
    public int getUses() {
        return getDelegate().getUses();
    }

    @Override
    public void resetUses() {
        getDelegate().resetUses();
    }

    @Override
    public int getMaxUses() {
        return getDelegate().getMaxUses();
    }

    @Override
    public void use() {
        getDelegate().use();
    }

    @Override
    public int getDemandBonus() {
        return getDelegate().getDemandBonus();
    }

    @Override
    public void increaseSpecialPrice(int increment) {
        getDelegate().increaseSpecialPrice(increment);
    }

    @Override
    public void clearSpecialPrice() {
        getDelegate().clearSpecialPrice();
    }

    @Override
    public int getSpecialPrice() {
        return getDelegate().getSpecialPrice();
    }

    @Override
    public void setSpecialPrice(int specialPrice) {
        getDelegate().setSpecialPrice(specialPrice);
    }

    @Override
    public float getPriceMultiplier() {
        return getDelegate().getPriceMultiplier();
    }

    @Override
    public int getMerchantExperience() {
        return getDelegate().getMerchantExperience();
    }

    @Override
    public boolean isDisabled() {
        return getDelegate().isDisabled();
    }

    @Override
    public void disable() {
        getDelegate().disable();
    }

    @Override
    public boolean hasBeenUsed() {
        return getDelegate().hasBeenUsed();
    }

    @Override
    public boolean shouldRewardPlayerExperience() {
        return getDelegate().shouldRewardPlayerExperience();
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound whole = new NbtCompound();
        whole.putBoolean("requiem:demon_trade", true);
        whole.put("demon_offer", this.demonOffer.toNbt());
        whole.put("vanilla_offer", this.vanillaOffer.toNbt());
        whole.putBoolean("exorcism", this.exorcism);
        if (this.demonCustomer) whole.putBoolean("demon_customer", true);
        return whole;
    }

    @Override
    public boolean matchesBuyItems(ItemStack first, ItemStack second) {
        return getDelegate().matchesBuyItems(first, second);
    }

    @Override
    public boolean depleteBuyItems(ItemStack firstBuyStack, ItemStack secondBuyStack) {
        return getDelegate().depleteBuyItems(firstBuyStack, secondBuyStack);
    }

    public void setRemnant(boolean demon) {
        demonCustomer = demon;
    }
}
