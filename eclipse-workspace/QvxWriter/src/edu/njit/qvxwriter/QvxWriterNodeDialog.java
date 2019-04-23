package edu.njit.qvxwriter;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.util.FilesHistoryPanel;
import org.knime.core.node.util.FilesHistoryPanel.LocationValidation;
import org.knime.core.node.workflow.FlowVariable;

import edu.njit.qvxwriter.QvxWriterNodeSettings.OverwritePolicy;

import static edu.njit.util.Util.removeSuffix;
import static edu.njit.util.Util.toTitleCase;

/**
 * <code>NodeDialog</code> for the "QvxWriter" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class QvxWriterNodeDialog extends NodeDialogPane {
		
	//settingsPanel and all of its sub-components
	private final JPanel settingsPanel;
	
	private final JPanel filesPanel;
	private final FilesHistoryPanel filesHistoryPanel;
	
	private final TableNamePanel tableNamePanel;
	
	private final JPanel overwritePolicyPanel;
	private final JRadioButton overwritePolicy_abortButton;
	private final JRadioButton overwritePolicy_overwriteButton;
	
	private final AdvancedPanel advancedPanel;
	private final FieldAttrPanel fieldAttributesPanel;
	//private final LimitRowsPanel limitRowsPanel;
		
	private final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	
    protected QvxWriterNodeDialog() {
        super();
            
        // Settings panel
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        
        filesPanel = new JPanel();
        filesPanel.setBorder(new TitledBorder("Output Location"));
        filesHistoryPanel = new FilesHistoryPanel(
        		createFlowVariableModel("CFGKEY_FILE", FlowVariable.Type.STRING),
        		"History ID", LocationValidation.FileOutput, ".qvx");
        filesHistoryPanel.setDialogTypeSaveWithExtension(".qvx");
        filesPanel.add(filesHistoryPanel);
        
        tableNamePanel = new TableNamePanel();
         
        overwritePolicyPanel = new JPanel();
        overwritePolicyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        overwritePolicyPanel.setBorder(new TitledBorder("If file exists..."));
        overwritePolicy_abortButton = new JRadioButton();
        overwritePolicy_abortButton.setText(OverwritePolicy.ABORT.toString());
        overwritePolicy_overwriteButton = new JRadioButton();
        overwritePolicy_overwriteButton.setText(OverwritePolicy.OVERWRITE.toString());
        overwritePolicy_abortButton.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(overwritePolicy_abortButton);
        group.add(overwritePolicy_overwriteButton);
        overwritePolicyPanel.add(overwritePolicy_abortButton);
        overwritePolicyPanel.add(overwritePolicy_overwriteButton);
        
        filesHistoryPanel.addChangeListener(new ChangeListener() {
        	@Override
        	public void stateChanged(final ChangeEvent e) {
        		String tableName = filesHistoryPanel.getSelectedFile();
        		File f = new File(tableName);
        		tableName = f.getName();	
        		tableName = removeSuffix(tableName, ".qvx");
        		tableName = toTitleCase(tableName);
        		tableNamePanel.setDefaultName(tableName);
        	}
        });
        
        settingsPanel.add(filesPanel);
        settingsPanel.add(tableNamePanel);
        settingsPanel.add(overwritePolicyPanel);       
        addTab("Settings", settingsPanel);
        
        advancedPanel = new AdvancedPanel();
        addTab("Advanced", advancedPanel);
        
        fieldAttributesPanel = new FieldAttrPanel();
        JScrollPane scrollPane = new JScrollPane(fieldAttributesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension((int)(SCREEN_WIDTH/5), (int)(SCREEN_HEIGHT/5)));
        addTab("Field Attributes", scrollPane);
                
        System.out.println("Settings dimension: " + settingsPanel.getPreferredSize());     
    }
    
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		System.out.println("NodeDialog: saveSettingsTo()");
		QvxWriterNodeSettings m_settings = new QvxWriterNodeSettings();
	
		//fileName
		String fileName = filesHistoryPanel.getSelectedFile();
		m_settings.setFileName(fileName);
		
		//overwritePolicy
		OverwritePolicy overwritePolicy = null;
		if (overwritePolicy_abortButton.isSelected()) {
			overwritePolicy = OverwritePolicy.ABORT;
		}else if (overwritePolicy_overwriteButton.isSelected()) {
			overwritePolicy = OverwritePolicy.OVERWRITE;
		}
		m_settings.setOverwritePolicy(overwritePolicy);
		
		advancedPanel.saveSettingsInto(m_settings);
		fieldAttributesPanel.saveSettingsInto(m_settings);
		tableNamePanel.saveSettingsInto(m_settings);
		
		m_settings.saveSettingsTo(settings);
	}
	
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
		
		System.out.println(specs.length);
		System.out.println(Arrays.toString(specs[0].getColumnNames()));
		
		System.out.println("NodeDialog: loadSettingsFrom()");
		try {	
			String fileName = settings.getString(QvxWriterNodeSettings.CFGKEY_FILE_NAME);
			System.out.println("loadSettingsFrom: fileName");
			String overwritePolicy = settings.getString(
					QvxWriterNodeSettings.CFGKEY_OVERWRITE_POLICY);			
			System.out.println("loadSettingsFrom: overwritePolicy");

			//fileName
			filesHistoryPanel.setSelectedFile(fileName);
			
			//overwritePolicy
			if (overwritePolicy.equals(OverwritePolicy.ABORT.toString())){
				overwritePolicy_abortButton.setSelected(true);
			}else if (overwritePolicy.equals(OverwritePolicy.OVERWRITE.toString())) {
				overwritePolicy_overwriteButton.setSelected(true);
			}
			
			System.out.println("loadSettingsFrom: creating other panels");
			advancedPanel.loadValuesIntoPanel(settings);
			fieldAttributesPanel.loadValuesIntoPanel(settings, specs[0]);
			tableNamePanel.loadValuesIntoPanel(settings);
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
	}
}
