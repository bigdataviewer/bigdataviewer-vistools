package bdv.util;

import java.util.Random;

import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.Masks;
import net.imglib2.roi.RealMaskRealInterval;
import net.imglib2.roi.Regions;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;

public class RealBoxSelectionExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50 );
		img.forEach( t -> t.set( random.nextInt( 128 ) ) );

		final AffineTransform3D imageTransform = new AffineTransform3D();
		imageTransform.set( 2, 2, 2 );
		final Bdv bdv = BdvFunctions.show( img, "image", BdvOptions.options().sourceTransform( imageTransform ) );

		final RealInterval initialInterval = Intervals.createMinMaxReal( 30, 30, 30, 80, 80, 80 );
		final RealInterval rangeInterval = Intervals.createMinMaxReal( 0, 0, 0, 100, 100, 100 );
		final AffineTransform3D boxTransform = new AffineTransform3D();
		boxTransform.rotate( 2,  0.5 );
		boxTransform.translate( 30, -20, 0 );

		final TransformedRealBoxSelectionDialog.Result result = BdvFunctions.selectRealBox(
				bdv,
				boxTransform,
				initialInterval,
				rangeInterval,
				BoxSelectionOptions.options()
						.title( "Select box to fill" ) );

		if ( result.isValid() )
		{
			final RealMaskRealInterval imageMask = result.asMask().transform( imageTransform );
			final RealRandomAccessibleRealInterval< BoolType > rrai = Masks.toRealRandomAccessibleRealInterval( imageMask );
			final IntervalView< BoolType > rai = Views.interval( Views.raster( rrai ), Intervals.smallestContainingInterval( rrai ) );
			Regions.sample( Regions.iterable( rai ), Views.extendZero( img ) ).forEach( t -> t.set( 255 ) );
			bdv.getBdvHandle().getViewerPanel().requestRepaint();
		}
	}
}
