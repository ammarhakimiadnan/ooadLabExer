import java.util.ArrayList;

public class Student extends Person {
    private ArrayList<Subject> subjecttaken = new ArrayList<Subject>();

    public Student(String fn, String ln) {
        super(fn, ln);
    }

    public void remSubject(String scode) {
        for (int i = 0; i < subjecttaken.size(); i++) {
            if (subjecttaken.get(i).getCode().equals(scode)) {
                subjecttaken.remove(i);
                System.out.println("Subject " + scode + " removed");
                return;
            }
        }
        System.out.println("Subject " + scode + " not found");
    }

    public void addSubject(String cd, char res) {
        subjecttaken.add(new Subject(cd, res));
    }

    public void printTranscript() {
        for (Subject s : subjecttaken) {
            System.out.println(s);
        }
    }

    @Override
    public String toString() {
        return "Student name: " + getName();
    }

    // Uncomment main below to test Student class on its own
    public static void main(String[] a) {
        Student a1 = new Student("Ali", "Baba");
        System.out.println(a1.toString());
        a1.addSubject("ABC123", 'A');
        a1.addSubject("XYZ987", 'C');
        a1.printTranscript();
        a1.remSubject("ABC123");
        a1.printTranscript();
        System.out.println(a1);
    }
}
