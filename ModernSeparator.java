import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JSeparator;

public class ModernSeparator extends JSeparator {
    public ModernSeparator() {
        super(JSeparator.HORIZONTAL);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(0, 8); // Thin separator height block
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Use theme border color
        g2.setColor(ThemeManager.isDarkMode() ? new java.awt.Color(0x334155) : new java.awt.Color(0xE5E7EB));
        g2.drawLine(8, 3, getWidth() - 8, 3);
        g2.dispose();
    }
}
