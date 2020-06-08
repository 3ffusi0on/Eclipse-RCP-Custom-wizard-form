package fr.com.app.ui.widgets.wizard;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public interface IStep {

	void				createBody(Composite parent, Object data);
	void				resetUI();
	Collection<IStatus>	validate();
	void					save(Object dataContainer);
	boolean				isEnable();
	
	// Getters
	String				getLabel();
	Image					getImage();
	StepStateEnum	getState();
	Composite			getBody();
	Image					getOkBtIcon();
	String				getOkBtLabel();
	String				getInformationMessage();
}
