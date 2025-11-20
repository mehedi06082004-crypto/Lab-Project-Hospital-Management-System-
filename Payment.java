public class Payment {
    private Patient patient;
    private double amount;
    private boolean isPaid;

    public Payment(Patient patient, double amount) {
        this.patient = patient;
        this.amount = amount;
        this.isPaid = false;
    }

    public double getAmount() {
        return amount;
    }

    public void makePayment() {
        if (!isPaid) {
            isPaid = true;
            System.out.println("✅ Payment of Tk " + amount + " received from patient: " + patient.getPatientId());
        } else {
            System.out.println("⚠️ Payment already made.");
        }
    }

    public void displayPayment() {
        System.out.println("\n--- Payment Details ---");
        System.out.println("Patient ID: " + patient.getPatientId());
        System.out.println("Amount: Tk " + amount);
        System.out.println("Payment Status: " + (isPaid ? "Paid" : "Unpaid"));
    }
}
