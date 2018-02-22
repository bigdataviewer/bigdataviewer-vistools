package bdv.util;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.BehaviourTransformEventHandler;
import bdv.BehaviourTransformEventHandlerFactory;
import bdv.BigDataViewer;
import bdv.BigDataViewerActions;
import bdv.cache.CacheControl.CacheControls;
import bdv.tools.VisibilityAndGroupingDialog;
import bdv.tools.bookmarks.Bookmarks;
import bdv.tools.bookmarks.BookmarksEditor;
import bdv.tools.brightness.BrightnessDialog;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.DisplayMode;
import bdv.viewer.NavigationActions;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerPanel.AlignPlane;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;

public class BdvHandlePanel extends BdvHandle
{
	private final BrightnessDialog brightnessDialog;

	private final VisibilityAndGroupingDialog activeSourcesDialog;

	private final ManualTransformationEditor manualTransformationEditor;

	private final Bookmarks bookmarks;

	private final BookmarksEditor bookmarksEditor;

	private final InputActionBindings keybindings;

	private final TriggerBehaviourBindings triggerbindings;

	public BdvHandlePanel( final Frame dialogOwner, final BdvOptions options )
	{
		super( options );

		final ViewerOptions viewerOptions = options.values.getViewerOptions();
		final InputTriggerConfig inputTriggerConfig = BigDataViewer.getInputTriggerConfig( viewerOptions );

		final TransformEventHandlerFactory< AffineTransform3D > thf = viewerOptions.values.getTransformEventHandlerFactory();
		if ( thf instanceof BehaviourTransformEventHandlerFactory )
			( ( BehaviourTransformEventHandlerFactory< ? > ) thf ).setConfig( inputTriggerConfig );

		cacheControls = new CacheControls();

		viewer = new ViewerPanel( new ArrayList<>(), 1, cacheControls, viewerOptions );
		if ( !options.values.hasPreferredSize() )
			viewer.getDisplay().setPreferredSize( null );
		viewer.getDisplay().addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				tryInitTransform();
			}
		} );

		setupAssignments = new SetupAssignments( new ArrayList<>(), 0, 65535 );

		keybindings = new InputActionBindings();
		SwingUtilities.replaceUIActionMap( viewer, keybindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( viewer, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keybindings.getConcatenatedInputMap() );

		triggerbindings = new TriggerBehaviourBindings();
		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		viewer.getDisplay().addHandler( mouseAndKeyHandler );

		final TransformEventHandler< ? > tfHandler = viewer.getDisplay().getTransformEventHandler();
		if ( tfHandler instanceof BehaviourTransformEventHandler )
			( ( BehaviourTransformEventHandler< ? > ) tfHandler ).install( triggerbindings );

		manualTransformationEditor = new ManualTransformationEditor( viewer, keybindings );

		bookmarks = new Bookmarks();
		bookmarksEditor = new BookmarksEditor( viewer, keybindings, bookmarks );

		brightnessDialog = new BrightnessDialog( dialogOwner, setupAssignments );
		activeSourcesDialog = new VisibilityAndGroupingDialog( dialogOwner, viewer.getVisibilityAndGrouping() );

		final NavigationActions navactions = new NavigationActions( inputTriggerConfig );
		navactions.install( keybindings, "navigation" );
		navactions.modes( viewer );
		navactions.sources( viewer );
		navactions.time( viewer );
		if ( options.values.is2D() )
			navactions.alignPlaneAction( viewer, AlignPlane.XY, "shift Z" );
		else
			navactions.alignPlanes( viewer );

		final BigDataViewerActions bdvactions = new BigDataViewerActions( inputTriggerConfig );
		bdvactions.install( keybindings, "bdv" );
		bdvactions.dialog( brightnessDialog );
		bdvactions.dialog( activeSourcesDialog );
		bdvactions.bookmarks( bookmarksEditor );
		bdvactions.manualTransform( manualTransformationEditor );

		viewer.setDisplayMode( DisplayMode.FUSED );
	}

	@Override
	public ManualTransformationEditor getManualTransformEditor()
	{
		return manualTransformationEditor;
	}

	@Override
	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	@Override
	public TriggerBehaviourBindings getTriggerbindings()
	{
		return triggerbindings;
	}

	@Override
	boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void close()
	{
		viewer.stop();
		brightnessDialog.dispose();
		activeSourcesDialog.dispose();
	}
}
