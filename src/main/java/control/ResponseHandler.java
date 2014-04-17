package main.java.control;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by bcorpade on 4/7/2014.
 */
public class ResponseHandler implements Observer {

    private JTextArea textArea;


    public ResponseHandler(JTextArea textA) {
        textArea = textA;
    }


    @Override
    public void update(Observable obj, Object arg) {
        LogEvent event = (LogEvent) arg;
        if (arg instanceof LogEvent) {
            textArea.append("\n"+event.getSource() + "\n");
            textArea.append("---------------------------------------------------------------------------------------------------------------------------------------------------------");
            textArea.append("\n");
        }
    }

}
