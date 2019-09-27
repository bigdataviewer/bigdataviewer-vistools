package bdv.util.volatiles;

import java.util.function.Predicate;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.Invalidate;

public class VolatileRandomAccessibleIntervalView< T, V extends Volatile< T > >
		extends AbstractWrappedInterval< RandomAccessibleInterval< V > >
		implements VolatileView< T, V >, RandomAccessibleInterval< V >, Invalidate< Long >
{
	private final VolatileViewData< T, V > viewData;

	public VolatileRandomAccessibleIntervalView(
			final VolatileViewData< T, V > viewData )
	{
		super( ( RandomAccessibleInterval< V > ) viewData.getImg() );
		this.viewData = viewData;
	}

	@Override
	public VolatileViewData< T, V > getVolatileViewData()
	{
		return viewData;
	}

	@Override
	public RandomAccess< V > randomAccess()
	{
		return sourceInterval.randomAccess();
	}

	@Override
	public RandomAccess< V > randomAccess( final Interval interval )
	{
		return sourceInterval.randomAccess( interval );
	}

	@Override
	public void invalidate( Long key )
	{
		this.viewData.invalidate( key );
	}

	@Override
	public void invalidateIf( long parallelismThreshold, Predicate< Long > condition )
	{
		this.viewData.invalidateIf( parallelismThreshold, condition );
	}

	@Override
	public void invalidateAll( long parallelismThreshold )
	{
		this.viewData.invalidateAll( parallelismThreshold );
	}
}
