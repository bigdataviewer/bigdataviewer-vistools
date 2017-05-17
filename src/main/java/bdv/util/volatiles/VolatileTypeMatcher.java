package bdv.util.volatiles;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.type.volatiles.VolatileUnsignedByteType;
import net.imglib2.type.volatiles.VolatileUnsignedLongType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;

public class VolatileTypeMatcher
{
	public static < T extends NativeType< T > > NativeType< ? > getVolatileTypeForType( final T type )
	{
		if ( type instanceof ARGBType )
			return new VolatileARGBType();
		else if ( type instanceof FloatType )
			return new VolatileFloatType();
		else if ( type instanceof ShortType )
			return new VolatileShortType();
		else if ( type instanceof UnsignedByteType )
			return new VolatileUnsignedByteType();
		else if ( type instanceof UnsignedLongType )
			return new VolatileUnsignedLongType();
		else if ( type instanceof UnsignedShortType )
			return new VolatileUnsignedShortType();
		else
			return null;
	}
}
