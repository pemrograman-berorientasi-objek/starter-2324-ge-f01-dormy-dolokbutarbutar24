package pbo.f01;

import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import pbo.f01.model.Dorm;
import pbo.f01.model.Student;

public class App {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("dormyPU");
    private static final EntityManager em = emf.createEntityManager();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("---")) break;
            processCommand(input);
        }
        scanner.close();
        em.close();
        emf.close();
    }

    private static void processCommand(String input) {
        String[] parts = input.split("#");
        switch (parts[0]) {
            case "dorm-add":
                addDorm(parts[1], Integer.parseInt(parts[2]), parts[3]);
                break;
            case "student-add":
                addStudent(parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]);
                break;
            case "display-all":
                displayAll();
                break;
            case "assign":
                assignStudentToDorm(parts[1], parts[2]);
                break;
            default:
                System.out.println("Unknown command: " + parts[0]);
        }
    }

    private static void addDorm(String name, int capacity, String gender) {
        em.getTransaction().begin();
        Dorm dorm = new Dorm(name, capacity, gender);
        em.persist(dorm);
        em.getTransaction().commit();
    }

    private static void addStudent(String id, String name, int entranceYear, String gender) {
        em.getTransaction().begin();
        Student student = new Student(id, name, entranceYear, gender);
        em.persist(student);
        em.getTransaction().commit();
    }

    private static void displayAll() {
        TypedQuery<Dorm> dormQuery = em.createQuery("SELECT d FROM Dorm d ORDER BY d.name", Dorm.class);
        List<Dorm> dorms = dormQuery.getResultList();
        for (Dorm dorm : dorms) {
            System.out.printf("%s|%s|%d|%d%n", dorm.getName(), dorm.getGender(), dorm.getCapacity(), dorm.getStudents().size());
            dorm.getStudents().stream()
                    .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                    .forEach(student -> System.out.printf("%s|%s|%d%n", student.getId(), student.getName(), student.getEntranceYear()));
        }
    }

    private static void assignStudentToDorm(String studentId, String dormName) {
        em.getTransaction().begin();
        Student student = em.find(Student.class, studentId);
        Dorm dorm = em.find(Dorm.class, dormName);
        if (student != null && dorm != null && dorm.getStudents().size() < dorm.getCapacity() && student.getGender().equals(dorm.getGender())) {
            student.setDorm(dorm);
            em.merge(student);
        }
        em.getTransaction().commit();
    }
}