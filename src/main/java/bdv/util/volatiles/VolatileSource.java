package bdv.util.volatiles;

import bdv.util.AbstractSource;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

public class VolatileSource< T extends NumericType< T >, V extends Volatile< T > & NumericType< V >> extends AbstractSource< V > {

    public final Source< T > source;

    public final SharedQueue queue;

    private V volatileTypeInstance;

    public VolatileSource(
            final Source< T > source,
            final V type,
			final SharedQueue queue)
    {
        super(type,source.getName());
        this.source = source;
        this.volatileTypeInstance = type;
        this.queue=queue;
    }

    @Override
    public boolean isPresent(int t) {
        return source.isPresent(t);
    }

    @Override
    public RandomAccessibleInterval< V > getSource(final int t, final int level )
    {
        return VolatileViews.wrapAsVolatile(source.getSource(t, level), queue, new CacheHints(LoadingStrategy.VOLATILE, level, true));
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

}
