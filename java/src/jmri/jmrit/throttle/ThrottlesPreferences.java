package jmri.jmrit.throttle;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store JMRI throttles preferences
 * 
 * @author Lionel Jeanson - 2009-2021
 * 
 */
public class ThrottlesPreferences {

    private boolean _useExThrottle = true;
    private boolean _useToolBar = true;
    private boolean _useFunctionIcon = false;
    private boolean _useLargeSpeedSlider = false;
    private boolean _resizeWinImg = false;
    private boolean _useRosterImage = true;
    private boolean _enableRosterSearch = true;
    private boolean _enableAutoLoad = true;
    private boolean _hideUndefinedFunButton = false;
    private boolean _ignoreThrottlePosition = true;
    private boolean _saveThrottleOnLayoutSave = true;
    private boolean _isSilentSteal = false;
    private boolean _isSilentShare = false;
    private String _defaultThrottleFilePath = null;
    private ThrottlesPreferencesWindowKeyboardControls _tpwkc = new ThrottlesPreferencesWindowKeyboardControls();
    protected boolean dirty = false;

    private Dimension _winDim = new Dimension(800, 600);
    private String prefFile;
    private ArrayList<PropertyChangeListener> listeners;

    public ThrottlesPreferences() {
        String dirname = FileUtil.getUserFilesPath() + "throttle" + File.separator;
        FileUtil.createDirectory(dirname);
        prefFile = dirname + "ThrottlesPreferences.xml";
        ThrottlesPrefsXml prefs = new ThrottlesPrefsXml();
        File file = new File(prefFile);
        Element root;
        try {
            root = prefs.rootFromFile(file);
        } catch (java.io.FileNotFoundException e2) {
            log.info("Did not find throttle preferences file.  This is normal if you haven't save the preferences before");
            root = null;
        } catch (Exception e) {
            log.error("Exception while loading throttles preferences: {}", e);
            root = null;
        }
        if (root != null) {
            load(root.getChild("throttlesPreferences"));
        }
    }

    public void load(org.jdom2.Element e) {
        if (e == null) {
            return;
        }
        org.jdom2.Attribute a;
        org.jdom2.Attribute b;
        if ((a = e.getAttribute("isUsingExThrottle")) != null) {
            setUseExThrottle(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isUsingToolBar")) != null) {
            setUsingToolBar(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isResizingWindow")) != null) {
            setResizeWindow(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isUsingFunctionIcon")) != null) {
            setUsingFunctionIcon(a.getValue().compareTo("true") == 0);
        }
        if (((a = e.getAttribute("windowDimensionWidth")) != null) && ((b = e.getAttribute("windowDimensionHeight")) != null)) {
            setWindowDimension(new Dimension(Integer.parseInt(a.getValue()), Integer.parseInt(b.getValue())));
        }
        if ((a = e.getAttribute("isSavingThrottleOnLayoutSave")) != null) {
            setSaveThrottleOnLayoutSave(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isUsingRosterImage")) != null) {
            setUseRosterImage(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isEnablingRosterSearch")) != null) {
            setEnableRosterSearch(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isAutoLoading")) != null) {
            setAutoLoad(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isHidingUndefinedFunctionButtons")) != null) {
            setHideUndefinedFuncButt(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isIgnoringThrottlePosition")) != null) {
            setIgnoreThrottlePosition(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isSilentSteal")) != null) {
            setSilentSteal(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isSilentShare")) != null) {
            setSilentShare(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isUsingLargeSpeedSlider")) != null) {
            setUseLargeSpeedSlider(a.getValue().compareTo("true") == 0);
        }
        if (e.getChild("throttlesControls") != null) {
            this._tpwkc.load(e.getChild("throttlesControls"));
        }
        if ((a = e.getAttribute("defaultThrottleFilePath")) != null) {
            setDefaultThrottleFilePath(a.getValue());
        }        
        
        this.dirty = false;
    }

    /**
     * @return true if preferences need to be saved
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * An extension of the abstract XmlFile. No changes made to that class.
     *
     */
    static class ThrottlesPrefsXml extends XmlFile {
    }

    public Element store() {
        org.jdom2.Element e = new org.jdom2.Element("throttlesPreferences");
        e.setAttribute("isUsingExThrottle", "" + isUsingExThrottle());
        e.setAttribute("isUsingToolBar", "" + isUsingToolBar());
        e.setAttribute("isUsingFunctionIcon", "" + isUsingFunctionIcon());
        e.setAttribute("isResizingWindow", "" + isResizingWindow());
        e.setAttribute("windowDimensionWidth", "" + (int) getWindowDimension().getWidth());
        e.setAttribute("windowDimensionHeight", "" + (int) getWindowDimension().getHeight());
        e.setAttribute("isSavingThrottleOnLayoutSave", "" + isSavingThrottleOnLayoutSave());
        e.setAttribute("isUsingRosterImage", "" + isUsingRosterImage());
        e.setAttribute("isEnablingRosterSearch", "" + isEnablingRosterSearch());
        e.setAttribute("isAutoLoading", "" + isAutoLoading());
        e.setAttribute("isHidingUndefinedFunctionButtons", "" + isHidingUndefinedFuncButt());
        e.setAttribute("isIgnoringThrottlePosition", "" + isIgnoringThrottlePosition());
        e.setAttribute("isSilentSteal", "" + isSilentSteal());
        e.setAttribute("isSilentShare", "" + isSilentShare());
        e.setAttribute("isUsingLargeSpeedSlider", "" + isUsingLargeSpeedSlider());
        e.setAttribute("defaultThrottleFilePath", "" + getDefaultThrottleFilePath());
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1);
        children.add(this._tpwkc.store());
        e.setContent(children);
        return e;
    }

    public void set(ThrottlesPreferences tp) {
        setWindowDimension(tp.getWindowDimension());
        setUseExThrottle(tp.isUsingExThrottle());
        setUsingToolBar(tp.isUsingToolBar());
        setUsingFunctionIcon(tp.isUsingFunctionIcon());
        setResizeWindow(tp.isResizingWindow());
        setSaveThrottleOnLayoutSave(tp.isSavingThrottleOnLayoutSave());
        setUseRosterImage(tp.isUsingRosterImage());
        setEnableRosterSearch(tp.isEnablingRosterSearch());
        setAutoLoad(tp.isAutoLoading());
        setHideUndefinedFuncButt(tp.isHidingUndefinedFuncButt());
        setIgnoreThrottlePosition(tp.isIgnoringThrottlePosition());
        setSilentSteal(tp.isSilentSteal());
        setSilentShare(tp.isSilentShare());
        setUseLargeSpeedSlider(tp.isUsingLargeSpeedSlider());
        setThrottlesKeyboardControls(tp.getThrottlesKeyboardControls());
        setDefaultThrottleFilePath(tp.getDefaultThrottleFilePath());
        
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                PropertyChangeListener l = listeners.get(i);
                PropertyChangeEvent e = new PropertyChangeEvent(this, "ThrottlePreferences", null, this);
                l.propertyChange(e);
            }
        }
    }

    public void save() {
        if (prefFile == null) {
            return;
        }
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract
        xf.makeBackupFile(prefFile);
        File file = new File(prefFile);
        try {
            //The file does not exist, create it before writing
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) // make directory, check result
                {
                    log.error("failed to make parent directory");
                }
            }
            if (!file.createNewFile()) // create file, check result
            {
                log.error("createNewFile failed");
            }
        } catch (Exception exp) {
            log.error("Exception while writing the new throttles preferences file, may not be complete: {}", exp);
        }

        try {
            Element root = new Element("throttles-preferences");
            Document doc = XmlFile.newDocument(root, XmlFile.getDefaultDtdLocation() + "throttles-preferences.dtd");
            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
/*TODO      java.util.Map<String,String> m = new java.util.HashMap<String,String>();
             m.put("type", "text/xsl");
             m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttles-preferences.xsl");
             ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
             doc.addContent(0,p);*/
            root.setContent(store());
            xf.writeXML(file, doc);
        } catch (java.io.IOException ex) {
            log.warn("Exception in storing throttles preferences xml: {}", ex);
        }
        this.dirty = false;
    }

    public Dimension getWindowDimension() {
        return _winDim;
    }

    public void setWindowDimension(Dimension d) {
        _winDim = d;
        this.dirty = true;
    }

    public boolean isUsingExThrottle() {
        return _useExThrottle;
    }

    public void setUseExThrottle(boolean exThrottle) {
        _useExThrottle = exThrottle;
        this.dirty = true;
    }

    public boolean isUsingToolBar() {
        return _useToolBar;
    }

    public void setUsingToolBar(boolean win4all) {
        _useToolBar = win4all;
        this.dirty = true;
    }

    /**
     * Check if function icons are in use.
     * 
     * @return user preference to use function icons.
     */
    public boolean isUsingFunctionIcon() {
        return _useFunctionIcon;
    }

    public void setUsingFunctionIcon(boolean useFunctionIcon) {
        _useFunctionIcon = useFunctionIcon;
        this.dirty = true;
    }

    public boolean isResizingWindow() {
        return _resizeWinImg;
    }

    public void setResizeWindow(boolean winImg) {
        _resizeWinImg = winImg;
        this.dirty = true;
    }

    public boolean isUsingRosterImage() {
        return _useRosterImage;
    }

    public void setUseRosterImage(boolean rosterImage) {
        _useRosterImage = rosterImage;
        this.dirty = true;
    }

    public boolean isEnablingRosterSearch() {
        return _enableRosterSearch;
    }

    public void setEnableRosterSearch(boolean b) {
        _enableRosterSearch = b;
        this.dirty = true;
    }

    public void setAutoLoad(boolean b) {
        _enableAutoLoad = b;
        this.dirty = true;
    }

    public boolean isAutoLoading() {
        return _enableAutoLoad;
    }

    public void setHideUndefinedFuncButt(boolean b) {
        _hideUndefinedFunButton = b;
        this.dirty = true;
    }

    public boolean isHidingUndefinedFuncButt() {
        return _hideUndefinedFunButton;
    }

    public void setIgnoreThrottlePosition(boolean b) {
        _ignoreThrottlePosition = b;
        this.dirty = true;
    }

    public boolean isIgnoringThrottlePosition() {
        return _ignoreThrottlePosition;
    }

    public void setSaveThrottleOnLayoutSave(boolean b) {
        _saveThrottleOnLayoutSave = b;
        this.dirty = true;
    }

    public boolean isSavingThrottleOnLayoutSave() {
        return _saveThrottleOnLayoutSave;
    }

    public boolean isSilentSteal() {
        return _isSilentSteal;
    }
    
    public boolean isSilentShare() {
        return _isSilentShare;
    }
    
    public void setSilentSteal(boolean b) {
        _isSilentSteal = b;
        this.dirty = true;
    }
    
    public void setSilentShare(boolean b) {
        _isSilentShare = b;
        this.dirty = true;
    }

    
    public void setUseLargeSpeedSlider(boolean b) {
        _useLargeSpeedSlider = b;
        this.dirty = true;
    }
    
    public boolean isUsingLargeSpeedSlider() {
        return _useLargeSpeedSlider;
    }
            
    public void setDefaultThrottleFilePath(String p) {
        _defaultThrottleFilePath = p;
        this.dirty = true;
    }
    
    public String getDefaultThrottleFilePath() {
        return _defaultThrottleFilePath;
    }
    
    /**
     * @return the throttles keyboard controls preferences
     */
    public ThrottlesPreferencesWindowKeyboardControls getThrottlesKeyboardControls() {
        return _tpwkc;
    }
    
    /**
     * Set the throttles keyboard controls preferences
     * @param tpwkc the new keyboard preferences
     */
    public void setThrottlesKeyboardControls(ThrottlesPreferencesWindowKeyboardControls tpwkc) {
        _tpwkc = tpwkc;
    }
         
    /**
     * Add an AddressListener. 
     * AddressListeners are notified when the user
     * selects a new address and when a Throttle is acquired for that address.
     * @param l listener to add.
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null) {
            listeners = new ArrayList<>(2);
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove an AddressListener.
     * @param l listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null) {
            return;
        }
        listeners.remove(l);
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottlesPreferences.class);
}
