package bdv.util;

import bdv.viewer.SourceAndConverter;
import net.imglib2.converter.Converter;
import net.imglib2.display.ColorConverter;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.display.ScaledARGBConverter;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Util;

import java.util.Random;

public class ExampleSourceAndConverter4D
{
	public static < R extends RealType< R > > void main( String[] args )
	{
		final Random random = new Random();

		final ArrayImg< IntType, IntArray > ints = ArrayImgs.ints( 100, 100, 100, 5 );
		ints.forEach( t -> t.set( random.nextInt( 65535 ) ) );

		final IntType t = Util.getTypeFromInterval( ints );

		final RandomAccessibleIntervalSource4D< R > raiSource4D
				= new RandomAccessibleIntervalSource4D( ints, t, "4d rai source" );

		final double typeMin = Math.max( 0, Math.min( t.getMinValue(), 65535 ) );
		final double typeMax = Math.max( 0, Math.min( t.getMaxValue(), 65535 ) );
		final RealARGBColorConverter< IntType > colorConverter = RealARGBColorConverter.create( t, typeMin, typeMax );

		final SourceAndConverter sourceAndConverter
				= new SourceAndConverter(
						raiSource4D,
						colorConverter );

		BdvFunctions.show( sourceAndConverter, 5 );
	}
}
