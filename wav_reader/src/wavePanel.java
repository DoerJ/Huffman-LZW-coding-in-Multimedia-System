import javax.swing.*;
import java.awt.*;

public class wavePanel extends JPanel {

    private static int width;
    private static int height;
    private static int X_AXIS;
    private static double[] left_channel;
    private static int num_frame;
    private static double offset;

    wavePanel(int width, int height, int num_frame, double[] left_channel) {

        this.width = width;
        this.height = height;
        this.num_frame = num_frame;
        this.left_channel = left_channel;
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        this.setBackground(Color.white);

        // the width offset for each vertical line
        offset = (double)width / (double)num_frame;
        // x-axis
        X_AXIS = height / 2;

        // draw waveform
        drawWave(g);
    }
    // draw waveform
    // display samples for 1s, i.e., 44100 samples / sec
    private void drawWave(Graphics wavePen) {

        // vertical line/ starting point: (x, height/2) ;
        // end point: (x, height/2 - length of vertical line)
        double x_pos = 0;
        int i = 0;
        double left_sample_value;
        double left_length;

        // set paint color
        wavePen.setColor(Color.BLUE);

        while(i < num_frame) {

            // left channel samples
            left_sample_value = left_channel[i];
            left_length = left_sample_value * (X_AXIS);
            wavePen.drawLine((int)Math.round(x_pos), X_AXIS, (int)Math.round(x_pos), X_AXIS - (int)left_length);

            x_pos += offset;
            i += 1;
        }
    }
}

