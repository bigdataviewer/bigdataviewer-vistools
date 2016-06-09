package bdv.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class DisplayRangeExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		final Random random = new Random();
		img.forEach( t -> t.set( random.nextInt() ) );

		final JFrame frame = new JFrame( "my test frame" );
		final BdvHandlePanel handle = new BdvHandlePanel( frame, Bdv.options() );
		frame.add( handle.getViewerPanel(), BorderLayout.CENTER );
		frame.setPreferredSize( new Dimension( 800, 600 ) );
		frame.pack();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setVisible( true );

		final BdvSource source = BdvFunctions.show( img, "img", Bdv.options().addTo( handle ) );
		source.setDisplayRangeBounds( 0, 1255 );

		synchronized ( DisplayRangeExample.class )
		{
			try
			{
				for ( int i = 1; i <= 100; ++i )
				{
					DisplayRangeExample.class.wait( 50 );
					source.setDisplayRange( 0, 255 + 10 * i );
					/*
					 * Press "S" to show brightness dialog and see the values change...
					 */
				}
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
}
