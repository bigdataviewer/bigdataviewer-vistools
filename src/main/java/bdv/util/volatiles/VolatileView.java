package bdv.util.volatiles;

import net.imglib2.Volatile;

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
