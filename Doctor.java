public class Doctor {
    private String doctorId;
    private String name;
    private String department;

    public Doctor(String doctorId, String name, String department) {
        this.doctorId = doctorId;
        this.name = name;
        this.department = department;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public void displayDoctor() {
        System.out.println("Doctor ID: " + doctorId);
        System.out.println("Name: " + name);
        System.out.println("Department: " + department);
    }
}
