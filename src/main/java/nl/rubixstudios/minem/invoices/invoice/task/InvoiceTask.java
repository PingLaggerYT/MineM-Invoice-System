package nl.rubixstudios.minem.invoices.invoice.task;

import nl.rubixstudios.minem.invoices.invoice.InvoiceManager;
import nl.rubixstudios.minem.invoices.invoice.InvoiceUser;
import nl.rubixstudios.minem.invoices.invoice.object.Invoice;
import nl.rubixstudios.minem.invoices.invoice.object.InvoiceStatus;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceTask extends BukkitRunnable {

    private final InvoiceManager invoiceManager;
    private long latestTimeSaved;

    public InvoiceTask(InvoiceManager invoiceManager) {
        this.invoiceManager = invoiceManager;
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - this.latestTimeSaved > 300000) {
            this.latestTimeSaved = System.currentTimeMillis();
            this.invoiceManager.saveInvoices(true);
        }

        if (this.invoiceManager.getInvoiceUsers().isEmpty()) return;

        for (InvoiceUser invoiceUser : this.invoiceManager.getInvoiceUsers()) {
            final List<Invoice> invoices = invoiceUser.getInvoices().stream().filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.OPEN).collect(Collectors.toList());
            if (invoices.isEmpty()) continue;

            for (Invoice invoice : invoices) {
                final Date currentDate = new Date();
                final Date dateToPay = invoice.getDateToPay();

                if (currentDate.after(dateToPay)) {
                    this.invoiceManager.payInvoice(invoiceUser.getPlayerId(), invoice.getInvoiceId(), true);
                }
            }
        }
    }
}
