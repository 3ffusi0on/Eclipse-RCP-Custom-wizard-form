/*******************************************************************************
 * Copyright (c) 2012 Laurent CARON. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation.
 *     Haixing Hu (https://github.com/Haixing-Hu/)  - Modification for personal use.
 *******************************************************************************/
package fr.com.app.ui.widgets.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import fr.com.app.ui.widgets.wizard.BreadCrumbItem.EtatBreadCrumItemEnum;




/**
 * Instances of this class support the layout of selectable bar items displayed
 * in a bread crumb.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>BreadCrumbItem</code>.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 */
public class BreadCrumb extends Canvas {

  private static final String IS_BUTTON_PRESSED = BreadCrumb.class.toString() + "_pressed";
  private final List<BreadCrumbItem> items;
  private static Color START_GRADIENT_COLOR = new Color(Display.getCurrent(), 255, 255, 255);
  private static Color END_GRADIENT_COLOR = new Color(Display.getCurrent(), 224, 224, 224);
  static Color BORDER_COLOR = new Color(Display.getCurrent(), 128, 128, 128);
  static Color BORDER_COLOR_1 = new Color(Display.getCurrent(), 212, 212, 212);
  static Color BORDER_COLOR_2 = new Color(Display.getCurrent(), 229, 229, 229);
  static Color BORDER_COLOR_3 = new Color(Display.getCurrent(), 243, 243, 243);
  boolean hasBorder = false;
  
  private BreadCrumbItem selectedItem;

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent
   *          a composite control which will be the parent of the new instance
   *          (cannot be null)
   * @param style
   *          the style of control to construct
   *
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   *
   * @see Widget#getStyle()
   */
  public BreadCrumb(final Composite parent, final int style) {
    super(parent, checkStyle(style) | SWT.DOUBLE_BUFFERED);
    this.items = new ArrayList<BreadCrumbItem>();
    this.hasBorder = (style & SWT.BORDER) != 0;
    addListeners();
  }

  private static int checkStyle(final int style) {
    if ((style & SWT.BORDER) != 0) {
      return style & ~ SWT.BORDER;
    }
    return 0;
  }

  private void addListeners() {
    addMouseDownListener();
    addMouseUpListener();
    addMouseHoverListener();
    addPaintListener(new PaintListener() {
      @Override
      public void paintControl(final PaintEvent e) {
        BreadCrumb.this.paintControl(e);
      }
    });
  }

  private void addMouseDownListener() {
    addListener(SWT.MouseDown, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        for (final BreadCrumbItem item : BreadCrumb.this.items) {
          if (item.getBounds().contains(event.x, event.y)) {
            final boolean isToggle = (item.getStyle() & SWT.TOGGLE) != 0;
            final boolean isPush = (item.getStyle() & SWT.PUSH) != 0;
            if (isToggle || isPush) {
            	if (!item.getEtat().equals(EtatBreadCrumItemEnum.TODO)) {
            		item.setSelection(! item.getSelection());
            		redraw();
            		update();
            	}
            }
            item.setData(IS_BUTTON_PRESSED, "*");
            return;
          }
        }
      }
    });
  }

  private void addMouseUpListener() {
    addListener(SWT.MouseUp, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        for (final BreadCrumbItem item : BreadCrumb.this.items) {
          if (item.getBounds().contains(event.x, event.y)) {
            if (item.getData(IS_BUTTON_PRESSED) == null) {
              // The button was not pressed
              return;
            }
            item.setData(IS_BUTTON_PRESSED, null);

            if ((item.getStyle() & SWT.PUSH) != 0) {
              item.setSelection(false);
            }

            if ((item.getStyle() & (SWT.TOGGLE | SWT.PUSH)) != 0) {
            	if (!item.getEtat().equals(EtatBreadCrumItemEnum.TODO)) {
            		item.fireSelectionEvent();
            		redraw();
            		update();
            	}
            }
            return;
          }
        }
      }
    });
  }

  private void addMouseHoverListener() {
    addListener(SWT.MouseHover, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        for (final BreadCrumbItem item : BreadCrumb.this.items) {
          if (item.getBounds().contains(event.x, event.y)) {
            setToolTipText(item.getTooltipText() == null ? "" : item
                .getTooltipText());
            return;
          }
        }
      }
    });
  }

  /**
   * Paint the component
   *
   * @param e
   *          event
   */
  private void paintControl(final PaintEvent e) {
    final GC gc = e.gc;
    gc.setAdvanced(true);
    gc.setAntialias(SWT.ON);

    final int width = getSize().x;
    final int height = getSize().y;

    drawBackground(gc, width, height);
    final Iterator<BreadCrumbItem> it = this.items.iterator();
    int x = 0;
    while (it.hasNext()) {
      final BreadCrumbItem item = it.next();
      item.setGc(gc).setToolbarHeight(height).setIsLastItemOfTheBreadCrumb(! it.hasNext());
      item.drawButtonAtPosition(x);
      x += item.getWidth();
    }
  }

  private void drawBackground(final GC gc, final int width, final int height) {
    gc.setForeground(START_GRADIENT_COLOR);
    gc.setBackground(END_GRADIENT_COLOR);
    gc.fillGradientRectangle(0, 0, width, height, true);

    if (this.hasBorder) {
      gc.setForeground(BORDER_COLOR);
      gc.drawRectangle(0, 0, width - 1, height - 1);
    }
  }

  /**
   * Add an item to the toolbar
   *
   * @param item
   *          roundedToolItem to add
   */
  void addItem(final BreadCrumbItem item) {
    this.items.add(item);
  }
  
  /**
   * Add list an item to the toolbar
   *
   * @param items List<BreadCrumbItem>
   *          roundedToolItem to add
   */
  void addItem(final List<BreadCrumbItem> items) {
	  for (BreadCrumbItem item : items) {
		  this.items.add(item);
	  }
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
   */
  @Override
  public Point computeSize(final int wHint, final int hHint,
      final boolean changed) {
    int width = 0, height = 0;
    for (final BreadCrumbItem item : this.items) {
      width += item.getWidth();
      height = Math.max(height, item.getHeight());
    }
    return new Point(Math.max(width, wHint), Math.max(height, hHint));
  }

  /**
   * Returns the item at the given, zero-relative index in the receiver. Throws
   * an exception if the index is out of range.
   *
   * @param index
   *          the index of the item to return
   * @return the item at the given index
   *
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public BreadCrumbItem getItem(final int index) {
    checkWidget();
    if ((index < 0) || (index > this.items.size())) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    return this.items.get(index);
  }

  /**
   * Returns the item at the given point in the receiver or null if no such item
   * exists. The point is in the coordinate system of the receiver.
   *
   * @param point
   *          the point used to locate the item
   * @return the item at the given point
   *
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public BreadCrumbItem getItem(final Point point) {
    checkWidget();
    for (final BreadCrumbItem item : this.items) {
      if (item.getBounds().contains(point)) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the number of items contained in the receiver.
   *
   * @return the number of items
   *
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getItemCount() {
    checkWidget();
    return this.items.size();
  }

  /**
   * Returns an array of <code>BreadCrumbItem</code>s which are the items in the
   * receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain its
   * list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the items in the receiver
   *
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public BreadCrumbItem[] getItems() {
    checkWidget();
    return this.items.toArray(new BreadCrumbItem[this.items.size()]);
  }

  /**
   * Searches the receiver's list starting at the first item (index 0) until an
   * item is found that is equal to the argument, and returns the index of that
   * item. If no item is found, returns -1.
   *
   * @param item
   *          the search item
   * @return the index of the item
   *
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the item is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int indexOf(final BreadCrumbItem item) {
    checkWidget();
    return this.items.indexOf(item);
  }

  /**
   * Remove an item to the toolbar
   *
   * @param item
   *          item to remove
   */
  public void removeItem(final BreadCrumbItem item) {
    this.items.remove(item);
  }
  
  /**
   * Méthode qui permet de sélectionner un item
   * @param numItem int
   */
  public void selectedItem(int numItem) {
	  // On désélectionne tout les items
	  for (BreadCrumbItem item : items) 
		  item.setSelection(false);
	  
	  // On sélection l'item choisit
	  if (numItem >= 0 && numItem < items.size()) {
		  this.items.get(numItem).setSelection(true);
		  selectedItem = this.items.get(numItem);
	  }
	  selectedItem.setEtat(EtatBreadCrumItemEnum.SELECTED);
	  update();
	  redraw();
  }
  
  /**
   * Méthode qui passe à l'item suivant
   */
  public void goNextItem() {
	  selectedItem.setEtat(EtatBreadCrumItemEnum.DONE);
	  
	  for (int i = indexOf(selectedItem) ; i < items.size(); i++) {
		  if ((i + 1) < items.size() && items.get(i + 1).isEnabled()) {
			  selectedItem = items.get(i+1);
			  selectedItem.setEtat(EtatBreadCrumItemEnum.SELECTED);
			  break;
		  }
	  }
	  update();
	  redraw();
  }
  
  /**
   * Méthode qui passe à l'item précédent
   */
  public void goPrevItem() {
	  selectedItem.setEtat(EtatBreadCrumItemEnum.TODO);
	  // On regarde le prochain item activé
	  for (int i = indexOf(selectedItem); i >= 0; i--) {
		  if ((i - 1) >= 0 && items.get(i - 1).isEnabled()) {
			  selectedItem = items.get(i-1);
			  selectedItem.setEtat(EtatBreadCrumItemEnum.SELECTED);
			  break;
		  }
	  }
	  update();
	  redraw();
  }
  
  /**
   * Méthode qui permet de désactiver un item
   */
  public void disableNextItem() {
	  // On regarde le prochain item activé
	  for (int i = 0; i < items.size(); i++) {
		  if (items.get(i).equals(selectedItem) && (i + 1) < items.size()) {
			  items.get(i+1).setEtat(EtatBreadCrumItemEnum.DISABLED);
			  items.get(i+1).setEnabled(false);
			  break;
		  }
	  }
	  update();
	  redraw();
  }
  
  /**
   * Sélection d'un item
   * @param item
   */
  public void goTo(int index) {
	  BreadCrumbItem item = this.getItem(index);
	  if (item.getEtat().equals(EtatBreadCrumItemEnum.DONE)) {
		  selectedItem.setEtat(EtatBreadCrumItemEnum.DONE);
		  item.setEtat(EtatBreadCrumItemEnum.SELECTED);
		  selectedItem = item;
	  }
  }

}
