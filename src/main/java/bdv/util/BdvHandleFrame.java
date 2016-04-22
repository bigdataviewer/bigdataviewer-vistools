package bdv.util;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import bdv.BigDataViewer;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.img.cache.Cache;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.DisplayMode;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerFrame;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.VisibilityAndGrouping.Event;

class BdvHandleFrame extends BdvHandle
{
	private BigDataViewer bdv;

	private final String frameTitle;

	public BdvHandleFrame( final BdvOptions options )
	{
		super( options );
		frameTitle = options.values.getFrameTitle();
		bdv = null;
	}

	public BigDataViewer getBigDataViewer()
	{
		return bdv;
	}

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
	boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		final Cache cache = new Cache.Dummy();
		final ProgressWriter progressWriter = new ProgressWriterConsole();
		bdv = new BigDataViewer(
				new ArrayList<>( converterSetups ),
				new ArrayList<>( sources ),
				null,
				numTimepoints,
				cache,
				frameTitle,
				progressWriter,
				bdvOptions.values.getViewerOptions() );
		viewer = bdv.getViewer();
		setupAssignments = bdv.getSetupAssignments();

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
