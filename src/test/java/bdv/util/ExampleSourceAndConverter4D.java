package bdv.util;

import bdv.viewer.SourceAndConverter;
import net.imglib2.converter.Converter;
import net.imglib2.display.ColorConverter;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.display.ScaledARGBConverter;
import net.imglib2.img.Img;
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
	public static void main( String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final int numTimePoints = 5;
		final Img< IntType > ints = ArrayImgs.ints( 100, 100, 100, numTimePoints );
		ints.forEach( t -> t.set( random.nextInt( 65535 ) ) );

		final IntType type = new IntType();
		final RandomAccessibleIntervalSource4D< IntType > raiSource4D =
				new RandomAccessibleIntervalSource4D<>( ints, type, "4d rai source" );
		final RealARGBColorConverter< IntType > colorConverter =
				RealARGBColorConverter.create( type, 0, 65535 );
		final SourceAndConverter< IntType > sourceAndConverter =
				new SourceAndConverter<>( raiSource4D, colorConverter );

		BdvFunctions.show( sourceAndConverter, numTimePoints );
	}
}
