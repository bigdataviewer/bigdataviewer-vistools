package bdv.util;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class TestFastRender
{
	public static void main( String[] args )
	{
		// grid 3x3
		final Random rnd = new Random( 2413 );

		BdvOptions options = BdvOptions.options();
		BdvStackSource<?> source = null;

		for ( int x = 0; x < 3; ++x )
			for ( int y = 0; y < 3; ++y )
			{
				final RandomAccessibleInterval<FloatType> img =
					Views.translate(
						ArrayImgs.floats( 512, 512, 100 ),
						x * 500, y * 500, 0 );

				final int value = rnd.nextInt( 100 ) + 50;

				for ( final FloatType t : Views.iterable( img ) )
					t.set( value + rnd.nextFloat() * 20 );

				if ( source != null )
					options.addTo( source );

				source = BdvFunctions.show(img, "x="+x+",y="+y, options );
				source.setDisplayRange( 0, 255 );
			}
	}
}
