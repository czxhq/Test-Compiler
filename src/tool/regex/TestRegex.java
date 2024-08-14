package tool.regex;

public class TestRegex {
    public static void main(String[] args) {
        String con = "++";
        try {
            System.out.println(RegexProcessor.process(con));
        } catch (RegexException e) {
            e.print();
        }
    }
}
