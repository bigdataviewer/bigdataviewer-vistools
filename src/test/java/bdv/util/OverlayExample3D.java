package bdv.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class OverlayExample3D
{

	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF003F00 ) );
		final Bdv bdv3D = BdvFunctions.show( img, "greens" );

		final ArrayList< RealPoint > points = new ArrayList<>();
		for ( int i = 0; i < 500; ++i )
			points.add( new RealPoint( random.nextInt( 100 ), random.nextInt( 100 ), random.nextInt( 100 ) ) );

		final BdvOverlay overlay = new BdvOverlay()
		{
			@Override
			protected void draw( final Graphics2D g )
			{
				final AffineTransform3D t = new AffineTransform3D();
				getCurrentTransform3D( t );

				final double[] lPos = new double[ 3 ];
				final double[] gPos = new double[ 3 ];
				for ( final RealPoint p : points )
				{
					p.localize( lPos );
					t.apply( lPos, gPos );
					final int size = getSize( gPos[ 2 ] );
					final int x = ( int ) ( gPos[ 0 ] - 0.5 * size );
					final int y = ( int ) ( gPos[ 1 ] - 0.5 * size );
					g.setColor( getColor( gPos[ 2 ] ) );
					g.fillOval( x, y, size, size );
				}
			}

			private Color getColor( final double depth )
			{
				int alpha = 255 - ( int ) Math.round( Math.abs( depth ) );

				if ( alpha < 64 )
					alpha = 64;

				return new Color( 255, 0, 0, alpha );
			}

			private int getSize( final double depth )
			{
				return ( int ) Math.max( 1, 10 - 0.1 * Math.round( Math.abs( depth ) ) );
			}
		};

		BdvFunctions.showOverlay( overlay, "overlay", Bdv.options().addTo( bdv3D ) );
		// TODO: add BdvOptions.closeAfterRemovingLastSource()
	}
}
