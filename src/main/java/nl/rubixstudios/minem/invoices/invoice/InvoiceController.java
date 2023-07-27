package nl.rubixstudios.minem.invoices.invoice;

import lombok.Getter;
import nl.rubixstudios.minem.invoices.MineMInvoices;
import nl.rubixstudios.minem.invoices.data.Config;
import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.menu.InvoiceMenuButtons;
import nl.rubixstudios.minem.invoices.invoice.object.Invoice;
import nl.rubixstudios.minem.invoices.invoice.object.InvoiceStatus;
import nl.rubixstudios.minem.invoices.util.ColorUtil;
import nl.rubixstudios.minem.invoices.util.NBTApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class InvoiceController implements Listener {

    @Getter private static InvoiceController instance;
    private final InvoiceManager invoiceManager;

    public InvoiceController() {
        instance = this;

        this.invoiceManager = new InvoiceManager();

        Bukkit.getPluginManager().registerEvents(this, MineMInvoices.getInstance());
    }

    public void disable() {
        this.invoiceManager.disable();
    }

    public void openInvoiceMenu(Player player, OfflinePlayer targetPlayer, boolean openInvoice) {
        final InvoiceUser invoiceUser = invoiceManager.getOrCreateInvoiceUser(targetPlayer.getUniqueId());

        final String inventoryOf = "[" + targetPlayer.getName() + "]";
        final String section = openInvoice ? Language.getMessage("INVOICE.MENU.SECTIONS.OPEN") : Language.getMessage("INVOICE.MENU.SECTIONS.PAID");
        final Inventory invoiceMenu = Bukkit.createInventory(null, 54, ColorUtil.translate("&8&l» " + Language.getMessage("INVOICE.MENU.SECTIONS.INVOICE") + " &8| " + section + " " + inventoryOf));

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44) {
                if (i == 3) {
                    invoiceMenu.setItem(i, InvoiceMenuButtons.openInvoices());
                } else if (i == 5) {
                    invoiceMenu.setItem(i, InvoiceMenuButtons.paidInvoices());
                } else if (i == 48) {
                    invoiceMenu.setItem(i, InvoiceMenuButtons.previousPage());
                } else if (i == 49) {
                    invoiceMenu.setItem(i, InvoiceMenuButtons.close());
                } else if (i == 50) {
                    invoiceMenu.setItem(i, InvoiceMenuButtons.nextPage());
                } else {
                    invoiceMenu.setItem(i, InvoiceMenuButtons.glass());
                }
                continue;
            }

            List<Invoice> invoices;
            if (openInvoice) {
                invoices = invoiceUser.getInvoices().stream().filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.OPEN).collect(Collectors.toList());
            } else {
                invoices = invoiceUser.getInvoices().stream().filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.PAID
                        || invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED).collect(Collectors.toList());
            }

            int invoiceIndex = i - 9;

            if (!invoices.isEmpty() && invoiceIndex < invoices.size()) {
                invoiceMenu.setItem(i, InvoiceMenuButtons.invoice(invoices.get(invoiceIndex)));
            }
        }

        player.openInventory(invoiceMenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final InventoryView inventoryView = event.getView();

        final ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR)return;

        String inventoryName = inventoryView.getTitle();
        OfflinePlayer targetPlayer = null;

        if (inventoryName.contains("[")) {
            inventoryName = inventoryName.split("\\[")[1].split("]")[0];
            targetPlayer = Bukkit.getOfflinePlayer(inventoryName);
        }

        if (NBTApiUtil.hasItemData(itemStack, "status") && NBTApiUtil.getItemDataString(itemStack, "status").equalsIgnoreCase(InvoiceStatus.OPEN.toString())) {
            openInvoiceMenu(player, targetPlayer != null ? targetPlayer : player, true);
        } else if (NBTApiUtil.hasItemData(itemStack, "status") && NBTApiUtil.getItemDataString(itemStack, "status").equalsIgnoreCase(InvoiceStatus.PAID.toString())) {
            openInvoiceMenu(player, targetPlayer != null ? targetPlayer : player, false);
        } else if (NBTApiUtil.hasItemData(itemStack, "isClose")) {
            player.closeInventory();
        } else if (NBTApiUtil.hasItemData(itemStack, "invoiceId")) {
            if (targetPlayer != null && !player.getUniqueId().equals(targetPlayer.getUniqueId())) {
                player.sendMessage(Language.getMessage("INVOICE.CHECK.CANNOT_PAY_OTHERS_INVOICE"));
            } else {
                int invoiceId = NBTApiUtil.getItemDataInt(itemStack, "invoiceId");
                String codeStatus = invoiceManager.payInvoice(player.getUniqueId(), invoiceId, false);

                switch (codeStatus) {
                    case "INVOICE_NOT_FOUND":
                        player.sendMessage(Language.getMessage("INVOICE.CHECK.INVOICE_NOT_FOUND"));
                        break;
                    case "INVOICE_ALREADY_PAID":
                        player.sendMessage(Language.getMessage("INVOICE.CHECK.INVOICE_ALREADY_PAID"));
                        break;
                    case "NOT_ENOUGH_MONEY":
                        player.sendMessage(Language.getMessage("INVOICE.CHECK.NOT_ENOUGH_MONEY"));
                        break;
                    case "INVOICE_PAID":
                        player.sendMessage(Language.getMessage("INVOICE.CHECK.INVOICE_PAID")
                                .replace("%invoice_id%", String.valueOf(invoiceId)));
                        openInvoiceMenu(player, targetPlayer != null ? targetPlayer : player, true);
                        break;
                    case "INVOICE_AUTO_PAID":
                        player.sendMessage(Language.getMessage("INVOICE.CHECK.INVOICE_AUTO_PAID")
                                .replace("%invoice_id%", String.valueOf(invoiceId)));
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (player == null) return;
        if (!player.getInventory().contains(Material.BLAZE_POWDER)) return;

        final InvoiceUser invoiceUser = this.invoiceManager.getOrCreateInvoiceUser(player.getUniqueId());
        if (invoiceUser.getInvoices().isEmpty()) return;

        player.sendMessage(Language.getMessage("INVOICE.CHECK.OPEN_INVOICE_NOTIFY")
                .replace("%amount%", String.valueOf(invoiceUser.getInvoices().size()))
                .replace("%command%", "/%command_name% check"
                        .replace("%command_name%", Config.getMessage("COMMAND_NAME"))));
    }
}
