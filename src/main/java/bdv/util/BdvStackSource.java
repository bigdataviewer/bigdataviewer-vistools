package bdv.util;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.ConverterSetupBounds;
import bdv.viewer.SourceAndConverter;
import java.util.HashSet;
import java.util.List;
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
		for ( final ConverterSetup setup : converterSetups )
			setup.setDisplayRange( min, max );
	}

	@Override
	public void setDisplayRangeBounds( final double min, final double max )
	{
		final ConverterSetupBounds bounds = getBdvHandle().getConverterSetups().getBounds();
		for ( final ConverterSetup setup : converterSetups )
			bounds.setBounds( setup, new Bounds( min, max ) );

		// TODO: REMOVE
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
		getBdvHandle().getViewerPanel().state().setCurrentSource( sources.get( 0 ) );
	}

	@Override
	public boolean isCurrent()
	{
		return sources.contains( getBdvHandle().getViewerPanel().state().getCurrentSource() );
	}

	@Override
	public void setActive( final boolean isActive )
	{
		getBdvHandle().getViewerPanel().state().setSourcesActive( sources, isActive );
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
