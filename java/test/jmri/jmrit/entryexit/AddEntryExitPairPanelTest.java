package jmri.jmrit.entryexit;

import java.awt.GraphicsEnvironment;
import java.util.HashMap;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
public class AddEntryExitPairPanelTest {

    static EntryExitTestTools tools;
    static HashMap<String, LayoutEditor> panels = new HashMap<>();
    static EntryExitPairs eep;

    //@Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AddEntryExitPairPanel t = new AddEntryExitPairPanel(panels.get("Alpha"));  // NOI18N
        Assert.assertNotNull("exists", t);  // NOI18N
    }

    @Test
    public void testPanelActions() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // Open the NX window
        AddEntryExitPairAction nxAction = new AddEntryExitPairAction("ENTRY EXIT", panels.get("Alpha"));  // NOI18N
        nxAction.actionPerformed(null);
        JFrameOperator nxFrame = new JFrameOperator(Bundle.getMessage("AddEntryExitPoints"));  // NOI18N
        Assert.assertNotNull("nxFrame", nxFrame);  // NOI18N
        JTableOperator tbl = new JTableOperator(nxFrame, 0);
        Assert.assertEquals("Initial row count", 6, tbl.getModel().getRowCount());  // NOI18N

        // Add new pair: NX-AE-Main to NX-To-Beta
        new JComboBoxOperator(nxFrame, 1).selectItem("NX-AE-Main");  // NOI18N
        new JComboBoxOperator(nxFrame, 2).selectItem("NX-To-Beta");  // NOI18N
        new JComboBoxOperator(nxFrame, 3).selectItem(Bundle.getMessage("FullInterlock"));  // NOI18N
        new JButtonOperator(nxFrame, Bundle.getMessage("AddPair")).push();  // NOI18N
        Assert.assertEquals("After add row count", 7, tbl.getModel().getRowCount());  // NOI18N

        // Delete a NX pair
        tbl.clickOnCell(3, 5);
        Assert.assertEquals("After delete row count", 6, tbl.getModel().getRowCount());  // NOI18N

        // Open the Options window - we've commented out the Jemmy approach
        //  String[] optionPath = {"Options", "Options"};  // NOI18N
        //  JMenuBarOperator nxMenu = new JMenuBarOperator(nxFrame);
        //  nxMenu.getTimeouts().setTimeout("JMenuOperator.WaitBeforePopupTimeout", 30L);
        //  nxMenu.pushMenu(optionPath);
        // and are doing it directly; see Issue #6081
        java.util.List<AddEntryExitPairFrame>  frames = jmri.util.JmriJFrame.getFrameList(AddEntryExitPairFrame.class);
        Assert.assertEquals("Should be only one frame", 1, frames.size());
        frames.get(0).nxPanel.optionWindow(null);


        // Close the options window
        JFrameOperator optionFrame = new JFrameOperator(Bundle.getMessage("OptionsTitle"));  // NOI18N
        Assert.assertNotNull("optionFrame", optionFrame);  // NOI18N
        new JButtonOperator(optionFrame, Bundle.getMessage("ButtonOK")).push();  // NOI18N
        optionFrame.dispose();

        nxFrame.dispose();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitUtil.resetProfileManager();

        panels = EntryExitTestTools.getPanels();
        Assert.assertEquals("Get LE panels", 2, panels.size());  // NOI18N
    }

    @AfterEach
    public void tearDown() {
        if (panels != null) {
            panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        }
        panels = null;

        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.clearRouteThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddEntryExitPairPanelTest.class);

}
