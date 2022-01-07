package cc.ricecx.ricestats.core.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;

//
// NMS Reflection stuff
//


public class NmsOperations {
    private static final Logger logger = Logger.getLogger("VillagerTradeListener.NmsOperations");
    private static final boolean allClassesAndMethodsOK;
    private static final String nmsVersionString;
    private static Method obcCraftAbstractVillager_getHandle;
    private static Method nmsEntityVillagerAbstract_getOffers;
    private static Method nmsMerchantRecipe_getSpecialPrice;
    private static Method nmsMerchantRecipe_getDemand;
    private static Method nmsMerchantRecipe_setSpecialPrice;
    private static Field nmsMerchantRecipe_demandField;
    private static Method nmsMerchantRecipe_getBuyItem1;
    private static Method obcCraftItemStack_asBukkitCopy;

    static {
        String nmsPackageName = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersionString = nmsPackageName.substring(nmsPackageName.lastIndexOf('.') + 1) + '.';
        boolean ok;
        try {
            // returns nms.EntityVillagerAbstract
            obcCraftAbstractVillager_getHandle = getMethod(getOBCClass("entity.CraftAbstractVillager"), "getHandle");
            // returns nms.MerchantRecipeList (extends ArrayList<MerchantRecipe>)
            // 1.18.1 > net.minecraft.world.entity.npc
            nmsEntityVillagerAbstract_getOffers = getMethod(getNMSFull("net.minecraft.world.entity.npc.EntityVillagerAbstract"), "fA");

            // net.minecraft.world.item.trading
            Class<?> merchantRecipeClazz = getNMSFull("net.minecraft.world.item.trading.MerchantRecipe");
            nmsMerchantRecipe_getSpecialPrice = getMethod(merchantRecipeClazz, "m");  // -> int
            nmsMerchantRecipe_getDemand = getMethod(merchantRecipeClazz, "k");  // -> int
            nmsMerchantRecipe_setSpecialPrice = getMethod(merchantRecipeClazz, "b", int.class);
            nmsMerchantRecipe_demandField = getField(merchantRecipeClazz, "h");
            nmsMerchantRecipe_getBuyItem1 = getMethod(merchantRecipeClazz, "a");  // -> nms.ItemStack
            // net.minecraft.world.item
            obcCraftItemStack_asBukkitCopy = getMethod(getOBCClass("inventory.CraftItemStack"), "asBukkitCopy", getNMSFull("net.minecraft.world.item.ItemStack"));  // STATIC -> ItemStack
            ok = true;
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException ex) {
            ok = false;
            logger.severe("Failure while reflecting: " + ex.getMessage());
            ex.printStackTrace();
        }
        allClassesAndMethodsOK = ok;
    }

    public static void checkAllClassesAndMethodsOK() throws InvalidNmsOperationsState {
        if (!allClassesAndMethodsOK) throw new InvalidNmsOperationsState(
                "NmsOperations: Some classes or methods were not successfully loaded!");
    }

    public static Class<?> getNMSFull(final String nmsClassName) throws ClassNotFoundException {

        return Class.forName(nmsClassName);
    }
    public static Class<?> getNMSClass(final String nmsClassName) throws ClassNotFoundException {
        String clazzName = "net.minecraft.server." + nmsVersionString + nmsClassName;
        return Class.forName(clazzName);
    }

    public static Class<?> getOBCClass(final String obcClassName) throws ClassNotFoundException {
        String clazzName = "org.bukkit.craftbukkit." + nmsVersionString + obcClassName;
        return Class.forName(clazzName);
    }

    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... params) throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(methodName, params);
        if (!method.isAccessible()) method.setAccessible(true);
        return method;
    }

    public static Field getField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        if (!field.isAccessible()) field.setAccessible(true);
        return field;
    }

    /**
     * Produces a price adjusted ItemStack of the first input item after player-villager reputation, HotV,
     * and demand are calculated. Simply put, the result reflects the price actually being asked by the villager
     * for the first input item.
     *
     * Notes:
     *   Price adjustments only occur when the villager is actively engaged in a trade with a player!
     *   Only the first ingredient is ever discounted - that's why there is no getPriceAdjustedIngredient2.
     * @param villager owning villager
     * @param offerIndex index of the offer/recipe;
     * @return Price adjusted ItemStack
     */
    public static ItemStack getPriceAdjustedIngredient1(final AbstractVillager villager, final int offerIndex) throws InvalidNmsOperationsState {
        checkAllClassesAndMethodsOK();
        try {
            final Object nmsAbstractVillager = obcCraftAbstractVillager_getHandle.invoke(villager);
            final Object nmsOffer = ((ArrayList)nmsEntityVillagerAbstract_getOffers.invoke(nmsAbstractVillager)).get(offerIndex);
            final Object nmsItemStack1 = nmsMerchantRecipe_getBuyItem1.invoke(nmsOffer);
            return (ItemStack)obcCraftItemStack_asBukkitCopy.invoke(null, nmsItemStack1);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /** This is the amount the price is adjusted by - it accounts for player-villager reputation scores and HotV effect.
     * NOTICE: this value will always be zero, unless the villager is engaged in a trade.*/
    public static int getOfferSpecialPriceDiff(final AbstractVillager villager, final int offerIndex) throws InvalidNmsOperationsState{
        checkAllClassesAndMethodsOK();
        try {
            final Object nmsAbstractVillager = obcCraftAbstractVillager_getHandle.invoke(villager);
            final Object nmsOffer = ((ArrayList)nmsEntityVillagerAbstract_getOffers.invoke(nmsAbstractVillager)).get(offerIndex);
            return (Integer)nmsMerchantRecipe_getSpecialPrice.invoke(nmsOffer);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new InvalidNmsOperationsState("Reflection failure while attempting to fetch offer specialPriceDiff", ex);
        }
    }

    /** The demand value affects pricing when it is GT zero. */
    public static int getOfferDemand(final AbstractVillager villager, final int offerIndex) throws InvalidNmsOperationsState {
        checkAllClassesAndMethodsOK();
        try {
            final Object nmsAbstractVillager = obcCraftAbstractVillager_getHandle.invoke(villager);
            final Object nmsOffer = ((ArrayList) nmsEntityVillagerAbstract_getOffers.invoke(nmsAbstractVillager)).get(offerIndex);
            return (Integer) nmsMerchantRecipe_getDemand.invoke(nmsOffer);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new InvalidNmsOperationsState("Reflection failure while attempting to fetch offer demand", ex);
        }
    }

    //
    // Bonus functions...
    // These functions are NOT useful for VillagerTradeEvent handling, but can be used in InventoryOpenEvent
    // handling to affect the price the player sees and pays.
    //

    /**
     * Setting the special price diff to a negative value will result in discounted pricing, setting it positive
     * will result in inflated pricing and setting it to zero will result in default pricing.
     */
    public static boolean setOfferSpecialPriceDiff(final AbstractVillager villager, final int offerIndex, final int specialPriceDiff) {
        checkAllClassesAndMethodsOK();
        try {
            final Object nmsAbstractVillager = obcCraftAbstractVillager_getHandle.invoke(villager);
            final Object nmsOffer = ((ArrayList)nmsEntityVillagerAbstract_getOffers.invoke(nmsAbstractVillager)).get(offerIndex);
            nmsMerchantRecipe_setSpecialPrice.invoke(nmsOffer, specialPriceDiff);
            return true;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public static boolean setOfferDemand(final AbstractVillager villager, final int offerIndex, final int demand) {
        checkAllClassesAndMethodsOK();
        try {
            final Object nmsAbstractVillager = obcCraftAbstractVillager_getHandle.invoke(villager);
            final Object nmsOffer = ((ArrayList)nmsEntityVillagerAbstract_getOffers.invoke(nmsAbstractVillager)).get(offerIndex);
            nmsMerchantRecipe_demandField.set(nmsOffer, demand);
            return true;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
