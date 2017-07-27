package bdv.util;

import java.util.function.Function;

import bdv.viewer.Interpolation;
import net.imglib2.RandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.type.Type;

public interface InterpolationFunction< T extends Type< T > > extends Function< Interpolation, InterpolatorFactory< T, RandomAccessible< T > > >
{
	public abstract InterpolatorFactory< T, RandomAccessible< T > > get( final Interpolation method );
	
	@Override
	public default InterpolatorFactory< T, RandomAccessible< T > > apply( final Interpolation t )
	{
		return get( t );
	}
}
