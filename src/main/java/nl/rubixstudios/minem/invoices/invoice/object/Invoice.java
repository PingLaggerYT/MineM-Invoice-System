package nl.rubixstudios.minem.invoices.invoice.object;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class Invoice {

    private final int invoiceId;
    private InvoiceStatus invoiceStatus;
    private final UUID invoiceAuthor;
    private double invoiceAmount;
    private final String invoiceReason;
    private final long invoiceDateTime;

    private int daysToPay;
    private long paymentDateTime;
    private UUID paidBy;

    private String cancelReason;
    private long canceledOnDateTime;
    private UUID canceledBy;

    public Invoice(int invoiceId, InvoiceStatus invoiceStatus, UUID invoiceAuthor, double invoiceAmount, String invoiceReason, Date invoiceDate) {
        this.invoiceId = invoiceId;
        this.invoiceStatus = invoiceStatus;
        this.invoiceAuthor = invoiceAuthor;
        this.invoiceAmount = invoiceAmount;
        this.invoiceReason = invoiceReason;
        this.invoiceDateTime = invoiceDate.getTime();
    }

    public Date getDateToPay() {
        return new Date(this.invoiceDateTime + (this.daysToPay * 86400000L));
    }

    public String getDateToPayInString() {
        // format datetopay to dd-mm-yyyy ##:## PM
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        return formatter.format(this.getDateToPay());
    }

    public int getDaysLeft() {
        return (int) ((this.getDateToPay().getTime() - new Date().getTime()) / 86400000L);
    }

    public String getDateInvoicePaidToString() {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        return formatter.format(new Date(this.paymentDateTime));
    }

    public String getDateInvoiceCanceledToString() {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        return formatter.format(new Date(this.canceledOnDateTime));
    }
}
