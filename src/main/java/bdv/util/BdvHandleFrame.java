package bdv.util;

import bdv.viewer.ViewerStateChange;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.BigDataViewer;
import bdv.cache.CacheControl.CacheControls;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.DisplayMode;
import bdv.viewer.NavigationActions;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel.AlignPlane;

public class BdvHandleFrame extends BdvHandle
{
	private BigDataViewer bdv;

	private final String frameTitle;

	BdvHandleFrame( final BdvOptions options )
	{
		super( options );
		frameTitle = options.values.getFrameTitle();
		bdv = null;
		cacheControls = new CacheControls();
	}

	public BigDataViewer getBigDataViewer()
	{
		return bdv;
	}

	@Override
	public void close()
	{
		if ( bdv != null )
		{
			final ViewerFrame frame = bdv.getViewerFrame();
			frame.dispatchEvent( new WindowEvent( frame, WindowEvent.WINDOW_CLOSING ) );
			bdv = null;
		}
		super.close();
	}

	@Override
	public ManualTransformationEditor getManualTransformEditor()
	{
		return bdv.getManualTransformEditor();
	}

	@Override
	public InputActionBindings getKeybindings()
	{
		return bdv.getViewerFrame().getKeybindings();
	}

	@Override
	public TriggerBehaviourBindings getTriggerbindings()
	{
		return bdv.getViewerFrame().getTriggerbindings();
	}

	@Override
	boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		final ProgressWriter progressWriter = new ProgressWriterConsole();
		final ViewerOptions viewerOptions = bdvOptions.values.getViewerOptions();
		final InputTriggerConfig inputTriggerConfig = BigDataViewer.getInputTriggerConfig( viewerOptions );
		bdv = new BigDataViewer(
				new ArrayList<>( converterSetups ),
				new ArrayList<>( sources ),
				null,
				numTimepoints,
				cacheControls,
				frameTitle,
				progressWriter,
				viewerOptions.inputTriggerConfig( inputTriggerConfig ) );
		viewer = bdv.getViewer();
		setupAssignments = bdv.getSetupAssignments();
		setups = bdv.getConverterSetups();

		if ( bdvOptions.values.is2D() )
		{
			final InputActionBindings keybindings = bdv.getViewerFrame().getKeybindings();
			final NavigationActions navactions = new NavigationActions( inputTriggerConfig );
			navactions.install( keybindings, "navigation" );
			navactions.modes( viewer );
			navactions.sources( viewer );
			navactions.time( viewer );
			navactions.alignPlaneAction( viewer, AlignPlane.XY, "shift Z" );
		}

		// this triggers repaint when PlaceHolderSources are toggled
		viewer.state().changeListeners().add( change -> {
			if ( change == ViewerStateChange.VISIBILITY_CHANGED )
				viewer.getDisplay().repaint();
		} );

		viewer.setDisplayMode( DisplayMode.FUSED );
		bdv.getViewerFrame().setVisible( true );

		final boolean initTransform = !sources.isEmpty();
		return initTransform;
	}
}
