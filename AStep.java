package fr.com.app.ui.widgets.wizard;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import fr.com.app.ui.resources.CommonImages;
import fr.com.app.ui.resources.IPluginImages;
import fr.com.app.ui.resources.PluginColors;
import fr.com.app.ui.resources.PluginToolkit;

public abstract class AStep implements IStep {
	
	// Toolkit
	protected PluginToolkit toolkit = PluginToolkit.getInstance();
	
	protected AWizard parentWizard;
	protected Composite body;
	protected String label;
	protected String informationMessage;
	protected StepStateEnum state;
	
	public AStep(AWizard parentWizard, Shell shell, String titre) {
		this.parentWizard = parentWizard;
		this.label = titre;
	}

	@Override
	public Composite getBody() {
		return body;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	@Override
	public StepStateEnum getState() {
		return state;
	}
	
	@Override
	public String getOkBtLabel() {
		return "Suivant";
	}
	
	@Override
	public Image getImage() {
		return CommonImages.getSharedImage(IPluginImages.IMG_STATUT_STARTED);
	}

	@Override
	public Image getOkBtIcon() {
		return null;
	}
	
	public String getInformationMessage() {
		return informationMessage;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<IStatus> validate() {
		return CollectionUtils.EMPTY_COLLECTION;
	}
	

	@Override
	public void save(Object dataContainer) {
	}
	
	@Override
	public boolean isEnable() {
		return true;
	}
	
	protected void createInformationLabel(Composite parent) {

		if (getInformationMessage() == null)
			return;
		
		Group group = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(group);
		group.setText("Information");
		group.setBackground(PluginColors.getInstance().getColor(SWT.COLOR_WHITE));
		
		Composite compositeInformation = PluginToolkit.getInstance().createComposite(group);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(compositeInformation);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(compositeInformation);

		Canvas canvas = new Canvas(compositeInformation, SWT.NONE);
		canvas.setBackground(PluginColors.getInstance().getColor(SWT.COLOR_WHITE));
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
			e.gc.drawImage(CommonImages.getSharedImage(IPluginImages.IMG_BULB_GREY), 10, 10);
			}
		});
		
		Label lblInformation = new Label(compositeInformation, SWT.WRAP);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | SWT.CENTER);
		lblInformation.setLayoutData(data);
		lblInformation.setBackground(PluginColors.getInstance().getColor(SWT.COLOR_WHITE));
		lblInformation.setText(informationMessage);
	}
}
