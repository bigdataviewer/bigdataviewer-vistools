package bdv.util;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Util;

import java.util.Random;

public class ExampleSource4D
{
	public static void main( String[] args )
	{
		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100, 5 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF00FF00 ) );

		final RandomAccessibleIntervalSource4D raiSource4D
				= new RandomAccessibleIntervalSource4D(
				img,
				Util.getTypeFromInterval( img ),
				"4d rai source" );

		BdvFunctions.show( raiSource4D, 5 );
	}
}
