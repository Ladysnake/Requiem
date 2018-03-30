# DISSOLUTION 0.7

## The Undying Update

#### New Items:

- Soul (Glass) vial:
  - A vial made with a solid glass resulting of the smelting soul sand. Used to capture Will O' Wisps and Faeries.
  - Obtained when smelting soul sand in a furnace (1:1 ratio).
  - 16 empty vials can be stacked. Can't be stacked when containing something (wisp or faerie).
  - Replaces the glass jar.
  - Subcategories:
    - Will O' Wisp in a soul vial:
      - Obtained when right clicking a wisp with a soul vial in the main hand.
      - Can release the wisp by right clicking the bottle.
    - Faerie in a soul vial:
      - Obtained when right clicking a faerie with a soul vial in the main hand.
      - Can be used as an alternative to ghast tears for brewing (empty vial stays in ingredient slot when used, like dragon breath, empty vial destroyed if Quark installed and dragon breath brewing option is enabled).
      - Can release the faerie by right clicking the bottle.
    - Splinter:
      - Obtained when renaming a Will O' Wisp in a soul vial to Splinter in an anvil (case sensitive).
      - Right clicking the bottle will result in making a grey will o' wisp appear: Splinter. This will o' wisp will follow your player, even after your death, and can only be put back in a vial by its owner (right clicking on it with a vial in the main hand).
      - Tooltip: "You were an angel among the rats. May you find your way back to heaven. Rest in peace."
- Flawless diamond:
  - Obtained when mining a diamond ore (10% chance to drop for each diamond, meaning that fortune doesn't cause a diamond ore to drop solely flawless diamonds).
  - Used to craft the Undying gemstone.
  - Can be used as a normal diamond.
- Undying gemstone:
  - Crafted from a flawless diamond, a wisp in a vial and a ghast tear (shapeless).
  - Can be put in the Ethereal Focus in order to activate the altar structure.
  - Retains the person who crafted it, in order to attribute the altar it is used on.
  - When crafted, the gem is inactive. Inactive gemstones can be activated by right clicking on them, if the user has no attributed gemstone or altar active. If contrary, the gemstone will not activate.

#### Miscellaneous changes:

- Regeneration now works on undead players as Purification, the previous effect has consequently been removed.
  - Considering you need to replenish 20 human points to become a human again, Regeneration replenishes human points at the same rate it regenerates life (therefore, we consider 1hp regenerated is replaced by gaining 1 human point in undead mode).
- Faerie aura: Faeries will now affect the player when close to them, granting a constant regeneration effect when staying near. They no longer just touch the player and get weak (weak faeries have been removed). Regeneration stacks up to regeneration 5, at a rate of one level per Faerie.
- New configuration option, experimental:
  - Takes a Boolean.
  - If true, makes the Passeress shrine, possession system, soul state, and world join voices available.