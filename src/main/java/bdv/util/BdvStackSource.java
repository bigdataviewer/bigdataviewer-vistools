package bdv.util;

import java.util.List;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;

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
		getBdvHandle().remove( converterSetups, sources, null, null );
		getBdvHandle().removeBdvSource( this );
		setBdvHandle( null );
	}

	@Override
	protected boolean isPlaceHolderSource()
	{
		return false;
	}

//	public T getType()
//	{
//		return type;
//	}
//
//	public List< ConverterSetup > getConverterSetups()
//	{
//		return converterSetups;
//	}
//
//	public List< SourceAndConverter< T > > getSources()
//	{
//		return sources;
//	}
}
