package bdv.util;

import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class PanelExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		final Random random = new Random();
		img.forEach( t -> t.set( random.nextInt() ) );


		// TODO: add BdvOptions.closeAfterRemovingLastSource()


		final JFrame frame = new JFrame( "my test frame" );


		final BdvHandlePanel handle = new BdvHandlePanel(
				frame,
				Bdv.options().transformEventHandlerFactory( BehaviourTransformEventHandlerPlanar.factory() ) );
		frame.add( handle.getViewerPanel() );
		frame.pack();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setVisible( true );


		synchronized ( PanelExample.class )
		{
			try
			{
				final BdvStackSource< ARGBType > source = BdvFunctions.show( handle, img, "img" );
				PanelExample.class.wait( 1000 );
				source.removeFromBdv();
				PanelExample.class.wait( 1000 );
				BdvFunctions.show( handle, img, "img" );
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}

	}
}
