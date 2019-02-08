import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Container implements ActionListener {

    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;
    private static JPanel BUTTON_PANEL;
    private static JButton NEXT;
    private static int button_level = 61;
    private static JFrame frame;
    // initialize the first page
    private static int PAGE_NUM = 0;

    private static bmpPanel BMP_PANEL;
    private static histogramPanel HISTOGRAM_PANEL;

    Container(int image_width, int image_height, Pixel[][] pixel_buffer) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;

        // set up the button
        NEXT = new JButton("NEXT PAGE");

        // set up bmp panel
        BMP_PANEL = new bmpPanel(image_width, image_height, pixel_buffer, PAGE_NUM);

        // "next" button panel
        BUTTON_PANEL = new JPanel();
        BUTTON_PANEL.setSize(new Dimension(image_width, image_height));
        BUTTON_PANEL.setBackground(Color.black);
        BUTTON_PANEL.add(NEXT);
        NEXT.addActionListener(this);

        frame = new JFrame("THE BMP IMAGE");
        frame.setSize(image_width, image_height + button_level);
        frame.add(BMP_PANEL, BorderLayout.CENTER);
        frame.add(BUTTON_PANEL, BorderLayout.PAGE_END);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        PAGE_NUM += 1;
        // if page 2: histogram of RGB channels
        if(PAGE_NUM % 5 == 1) {
            frame.remove(BMP_PANEL);
            HISTOGRAM_PANEL = new histogramPanel(image_width, image_height, pixel_buffer);
            frame.add(HISTOGRAM_PANEL, BorderLayout.CENTER);
            frame.revalidate();
        }
        else {
            frame.remove(BMP_PANEL);
            BMP_PANEL = new bmpPanel(image_width, image_height, pixel_buffer, PAGE_NUM);
            frame.add(BMP_PANEL, BorderLayout.CENTER);
            frame.revalidate();
        }
    }
}
