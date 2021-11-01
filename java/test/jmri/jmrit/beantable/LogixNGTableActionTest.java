package jmri.jmrit.beantable;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.tree.TreePath;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.tools.swing.ConditionalNGEditor;

import jmri.util.*;
import jmri.util.junit.rules.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.Timeout;

import org.netbeans.jemmy.operators.*;

/*
* Tests for the LogixNGTableAction Class
* Re-created using JUnit4 with support for the new conditional editors
* @author Dave Sand Copyright (C) 2017 (for the LogixTableActionTest class)
* @author Daniel Bergqvist Copyright (C) 2019
*/
public class LogixNGTableActionTest extends AbstractTableActionBase<LogixNG> {

    static final ResourceBundle rbxLogixNGSwing = ResourceBundle.getBundle("jmri.jmrit.logixng.tools.swing.LogixNGSwingBundle");

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    @Test
    public void testCtor() {
        Assert.assertNotNull("LogixNGTableActionTest Constructor Return", new LogixNGTableAction());  // NOI18N
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LogixNGTableAction Constructor Return", new LogixNGTableAction("test"));  // NOI18N
    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleLogixNGTable");  // NOI18N
    }

    @Override
    @Test
    public void testGetClassDescription() {
        Assert.assertEquals("LogixNG Table Action class description", Bundle.getMessage("TitleLogixNGTable"), a.getClassDescription());  // NOI18N
    }

    /**
     * Check the return value of includeAddButton.
     * <p>
     * The table generated by this action includes an Add Button.
     */
    @Override
    @Test
    public void testIncludeAddButton() {
        Assert.assertTrue("Default include add button", a.includeAddButton());  // NOI18N
    }

    @Override
    public String getAddFrameName(){
        return Bundle.getMessage("TitleAddLogixNG");
    }

    @Test
    @Override
    public void testAddThroughDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1");
        Assert.assertNull("LogixNG does not exist", logixNG);

        // find the "Add... " button and press it.
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),Bundle.getMessage("ButtonAdd"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        JFrameOperator jf = new JFrameOperator(f1);
        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jf,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        //Enter IQ1 in the text field labeled "System Name:"
        JLabelOperator jlo = new JLabelOperator(jf, "LogixNG" + " " + Bundle.getMessage("ColumnSystemName") + ":");
//        JLabelOperator jlo = new JLabelOperator(jf,Bundle.getMessage("LabelSystemName"));
        ((JTextField)jlo.getLabelFor()).setText("IQ1");
        //and press create
        jmri.util.swing.JemmyUtil.pressButton(jf,Bundle.getMessage("ButtonCreate"));

        // Click button "Done" on the EditLogixNG frame
        String title = String.format("Edit LogixNG %s", "IQ1");
        JFrame frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        JFrameOperator jf2 = new JFrameOperator(frame);
        jmri.util.swing.JemmyUtil.pressButton(jf2,Bundle.getMessage("ButtonDone"));
        JUnitUtil.dispose(frame);

        // Test that we can open the LogixNGEdtior window twice
        logixNGTable.editPressed("IQ101");  // NOI18N
        // Click button "Done" on the EditLogixNG frame
        title = String.format("Edit LogixNG %s - %s", "IQ101", "LogixNG 101");
        frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        jf2 = new JFrameOperator(frame);
        jmri.util.swing.JemmyUtil.pressButton(jf2,Bundle.getMessage("ButtonDone"));
        JUnitUtil.dispose(frame);

        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1");
        Assert.assertNotNull("LogixNG has been created", logixNG);
    }

    @Test
    @Override
    public void testEditButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ101");
        Assert.assertNotNull("LogixNG exists", logixNG);

        logixNGTable.editPressed("IQ101");  // NOI18N

        String title = String.format("Edit LogixNG %s - %s", logixNG.getSystemName(), logixNG.getUserName());
        JFrame frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
//        JFrame frame2 = JFrameOperator.waitJFrame(Bundle.getMessage("EditTitle"), true, true);  // NOI18N

        // Click button "New ConditionalNG" on the EditLogixNG frame
        JFrameOperator jf = new JFrameOperator(frame);
        jmri.util.swing.JemmyUtil.pressButton(jf,"New ConditionalNG");


        JDialogOperator addDialog = new JDialogOperator("Add ConditionalNG");  // NOI18N
        new JButtonOperator(addDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Close window
        JFrameOperator editConditionalNGFrameOperator = new JFrameOperator("Edit ConditionalNG " + logixNG.getConditionalNG(0));
        new JMenuBarOperator(editConditionalNGFrameOperator).pushMenu("File|Close Window", "|");

        Assert.assertNotNull(frame);
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(frame),Bundle.getMessage("ButtonDone"));
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testLogixNGBrowser() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.browserPressed("IQ101");  // NOI18N

        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("LogixNG_Browse_Title"), true, true);  // NOI18N
        Assert.assertNotNull(frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testTreeEditor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setProperty("jmri.jmrit.beantable.LogixNGTableAction", "Edit Mode", "TREEEDIT");  // NOI18N
        a.actionPerformed(null);
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;
        JFrameOperator logixNGFrame = new JFrameOperator(Bundle.getMessage("TitleLogixNGTable"));  // NOI18N
        Assert.assertNotNull(logixNGFrame);

        logixNGTable.editPressed("IQ104");  // NOI18N
        JFrameOperator cdlFrame = new JFrameOperator(jmri.Bundle.formatMessage(rbxLogixNGSwing.getString("TitleEditLogixNG"), "IQ104"));  // NOI18N
        Assert.assertNotNull(cdlFrame);
        new JMenuBarOperator(cdlFrame).pushMenuNoBlock(Bundle.getMessage("MenuFile")+"|"+rbxLogixNGSwing.getString("CloseWindow"), "|");  // NOI18N
        logixNGFrame.dispose();
    }

    @Test
    public void testAddLogixNGAutoName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        logixNGTable.addPressed(null);
        JFrameOperator addFrame = new JFrameOperator(Bundle.getMessage("TitleAddLogixNG"));  // NOI18N
        Assert.assertNotNull("Found Add LogixNG Frame", addFrame);  // NOI18N

        new JTextFieldOperator(addFrame, 1).setText("LogixNG 999");  // NOI18N
        new JButtonOperator(addFrame, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        LogixNG chk999 = jmri.InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getLogixNG("LogixNG 999");  // NOI18N
        Assert.assertNotNull("Verify 'LogixNG 999' Added", chk999);  // NOI18N

        // Add creates an edit frame; find and dispose
        JFrame editFrame = JFrameOperator.waitJFrame(jmri.Bundle.formatMessage(rbxLogixNGSwing.getString("TitleEditLogixNG2"), "IQ:AUTO:0001", "LogixNG 999"), true, true);  // NOI18N
        JUnitUtil.dispose(editFrame);

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testAddLogixNG() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        logixNGTable.addPressed(null);
        JFrameOperator addFrame = new JFrameOperator(Bundle.getMessage("TitleAddLogixNG"));  // NOI18N
        Assert.assertNotNull("Found Add LogixNG Frame", addFrame);  // NOI18N

        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(addFrame,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        new JTextFieldOperator(addFrame, 0).setText("IQ105");  // NOI18N
        new JTextFieldOperator(addFrame, 1).setText("LogixNG 105");  // NOI18N
        new JButtonOperator(addFrame, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        LogixNG chk105 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getLogixNG("LogixNG 105");  // NOI18N
        Assert.assertNotNull("Verify IQ105 Added", chk105);  // NOI18N

        // Add creates an edit frame; find and dispose
        JFrame editFrame = JFrameOperator.waitJFrame(jmri.Bundle.formatMessage(rbxLogixNGSwing.getString("TitleEditLogixNG2"), "IQ105", "LogixNG 105"), true, true);  // NOI18N
        JUnitUtil.dispose(editFrame);

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testDeleteLogixNG() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQ102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "Are you sure you want to delete IQ102?");  // NOI18N
        logixNGTable.deletePressed("IQ102");  // NOI18N
        t1.join();
        LogixNG chk102 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");  // NOI18N
        Assert.assertNotNull("Verify IQ102 Not Deleted", chk102);  // NOI18N

        // Delete IQ103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "Are you sure you want to delete IQ103?");  // NOI18N
        logixNGTable.deletePressed("IQ103");  // NOI18N
        t2.join();
        LogixNG chk103 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");  // NOI18N
        Assert.assertNull("Verify IQ103 Is Deleted", chk103);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testDeleteLogixNGWithConditionalNG() throws InterruptedException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        LogixNG logixNG_102 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");   // NOI18N
        InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG_102, "IQC102", null);   // NOI18N

        LogixNG logixNG_103 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");   // NOI18N
        InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG_103, "IQC103", null);   // NOI18N

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQ102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "Are you sure you want to delete IQ102 and its children?");  // NOI18N
        logixNGTable.deletePressed("IQ102");  // NOI18N
        t1.join();
        LogixNG log102 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");  // NOI18N
        Assert.assertNotNull("Verify IQ102 Not Deleted", log102);  // NOI18N
        ConditionalNG cond102 = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC102");   // NOI18N
        Assert.assertNotNull("Verify IQC102 Not Deleted", cond102);  // NOI18N

        // Delete IQ103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "Are you sure you want to delete IQ103 and its children?");  // NOI18N
        logixNGTable.deletePressed("IQ103");  // NOI18N
        t2.join();
        LogixNG chk103 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");  // NOI18N
        Assert.assertNull("Verify IQ103 Is Deleted", chk103);  // NOI18N
        ConditionalNG cond103 = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC103");   // NOI18N
        Assert.assertNull("Verify IQC103 Is Deleted", cond103);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testDeleteLogixNGWithDigitalAction() throws InterruptedException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        LogixNG logixNG_102 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");   // NOI18N
        ConditionalNG conditionalNG_102 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG_102, "IQC102", null);   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_102 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA102", null);
        conditionalNG_102.getFemaleSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_102));

        LogixNG logixNG_103 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");   // NOI18N
        ConditionalNG conditionalNG_103 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG_103, "IQC103", null);   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_103 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA103", null);
        conditionalNG_103.getFemaleSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_103));

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQ102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "Are you sure you want to delete IQ102 and its children?");  // NOI18N
        logixNGTable.deletePressed("IQ102");  // NOI18N
        t1.join();
        LogixNG log102 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");  // NOI18N
        Assert.assertNotNull("Verify IQ102 Not Deleted", log102);  // NOI18N
        ConditionalNG cond102 = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC102");   // NOI18N
        Assert.assertNotNull("Verify IQC102 Not Deleted", cond102);  // NOI18N
        MaleSocket digMany102 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA102");   // NOI18N
        Assert.assertNotNull("Verify IQDA102 Not Deleted", digMany102);  // NOI18N

        // Delete IQ103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "Are you sure you want to delete IQ103 and its children?");  // NOI18N
        logixNGTable.deletePressed("IQ103");  // NOI18N
        t2.join();
        LogixNG chk103 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");  // NOI18N
        Assert.assertNull("Verify IQ103 Is Deleted", chk103);  // NOI18N
        ConditionalNG cond103 = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC103");   // NOI18N
        Assert.assertNull("Verify IQC103 Is Deleted", cond103);  // NOI18N
        MaleSocket digMany103 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA103");   // NOI18N
        Assert.assertNull("Verify IQDA103 Is Deleted", digMany103);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testDeleteLogixNGWithDigitalActionWithListenerRef() throws InterruptedException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        PropertyChangeListener pcl = (PropertyChangeEvent evt) -> {
            throw new UnsupportedOperationException("Not supported");
        };
        
        final String listenerRefs =
                "<html>\n" +
                "  <head>\n" +
                "    \n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <br>\n" +
                "    It is in use by 1 other objects including.\n" +
                "\n" +
                "    <ul>\n" +
                "      <li>\n" +
                "        A listener ref\n" +
                "      </li>\n" +
                "    </ul>\n" +
                "  </body>\n" +
                "</html>\n";

        LogixNG logixNG_102 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");   // NOI18N
        ConditionalNG conditionalNG_102 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG_102, "IQC102", null);   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_102 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA102", null);
        conditionalNG_102.getFemaleSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_102));
        digitalMany_102.addPropertyChangeListener(pcl, null, "A listener ref");

        LogixNG logixNG_103 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");   // NOI18N
        ConditionalNG conditionalNG_103 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG_103, "IQC103", null);   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_103 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA103", null);
        conditionalNG_103.getFemaleSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_103));
        digitalMany_103.addPropertyChangeListener(pcl, null, "A listener ref");

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQ102, respond No
        Thread t1 = createModalDialogOperatorThread_WithListenerRefs(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), listenerRefs);  // NOI18N
        logixNGTable.deletePressed("IQ102");  // NOI18N
        t1.join();
        LogixNG log102 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");  // NOI18N
        Assert.assertNotNull("Verify IQ102 Not Deleted", log102);  // NOI18N
        ConditionalNG cond102 = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC102");   // NOI18N
        Assert.assertNotNull("Verify IQC102 Not Deleted", cond102);  // NOI18N
        MaleSocket digMany102 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA102");   // NOI18N
        Assert.assertNotNull("Verify IQDA102 Not Deleted", digMany102);  // NOI18N

        // Delete IQ103, respond Yes
        Thread t2 = createModalDialogOperatorThread_WithListenerRefs(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), listenerRefs);  // NOI18N
        logixNGTable.deletePressed("IQ103");  // NOI18N
        t2.join();
        LogixNG chk103 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");  // NOI18N
        Assert.assertNull("Verify IQ103 Is Deleted", chk103);  // NOI18N
        ConditionalNG cond103 = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC103");   // NOI18N
        Assert.assertNull("Verify IQC103 Is Deleted", cond103);  // NOI18N
        MaleSocket digMany103 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA103");   // NOI18N
        Assert.assertNull("Verify IQDA103 Is Deleted", digMany103);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String labelText) {
        RuntimeException e = new RuntimeException("Caller");
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            try {
            new JLabelOperator(jdo, labelText);     // Throws exception if not found
            } catch (Exception e2) {
                e.printStackTrace();
                throw e2;
            }
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    private JEditorPane findTextArea(Container container) {
        for (Component component : container.getComponents()) {
//            System.out.format("Component: %s,%n", component.getClass().getName());
            if (component instanceof JEditorPane) {
                return (JEditorPane) component;
            }
            if (component instanceof Container) {
                JEditorPane textArea = findTextArea((Container) component);
                if (textArea != null) return textArea;
            }
        }
        return null;
    }

    Thread createModalDialogOperatorThread_WithListenerRefs(String dialogTitle, String buttonText, String listenerRefs) {
        RuntimeException e = new RuntimeException("Caller");
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            try {
            JEditorPane textArea = findTextArea((Container) jdo.getComponent(0));
            Assert.assertNotNull(textArea);
            Assert.assertEquals(listenerRefs, textArea.getText());
//            if (textArea != null) {
//                System.out.format("TextArea found: '%s'%n", textArea.getText());
//            } else {
//                System.out.format("TextArea not found%n");
//            }
//            new JLabelOperator(jdo, labelText);     // Throws exception if not found
            } catch (Exception e2) {
                e.printStackTrace();
                throw e2;
            }
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    // Test that it's possible to
    // * Add a LogixNG
    // * Enable the LogixNG
    // * Add a ConditionalNG
    // * Add a IfThenElse
    // * Add a ExpressionSensor
    // * Add a ActionTurnout
    // After that, test that the LogixNG is executed properly
    @Test
    public void testAddAndRun() throws JmriException, InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");

        // * Add a LogixNG
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrameOperator logixNGFrameOperator = new JFrameOperator(Bundle.getMessage("TitleLogixNGTable"));  // NOI18N

        logixNGTable.addPressed(null);
        JFrameOperator addFrame = new JFrameOperator(Bundle.getMessage("TitleAddLogixNG"));  // NOI18N
        Assert.assertNotNull("Found Add LogixNG Frame", addFrame);  // NOI18N

        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(addFrame,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        new JTextFieldOperator(addFrame, 0).setText("IQ105");  // NOI18N
        new JTextFieldOperator(addFrame, 1).setText("LogixNG 105");  // NOI18N
        new JButtonOperator(addFrame, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        LogixNG logixNG = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getLogixNG("LogixNG 105");  // NOI18N
        Assert.assertNotNull("Verify IQ105 Added", logixNG);  // NOI18N


        // Close Edit LogixNG frame by click on button "Done" on the EditLogixNG frame
        String title = String.format("Edit LogixNG %s - %s", logixNG.getSystemName(), logixNG.getUserName());
        JFrame editLogixNGframe = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        JFrameOperator jf2 = new JFrameOperator(editLogixNGframe);
        jmri.util.swing.JemmyUtil.pressButton(jf2,Bundle.getMessage("ButtonDone"));

        // Operate on the table
        JTableOperator tableOperator = new JTableOperator(logixNGFrameOperator);
        int columnSystemName = tableOperator.findColumn("System name");
        int columnEnabled = tableOperator.findColumn("Enabled");
        int columnMenu = tableOperator.findColumn("Menu");
        int row = tableOperator.findCellRow("IQ105", columnSystemName, 0);

        Assert.assertTrue("LogixNG is enabled on creation", logixNG.isEnabled());

        // Disable the LogixNG
        tableOperator.setValueAt(false, row, columnEnabled);

        Assert.assertFalse("LogixNG has been disabled", logixNG.isEnabled());

        // Enable the LogixNG
        tableOperator.setValueAt(true, row, columnEnabled);

        Assert.assertTrue("LogixNG has been enabled", logixNG.isEnabled());


        // Edit the LogixNG
        tableOperator.setValueAt("Edit", row, columnMenu);

        // Open Edit ConditionalNG  frame
        title = String.format("Edit LogixNG %s - %s", logixNG.getSystemName(), logixNG.getUserName());
        editLogixNGframe = JFrameOperator.waitJFrame(title, true, true);  // NOI18N


        // Click button "New ConditionalNG" on the EditLogixNG frame
        JFrameOperator jf = new JFrameOperator(editLogixNGframe);
        jmri.util.swing.JemmyUtil.pressButton(jf,"New ConditionalNG");

        JDialogOperator addDialog = new JDialogOperator("Add ConditionalNG");  // NOI18N
        new JTextFieldOperator(addDialog, 0).setText("IQC105");  // NOI18N
        new JTextFieldOperator(addDialog, 1).setText("ConditionalNG 105");  // NOI18N
        new JButtonOperator(addDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        new org.netbeans.jemmy.QueueTool().waitEmpty();

        ConditionalNG conditionalNG = jmri.InstanceManager.getDefault(ConditionalNG_Manager.class).getConditionalNG(logixNG, "ConditionalNG 105");  // NOI18N
        Assert.assertNotNull("Verify IQC105 Added", conditionalNG);  // NOI18N


        // https://www.javatips.net/api/org.netbeans.jemmy.operators.jtreeoperator

        // Get tree edit window
        title = String.format("Edit ConditionalNG %s - %s", conditionalNG.getSystemName(), conditionalNG.getUserName());
        JFrameOperator treeFrame = new JFrameOperator(title);
        JTreeOperator jto = new JTreeOperator(treeFrame);
        Assert.assertEquals("Initial number of rows in the tree", 1, jto.getRowCount());


        // We click on the root female socket to open the popup menu
        TreePath tp = jto.getPathForRow(0);

        JPopupMenu jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        JDialogOperator addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.COMMON);
        new JComboBoxOperator(addItemDialog, 1).selectItem("If then else");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        JUnitUtil.waitFor(() -> {return conditionalNG.getChild(0).isConnected();});

        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
        Assert.assertEquals("Action is correct", "If Then Else. Execute on change",
                conditionalNG.getChild(0).getConnectedSocket().getLongDescription());
        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());


        Thread.sleep(100);

        // We click on the IfThenElse if-expression female socket to open the popup menu
        tp = jto.getPathForRow(1);

        jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        addItemDialog = new JDialogOperator("Add ? ");  // NOI18N
        // Select ExpressionSensor
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.ITEM);
        new JComboBoxOperator(addItemDialog, 1).selectItem("Sensor");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the expression
        addItemDialog = new JDialogOperator("Add ? ");  // NOI18N

        // Select to use sensor IS1
        new JComboBoxOperator(addItemDialog, 0).setSelectedIndex(1);
        new JComboBoxOperator(addItemDialog, 1).setSelectedItem(Is_IsNot_Enum.Is);
        new JComboBoxOperator(addItemDialog, 2).setSelectedItem(ExpressionSensor.SensorState.Active);
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());

        JUnitUtil.waitFor(() -> {return conditionalNG.getChild(0).getConnectedSocket().getChild(0).getConnectedSocket() != null;});

        Assert.assertEquals("Expression is correct", "Sensor IS1 is Active",
                conditionalNG.getChild(0).getConnectedSocket().getChild(0).getConnectedSocket().getLongDescription());


        Thread.sleep(100);

        // We click on the IfThenElse then-action female socket to open the popup menu
        tp = jto.getPathForRow(2);

        jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        // Select ExpressionSensor
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.ITEM);
        new JComboBoxOperator(addItemDialog, 1).selectItem("Turnout");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N

        // Select to use sensor IS1
        new JComboBoxOperator(addItemDialog, 0).setSelectedIndex(1);
        new JComboBoxOperator(addItemDialog, 1).setSelectedItem(ActionTurnout.TurnoutState.Thrown);
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        JUnitUtil.waitFor(() -> {return conditionalNG.getChild(0).getConnectedSocket().getChild(1).getConnectedSocket() != null;});

        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());
        Assert.assertEquals("Expression is correct", "Set turnout IT1 to state Thrown",
                conditionalNG.getChild(0).getConnectedSocket().getChild(1).getConnectedSocket().getLongDescription());


        // Close EditConditionalNG window
        JFrameOperator editConditionalNGFrameOperator = new JFrameOperator("Edit ConditionalNG " + logixNG.getConditionalNG(0));
        new JMenuBarOperator(editConditionalNGFrameOperator).pushMenu("File|Close Window", "|");


        logixNG.getConditionalNG(0).setRunDelayed(false);

        // Test that the LogixNG is running
        sensor1.setState(Sensor.INACTIVE);
        turnout1.setState(Turnout.CLOSED);
        Assert.assertTrue("Sensor is inactive", sensor1.getState() == Sensor.INACTIVE);
        Assert.assertTrue("Turnout is closed", turnout1.getState() == Turnout.CLOSED);

        // Activate sensor. This should throw the turnout
        sensor1.setState(Sensor.ACTIVE);
        Assert.assertTrue("Sensor is active", sensor1.getState() == Sensor.ACTIVE);
        Assert.assertTrue("Turnout is thrown", turnout1.getState() == Turnout.THROWN);

        // Close Edit LogixNG frame
        JUnitUtil.dispose(editLogixNGframe);

        // Close LogixNG frame
        logixNGFrameOperator.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initLogixNGManager();

//        InstanceManager.getDefault(LogixNGPreferences.class).setLimitRootActions(false);

        InstanceManager.getDefault(UserPreferencesManager.class)
                .setSimplePreferenceState(ConditionalNGEditor.class.getName()+".AutoSystemName", true);

        InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("IQ101", "LogixNG 101");
        InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("IQ102", "LogixNG 102");
        InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("IQ103", "LogixNG 103");
        InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("IQ104", "LogixNG 104");

        helpTarget = "package.jmri.jmrit.beantable.LogixNGTable";
        a = new LogixNGTableAction();
    }

    @AfterEach
    @Override
    public void tearDown() {
        a = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTableActionTest.class);

}
