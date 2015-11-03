/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 14, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr;

import java.awt.event.ActionListener;
import java.util.List;

import com.sandy.apps.pluto.shared.STException ;

/**
 * This interface defines the contract that any configuration panel needs to
 * implement to be added as a managed component under the configuration wizard
 * dialog. The operations of this interface defines the inversion of control
 * points which will be invoked by the configuration wizard during different
 * state change scenarios.
 * <p>
 * Note that the dialog manager can also house non data entry panels. For non
 * data entry panels, most of the methods of this interface are not required.
 * In such a case, the subclass panel can choose to derive from an abstract
 * implementation of this interface named AbstractDialogPanel.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IPlutoFramePanel extends ActionListener {

    /** @return The name of this panel. This name will be used for display. */
    public String getName() ;

    /**
     * Concrete implementations can return a two dimensional array of Objects
     * representing the action buttons to be displayed for this particular
     * panel. Each row in the array corresponds to the the attributes of a
     * button -
     * <ul>
     *  <li>Element 0 [String][M] - Action command for the button</li>
     *  <li>Element 1 [Image] [M] - Button image</li>
     *  <li>Element 2 [Image] [O] - Pressed image for button</li>
     *  <li>Element 3 [Image] [O] - Roll over image for button[</li>
     * </ul>
     * <p>
     * The cancel icon is appended to the action panel by default and pressing
     * the cancel will result in disposing the configuration wizard.
     *
     * @return A two dimensional array of button attributes.
     */
    public Object[][] getPanelIcons() ;

    /**
     * This is the first method invoked by the configuration wizard on the
     * chart panel. Concrete subclasses should implement this operation to
     * fetch the data that has to be pre populated in the UI controls before
     * the panel is made visible.
     * <p>
     * The concrete implementation are also expected to implement the logic of
     * setting up the UI and setting the state of the UI components based on the
     * initialization data retrieved during the initialize data operation.
     * <p>
     * This method will also be invoked when a tab is made visible.
     *
     * @throws STException If an exception condition is encountered during
     *         the initialization process.
     */
    public void initializeData() throws STException ;

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() ;

    /**
     * This operation is invoked by the wizard when the user changes tabs in
     * the wizard. Concrete implementations should return a true if the data
     * has been changed by the user since the last time it was loaded. If a
     * true is returned, the wizard will ask for user confirmation before the
     * target tab is highlighted.
     *
     * @return true if the data has been changed, false otherwise.
     *
     * @throws STException If an exception condition is encountered.
     */
    public boolean isDirty() ;

    /**
     * This method is called before the panel is asked to save it's data. If
     * this method returns a non empty list, the save operation will not be
     * invoked but the messages will be shown to the user and the user given
     * a chance to correct the errors and save again.
     *
     * @return A list of strings, each representing an error message or a null
     *         or an empty list if there are no validation errors.
     *
     * @throws STException If an exception condition is encountered during
     *         validation.
     */
    public List<String> validateUserInput() throws STException ;

    /**
     * This method is invoked by the wizard only if the following conditions
     * are satisfied:
     * <ul>
     *  <li>The user has clicked on the OK button</li>
     *  <li>The tab has dirty data</li>
     *  <li>The validation of the data in the tab has been successful</li>
     * </ul>
     * Concrete implementations should implement this method to persist the
     * configuration data.
     *
     * @throws STException If an exception condition is encountered during
     *         validation.
     */
    public void save() throws STException ;

    /**
     * This method is called by the dialog manager on the instance of the
     * dialog panel to register itself with the panel. The reference passed
     * as parameter can be saved by the dialog panel and used to invoke
     * operations provided by the dialog manager, for example requests for
     * enabling or disabling a particular button based on its action command.
     */
    public void setDialogManager( final PlutoInternalFrame dlgMgr ) ;

    /**
     * Returns the title of the panel. It is not necessary that the panel
     * implements this method. The return value of this method is used to
     * show a visual marker when the panel is hidden or minimized. If the
     * panel does not implement this method, the UI framewok will use the
     * display title of the panel.
     *
     * By default this method returns null.
     */
    public String getTitle() ;
}
