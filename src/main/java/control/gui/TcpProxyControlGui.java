package main.java.control.gui;

import main.java.control.EventSource;
import main.java.control.PortForward;
import main.java.control.ResponseHandler;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

public class TcpProxyControlGui extends AbstractControllerGui
        implements JMeterGUIComponent, ActionListener, UnsharedComponent {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private JTextField localPortField;

    private JTextField remotePortField;

    private JTextField remoteServerField;

    private JTextArea textArea;

    private JButton stop, start, clearResults;

    private static final String ACTION_STOP = "stop"; 

    private static final String ACTION_START = "start"; 

    private static final String ACTION_CLEAR = "clear"; 

    private PortForward portForward;

    EventSource eventSource  = new EventSource();

    public TcpProxyControlGui() {
        log.debug("Creating TcpProxyControlGui");
        init();
    }

    @Override
    public TestElement createTestElement() {
        PortForward portForward = new PortForward(eventSource);
        log.debug("creating/configuring model = " + portForward);
        modifyTestElement(portForward);
        return portForward;
    }

    @Override
    public void modifyTestElement(TestElement el) {
        configureTestElement(el);
        if (el instanceof PortForward) {
            PortForward portForward = (PortForward) el;
            portForward.setLocalPort(Integer.parseInt(localPortField.getText()));
            portForward.setRemoteServerName(remoteServerField.getText());
            portForward.setRemotePort(Integer.parseInt(remotePortField.getText()));
        }
    }

    @Override
    public String getLabelResource() {
        return "TCP Proxy Plug in"; 
    }

    public String getStaticLabel() {
        return "TCP Proxy Server";
    }


    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(new String[]{MenuFactory.NON_TEST_ELEMENTS});
    }

    @Override
    public void configure(TestElement element) {
        log.debug("Configuring tcpproxy.gui with " + element);
        super.configure(element);
        portForward = (PortForward) element;
        localPortField.setText(String.valueOf(portForward.getLocalPort()));
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        final String command = action.getActionCommand();

        if (command.equals(ACTION_STOP)) {
            portForward.stopServer();
            stop.setEnabled(false);
            start.setEnabled(true);
        } else if (command.equals(ACTION_START)) {
            modifyTestElement(portForward);
            portForward.startServer();
            start.setEnabled(false);
            stop.setEnabled(true);
        } else if(command.equals(ACTION_CLEAR)){
            textArea.setText("");
        }
    }

    private void init() {

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        Box myBox = Box.createVerticalBox();
        myBox.add(createPortPanel());
        myBox.add(Box.createVerticalStrut(5));
        mainPanel.add(myBox, BorderLayout.NORTH);

        Box resultsBox = Box.createVerticalBox();
        resultsBox.add(createResultsPanel());
        mainPanel.add(resultsBox, BorderLayout.CENTER);

        mainPanel.add(createControls(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createControls() {
        start = new JButton(JMeterUtils.getResString("start")); 
        start.addActionListener(this);
        start.setActionCommand(ACTION_START);
        start.setEnabled(true);

        stop = new JButton(JMeterUtils.getResString("stop")); 
        stop.addActionListener(this);
        stop.setActionCommand(ACTION_STOP);
        stop.setEnabled(false);

        clearResults = new JButton("Clear Results"); 
        clearResults.addActionListener(this);
        clearResults.setActionCommand(ACTION_CLEAR);
        clearResults.setEnabled(true);

        JPanel panel = new JPanel();
        panel.add(start);
        panel.add(stop);
        panel.add(clearResults);
        return panel;
    }

    private JPanel createPortPanel() {
        localPortField = new JTextField(PortForward.DEFAULT_PORT_S, 8);
        localPortField.setName(String.valueOf(PortForward.LOCAL_PORT));

        JLabel label = new JLabel("Local Port"); 
        label.setLabelFor(localPortField);

        remotePortField = new JTextField(PortForward.DEFAULT_REMOTE_PORT_S, 8);
        remotePortField.setName(String.valueOf(PortForward.LOCAL_PORT));

        JLabel label2 = new JLabel("Remote Port"); 
        label.setLabelFor(remotePortField);

        remoteServerField = new JTextField(PortForward.REMOTE_SERVER_NAME, 8);
        remoteServerField.setName(String.valueOf(PortForward.LOCAL_PORT));

        JLabel label3 = new JLabel("Remote Server Name"); 
        label.setLabelFor(remoteServerField);

        HorizontalPanel panel = new HorizontalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "TCP Proxy Settings"));

        panel.add(label);
        panel.add(localPortField);
        panel.add(label3);
        panel.add(remoteServerField);
        panel.add(label2);
        panel.add(remotePortField);
        panel.add(Box.createHorizontalStrut(10));

        return panel;
    }

    private JPanel createResultsPanel() {

        textArea = new JTextArea(10, 10);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JLabel textAreaLabel = new JLabel("Results:");
        textAreaLabel.setLabelFor(textArea);
        JScrollPane scroll= new JScrollPane(textArea);
        textArea.setCaretPosition(textArea.getDocument().getLength());
        HorizontalPanel panel = new HorizontalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "TCP Proxy"));
        panel.add(textAreaLabel);
        ResponseHandler rh = new ResponseHandler(textArea);
        eventSource.addObserver(rh);
        panel.add(scroll);
        panel.add(Box.createHorizontalStrut(10));
        return panel;

    }

    @Override
    public void clearGui() {
        super.clearGui();
//        localPortField.setText(PortForward.DEFAULT_PORT_S);
//        remoteServerField.setText("localhost");
//        remotePortField.setText("8080");
    }

}