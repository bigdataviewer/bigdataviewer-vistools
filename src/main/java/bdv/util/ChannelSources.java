package bdv.util;

import bdv.cache.CacheControl;
import bdv.viewer.SourceAndConverter;
import java.util.List;

/**
 * Channels of an image as a collection of {@link SourceAndConverter
 * sources}. The channels should all have the same pixel type, number of
 * timepoints, and dimensions.
 */
public interface ChannelSources< T >
{
	/**
	 * Get the list of sources, one for each channel.
	 */
	List< SourceAndConverter< T > > getSources();

	/**
	 * Get the number timepoints.
	 */
	int numTimepoints();

	/**
	 * Get (an instance of) the pixel type.
	 */
	default T getType()
	{
		return getSources().get( 0 ).getSpimSource().getType();
	}

	/**
	 * Get handle for controlling cache behaviour.
	 */
	default CacheControl getCacheControl()
	{
		return null;
	}
}
