package bdv.util;

import java.util.Random;

import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.tools.boundingbox.TransformedBoxSelectionDialog;

public class BoxSelectionExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50, 10 );
		img.forEach( t -> t.set( random.nextInt( 128 ) ) );

		final AffineTransform3D imageTransform = new AffineTransform3D();
		imageTransform.set( 2, 2, 2 );
		final Bdv bdv = BdvFunctions.show( img, "image", BdvOptions.options().sourceTransform( imageTransform ) );

		final Interval initialInterval = Intervals.createMinMax( 30, 30, 15, 80, 80, 40 );
		final Interval rangeInterval = Intervals.createMinMax( 0, 0, 0, 100, 100, 50 );
		final TransformedBoxSelectionDialog.Result result = BdvFunctions.selectBox(
				bdv,
				imageTransform,
				initialInterval,
				rangeInterval,
				BoxSelectionOptions.options()
						.title( "Select box to fill" )
						.selectTimepointRange()
						.initialTimepointRange( 0, 5 ) );

		if ( result.isValid() )
		{
			for ( int tp = result.getMinTimepoint(); tp <= result.getMaxTimepoint(); ++tp )
				Views.interval( Views.extendZero( Views.hyperSlice( img, 3, tp ) ), result.getInterval() ).forEach( t -> t.set( 255 ) );
			bdv.getBdvHandle().getViewerPanel().requestRepaint();
		}
	}

}
