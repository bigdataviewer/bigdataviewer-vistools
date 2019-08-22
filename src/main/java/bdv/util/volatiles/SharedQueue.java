package bdv.util.volatiles;

import java.util.concurrent.Callable;

import bdv.cache.CacheControl;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;

/**
 * Queue and threads for asynchronously loading data into a cache
 *
 * @author Tobias Pietzsch
 */
public class SharedQueue extends BlockingFetchQueues< Callable< ? > > implements CacheControl
{
	private final FetcherThreads fetcherThreads;

	public SharedQueue( final int numFetcherThreads, final int numPriorities )
	{
		super( numPriorities, numFetcherThreads );
		fetcherThreads = new FetcherThreads( this, numFetcherThreads );
	}

	public SharedQueue( final int numFetcherThreads )
	{
		this( numFetcherThreads, 1 );
	}

	public void shutdown()
	{
		fetcherThreads.shutdown();
		clear();
	}

	@Override
	public void prepareNextFrame()
	{
		clearToPrefetch();
	}
}
