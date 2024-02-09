package bdv.util.volatiles;

import net.imglib2.cache.AbstractCache;
import net.imglib2.cache.volatiles.AbstractVolatileCache;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         This interface is used to expose controls of
 *         {@link AbstractVolatileCache volatile caches} and
 *         {@link AbstractCache caches}, e.g. in {@link VolatileViewData}.
 *         Methods in this interface may have implications and/or side-effects
 *         and should be used with caution/by callers who are aware of these.
 *
 */
public interface CacheControlUnsafe
{

	/**
	 * Invalidate all existing cache entries, e.g.
	 * {@link AbstractCache#invalidateAll() or
	 * {@link AbstractVolatileCache#invalidateAll()}}.
	 */
	public void invalidateAll();

}
