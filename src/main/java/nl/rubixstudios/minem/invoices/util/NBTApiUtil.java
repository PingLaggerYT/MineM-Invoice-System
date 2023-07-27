package nl.rubixstudios.minem.invoices.util;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class NBTApiUtil {

    public static boolean hasItemData(ItemStack itemStack, String key) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.hasKey(key);
    }

    public static void setItemDataInt(ItemStack itemStack, String key, int value) {
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(key, value);
        nbtItem.applyNBT(itemStack);
    }

    public static int getItemDataInt(ItemStack itemStack, String key) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getInteger(key);
    }

    public static void setItemDataString(ItemStack itemStack, String key, String value) {
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString(key, value);
        nbtItem.applyNBT(itemStack);
    }

    public static String getItemDataString(ItemStack itemStack, String key) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getString(key);
    }

    public static void setItemDataBoolean(ItemStack itemStack, String key, boolean value) {
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setBoolean(key, value);
        nbtItem.applyNBT(itemStack);
    }

    public static boolean getItemDataBoolean(ItemStack itemStack, String key) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getBoolean(key);
    }
}
