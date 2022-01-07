package cc.ricecx.ricestats.core.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Performs all of the logic needed to trigger the, cancelable, custom VillagerTradeListener.VillagerTradeEvent.
 * https://www.spigotmc.org/threads/adding-a-cancellable-villagertradeevent.411963/
 */
public class VillagerTradeListener implements Listener {

    private static final Set<InventoryAction> purchaseSingleItemActions;

    static {
        // Each of these action types are single order purchases that do not require free inventory space to satisfy.
        // I.e. they stack up on the cursor (hover under the mouse).
        purchaseSingleItemActions = new HashSet<>();
        purchaseSingleItemActions.addAll(List.of(
                InventoryAction.PICKUP_ONE,
                InventoryAction.PICKUP_ALL,
                InventoryAction.PICKUP_HALF,
                InventoryAction.PICKUP_SOME,
                InventoryAction.DROP_ONE_SLOT,
                InventoryAction.DROP_ALL_SLOT,
                InventoryAction.HOTBAR_SWAP));
    }

    /**
     * Because there is no Inventory:clone method
     */
    public static class InventorySnapshot implements InventoryHolder {
        Inventory inventory;

        public InventorySnapshot(Inventory inv) {
            ItemStack[] source = inv.getStorageContents();
            inventory = Bukkit.createInventory(this, source.length, "Snapshot");
            for (int i = 0; i < source.length; i++) {
                inventory.setItem(i, source[i] != null ? source[i].clone() : null);
            }
        }

        public InventorySnapshot(int size) {
            inventory = Bukkit.createInventory(this, size, "Snapshot");
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    public static class VillagerTradeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        final HumanEntity player;
        final AbstractVillager villager;
        final MerchantRecipe recipe;
        final int offerIndex;
        final int orders;
        final int ingredientOneDiscountedPrice;
        final int ingredientOneTotalAmount;
        final int ingredientTwoTotalAmount;
        final int amountPurchased;
        final int amountLost;
        boolean cancelled = false;

        public VillagerTradeEvent(HumanEntity player, AbstractVillager villager, MerchantRecipe recipe, int offerIndex,
                                  int orders, int ingredientOneDiscountedPrice,
                                  int amountPurchased, int amountLost) {
            this.player = player;
            this.villager = villager;
            this.recipe = recipe;
            this.offerIndex = offerIndex;
            this.orders = orders;
            this.ingredientOneDiscountedPrice = ingredientOneDiscountedPrice;
            this.amountPurchased = amountPurchased;
            this.amountLost = amountLost;

            ingredientOneTotalAmount = ingredientOneDiscountedPrice * orders;
            if (recipe.getIngredients().size() > 1) {
                ItemStack bb = recipe.getIngredients().get(1);
                ingredientTwoTotalAmount = bb.getType() != Material.AIR ? bb.getAmount() * orders : 0;
            } else {
                ingredientTwoTotalAmount = 0;
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Cancels the trade. Note the client will need to close and reopen the trade window to, for example, see that
         * a canceled trade is not sold out.
         */
        public void setCancelled(boolean toCancel) {
            cancelled = toCancel;
        }

        public HumanEntity getPlayer() {
            return player;
        }

        public AbstractVillager getVillager() {
            return villager;
        }

        public MerchantRecipe getRecipe() {
            return recipe;
        }

        /**
         * For the total count of the item purchased use {@code getAmountPurchased}.
         */
        public int getOrders() {
            return orders;
        }

        public int getOfferIndex() {
            return offerIndex;
        }

        /**
         * The actual amount of ingredient one charged for a single 'order'; e.g. the price after all
         * gossip/player-reputation and hero of the village effects have been applied.
         * Note that only the first ingredient is discounted by the villager.
         *
         * @return amount of item 1 each order actually cost.
         */
        public int getIngredientOneDiscountedPrice() {
            return ingredientOneDiscountedPrice;
        }

        /**
         * The total amount of {@code recipe.getIngredients().get(0)} spent
         */
        public int getIngredientOneTotalAmount() {
            return ingredientOneTotalAmount;
        }

        /**
         * The total amount of {@code recipe.getIngredients().get(1)} spent, or zero if no ingredient 2
         */
        public int getIngredientTwoTotalAmount() {
            return ingredientTwoTotalAmount;
        }

        @NotNull
        public String getBestNameForIngredientOne() {
            return bestNameFor(recipe.getIngredients().get(0));
        }

        @Nullable
        public String getBestNameForIngredientTwo() {
            if (recipe.getIngredients().size() > 1) {
                ItemStack stack = recipe.getIngredients().get(1);
                if (stack != null)
                    return bestNameFor(stack);
            }
            return null;
        }

        @NotNull
        public String getBestNameForResultItem() {
            return bestNameFor(recipe.getResult());
        }

        /**
         * Total amount of {@code recipe.getResult()} purchased. This value is the total count the player received.
         */
        public int getAmountPurchased() {
            return amountPurchased;
        }

        /**
         * When the player does not have inventory space for all of the items purchased they may drop or simply
         * be lost. I've seen both happen.
         */
        public int getAmountLost() {
            return amountLost;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return handlers;
        }

        @NotNull
        static private String bestNameFor(ItemStack stack) {
            if (stack == null) return "null";
            if (stack.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) stack.getItemMeta();
                if (meta != null && meta.hasTitle() && meta.getTitle() != null)
                    return ChatColor.stripColor(meta.getTitle());
                // TODO: fallback to finding enchants
            }
            if (stack.getItemMeta() != null) {
                ItemMeta meta = stack.getItemMeta();
                if (meta.hasDisplayName())
                    return ChatColor.stripColor(meta.getDisplayName());
            }
            return stack.getType().name();
        }

        public String bestNameForVillager() {
            if (villager.getCustomName() != null)
                return villager.getCustomName();
            return villager.getName();
        }

        public boolean isWanderingTraider() {
            return villager instanceof WanderingTrader;
        }

        @NotNull
        public Villager.Profession getVillagerProfession() {
            if (!(villager instanceof Villager)) return Villager.Profession.NONE;
            return ((Villager) villager).getProfession();
        }

        @Nullable
        public Villager.Type getVillagerType() {
            if (!(villager instanceof Villager)) return null;
            return ((Villager) villager).getVillagerType();
        }
    }

    public VillagerTradeListener(JavaPlugin owner) {
        owner.getServer().getPluginManager().registerEvents(this, owner);
    }

    /**
     * Calculates if the given stacks are of the exact same item thus could be stacked.
     *
     * @return true - stacks can be combined (assuming a max stack size > 1).
     */
    @Contract("null,null->true")
    public static boolean areStackable(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == null && b == null || (a == null && b.getType() == Material.AIR)
                || (b == null && a.getType() == Material.AIR)) return true;
        if (a == null || b == null || a.getType() != b.getType()) return false;
        if (a.getItemMeta() == null && b.getItemMeta() == null) return true;
        if (a.getItemMeta() == null || b.getItemMeta() == null) return false;
        return a.getItemMeta().equals(b.getItemMeta());
    }

    @EventHandler
    public void onInventoryClickEvent(final InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.NOTHING) return;
        if (event.getInventory().getType() != InventoryType.MERCHANT) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        // Currently (1.15.x) there are no non-AbstractVillager Merchants
        if (!(event.getInventory().getHolder() instanceof AbstractVillager)) return;

        final HumanEntity player = event.getWhoClicked();
        final AbstractVillager villager = (AbstractVillager) event.getInventory().getHolder();
        final MerchantInventory merchantInventory = (MerchantInventory) event.getInventory();
        final MerchantRecipe recipe = merchantInventory.getSelectedRecipe();

        if (recipe == null) return;
        final ItemStack discountedA = NmsOperations.getPriceAdjustedIngredient1(villager, merchantInventory.getSelectedRecipeIndex());
        final int discountedPriceA = discountedA.getAmount();
        final int maxUses = recipe.getMaxUses() - recipe.getUses();

        VillagerTradeEvent vtEvent = null;
        if (purchaseSingleItemActions.contains(event.getAction())) {
            vtEvent = new VillagerTradeEvent(
                    player, villager, recipe, merchantInventory.getSelectedRecipeIndex(),
                    1, discountedPriceA,
                    recipe.getResult().getAmount(), 0
            );
        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // This situation is where the player SHIFT+CLICKS the output item to buy multiple times at once.
            // Because this event is fired before any inventories have changed - we need to simulate what will happen
            // when the inventories update.
            InventorySnapshot playerSnap = new InventorySnapshot(player.getInventory());
            InventorySnapshot merchantSnap = new InventorySnapshot(9);
            for (int i = 0; i < 3; i++) {
                if (merchantInventory.getItem(i) != null)
                    merchantSnap.getInventory().setItem(i, merchantInventory.getItem(i).clone());
            }
            List<ItemStack> ingredients = recipe.getIngredients();
            ItemStack ma = merchantSnap.getInventory().getItem(0);
            ItemStack mb = merchantSnap.getInventory().getItem(1);
            ItemStack ra = ingredients.get(0);
            ItemStack rb = ingredients.size() > 1 ? ingredients.get(1) : null;
            if (rb != null && rb.getType() == Material.AIR) rb = null;

            if (areStackable(ra, mb)) {
                ItemStack tmp = ma;
                ma = mb;
                mb = tmp;
            }

            int amount = ma.getAmount() / discountedPriceA;
            if (rb != null && mb != null && rb.getType() != Material.AIR && mb.getType() != Material.AIR) {
                amount = Math.min(amount, mb.getAmount() / rb.getAmount());
            }
            amount = clamp(amount, 0, maxUses);

            // In order for "failed" below to be populated we need to compute each stack here
            int maxStackSize = recipe.getResult().getMaxStackSize();
            List<ItemStack> stacks = new ArrayList<>();
            int unaccounted = amount;
            while (unaccounted != 0) {
                ItemStack stack = recipe.getResult().clone();
                stack.setAmount(Math.min(maxStackSize, unaccounted));
                stacks.add(stack);
                unaccounted -= stack.getAmount();
            }
            HashMap<Integer, ItemStack> failed = playerSnap.getInventory().addItem(stacks.toArray(new ItemStack[0]));
            int loss = 0;
            if (!failed.isEmpty()) {
                // int requested = amount;
                for (ItemStack stack : failed.values()) {
                    amount -= stack.getAmount();
                }
                // If a partial result is delivered, the rest of it is dropped... or just lost... I've seen both happen
                int rem = amount % recipe.getResult().getAmount();
                if (rem != 0) {
                    loss = recipe.getResult().getAmount() - rem;
                    amount += loss;
                }
            }
            int orders = amount / recipe.getResult().getAmount();
            vtEvent = new VillagerTradeEvent(
                    player, villager, recipe, merchantInventory.getSelectedRecipeIndex(),
                    orders, discountedPriceA,
                    amount, loss
            );
        }
        if (vtEvent != null) {
            vtEvent.setCancelled(event.isCancelled());
            Bukkit.getPluginManager().callEvent(vtEvent);
            event.setCancelled(vtEvent.isCancelled());
        }
    }


    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}

