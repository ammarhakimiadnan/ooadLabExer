public class Subject {
    private String code;
    private char result;

    public Subject(String cd, char res) {
        this.code = cd;
        this.result = res;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public char getResult() {
        return result;
    }

    public void setResult(char result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Subject: " + code + " - " + result;
    }
}
