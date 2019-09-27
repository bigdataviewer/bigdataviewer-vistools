package bdv.util.volatiles;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.CreateInvalid;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.img.WrappedImg;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;

import java.util.Set;

import static net.imglib2.img.basictypeaccess.AccessFlags.DIRTY;
import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;

/**
 * Wrap view cascades ending in {@link CachedCellImg} as volatile views.
 * {@link RandomAccessible}s wrapped in this way can be displayed in
 * BigDataViewer while loading asynchronously.
 *
 * @author Tobias Pietzsch
 */
public class VolatileViews
{
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

	// ==============================================================

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
					sourceData.getVolatileType(),
					sourceData.getInvalidate() );
		}
		else if ( rai instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) rai;
			final VolatileViewData< T, V > sourceData = wrapAsVolatileViewData( view.getSource(), queue, hints );
			return new VolatileViewData<>(
					new MixedTransformView<>( sourceData.getImg(), view.getTransformToSource() ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType(),
					sourceData.getInvalidate() );
		}
		else if ( rai instanceof WrappedImg )
		{
			return wrapAsVolatileViewData( ( ( WrappedImg< T > ) rai ).getImg(), queue, hints );
		}

		throw new IllegalArgumentException();
	}

	@SuppressWarnings( "unchecked" )
	private static < T extends NativeType< T >, V extends Volatile< T > & NativeType< V >, A > VolatileViewData< T, V > wrapCachedCellImg(
			final CachedCellImg< T, A > cachedCellImg,
			SharedQueue queue,
			CacheHints hints )
	{
		final T type = cachedCellImg.createLinkedType();
		final CellGrid grid = cachedCellImg.getCellGrid();
		final Cache< Long, Cell< A > > cache = cachedCellImg.getCache();

		final Set< AccessFlags > flags = AccessFlags.ofAccess( cachedCellImg.getAccessType() );
		if ( !flags.contains( VOLATILE ) )
			throw new IllegalArgumentException( "underlying " + CachedCellImg.class.getSimpleName() + " must have volatile access type" );
		final boolean dirty = flags.contains( DIRTY );

		final V vtype = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );
		if ( queue == null )
			queue = new SharedQueue( 1, 1 );
		if ( hints == null )
			hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
		@SuppressWarnings( "rawtypes" )
		final VolatileCache<Long, Cell<? extends VolatileArrayDataAccess< ? >>> volatileCache = createVolatileCache( grid, vtype, dirty, ( Cache ) cache, queue );

		final VolatileCachedCellImg< V, ? extends VolatileArrayDataAccess< ? > > volatileImg = new VolatileCachedCellImg<>( grid, vtype, hints, volatileCache.unchecked()::get );

		return new VolatileViewData<>( volatileImg, queue, type, vtype, volatileCache );
	}

	private static < T extends NativeType< T >, A extends VolatileArrayDataAccess< A > > VolatileCache<Long, Cell<A>> createVolatileCache(
			final CellGrid grid,
			final T type,
			final boolean dirty,
			final Cache< Long, Cell< A > > cache,
			final SharedQueue queue )
	{
		final CreateInvalid< Long, Cell< A > > createInvalid = CreateInvalidVolatileCell.get( grid, type, dirty );
		return new WeakRefVolatileCache<>( cache, queue, createInvalid );
	}
}

