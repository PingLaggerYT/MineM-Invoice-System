package nl.rubixstudios.minem.invoices.util;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class NBTApiUtil {

    public static boolean hasItemData(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.hasKey(key);
    }

    public static void setItemDataInt(ItemStack itemStack, String key, int value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(key, value);
    }

    public static int getItemDataInt(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getInteger(key);
    }

    public static void setItemDataString(ItemStack itemStack, String key, String value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString(key, value);
    }

    public static String getItemDataString(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getString(key);
    }

    public static void setItemDataBoolean(ItemStack itemStack, String key, boolean value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setBoolean(key, value);
    }

    public static boolean getItemDataBoolean(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getBoolean(key);
    }

    public static void setItemDataDouble(ItemStack itemStack, String key, double value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setDouble(key, value);
    }

    public static double getItemDataDouble(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getDouble(key);
    }

    public static void setItemDataFloat(ItemStack itemStack, String key, float value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setFloat(key, value);
    }

    public static float getItemDataFloat(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getFloat(key);
    }

    public static void setItemDataLong(ItemStack itemStack, String key, long value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setLong(key, value);
    }

    public static long getItemDataLong(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getLong(key);
    }

    public static void setItemDataShort(ItemStack itemStack, String key, short value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setShort(key, value);
    }

    public static short getItemDataShort(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getShort(key);
    }

    public static void setItemDataByte(ItemStack itemStack, String key, byte value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setByte(key, value);
    }

    public static byte getItemDataByte(ItemStack itemStack, String key) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getByte(key);
    }

    public static void setItemDataByteArray(ItemStack itemStack, String key, byte[] value) {
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setByteArray(key, value);
    }
}
