package bdv.util;

import java.util.ArrayList;
import java.util.List;

import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

abstract class BdvHandle implements Bdv
{
	protected ViewerPanel viewer;

	protected SetupAssignments setupAssignments;

	protected final ArrayList< BdvSource > bdvSources;

	protected final ViewerOptions viewerOptions;

	protected boolean hasPlaceHolderSources;

	protected final int origNumTimepoints;

	public BdvHandle( final BdvOptions options )
	{
		viewerOptions = options.values.getViewerOptions();
		bdvSources = new ArrayList<>();
		origNumTimepoints = 1;
	}

	@Override
	public BdvHandle getBdvHandle()
	{
		return this;
	}

	public ViewerPanel getViewerPanel()
	{
		return viewer;
	}

	public SetupAssignments getSetupAssignments()
	{
		return setupAssignments;
	}

	public int getUnusedSetupId()
	{
		return ( setupAssignments == null ) ? 0 : BdvFunctions.getUnusedSetupId( setupAssignments );
	}

	abstract void createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints );

	void add(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		if ( viewer == null )
		{
			createViewer( converterSetups, sources, numTimepoints );
		}
		else
		{
			final boolean initTransform = ( viewer.getState().numSources() == 0 ) && !sources.isEmpty();

			if ( converterSetups != null )
				for ( final ConverterSetup setup : converterSetups )
				{
					setupAssignments.addSetup( setup );
					setup.setViewer( viewer );
				}

			if ( sources != null )
				for ( final SourceAndConverter< ? > soc : sources )
					viewer.addSource( soc );

			InitializeViewerState.initTransform( viewer );

			if ( initTransform )
				InitializeViewerState.initTransform( viewer );
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
		if ( viewer == null )
			return;

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
				viewer.removeSource( soc.getSpimSource() );
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

	void updateHasPlaceHolderSources()
	{
		for ( final BdvSource s : bdvSources )
			if ( s.isPlaceHolderSource() )
			{
				hasPlaceHolderSources = true;
				return;
			}
		hasPlaceHolderSources = false;
	}

	void updateNumTimepoints()
	{
		int numTimepoints = origNumTimepoints;
		for ( final BdvSource s : bdvSources )
			numTimepoints = Math.max( numTimepoints, s.getNumTimepoints() );
		if ( viewer != null )
			viewer.setNumTimepoints( numTimepoints );
	}
}
