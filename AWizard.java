package fr.com.app.ui.widgets.wizard;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import fr.com.app.ui.resources.PluginToolkit;
import fr.com.app.ui.widgets.wizard.BreadCrumbItem.EtatBreadCrumItemEnum;

public abstract class AWizard extends Dialog {

	private String title;
	private List<IStep> steps;
	private IStep currentStep;
	private int currentStepIndex;
	private Composite mainContainer;
	private Composite menuComposite;
	private BreadCrumb breadCrumb;
	
	private Composite buttonsComposite;
	
	private Composite bodyComposite;
	private StackLayout stackLayout;
	
	private boolean readOnly = false;
	
	// Wizard controls
	private Button btPrevious;
	private Button btNext;
	private Button btCancel;
	
	// Data
	private Object dataContainer;

	public AWizard(Shell parentShell, Object dataContainer, String title) {
		super(parentShell);
		
		this.title = title;
		this.steps = new LinkedList<IStep>();
		this.currentStepIndex = 0;
		this.dataContainer = dataContainer;
	}

	public void addStep(IStep newStep) {
		steps.add(newStep);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {

		Composite body = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(body);
		GridLayoutFactory.fillDefaults().applyTo(body);

		this.mainContainer = PluginToolkit.getInstance().createComposite(body);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(10, 10).applyTo(mainContainer);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainContainer);
		
		// Menu
		createMenu(mainContainer);
		
		// Body
		createBody(mainContainer);
		
		// Buttons
		createButtons(mainContainer);
		
		return body;
	}
	
	private void createBody(Composite parent) {
		this.bodyComposite = PluginToolkit.getInstance().createComposite(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(bodyComposite);
		GridLayoutFactory.fillDefaults().applyTo(bodyComposite);
		stackLayout = new StackLayout();
		bodyComposite.setLayout(stackLayout);
		
		if (steps.size() == 0)
			return;
		
		currentStep = steps.get(currentStepIndex);

		// On ne recrée pas l'UI si elle existe
		if (currentStep.getBody() == null)
			currentStep.createBody(bodyComposite, dataContainer);
		stackLayout.topControl = currentStep.getBody();
		
	}

	private void createButtons(Composite parent) {
		this.buttonsComposite = PluginToolkit.getInstance().createComposite(parent);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonsComposite);
		GridLayoutFactory.fillDefaults().equalWidth(true).applyTo(buttonsComposite);
		buttonsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
		//TODO taille bouton pas belle
		
		btPrevious = createButton(buttonsComposite, IDialogConstants.BACK_ID, "Précedent", true);
		btPrevious.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		
		btCancel = createButton(buttonsComposite, IDialogConstants.CANCEL_ID, "Annuler", true);
		btCancel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		
		btNext = createButton(buttonsComposite, IDialogConstants.OK_ID, "Suivant", true);
		btNext.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		setupBtState();
	}

	private void setupBtState() {
		if (steps.size() == 0)
			return;
		
		btPrevious.setEnabled( ! (steps.size() == 1 || currentStepIndex == 0));
		btNext.setText(currentStep.getOkBtLabel());
		buttonsComposite.getParent().layout(true);
		buttonsComposite.redraw();
	}

	private void createMenu(Composite parent) {
		this.menuComposite = PluginToolkit.getInstance().get(SWT.BORDER).createComposite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(menuComposite);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(menuComposite);
		menuComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, GridData.VERTICAL_ALIGN_BEGINNING, false, false));
		
		// Création de l'objet
		breadCrumb = new BreadCrumb(menuComposite, SWT.BORDER);
		breadCrumb.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));

		for (int i=0; i < steps.size(); i++) {
			final BreadCrumbItem item = new BreadCrumbItem(breadCrumb, SWT.CENTER | SWT.PUSH);
			
			IStep step = steps.get(i);
			
			item.setText(step.getLabel());
			item.setImage(step.getImage());
			item.setSelectionImage(step.getImage());
			item.setDisabledImage(step.getImage());

			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					BreadCrumbItem item = (BreadCrumbItem)e.item;
					
					goToStep(breadCrumb.indexOf(item));
					
					for (int i = currentStepIndex + 1; i < breadCrumb.getItemCount(); i++)
						breadCrumb.getItem(i).setEtat(EtatBreadCrumItemEnum.TODO);
					breadCrumb.selectedItem(currentStepIndex);
				}

			});
		}
		breadCrumb.selectedItem(0);
	}
	
	@Override
	protected void okPressed() {
		Collection<IStatus> errors = currentStep.validate();
		if (errors.size() > 0) {
			StringBuilder message = new StringBuilder();
			for (IStatus error : errors) {
				message.append(error.getMessage());
				message.append("\n");
			}
			MessageDialog.openError(getShell(), "Erreur", message.toString());
			return;
		}
		
		currentStep.save(dataContainer);
		
		if (currentStepIndex == steps.size() - 1) {
			save();
			super.okPressed();
			return;
		}

		goToStep(currentStepIndex + 1);
	}
	
	
	protected abstract void save();

	protected void previousPressed() {
		
		// Should not happen
		if (currentStepIndex == 0)
			return;
	
		goToStep(currentStepIndex - 1);
	}
	
	private void goToStep(int index) {

		IStep newStep = steps.get(index);
		
		// FIXME Known issue :
		// si la derniere step est disable, on va avoir des ennuis
		if( ! newStep.isEnable()) {
			breadCrumb.disableNextItem();
			if (index == currentStepIndex + 1) {
				currentStepIndex = index;
				goToStep(index + 1);
			} else {
				currentStepIndex = index;
				goToStep(index - 1);
			}
			return;
		} else {
			breadCrumb.getItem(index).setEnabled(true);
		}
		
		
		// On ne recrée pas l'UI si elle existe
		if (newStep.getBody() == null || newStep.getBody().isDisposed())
			newStep.createBody(bodyComposite, dataContainer);
		
		// Si la step précendante était avant dans le fil d'execution, on reset les
		// valeurs par defaut de la nouvelle step
		if (currentStepIndex > index)
			currentStep.resetUI();
		
		currentStep = newStep;
		
		stackLayout.topControl = currentStep.getBody();
		bodyComposite.layout();

		if (index == currentStepIndex + 1)
			breadCrumb.goNextItem();
		else if (index == currentStepIndex - 1)
			breadCrumb.goPrevItem();
		else
			breadCrumb.goTo(index);
		
		currentStepIndex = index;
		
		menuComposite.layout();
		
		setupBtState();
	}
	
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	
	@Override
	protected void cancelPressed() {
		if ( ! MessageDialog.openQuestion(this.getShell(), "Attention", "Vous êtes sur de fermer cette fenêtre. "
				+ "Toutes les modifications non sauvegardées seront perdues. Vous vraiment continuer ?"))
			return;
		
		super.cancelPressed();
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.BACK_ID) {
			previousPressed();
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// pas de bouton par defaut
		parent.dispose();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(this.title);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1200, 850);
	}
	

	@Override
	protected boolean isResizable() {
		return true;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public Button getBtPrevious() {
		return btPrevious;
	}
	
	public Button getBtNext() {
		return btNext;
	}
	
	public Button getBtCancel() {
		return btCancel;
	}
}
