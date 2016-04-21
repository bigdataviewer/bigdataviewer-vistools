package bdv.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class FrameExample
{
	public static void main( final String[] args )
	{

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		final Random random = new Random();
		img.forEach( t -> t.set( random.nextInt() ) );

		// TODO: add BdvOptions.closeAfterRemovingLastSource()

		final BdvHandleFrame handle = new BdvHandleFrame( Bdv.options().transformEventHandlerFactory( BehaviourTransformEventHandlerPlanar.factory() ) );
		final List< ? extends ConverterSetup > converterSetups = new ArrayList<>();
		final List< ? extends SourceAndConverter< ? > > sources = new ArrayList<>();
		final int numTimepoints = 1;
		handle.add( converterSetups, sources, numTimepoints );

		synchronized ( FrameExample.class )
		{
			try
			{
				final BdvStackSource< ARGBType > source = BdvFunctions.show( handle, img, "img" );
				FrameExample.class.wait( 1000 );
				source.removeFromBdv();
				FrameExample.class.wait( 1000 );
				BdvFunctions.show( handle, img, "img" );
//				FrameExample.class.wait( 1000 );
//				handle.close();
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
}
