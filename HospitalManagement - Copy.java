import java.util.ArrayList;
import java.util.Scanner;

public class HospitalManagement {
    private static ArrayList<Doctor> doctors = new ArrayList<>();
    private static ArrayList<Patient> patients = new ArrayList<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 🔐 Login Section
        boolean loggedIn = false;
        int attempts = 3;

        while (attempts > 0 && !loggedIn) {
            loggedIn = login(sc);
            if (!loggedIn) {
                attempts--;
                System.out.println("Attempts left: " + attempts);
                if (attempts == 0) {
                    System.out.println("Too many failed attempts. Exiting program...");
                    sc.close();
                    return;
                }
            }
        }

        // Main Menu
        int choice;
        do {
            System.out.println("\n--- Hospital Management System ---");
            System.out.println("1. Add Doctor");
            System.out.println("2. Add Patient");
            System.out.println("3. Assign Doctor to Patient");
            System.out.println("4. Generate Bill");
            System.out.println("5. Show Patient List by Department");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addDoctor(sc);
                    break;
                case 2:
                    addPatient(sc);
                    break;
                case 3:
                    assignDoctorToPatient(sc);
                    break;
                case 4:
                    generateBill(sc);
                    break;
                case 5:
                    showPatientsByDepartment(sc);
                    break;
                case 6:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        } while (choice != 6);

        sc.close();
    }

    // 🔐 Login Method
    private static boolean login(Scanner sc) {
        String username = "admin";
        String password = "12345";

        System.out.println("\n===== Login =====");
        System.out.print("Enter Username: ");
        String userInput = sc.nextLine();

        System.out.print("Enter Password: ");
        String passInput = sc.nextLine();

        if (userInput.equals(username) && passInput.equals(password)) {
            System.out.println("\n✅ Login Successful!\n");
            return true;
        } else {
            System.out.println("\n❌ Invalid username or password.\n");
            return false;
        }
    }

    // ➕ Add Doctor
    private static void addDoctor(Scanner sc) {
        System.out.print("Enter Doctor ID: ");
        String id = sc.nextLine();
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Department: ");
        String dept = sc.nextLine();
        doctors.add(new Doctor(id, name, dept));
        System.out.println("Doctor added successfully!");
    }

    // ➕ Add Patient
    private static void addPatient(Scanner sc) {
        System.out.print("Enter Patient ID: ");
        String id = sc.nextLine();
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Department: ");
        String dept = sc.nextLine();
        patients.add(new Patient(id, name, dept));
        System.out.println("Patient added successfully!");
    }

    // 🔁 Assign Doctor to Patient
    private static void assignDoctorToPatient(Scanner sc) {
        System.out.print("Enter Patient ID: ");
        String pid = sc.nextLine();
        System.out.print("Enter Doctor ID: ");
        String did = sc.nextLine();

        Patient patient = null;
        Doctor doctor = null;

        for (Patient p : patients) {
            if (p.getPatientId().equals(pid)) {
                patient = p;
                break;
            }
        }

        for (Doctor d : doctors) {
            if (d.getDoctorId().equals(did)) {
                doctor = d;
                break;
            }
        }

        if (patient != null && doctor != null) {
            patient.assignDoctor(doctor);
        } else {
            System.out.println("Invalid Patient ID or Doctor ID!");
        }
    }

    // 💰 Generate Bill
    private static void generateBill(Scanner sc) {
        System.out.print("Enter Patient ID: ");
        String pid = sc.nextLine();
        System.out.print("Enter Bill Amount: ");
        double amount = sc.nextDouble();
        sc.nextLine(); // consume newline

        for (Patient p : patients) {
            if (p.getPatientId().equals(pid)) {
                p.generateBill(amount);
                return;
            }
        }
        System.out.println("Patient not found!");
    }

    // 📋 Show Patients by Department
    private static void showPatientsByDepartment(Scanner sc) {
        System.out.print("Enter Department: ");
        String dept = sc.nextLine();
        System.out.println("Patients in " + dept + " Department:");
        for (Patient p : patients) {
            if (p.getDepartment().equalsIgnoreCase(dept)) {
                p.displayPatient();
                System.out.println("----------------");
            }
        }
    }
}
