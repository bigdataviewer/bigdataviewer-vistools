package bdv.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.ARGBType;

public class OverlayExample2D
{

	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF003F00 ) );
		final Bdv bdv2D = BdvFunctions.show( img, "greens", Bdv.options().is2D() );

		final ArrayList< RealPoint > points = new ArrayList<>();
		for ( int i = 0; i < 1100; ++i )
			points.add( new RealPoint( random.nextInt( 100 ), random.nextInt( 100 ) ) );

		final BdvOverlay overlay = new BdvOverlay()
		{
			@Override
			protected void draw( final Graphics2D g )
			{
				final AffineTransform2D t = new AffineTransform2D();
				getCurrentTransform2D( t );

				g.setColor( Color.RED );

				final double[] lPos = new double[ 2 ];
				final double[] gPos1 = new double[ 2 ];
				final double[] gPos2 = new double[ 2 ];
				final int start = info.getTimePointIndex() * 10;
				final int end = info.getTimePointIndex() * 10 + 100;
				for ( int i = start; i < end; i+=2 )
				{
					points.get( i ).localize( lPos );
					t.apply( lPos, gPos1 );
					points.get( i + 1 ).localize( lPos );
					t.apply( lPos, gPos2 );
					g.drawLine( ( int ) gPos1[ 0 ], ( int ) gPos1[ 1 ], ( int ) gPos2[ 0 ], ( int ) gPos2[ 1 ] );
				}
			}
		};

		BdvFunctions.showOverlay( overlay, "overlay", Bdv.options().addTo( bdv2D ) );
		// TODO: add BdvOptions.closeAfterRemovingLastSource()
	}
}
