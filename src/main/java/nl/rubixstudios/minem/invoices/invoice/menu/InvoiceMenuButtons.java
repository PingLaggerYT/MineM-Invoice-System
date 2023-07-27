package nl.rubixstudios.minem.invoices.invoice.menu;

import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.object.Invoice;
import nl.rubixstudios.minem.invoices.invoice.object.InvoiceStatus;
import nl.rubixstudios.minem.invoices.util.ColorUtil;
import nl.rubixstudios.minem.invoices.util.NBTApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InvoiceMenuButtons {

    public static ItemStack previousPage() {
        final ItemStack item = new ItemStack(Material.PAPER);
        final ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Language.getMessage("INVOICE.BUTTONS.PREVIOUS_PAGE"));
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataBoolean(item, "isPreviousPage", true);
        return item;
    }

    public static ItemStack nextPage() {
        final ItemStack item = new ItemStack(Material.PAPER);
        final ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Language.getMessage("INVOICE.BUTTONS.NEXT_PAGE"));
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataBoolean(item, "isNextPage", true);
        return item;
    }

    public static ItemStack close() {
        final ItemStack item = new ItemStack(Material.BARRIER);
        final ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Language.getMessage("INVOICE.BUTTONS.CLOSE"));
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataBoolean(item, "isClose", true);
        return item;
    }

    public static ItemStack openInvoices() {
        final ItemStack item = new ItemStack(Material.PAPER);
        final ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Language.getMessage("INVOICE.BUTTONS.OPEN_INVOICES"));
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataString(item, "status", InvoiceStatus.OPEN.toString());
        return item;
    }

    public static ItemStack paidInvoices() {
        final ItemStack item = new ItemStack(Material.MAP);
        final ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Language.getMessage("INVOICE.BUTTONS.PAID_INVOICES"));
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataString(item, "status", InvoiceStatus.PAID.toString());
        return item;
    }

    public static ItemStack glass() {
        final ItemStack item;
        if (isVersionBefore113()) {
            item = new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 7); // Gray Stained Glass Pane
        } else {
            item = new ItemStack(Material.getMaterial("GRAY_STAINED_GLASS_PANE"));
        }
        final ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ColorUtil.translate(""));
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataBoolean(item, "isGlass", true);
        return item;
    }

    private static boolean isVersionBefore113() {
        String version = Bukkit.getServer().getVersion();
        return version.contains("1.7") || version.contains("1.8") || version.contains("1.9") ||
                version.contains("1.10") || version.contains("1.11") || version.contains("1.12");
    }


    public static ItemStack invoice(Invoice invoice) {
        final ItemStack item = new ItemStack(Material.PAPER);
        final ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(Language.getMessage("INVOICE.BUTTONS.INVOICE.TITLE")
                .replace("%invoice_id%", String.valueOf(invoice.getInvoiceId()))
        );
        final List<String> lore = new ArrayList<>();

        Language.getMessageList("INVOICE.BUTTONS.INVOICE.LORE").forEach(line -> lore.add(ColorUtil.translate(line
                .replace("%sender%", Bukkit.getOfflinePlayer(invoice.getInvoiceAuthor()).getName())
                .replace("%status%", invoice.getInvoiceStatus().getPrefix())
                .replace("%amount%", String.valueOf(invoice.getInvoiceAmount()))
                .replace("%reason%", invoice.getInvoiceReason())
        )));

        if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            Language.getMessageList("INVOICE.BUTTONS.INVOICE.WHEN_PAID").forEach(line -> lore.add(ColorUtil.translate(line
                    .replace("%date%", invoice.getDateInvoicePaidToString())
                    .replace("%paidBy%", Bukkit.getOfflinePlayer(invoice.getPaidBy()).getName())
            )));
        } else if (invoice.getInvoiceStatus() == InvoiceStatus.AUTOPAID) {
            Language.getMessageList("INVOICE.BUTTONS.INVOICE.WHEN_AUTO_PAID").forEach(line -> {
                lore.add(ColorUtil.translate(line
                        .replace("%date%", invoice.getDateInvoicePaidToString())
                ));
            });
        } else if (invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED) {
            Language.getMessageList("INVOICE.BUTTONS.INVOICE.WHEN_CANCELLED").forEach(line -> lore.add(ColorUtil.translate(line
                    .replace("%date%", invoice.getDateInvoiceCanceledToString())
                    .replace("%reason%", invoice.getCancelReason())
                    .replace("%canceledBy%", Bukkit.getOfflinePlayer(invoice.getCanceledBy()).getName())
            )));
        } else {
            Language.getMessageList("INVOICE.BUTTONS.INVOICE.WHEN_NOT_PAID_YET").forEach(line -> lore.add(ColorUtil.translate(line
                    .replace("%dateToPay%", invoice.getDateToPayInString())
                    .replace("%daysLeft%", String.valueOf(invoice.getDaysLeft()))
            )));
        }

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        NBTApiUtil.setItemDataInt(item, "invoiceId", invoice.getInvoiceId());
        return item;
    }
}
