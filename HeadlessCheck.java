import java.awt.GraphicsEnvironment;
public class HeadlessCheck {
    public static void main(String[] args) {
        System.out.println("Is Headless: " + GraphicsEnvironment.isHeadless());
        try {
            new javax.swing.JFrame().dispose();
            System.out.println("Successfully created a JFrame");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
