package bdv.util;

import java.util.Random;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class FrameExample
{
	public static void main( final String[] args )
	{

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();
		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 3 );
		img.forEach( t -> t.set( random.nextInt() ) );
		final ArrayImg< ARGBType, IntArray > img2 = ArrayImgs.argbs( 100, 100, 3 );

		final BdvSource s1 = BdvFunctions.show( img, "img" );
		final BdvSource s2 = BdvFunctions.show( img2, "img2", Bdv.options().addTo( s1 ) );


		final Bdv bdv = BdvFunctions.show( img, "img",
				Bdv.options().is2D().axisOrder( AxisOrder.XYC ) );
		BdvFunctions.show( img2, "img2",
				Bdv.options().addTo( bdv ).axisOrder( AxisOrder.XYC ) );

	}
}
