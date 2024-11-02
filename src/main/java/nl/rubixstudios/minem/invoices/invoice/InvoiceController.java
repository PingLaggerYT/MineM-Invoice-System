package nl.rubixstudios.minem.invoices.invoice;

import com.cryptomorin.xseries.XMaterial;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import lombok.Getter;
import nl.rubixstudios.minem.invoices.MineMInvoices;
import nl.rubixstudios.minem.invoices.data.Config;
import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.menu.InvoiceMenuButtons;
import nl.rubixstudios.minem.invoices.invoice.object.Invoice;
import nl.rubixstudios.minem.invoices.invoice.object.InvoiceStatus;
import nl.rubixstudios.minem.invoices.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
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
                handleNonClickableItems(invoiceMenu, i);
                continue;
            }

            List<Invoice> invoices = filterInvoices(openInvoice, invoiceUser);

            int invoiceIndex = i - 9;
            if (!invoices.isEmpty() && invoiceIndex < invoices.size()) {
                invoiceMenu.setItem(i, InvoiceMenuButtons.invoice(invoices.get(invoiceIndex)));
            }
        }
        player.openInventory(invoiceMenu);
    }

    private void handleNonClickableItems(Inventory invoiceMenu, int index) {
        switch (index) {
            case 3:
                invoiceMenu.setItem(index, InvoiceMenuButtons.openInvoices());
                break;
            case 5:
                invoiceMenu.setItem(index, InvoiceMenuButtons.paidInvoices());
                break;
            case 48:
                invoiceMenu.setItem(index, InvoiceMenuButtons.previousPage());
                break;
            case 49:
                invoiceMenu.setItem(index, InvoiceMenuButtons.close());
                break;
            case 50:
                invoiceMenu.setItem(index, InvoiceMenuButtons.nextPage());
                break;
            default:
                invoiceMenu.setItem(index, InvoiceMenuButtons.glass());
                break;
        }
    }


    private List<Invoice> filterInvoices(boolean openInvoice, InvoiceUser invoiceUser) {
        return openInvoice ?
                invoiceUser.getInvoices().stream().filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.OPEN).collect(Collectors.toList()) :
                invoiceUser.getInvoices().stream().filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.PAID
                        || invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED).collect(Collectors.toList());
    }

    private OfflinePlayer getTargetPlayer(String inventoryName) {
        if (inventoryName.contains("[")) {
            String playerName = inventoryName.split("\\[")[1].split("]")[0];
            return Bukkit.getOfflinePlayer(playerName);
        }
        return null;
    }

    private void handleInvoicePayment(Player player, OfflinePlayer targetPlayer, ItemStack itemStack) {
        if (targetPlayer != null && !player.getUniqueId().equals(targetPlayer.getUniqueId())) {
            player.sendMessage(Language.getMessage("INVOICE.CHECK.CANNOT_PAY_OTHERS_INVOICE"));
        } else {
            int invoiceId = NBTEditor.getInt(itemStack, "invoiceId");
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (player == null || !player.getInventory().contains(XMaterial.BLAZE_POWDER.parseMaterial())) return;

        final InvoiceUser invoiceUser = this.invoiceManager.getOrCreateInvoiceUser(player.getUniqueId());
        if (invoiceUser.getInvoices().isEmpty()) return;

        player.sendMessage(Language.getMessage("INVOICE.CHECK.OPEN_INVOICE_NOTIFY")
                .replace("%amount%", String.valueOf(invoiceUser.getInvoices().size()))
                .replace("%command%", String.format("/%s check", Config.getString("COMMAND_NAME"))));
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null || itemStack.getType() == XMaterial.AIR.parseMaterial()) return;

        final String inventoryName = event.getView().getTitle();
        OfflinePlayer targetPlayer = getTargetPlayer(inventoryName);

        if (NBTEditor.contains(itemStack, "status")) {
            String status = NBTEditor.getString(itemStack, "status");
            event.setCancelled(true);
            if (status.equalsIgnoreCase(InvoiceStatus.OPEN.toString())) {
                openInvoiceMenu(player, targetPlayer != null ? targetPlayer : player, true);
            } else if (status.equalsIgnoreCase(InvoiceStatus.PAID.toString())) {
                openInvoiceMenu(player, targetPlayer != null ? targetPlayer : player, false);
            }
        } else if (NBTEditor.contains(itemStack, "isClose")) {
            player.closeInventory();
        } else if (NBTEditor.contains(itemStack, "invoiceId")) {
            event.setCancelled(true);
            handleInvoicePayment(player, targetPlayer, itemStack);
        } else if (NBTEditor.contains(itemStack, "isNextPage")
                || NBTEditor.contains(itemStack, "isPreviousPage")
                || NBTEditor.contains(itemStack, "isGlass")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            boolean isItemEnabled = Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_ITEM.ENABLED").equalsIgnoreCase("true");
            if (isItemEnabled) {
                final Material clickedItem = XMaterial.matchXMaterial(Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_ITEM.ITEM.MATERIAL")).get().parseMaterial();
                if (clickedItem != null) {
                    final ItemStack item = player.getInventory().getItemInMainHand();
                    if (item == null) return;

                    boolean needsPermission = Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_ITEM.PERMISSION.NEEDS_PERMISSION").equalsIgnoreCase("true");
                    if (needsPermission) {
                        if (!player.hasPermission(Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_ITEM.PERMISSION.PERMISSION"))) {
                            player.sendMessage(Language.getMessage("INVOICE.CHECK.NO_PERMISSION_TO_OPEN_INVOICE_MENU"));
                            return;
                        }
                    }

                    if (item.getType() == clickedItem) {
                        openInvoiceMenu(player, player, true);
                    }
                }
            }
        }

        boolean isBlockEnabled = Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_BLOCK.ENABLED").equalsIgnoreCase("true");
        if (isBlockEnabled) {
            final Material clickedBlock = XMaterial.matchXMaterial(Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_BLOCK.BLOCK.MATERIAL")).get().parseMaterial();
            if (clickedBlock != null) {
                final Block block = event.getClickedBlock();
                if (block == null) return;

                boolean needsPermission = Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_BLOCK.PERMISSION.NEEDS_PERMISSION").equalsIgnoreCase("true");
                if (needsPermission) {
                    if (!player.hasPermission(Config.getString("OPEN_INVOICE_MENU.RIGHT_CLICK_BLOCK.PERMISSION.PERMISSION"))) {
                        player.sendMessage(Language.getMessage("INVOICE.CHECK.NO_PERMISSION_TO_OPEN_INVOICE_MENU"));
                        return;
                    }
                }

                if (block.getType() == clickedBlock) {
                    openInvoiceMenu(player, player, true);
                    return;
                }
            }
        }
    }
}
