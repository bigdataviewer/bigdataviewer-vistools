package bdv.util.volatiles;

import bdv.cache.CacheControl;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;

/**
 * Metadata associated with a {@link VolatileView}. It comprises the types
 * of the original and volatile image, a {@link CacheControl} for the
 * volatile cache, and the wrapped {@link RandomAccessible}.
 * <p>
 * {@link VolatileViewData} is used while wrapping deeper layers of a view
 * cascade (ending at a {@link CachedCellImg}) and only on the top layer
 * wrapped as a {@link RandomAccessible} / {@link RandomAccessibleInterval}.
 * </p>
 *
 * @param <T>
 *            original image pixel type
 * @param <V>
 *            corresponding volatile pixel type
 *
 * @author Tobias Pietzsch
 * @author Philipp Hanslovsky
 */
public class VolatileViewData< T, V extends Volatile< T > >
{
	private final RandomAccessible< V > img;

	private final CacheControl cacheControl;

	private final Runnable clearCache;

	private final T type;

	private final V volatileType;

	public VolatileViewData(
			final RandomAccessible< V > img,
			final CacheControl cacheControl,
			final Runnable clearCache,
			final T type,
			final V volatileType )
	{
		this.img = img;
		this.cacheControl = cacheControl;
		this.clearCache = clearCache;
		this.type = type;
		this.volatileType = volatileType;
	}

	/**
	 * Get the wrapped {@link RandomAccessible}.
	 *
	 * @return the wrapped {@link RandomAccessible}
	 */
	public RandomAccessible< V > getImg()
	{
		return img;
	}

	/**
	 * Get the {@link CacheControl} for the {@link CachedCellImg}(s) at the
	 * bottom of the view cascade.
	 *
	 * @return the {@link CacheControl} for the {@link CachedCellImg}(s) at the
	 *         bottom of the view cascade
	 */
	public CacheControl getCacheControl()
	{
		return cacheControl;
	}

	/**
	 * Get a handle to the {@link Cache#invalidateAll()} method of the
	 * underlying {@link Cache}.
	 *
	 * @return a handle to the {@link Cache#invalidateAll()} method of the
	 *         underlying {@link Cache}.
	 */
	public Runnable getClearCache()
	{
		return this.clearCache;
	}

	/**
	 * Get the pixel type of the original image.
	 *
	 * @return the pixel type of the original image
	 */
	public T getType()
	{
		return type;
	}

	/**
	 * Get the pixel type of the wrapped {@link RandomAccessible}.
	 *
	 * @return the pixel type of the wrapped {@link RandomAccessible}
	 */
	public V getVolatileType()
	{
		return volatileType;
	}
}
