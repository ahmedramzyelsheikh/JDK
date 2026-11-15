import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * The same invoice logic BEFORE and AFTER refactoring. Both produce identical results (checked in main),
 * which is the point: refactoring changes structure, never behaviour.
 * Smells in "before": long method, magic numbers, type codes with if/else, primitive obsession,
 * cryptic names, mixed responsibilities (calculation + formatting).
 * Run: java RefactoringKata.java
 */
public class RefactoringKata {

    // =============================== BEFORE ===============================
    static String calc(List<String[]> l, int t) {          // t: 1 = regular, 2 = student, 3 = employee
        double s = 0;
        for (String[] x : l) {
            double p = Double.parseDouble(x[1]);
            int q = Integer.parseInt(x[2]);
            s = s + p * q;
        }
        if (t == 2) {
            s = s - s * 0.15;
        } else if (t == 3) {
            s = s - s * 0.3;
        }
        if (s > 500) {
            s = s - 25;
        }
        double tax = s * 0.14;
        return "Total: " + String.format("%.2f", s + tax);
    }

    // =============================== AFTER ================================
    enum CustomerType {
        REGULAR("0"), STUDENT("0.15"), EMPLOYEE("0.30");

        private final BigDecimal discountRate;

        CustomerType(String rate) {
            this.discountRate = new BigDecimal(rate);
        }

        BigDecimal applyDiscount(BigDecimal amount) {
            return amount.subtract(amount.multiply(discountRate));
        }
    }

    record InvoiceLine(String product, BigDecimal unitPrice, int quantity) {
        BigDecimal total() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    static final class InvoiceCalculator {
        static final BigDecimal BIG_ORDER_THRESHOLD = new BigDecimal("500");
        static final BigDecimal BIG_ORDER_REBATE = new BigDecimal("25");
        static final BigDecimal VAT_RATE = new BigDecimal("0.14");

        BigDecimal totalWithVat(List<InvoiceLine> lines, CustomerType customerType) {
            BigDecimal net = applyBigOrderRebate(customerType.applyDiscount(subtotal(lines)));
            return net.add(net.multiply(VAT_RATE)).setScale(2, RoundingMode.HALF_UP);
        }

        private BigDecimal subtotal(List<InvoiceLine> lines) {
            return lines.stream().map(InvoiceLine::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal applyBigOrderRebate(BigDecimal amount) {
            return amount.compareTo(BIG_ORDER_THRESHOLD) > 0 ? amount.subtract(BIG_ORDER_REBATE) : amount;
        }
    }

    static String formatTotal(BigDecimal total) {          // presentation separated from calculation
        return "Total: " + total;
    }

    public static void main(String[] args) {
        List<String[]> rawLines = List.of(new String[] {"laptop", "450", "1"}, new String[] {"mouse", "25", "3"});
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("laptop", new BigDecimal("450"), 1),
                new InvoiceLine("mouse", new BigDecimal("25"), 3));

        InvoiceCalculator calculator = new InvoiceCalculator();
        int typeCode = 1;
        for (CustomerType type : CustomerType.values()) {
            String before = calc(rawLines, typeCode++);
            String after = formatTotal(calculator.totalWithVat(lines, type));
            System.out.printf("%-8s before: %-16s after: %-16s same: %s%n", type, before, after, before.equals(after));
        }
    }
}
