import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/hospital_home_work";
    private static final String username = "root";
    private static final String password = "4544120";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("***ДОБРО ПОЖАЛОВАТЬ***");
                System.out.println("1. Просмотр записей");
                System.out.println("2. Добавить пациента");
                System.out.println("3. Просмотр пациентов");
                System.out.println("4. Просмотр врачей");
                System.out.println("5. Записаться на прием");

                System.out.println("6. Выход");

                System.out.print("Ваш выбор: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        listAppointments(connection);
                        break;
                    case 2:
                        addPatient(connection, scanner);
                        break;
                    case 3:
                        listPatients(connection);
                        break;
                    case 4:
                        listDoctors(connection);
                        break;
                    case 5:
                        makeAppointment(connection, scanner);
                        break;
                    case 6:
                        System.out.println("Выход из системы...");
                        connection.close();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неправильный ввод");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listAppointments(Connection connection) {
        String query = "select * from appointments";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            System.out.println();
            while (resultSet.next()) {
                int id = resultSet.getInt("appointments_id");
                String appointmentsDate = String.valueOf(resultSet.getDate("appointments_date"));
                int doctorId = resultSet.getInt("doctor_id");
                int patientId = resultSet.getInt("patient_id");
                System.out.printf("%d. %s, %d, %d\n", id, appointmentsDate, doctorId, patientId);
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean appointmentsExists(Connection connection, int appointmentsId) {
        String query = "select * from appointments where appointments_id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, appointmentsId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void makeAppointment(Connection connection, Scanner scanner) {
        String query = "insert into appointments(appointments_id, appointments_date, doctor_id, patient_id) values(?, ?, ?, ?)";
        System.out.print("Все назначения: ");
        listAppointments(connection);
        System.out.print("Список пациентов: ");
        listPatients(connection);
        System.out.print("Введите номер пациента: ");
        int patientId = scanner.nextInt();
        System.out.println("Выберите доктора");
        listDoctors(connection);
        System.out.print("Введите номер доктора: ");
        int doctorId = scanner.nextInt();
        System.out.print("Введите порядковый номер назначения: ");
        int id = scanner.nextInt();
        System.out.print("Введите дату приема: ");
        scanner.nextLine();
        Date appointmentDate = Date.valueOf(scanner.nextLine());
        if (appointmentsExists(connection, id)) {
            System.out.println("Запись с таким порядковым номером уже существует");
            return;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.setDate(2, appointmentDate);
            preparedStatement.setInt(3, doctorId);
            preparedStatement.setInt(4, patientId);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Прием назначен");
            } else {
                System.out.println("Не удалось назначить прием");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private static void listDoctors(Connection connection) {
        String query = "select * from doctors";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            System.out.println();
            while (resultSet.next()) {
                int id = resultSet.getInt("doctor_id");
                String doctorName = resultSet.getString("doctor_name");
                String doctorSpeciality = resultSet.getString("doctor_speciality");
                System.out.printf("%d. %s, %s\n", id, doctorName, doctorSpeciality);
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listPatients(Connection connection) {
        String query = "select * from patients";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            System.out.println();
            while (resultSet.next()) {
                int id = resultSet.getInt("patient_id");
                String patientName = resultSet.getString("patient_name");
                int patientAge = resultSet.getInt("patient_age");
                System.out.printf("%d. %s, %d лет\n", id, patientName, patientAge);
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addPatient(Connection connection, Scanner scanner) {
        String query = "insert into patients(patient_name, patient_age) values(?, ?)";
        try {
            scanner.nextLine();
            System.out.print("Введите имя пациента: ");
            String patientName = scanner.nextLine();
            System.out.print("Введите возраст пациента: ");
            int patientAge = scanner.nextInt();

            if (patientExists(connection, patientName, patientAge)) {
                System.out.println("Пациент уже числится в базе данных");
                return;
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, patientName);
            preparedStatement.setInt(2, patientAge);

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Пациент успешно добавлен");
            } else {
                System.out.println("Не удалось добавить пациента");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean patientExists(Connection connection, String patientName, int patientAge) {
        String query = "select * from patients where patient_name = ? and patient_age = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, patientName);
            preparedStatement.setInt(2, patientAge);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}