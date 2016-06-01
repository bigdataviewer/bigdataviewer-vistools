package bdv.util;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BigDataViewer;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.img.cache.Cache;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.DisplayMode;
import bdv.viewer.InputActionBindings;
import bdv.viewer.NavigationActions;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TriggerBehaviourBindings;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel.AlignPlane;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.VisibilityAndGrouping.Event;

public class BdvHandleFrame extends BdvHandle
{
	private BigDataViewer bdv;

	private final String frameTitle;

	BdvHandleFrame( final BdvOptions options )
	{
		super( options );
		frameTitle = options.values.getFrameTitle();
		bdv = null;
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
			viewer = null;
			setupAssignments = null;
			bdvSources.clear();
		}
	}

	@Override
	public InputActionBindings getKeybindings() {
		return bdv.getViewerFrame().getKeybindings();
	}

	@Override
	public TriggerBehaviourBindings getTriggerbindings() {
		return bdv.getViewerFrame().getTriggerbindings();
	}

	@Override
	boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		final Cache cache = new Cache.Dummy();
		final ProgressWriter progressWriter = new ProgressWriterConsole();
		final ViewerOptions viewerOptions = bdvOptions.values.getViewerOptions();
		bdv = new BigDataViewer(
				new ArrayList<>( converterSetups ),
				new ArrayList<>( sources ),
				null,
				numTimepoints,
				cache,
				frameTitle,
				progressWriter,
				viewerOptions );
		viewer = bdv.getViewer();
		setupAssignments = bdv.getSetupAssignments();

		if ( bdvOptions.values.is2D() )
		{
			final InputTriggerConfig inputTriggerConfig = BigDataViewer.getInputTriggerConfig( viewerOptions );
			final InputActionBindings keybindings = bdv.getViewerFrame().getKeybindings();
			final NavigationActions navactions = new NavigationActions( keybindings, inputTriggerConfig );
			navactions.modes( viewer );
			navactions.sources( viewer );
			navactions.time( viewer );
			navactions.alignPlaneAction( viewer, AlignPlane.XY, "shift Z" );
		}

		// this triggers repaint when PlaceHolderSources are toggled
		viewer.getVisibilityAndGrouping().addUpdateListener(
				new VisibilityAndGrouping.UpdateListener()
				{
					@Override
					public void visibilityChanged( final Event e )
					{
						if ( hasPlaceHolderSources )
							viewer.getDisplay().repaint();
					}
				} );

		viewer.setDisplayMode( DisplayMode.FUSED );
		bdv.getViewerFrame().setVisible( true );

		final boolean initTransform = !sources.isEmpty();
		return initTransform;
	}
}
