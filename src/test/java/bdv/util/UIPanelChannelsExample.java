package bdv.util;

import bdv.util.uipanel.BdvUIPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class UIPanelChannelsExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final JFrame frame = new JFrame( "my test frame" );
		final BdvUIPanel bdv = new BdvUIPanel( frame, Bdv.options().is2D().preferredSize( 800, 600 ) );
		frame.add( bdv.getSplitPane(), BorderLayout.CENTER );
//		final BdvHandlePanel bdv = new BdvHandlePanel( frame, Bdv.options().is2D().preferredSize( 800, 600 ) );
//		frame.add( bdv.getViewerPanel(), BorderLayout.CENTER );
		frame.setPreferredSize( new Dimension( 1200, 600 ) );
		frame.pack();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setVisible( true );

		final ArrayImg< UnsignedShortType, ShortArray > img = ArrayImgs.unsignedShorts( 100, 100, 100, 2 );
		final Random random = new Random();
		Views.interval(
				Views.hyperSlice( img, 3, 0 ),
				Intervals.createMinSize( 0, 0, 0, 50, 100, 100 )
		).forEach( t -> t.set( random.nextInt( 256 ) ) );
		Views.interval(
				Views.hyperSlice( img, 3, 1 ),
				Intervals.createMinSize( 50, 0, 0, 50, 100, 100 )
		).forEach( t -> t.set( random.nextInt( 256 ) ) );

		final BdvStackSource< UnsignedShortType > multichannel =
				BdvFunctions.show( img, "multichannel", Bdv.options().addTo( bdv ).axisOrder( AxisOrder.XYZC ) );
	}
}
