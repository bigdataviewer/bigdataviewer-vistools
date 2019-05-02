package bdv.util;

import javax.swing.JDialog;

import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping;

public class SelectionAndGroupingDialog extends JDialog {

	private SelectionAndGroupingTabs selGro;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectionAndGroupingDialog(final ViewerPanel vp, final VisibilityAndGrouping visGro,
			final ManualTransformationEditor manualTE, final SetupAssignments sa) {
		selGro = new SelectionAndGroupingTabs(vp, visGro, manualTE, sa);
		this.add(selGro);
	}

	public SelectionAndGroupingTabs getSelectionAndGrouping() {
		return selGro;
	}
}
