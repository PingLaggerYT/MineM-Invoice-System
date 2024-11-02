package nl.rubixstudios.minem.invoices.util.item;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for retrieving string NBT data from ItemStacks.
 */
public class NBTItemUtil {

    /**
     * Retrieves a string NBT value from the specified ItemStack.
     *
     * @param itemStack The ItemStack to retrieve the NBT value from.
     * @param key       The key of the NBT value.
     * @return The string value associated with the key in the ItemStack's NBT data.
     */
    public static String getStringNBT(ItemStack itemStack, String key) {
        return NBTEditor.getString(itemStack, key);
    }

    /**
     * Retrieves an integer NBT value from the specified ItemStack.
     * @param itemStack The ItemStack to retrieve the NBT value from.
     * @param key The key of the NBT value.
     * @return The integer value associated with the key in the ItemStack's NBT data.
     */
    public static int getIntNBT(ItemStack itemStack, String key) {
        return NBTEditor.getInt(itemStack, key);
    }

    /**
     * Retrieves a boolean NBT value from the specified ItemStack.
     * @param clickedItem The ItemStack to retrieve the NBT value from.
     * @param key The key of the NBT value.
     * @return The boolean value associated with the key in the ItemStack's NBT data.
     */
    public static boolean getBooleanNBT(ItemStack clickedItem, String key) {
        return NBTEditor.getBoolean(clickedItem, key);
    }
}
