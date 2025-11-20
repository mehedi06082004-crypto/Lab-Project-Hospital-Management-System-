import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HospitalManagementGUI {
    private static final Logger LOGGER = Logger.getLogger(HospitalManagementGUI.class.getName());
    private static final String DATA_FILE = "hospital_data.ser";

    // Application state (Model)
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Patient> patients = new ArrayList<>();

    // Swing components (View)
    private final JFrame frame = new JFrame("Hospital Management System");
    private final DefaultTableModel doctorTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Department"}, 0);
    private final DefaultTableModel patientTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Department", "Assigned Doctor", "Outstanding Bill"}, 0);

    // Constructor sets up UI and event wiring (Controller)
    public HospitalManagementGUI() {
        setupLookAndFeel();
        setupUI();
        loadDataInBackground();
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to set native look and feel", e);
        }
    }

    private void setupUI() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top: toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton addDoctorBtn = new JButton("Add Doctor");
        JButton addPatientBtn = new JButton("Add Patient");
        JButton assignBtn = new JButton("Assign Doctor");
        JButton billBtn = new JButton("Generate Bill");
        JButton saveBtn = new JButton("Save");
        toolbar.add(addDoctorBtn);
        toolbar.add(addPatientBtn);
        toolbar.add(assignBtn);
        toolbar.add(billBtn);
        toolbar.addSeparator();
        toolbar.add(saveBtn);

        root.add(toolbar, BorderLayout.NORTH);

        // Center: split pane with doctors and patients table
        JTable doctorTable = new JTable(doctorTableModel);
        JTable patientTable = new JTable(patientTableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane leftScroll = new JScrollPane(doctorTable);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Doctors"));
        JScrollPane rightScroll = new JScrollPane(patientTable);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Patients"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        split.setResizeWeight(0.4);
        root.add(split, BorderLayout.CENTER);

        // Bottom: search and status
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Patients by Department:"));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton clearSearchBtn = new JButton("Clear");
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn);
        bottom.add(searchPanel, BorderLayout.NORTH);

        JLabel status = new JLabel("Ready");
        bottom.add(status, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        // Wire button actions
        addDoctorBtn.addActionListener(e -> showAddDoctorDialog());
        addPatientBtn.addActionListener(e -> showAddPatientDialog());
        assignBtn.addActionListener(e -> showAssignDoctorDialog());
        billBtn.addActionListener(e -> showGenerateBillDialog());
        saveBtn.addActionListener(e -> saveDataInBackground(status));

        searchBtn.addActionListener(e -> filterPatientsByDepartment(searchField.getText().trim()));
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            refreshPatientTable();
        });

        // Double-click patient to view details
        patientTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = patientTable.getSelectedRow();
                    if (row >= 0) {
                        String pid = (String) patientTableModel.getValueAt(row, 0);
                        showPatientDetails(pid);
                    }
                }
            }
        });

        frame.setContentPane(root);
        frame.setSize(900, 560);
        frame.setLocationRelativeTo(null);
    }

    private void showAddDoctorDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();
        panel.add(new JLabel("Doctor ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Department:"));
        panel.add(deptField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Doctor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();
            if (validateIdNameDept(id, name, dept)) {
                synchronized (doctors) {
                    if (findDoctorById(id).isPresent()) {
                        JOptionPane.showMessageDialog(frame, "Doctor with this ID already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Doctor d = new Doctor(id, name, dept);
                    doctors.add(d);
                    doctorTableModel.addRow(new Object[]{d.getDoctorId(), d.getName(), d.getDepartment()});
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter valid non-empty values.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showAddPatientDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();
        panel.add(new JLabel("Patient ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Department:"));
        panel.add(deptField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Patient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();
            if (validateIdNameDept(id, name, dept)) {
                synchronized (patients) {
                    if (findPatientById(id).isPresent()) {
                        JOptionPane.showMessageDialog(frame, "Patient with this ID already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Patient p = new Patient(id, name, dept);
                    patients.add(p);
                    patientTableModel.addRow(new Object[]{p.getPatientId(), p.getName(), p.getDepartment(), p.getAssignedDoctorName(), p.getOutstandingBill()});
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter valid non-empty values.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showAssignDoctorDialog() {
        String pid = JOptionPane.showInputDialog(frame, "Enter Patient ID:");
        if (pid == null || pid.trim().isEmpty()) return;
        Optional<Patient> optP = findPatientById(pid.trim());
        if (!optP.isPresent()) {
            JOptionPane.showMessageDialog(frame, "Patient not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Patient p = optP.get();

        String[] docIds;
        synchronized (doctors) {
            if (doctors.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No doctors registered.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            docIds = doctors.stream().map(Doctor::getDoctorId).toArray(String[]::new);
        }

        String did = (String) JOptionPane.showInputDialog(frame, "Select Doctor:", "Assign Doctor", JOptionPane.PLAIN_MESSAGE, null, docIds, docIds[0]);
        if (did == null) return;
        Optional<Doctor> optD = findDoctorById(did);
        if (!optD.isPresent()) {
            JOptionPane.showMessageDialog(frame, "Doctor not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        p.assignDoctor(optD.get());
        refreshPatientTable();
    }

    private void showGenerateBillDialog() {
        String pid = JOptionPane.showInputDialog(frame, "Enter Patient ID:");
        if (pid == null || pid.trim().isEmpty()) return;
        Optional<Patient> optP = findPatientById(pid.trim());
        if (!optP.isPresent()) {
            JOptionPane.showMessageDialog(frame, "Patient not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Patient p = optP.get();

        String amtStr = JOptionPane.showInputDialog(frame, "Enter Bill Amount:");
        if (amtStr == null || amtStr.trim().isEmpty()) return;
        try {
            double amount = Double.parseDouble(amtStr);
            if (amount <= 0) throw new NumberFormatException("Amount must be positive");
            p.generateBill(amount);
            refreshPatientTable();
            JOptionPane.showMessageDialog(frame, "Bill generated successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid amount. Enter a positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void filterPatientsByDepartment(String dept) {
        if (dept == null || dept.isEmpty()) {
            refreshPatientTable();
            return;
        }
        DefaultTableModel model = patientTableModel;
        model.setRowCount(0);
        synchronized (patients) {
            for (Patient p : patients) {
                if (p.getDepartment().equalsIgnoreCase(dept)) {
                    model.addRow(new Object[]{p.getPatientId(), p.getName(), p.getDepartment(), p.getAssignedDoctorName(), p.getOutstandingBill()});
                }
            }
        }
    }

    private void refreshPatientTable() {
        DefaultTableModel model = patientTableModel;
        model.setRowCount(0);
        synchronized (patients) {
            for (Patient p : patients) {
                model.addRow(new Object[]{p.getPatientId(), p.getName(), p.getDepartment(), p.getAssignedDoctorName(), p.getOutstandingBill()});
            }
        }
    }

    private void refreshDoctorTable() {
        DefaultTableModel model = doctorTableModel;
        model.setRowCount(0);
        synchronized (doctors) {
            for (Doctor d : doctors) {
                model.addRow(new Object[]{d.getDoctorId(), d.getName(), d.getDepartment()});
            }
        }
    }

    private void showPatientDetails(String pid) {
        Optional<Patient> opt = findPatientById(pid);
        if (!opt.isPresent()) return;
        Patient p = opt.get();
        String message = String.format("Patient ID: %s\nName: %s\nDepartment: %s\nAssigned Doctor: %s\nOutstanding Bill: %.2f",
                p.getPatientId(), p.getName(), p.getDepartment(), p.getAssignedDoctorName(), p.getOutstandingBill());
        JOptionPane.showMessageDialog(frame, message, "Patient Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean validateIdNameDept(String id, String name, String dept) {
        return id != null && !id.isEmpty() && name != null && !name.isEmpty() && dept != null && !dept.isEmpty();
    }

    private Optional<Doctor> findDoctorById(String id) {
        synchronized (doctors) {
            return doctors.stream().filter(d -> d.getDoctorId().equals(id)).findFirst();
        }
    }

    private Optional<Patient> findPatientById(String id) {
        synchronized (patients) {
            return patients.stream().filter(p -> p.getPatientId().equals(id)).findFirst();
        }
    }

    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(frame, "Do you want to save changes before exit?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) return;
        if (option == JOptionPane.YES_OPTION) {
            saveDataInBackground(null);
        }
        System.exit(0);
    }

    // Persistence
    private void saveDataInBackground(JLabel statusLabel) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                    synchronized (doctors) {
                        synchronized (patients) {
                            DataBundle bundle = new DataBundle(doctors, patients);
                            oos.writeObject(bundle);
                            LOGGER.info("Data saved to " + DATA_FILE);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to save data", e);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Failed to save data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }

            @Override
            protected void done() {
                if (statusLabel != null) statusLabel.setText("Saved at " + java.time.LocalTime.now().withNano(0));
            }
        };
        worker.execute();
    }

    private void loadDataInBackground() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                File f = new File(DATA_FILE);
                if (!f.exists()) return null;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                    Object obj = ois.readObject();
                    if (obj instanceof DataBundle) {
                        DataBundle bundle = (DataBundle) obj;
                        synchronized (doctors) {
                            synchronized (patients) {
                                doctors.clear();
                                doctors.addAll(bundle.getDoctors());
                                patients.clear();
                                patients.addAll(bundle.getPatients());
                            }
                        }
                        LOGGER.info("Data loaded from " + DATA_FILE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to load data, starting fresh", e);
                }
                return null;
            }

            @Override
            protected void done() {
                refreshDoctorTable();
                refreshPatientTable();
                frame.setVisible(true);
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        // Simple login screen before showing the main UI
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog();
            boolean ok = login.showDialog();
            if (!ok) {
                System.exit(0);
            }
            new HospitalManagementGUI();
        });
    }

    // -------------------- Inner classes (Model) --------------------
    private static class Doctor implements Serializable {
        private final String doctorId;
        private final String name;
        private final String department;

        public Doctor(String doctorId, String name, String department) {
            this.doctorId = doctorId;
            this.name = name;
            this.department = department;
        }

        public String getDoctorId() { return doctorId; }
        public String getName() { return name; }
        public String getDepartment() { return department; }

        @Override
        public String toString() { return name + " (" + doctorId + ")"; }
    }

    private static class Patient implements Serializable {
        private final String patientId;
        private final String name;
        private final String department;
        private Doctor assignedDoctor; // transient linking to doctor object in memory
        private double outstandingBill = 0.0;

        public Patient(String patientId, String name, String department) {
            this.patientId = patientId;
            this.name = name;
            this.department = department;
        }

        public String getPatientId() { return patientId; }
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public double getOutstandingBill() { return outstandingBill; }

        public void assignDoctor(Doctor d) { this.assignedDoctor = d; }
        public void generateBill(double amount) { this.outstandingBill += amount; }
        public String getAssignedDoctorName() { return assignedDoctor == null ? "-" : assignedDoctor.getName(); }
    }

    // Simple bundle for serialization
    private static class DataBundle implements Serializable {
        private final List<Doctor> doctors;
        private final List<Patient> patients;

        public DataBundle(List<Doctor> doctors, List<Patient> patients) {
            // Create copies to avoid concurrency issues
            this.doctors = new ArrayList<>(doctors);
            this.patients = new ArrayList<>(patients);
        }

        public List<Doctor> getDoctors() { return doctors; }
        public List<Patient> getPatients() { return patients; }
    }

    // Lightweight login dialog
    private static class LoginDialog {
        private final JDialog dialog;
        private boolean success = false;

        public LoginDialog() {
            dialog = new JDialog((Frame) null, "Login", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.setBorder(new EmptyBorder(12, 12, 12, 12));

            JPanel fields = new JPanel(new GridLayout(0, 1, 6, 6));
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            fields.add(new JLabel("Username:"));
            fields.add(userField);
            fields.add(new JLabel("Password:"));
            fields.add(passField);
            p.add(fields, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton loginBtn = new JButton("Login");
            JButton cancelBtn = new JButton("Cancel");
            buttons.add(loginBtn);
            buttons.add(cancelBtn);
            p.add(buttons, BorderLayout.SOUTH);

            loginBtn.addActionListener(e -> {
                String u = userField.getText().trim();
                String pw = new String(passField.getPassword());
                if ("admin".equals(u) && "12345".equals(pw)) {
                    success = true;
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
            cancelBtn.addActionListener(e -> dialog.dispose());

            dialog.setContentPane(p);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
        }

        public boolean showDialog() {
            dialog.setVisible(true);
            return success;
        }
    }
}
