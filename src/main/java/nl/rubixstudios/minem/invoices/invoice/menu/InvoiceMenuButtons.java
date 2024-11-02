package nl.rubixstudios.minem.invoices.invoice.menu;

import com.cryptomorin.xseries.XMaterial;
import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.object.Invoice;
import nl.rubixstudios.minem.invoices.invoice.object.InvoiceStatus;
import nl.rubixstudios.minem.invoices.util.ColorUtil;
import nl.rubixstudios.minem.invoices.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InvoiceMenuButtons {

    public static ItemStack previousPage() {
        return new ItemBuilder(XMaterial.PAPER.parseMaterial())
                .setName(Language.getMessage("INVOICE.BUTTONS.PREVIOUS_PAGE"))
                .setNBT("isPreviousPage", true)
                .toItemStack();
    }

    public static ItemStack nextPage() {
        return new ItemBuilder(XMaterial.PAPER.parseMaterial())
                .setName(Language.getMessage("INVOICE.BUTTONS.NEXT_PAGE"))
                .setNBT("isNextPage", true)
                .toItemStack();
    }

    public static ItemStack close() {
        return new ItemBuilder(XMaterial.BARRIER.parseMaterial())
                .setName(Language.getMessage("INVOICE.BUTTONS.CLOSE"))
                .setNBT("isClose", true)
                .toItemStack();
    }

    public static ItemStack openInvoices() {
        return new ItemBuilder(XMaterial.PAPER.parseMaterial())
                .setName(Language.getMessage("INVOICE.BUTTONS.OPEN_INVOICES"))
                .setNBT("status", InvoiceStatus.OPEN.toString())
                .toItemStack();
    }

    public static ItemStack paidInvoices() {
        return new ItemBuilder(XMaterial.MAP.parseMaterial())
                .setName(Language.getMessage("INVOICE.BUTTONS.PAID_INVOICES"))
                .setNBT("status", InvoiceStatus.PAID.toString())
                .toItemStack();
    }

    public static ItemStack glass() {
        return new ItemBuilder(XMaterial.GLASS_PANE.parseMaterial())
                .setName(ColorUtil.translate(""))
                .setNBT("isGlass", true)
                .toItemStack();
    }


    public static ItemStack invoice(Invoice invoice) {
        final ItemBuilder itemBuilder = new ItemBuilder(XMaterial.PAPER.parseMaterial());
        itemBuilder.setName(Language.getMessage("INVOICE.BUTTONS.INVOICE.TITLE")
                .replace("%invoice_id%", String.valueOf(invoice.getInvoiceId()))
        );

        final List<String> lore = new ArrayList<>();

        Language.getMessageList("INVOICE.BUTTONS.INVOICE.LORE").forEach(line -> lore.add(ColorUtil.translate(line
                .replace("%sender%", Bukkit.getOfflinePlayer(invoice.getInvoiceAuthor()).getName())
                .replace("%status%", invoice.getInvoiceStatus().getPrefix())
                .replace("%price%", String.valueOf(invoice.getInvoiceAmount()))
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
            Language.getMessageList("INVOICE.BUTTONS.INVOICE.WHEN_NOT_PAID").forEach(line -> lore.add(ColorUtil.translate(line
                    .replace("%dateToPay%", invoice.getDateToPayInString())
            )));
        }

        itemBuilder.setLore(lore);
        itemBuilder.setNBT("invoiceId", invoice.getInvoiceId());

        return itemBuilder.toItemStack();
    }
}
