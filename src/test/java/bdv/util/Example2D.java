package bdv.util;

import java.util.Random;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class Example2D
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF00FF00 ) );
		final Bdv bdv3D = BdvFunctions.show( img, "greens", Bdv.options().is2D() );

		final ArrayImg< ARGBType, IntArray > img2 = ArrayImgs.argbs( 100, 100, 100 );
		img2.forEach( t -> t.set( random.nextInt() & 0xFFFF0000 ) );
		BdvFunctions.show( img2, "reds", Bdv.options().addTo( bdv3D ) );
	}
}
