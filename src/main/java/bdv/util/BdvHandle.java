package bdv.util;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import bdv.BigDataViewer;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.img.cache.Cache;
import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.DisplayMode;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.VisibilityAndGrouping.Event;
import bdv.viewer.state.SourceState;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

class BdvHandle implements Bdv
{
	private BigDataViewer bdv;

	private final ArrayList< BdvSource > bdvSources;

	private final String frameTitle;

	private final ViewerOptions viewerOptions;

	private boolean hasPlaceHolderSources;

	private final int origNumTimepoints;

	public BdvHandle( final BdvOptions options )
	{
		frameTitle = options.values.getFrameTitle();
		viewerOptions = options.values.getViewerOptions();
		bdv = null;
		bdvSources = new ArrayList<>();
		origNumTimepoints = 1;
	}

	public int getUnusedSetupId()
	{
		return ( bdv == null ) ? 0 : BdvFunctions.getUnusedSetupId( bdv );
	}

	public BigDataViewer getBigDataViewer()
	{
		return bdv;
	}

	public ViewerPanel getViewerPanel()
	{
		return ( bdv == null ) ? null : bdv.getViewer();
	}

	@Override
	public BdvHandle getBdvHandle()
	{
		return this;
	}

	void add(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		if ( bdv == null )
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
					viewerOptions );

			// this triggers repaint when PlaceHolderSources are toggled
			bdv.getViewer().getVisibilityAndGrouping().addUpdateListener(
					new VisibilityAndGrouping.UpdateListener()
					{
						@Override
						public void visibilityChanged( final Event e )
						{
							if ( hasPlaceHolderSources )
								bdv.getViewer().getDisplay().repaint();
						}
					} );

			bdv.getViewer().setDisplayMode( DisplayMode.FUSED );
			bdv.getViewerFrame().setVisible( true );
			InitializeViewerState.initTransform( bdv.getViewer() );
		}
		else
		{
			final SetupAssignments setupAssignments = bdv.getSetupAssignments();
			final ViewerPanel viewer = bdv.getViewer();

			if ( converterSetups != null )
				for ( final ConverterSetup setup : converterSetups )
				{
					setupAssignments.addSetup( setup );
					setup.setViewer( viewer );
				}

			if ( sources != null )
				for ( final SourceAndConverter< ? > soc : sources )
					viewer.addSource( soc );
		}

		updateHasPlaceHolderSources();
		updateNumTimepoints();
	}

	void remove(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final List< TransformListener< AffineTransform3D > > transformListeners,
			final List< OverlayRenderer > overlays )
	{
		if ( bdv == null )
			return;

		final SetupAssignments setupAssignments = bdv.getSetupAssignments();
		final ViewerPanel viewer = bdv.getViewer();

		if ( converterSetups != null )
			for ( final ConverterSetup setup : converterSetups )
				setupAssignments.removeSetup( setup );

		if ( transformListeners != null )
			for ( final TransformListener< AffineTransform3D > l : transformListeners )
				viewer.removeTransformListener( l );

		if ( overlays != null )
			for ( final OverlayRenderer o : overlays )
				viewer.getDisplay().removeOverlayRenderer( o );

		if ( sources != null )
			for ( final SourceAndConverter< ? > soc : sources )
			{
				// is this the last source?
				final List< SourceState< ? > > sourcesLeft = viewer.getState().getSources();
				if ( sourcesLeft.size() == 1 && sourcesLeft.get( 0 ).getSpimSource() == soc.getSpimSource() )
				{
					// it is the last source --> just close the BigDataViewer.
					final ViewerFrame frame = bdv.getViewerFrame();
					frame.dispatchEvent( new WindowEvent( frame, WindowEvent.WINDOW_CLOSING ) );
					bdv = null;
					return;
				}
				viewer.removeSource( soc.getSpimSource() );
			}
	}

	void addBdvSource( final BdvSource bdvSource )
	{
		bdvSources.add( bdvSource );
		updateHasPlaceHolderSources();
		updateNumTimepoints();
	}

	void removeBdvSource( final BdvSource bdvSource )
	{
		bdvSources.remove( bdvSource );
		updateHasPlaceHolderSources();
		updateNumTimepoints();
	}

	private void updateHasPlaceHolderSources()
	{
		for ( final BdvSource s : bdvSources )
			if ( s.isPlaceHolderSource() )
			{
				hasPlaceHolderSources = true;
				return;
			}
		hasPlaceHolderSources = false;
	}

	private void updateNumTimepoints()
	{
		int numTimepoints = origNumTimepoints;
		for ( final BdvSource s : bdvSources )
			numTimepoints = Math.max( numTimepoints, s.getNumTimepoints() );
		bdv.getViewer().setNumTimepoints( numTimepoints );
	}
}
