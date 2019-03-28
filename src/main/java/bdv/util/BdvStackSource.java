package bdv.util;

import java.util.HashSet;
import java.util.List;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import net.imglib2.type.numeric.ARGBType;

public class BdvStackSource< T > extends BdvSource
{
	private final T type;

	private final List< ConverterSetup > converterSetups;

	private final List< SourceAndConverter< T > > sources;

	protected BdvStackSource(
			final BdvHandle bdv,
			final int numTimepoints,
			final T type,
			final List< ConverterSetup > converterSetups,
			final List< SourceAndConverter< T > > sources )
	{
		super( bdv, numTimepoints );
		this.type = type;
		this.converterSetups = converterSetups;
		this.sources = sources;
	}

	@Override
	public void removeFromBdv()
	{
		getBdvHandle().remove( converterSetups, sources, null, null, null, null );
		getBdvHandle().removeBdvSource( this );
		setBdvHandle( null );
	}

	@Override
	protected boolean isPlaceHolderSource()
	{
		return false;
	}

	@Override
	public void setColor( final ARGBType color )
	{
		for ( final ConverterSetup setup : converterSetups )
			setup.setColor( color );
	}

	@Override
	public void setDisplayRange( final double min, final double max )
	{
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignments sa = getBdvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			group.getMinBoundedValue().setCurrentValue( min );
			group.getMaxBoundedValue().setCurrentValue( max );
		}
	}

	@Override
	public void setDisplayRangeBounds( final double min, final double max )
	{
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignments sa = getBdvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
			group.setRange( min, max );
	}

	@Override
	public void setCurrent()
	{
		getBdvHandle().getViewerPanel().getVisibilityAndGrouping().setCurrentSource( sources.get( 0 ).getSpimSource() );
	}

	@Override
	public boolean isCurrent()
	{
		final ViewerState state = getBdvHandle().getViewerPanel().getState();
		final List< SourceState< ? > > ss = state.getSources();
		final int i = state.getCurrentSource();
		if ( i >= 0 && i < ss.size() )
		{
			final Source< ? > spimSource = ss.get( i ).getSpimSource();
			for ( final SourceAndConverter< T > source : sources )
				if ( spimSource.equals( source.getSpimSource() ) )
					return true;
		}
		return false;
	}

	@Override
	public void setActive( final boolean isActive )
	{
		for ( final SourceAndConverter< T > source : sources )
			getBdvHandle().getViewerPanel().getVisibilityAndGrouping().setSourceActive( source.getSpimSource(), isActive );
	}

//	public T getType()
//	{
//		return type;
//	}

	public List< ConverterSetup > getConverterSetups()
	{
		return converterSetups;
	}

	public List< SourceAndConverter< T > > getSources()
	{
		return sources;
	}
}
