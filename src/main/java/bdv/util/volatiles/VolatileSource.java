package bdv.util.volatiles;

import bdv.util.AbstractSource;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.HashMap;

/**
 * Class that wraps a Source that uses concrete type into a source that uses Volatile type
 * queue and cachedhints can be null
 *
 * @param <T>
 *            original image source pixel type
 * @param <V>
 *            corresponding volatile pixel type
 */

public class VolatileSource< T extends NumericType< T >, V extends Volatile< T > & NumericType< V >> extends AbstractSource< V > {

    public final Source< T > source;

    public final SharedQueue queue;

    public final CacheHints cacheHints;

    private V volatileTypeInstance;

    private HashMap<TimepointLevelPair, RandomAccessibleInterval< V >> volatileWrappedSources; // Avoid rewrapping -> limitation if source.get(t,level) is changing...

    public VolatileSource(
            final Source< T > source,
            final V type,
			final SharedQueue queue,
            final CacheHints cachedHints)
    {
        super(type,source.getName());

        this.source = source;
        this.volatileTypeInstance = type;
        this.queue=queue;
        this.cacheHints=cachedHints;
        volatileWrappedSources = new HashMap<>();
    }

    @Override
    public boolean isPresent(int t) {
        return source.isPresent(t);
    }

    @Override
    public RandomAccessibleInterval< V > getSource(final int t, final int level )
    {
        TimepointLevelPair tlp = new TimepointLevelPair(t,level);
        if (!volatileWrappedSources.containsKey(tlp)) {
            volatileWrappedSources.put(tlp, VolatileViews.wrapAsVolatile(source.getSource(t, level), queue, cacheHints));
        }
        return volatileWrappedSources.get(tlp);
    }

    @Override
    public synchronized void getSourceTransform( final int t, final int level, final AffineTransform3D transform )
    {
        source.getSourceTransform( t, level, transform );
    }

    @Override
    public V getType() {
        return volatileTypeInstance;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public VoxelDimensions getVoxelDimensions()
    {
        return source.getVoxelDimensions();
    }

    @Override
    public int getNumMipmapLevels()
    {
        return source.getNumMipmapLevels();
    }

    /**
     * Simple class to use two int (timepoint, level) as a key for a hashmap
     */
    public class TimepointLevelPair {
        int timepoint;
        int level;
        public TimepointLevelPair(int timepoint, int level) {
            this.timepoint=timepoint;
            this.level=level;
        }

        @Override
        public int hashCode() {
            return timepoint * 31 + level;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            TimepointLevelPair tlp = (TimepointLevelPair) obj;
            if ((tlp.hashCode()!=this.hashCode())) {
                return false;
            } else {
                return (timepoint==tlp.timepoint)&&(level==tlp.level);
            }
        }

    }

}
