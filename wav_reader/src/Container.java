import javax.swing.*;
import java.awt.*;

public class Container extends JPanel {

    private static int panel_width;
    private static int panel_height;
    private static int num_frame;
    private static int max_sample;
    private static double[] left_channel_sample;
    private static int[] left_signed_sample;
    private static wavePanel wav;
    private static JPanel TEXT_PANEL;
    private static JLabel NUM_SAMPLE;
    private static JLabel MAX_SAMPLE;
    private static int TEXT_LEVEL = 50;

    Container(int panel_width, int panel_height, int num_frame, double[] left_channel_sample, int[] left_signed_sample) {

        this.panel_width = panel_width;
        this.panel_height = panel_height;
        this.num_frame = num_frame;
        this.left_channel_sample = left_channel_sample;
        this.left_signed_sample = left_signed_sample;

        // set up the wav panel
        wav = new wavePanel(panel_width, panel_height, num_frame, left_channel_sample);

        // get the maximum sample value
        getMaxSampleValue();

        // set up the text panel
        TEXT_PANEL = new JPanel();
        TEXT_PANEL.setBackground(Color.black);
        TEXT_PANEL.setLayout(new BoxLayout(TEXT_PANEL, BoxLayout.Y_AXIS));

        // set up labels
        NUM_SAMPLE = new JLabel("THE NUMBER OF SAMPLES: " + num_frame + " SAMPLES");
        NUM_SAMPLE.setForeground(Color.white);

        MAX_SAMPLE = new JLabel("THE MAXIMUM SAMPLE VALUE: " + max_sample + " VOLTAGES");
        MAX_SAMPLE.setForeground(Color.white);

        TEXT_PANEL.add(NUM_SAMPLE);
        TEXT_PANEL.add(MAX_SAMPLE);

        // set up the frame
        JFrame frame = new JFrame("THE WAVEFORM DISPLAY");
        frame.setSize(panel_width, panel_height + TEXT_LEVEL);
        frame.add(wav, BorderLayout.CENTER);
        frame.add(TEXT_PANEL, BorderLayout.PAGE_END);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void getMaxSampleValue() {

        double max = 0.0;
        for(int i = 0; i < left_signed_sample.length; i ++) {
            if(left_signed_sample[i] > max) {
                max = left_signed_sample[i];
            }
        }
        max_sample = (int)max;
    }
}
