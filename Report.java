
public class Report {
    private Patient patient;
    private String diagnosis;
    private String prescription;

    public Report(Patient patient, String diagnosis, String prescription) {
        this.patient = patient;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void displayReport() {
        System.out.println("\n--- Patient Report ---");
        System.out.println("Patient ID: " + patient.getPatientId());
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Prescription: " + prescription);
    }
}
