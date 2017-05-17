package bdv.util.volatiles;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.Volatile;

public class VolatileRandomAccessibleView< T, V extends Volatile< T > >
	implements VolatileView< T, V >, RandomAccessible< V >
{
	private final VolatileViewData< T, V > viewData;

	public VolatileRandomAccessibleView(
			final VolatileViewData< T, V > viewData )
	{
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
		return viewData.getImg().randomAccess();
	}

	@Override
	public RandomAccess< V > randomAccess( final Interval interval )
	{
		return viewData.getImg().randomAccess( interval );
	}

	@Override
	public int numDimensions()
	{
		return viewData.getImg().numDimensions();

	}
}
