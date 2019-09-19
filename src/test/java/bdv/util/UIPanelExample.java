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
import net.imglib2.type.numeric.ARGBType;

public class UIPanelExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		final Random random = new Random();
		img.forEach( t -> t.set( random.nextInt() ) );

		final JFrame frame = new JFrame( "my test frame" );
		final BdvUIPanel bdv = new BdvUIPanel( frame, Bdv.options().is2D() );
		frame.add( bdv.getSplitPane(), BorderLayout.CENTER );
		bdv.getSplitPane().setDividerLocation( 0.8 );
		frame.setPreferredSize( new Dimension( 800, 600 ) );
		frame.pack();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setVisible( true );

		BdvFunctions.show( img, "img", Bdv.options().addTo( bdv ) );
	}
}
