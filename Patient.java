public class Patient {
    private String patientId;
    private String name;
    private String department;
    private Doctor assignedDoctor;

    public Patient(String patientId, String name, String department) {
        this.patientId = patientId;
        this.name = name;
        this.department = department;
        this.assignedDoctor = null;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public void assignDoctor(Doctor doctor) {
        this.assignedDoctor = doctor;
        System.out.println("Doctor " + doctor.getName() + " assigned to patient " + name);
    }

    public void displayPatient() {
        System.out.println("Patient ID: " + patientId);
        System.out.println("Name: " + name);
        System.out.println("Department: " + department);
        if (assignedDoctor != null) {
            System.out.println("Assigned Doctor: " + assignedDoctor.getName());
        } else {
            System.out.println("Assigned Doctor: None");
        }
    }

    public void generateBill(double amount) {
        System.out.println("Bill for patient " + name + ": $" + amount);
    }
}
