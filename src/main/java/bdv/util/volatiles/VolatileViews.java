package bdv.util.volatiles;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.AccessFlags;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.CreateInvalid;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;

public class VolatileViews
{
	/**
	 * Something that provides {@link VolatileViewData}.
	 *
	 * @param <T>
	 *            original image pixel type
	 * @param <V>
	 *            corresponding volatile pixel type
	 *
	 * @author Tobias Pietzsch
	 */
	public interface VolatileView< T, V extends Volatile< T > >
	{
		public VolatileViewData< T, V > getVolatileViewData();
	}

	public static class VolatileRandomAccessibleIntervalView< T, V extends Volatile< T > >
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

	public static class VolatileRandomAccessibleView< T, V extends Volatile< T > >
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

	public static < T, V extends Volatile< T > > RandomAccessibleInterval< V > wrapAsVolatile(
			final RandomAccessibleInterval< T > rai )
	{
		return wrapAsVolatile( rai, null, null );
	}

	public static < T, V extends Volatile< T > > RandomAccessibleInterval< V > wrapAsVolatile(
			final RandomAccessibleInterval< T > rai,
			final SharedQueue queue )
	{
		return wrapAsVolatile( rai, queue, null );
	}

	public static < T, V extends Volatile< T > > RandomAccessibleInterval< V > wrapAsVolatile(
			final RandomAccessibleInterval< T > rai,
			final SharedQueue queue,
			final CacheHints hints )
	{
		@SuppressWarnings( "unchecked" )
		final VolatileViewData< T, V > viewData = ( VolatileViewData< T, V > ) wrapAsVolatileViewData( rai, queue, hints );
		return new VolatileRandomAccessibleIntervalView<>( viewData );
	}

	public static < T, V extends Volatile< T > > RandomAccessible< V > wrapAsVolatile(
			final RandomAccessible< T > rai )
	{
		return wrapAsVolatile( rai, null, null );
	}

	public static < T, V extends Volatile< T > > RandomAccessible< V > wrapAsVolatile(
			final RandomAccessible< T > rai,
			final SharedQueue queue )
	{
		return wrapAsVolatile( rai, queue, null );
	}

	public static < T, V extends Volatile< T > > RandomAccessible< V > wrapAsVolatile(
			final RandomAccessible< T > rai,
			final SharedQueue queue,
			final CacheHints hints )
	{
		@SuppressWarnings( "unchecked" )
		final VolatileViewData< T, V > viewData = ( VolatileViewData< T, V > ) wrapAsVolatileViewData( rai, queue, hints );
		return new VolatileRandomAccessibleView<>( viewData );
	}

	@SuppressWarnings( "unchecked" )
	private static < T, V extends Volatile< T > > VolatileViewData< T, V > wrapAsVolatileViewData(
			final RandomAccessible< T > rai,
			final SharedQueue queue,
			final CacheHints hints )
	{
		if ( rai instanceof CachedCellImg )
		{
			@SuppressWarnings( "rawtypes" )
			final Object o = wrapCachedCellImg( ( CachedCellImg ) rai, queue, hints );
			/*
			 * Need to assign to a Object first to satisfy Eclipse... Otherwise
			 * the following "unnecessary cast" will be removed, followed by
			 * compile error. Proposed solution: Add cast. Doh...
			 */
			final VolatileViewData< T, V > viewData = ( VolatileViewData< T, V > ) o;
			return viewData;
		}
		else if ( rai instanceof IntervalView )
		{
			final IntervalView< T > view = ( IntervalView< T > ) rai;
			final VolatileViewData< T, V > sourceData = wrapAsVolatileViewData( view.getSource(), queue, hints );
			return new VolatileViewData<>(
					new IntervalView<>( sourceData.getImg(), view ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType() );
		}
		else if ( rai instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) rai;
			final VolatileViewData< T, V > sourceData = wrapAsVolatileViewData( view.getSource(), queue, hints );
			return new VolatileViewData<>(
					new MixedTransformView<>( sourceData.getImg(), view.getTransformToSource() ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType() );
		}

		throw new IllegalArgumentException();
	}

	@SuppressWarnings( "unchecked" )
	private static < T extends NativeType< T >, V extends Volatile< T > & NativeType< V >, A > VolatileViewData< T, V > wrapCachedCellImg(
			final CachedCellImg< T, A > cachedCellImg,
			final SharedQueue queue,
			final CacheHints hints )
	{
		final T type = cachedCellImg.createLinkedType();
		final CellGrid grid = cachedCellImg.getCellGrid();
		final Cache< Long, Cell< A > > cache = cachedCellImg.getCache();

		final AccessFlags[] flags = AccessFlags.of( cachedCellImg.getAccessType() );
		if ( !AccessFlags.isVolatile( flags ) )
			throw new IllegalArgumentException( "underlying " + CachedCellImg.class.getSimpleName() + " must have volatile access type" );

		final V vtype = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );
		@SuppressWarnings( "rawtypes" )
		final VolatileCachedCellImg< V, ? > img = createVolatileCachedCellImg( grid, vtype, flags, ( Cache ) cache, queue, hints );

		return new VolatileViewData<>( img, queue, type, vtype );
	}

	private static < T extends NativeType< T >, A extends VolatileArrayDataAccess< A > > VolatileCachedCellImg< T, A > createVolatileCachedCellImg(
			final CellGrid grid,
			final T type,
			final AccessFlags[] accessFlags,
			final Cache< Long, Cell< A > > cache,
			SharedQueue queue,
			CacheHints hints )
	{
		if ( queue == null )
			queue = new SharedQueue( 1, 1 );
		if ( hints == null )
			hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );

		final CreateInvalid< Long, Cell< A > > createInvalid = CreateInvalidVolatileCell.get( grid, type, accessFlags );
		final VolatileCache< Long, Cell< A > > volatileCache = new WeakRefVolatileCache<>( cache, queue, createInvalid );
		final VolatileCachedCellImg< T, A > volatileImg = new VolatileCachedCellImg<>( grid, type, hints, volatileCache.unchecked()::get );
		return volatileImg;
	}
}
