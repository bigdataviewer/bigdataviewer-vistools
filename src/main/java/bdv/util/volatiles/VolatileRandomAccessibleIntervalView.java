package bdv.util.volatiles;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;

public class VolatileRandomAccessibleIntervalView< T, V extends Volatile< T > >
	extends AbstractWrappedInterval< RandomAccessibleInterval< V > >
	implements VolatileView< T, V >, RandomAccessibleInterval< V >
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
}
