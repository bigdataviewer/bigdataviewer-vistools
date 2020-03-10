package bdv.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import static org.junit.Assume.assumeFalse;

public class BdvHandlePanelGarbageCollectionTest
{

	@Test
	public void testBdv() throws InterruptedException
	{
		assumeFalse( GraphicsEnvironment.isHeadless() );

		for ( int i = 0; i < 8; i++ )
			showAndCloseJFrame( this::addBdvHandlePanel );
	}

	@Test(expected = OutOfMemoryError.class)
	public void testMemoryExaustion() {
		assumeFalse( GraphicsEnvironment.isHeadless() );

		for ( int i = 0; i < 8; i++ )
			showJFrame( this::addBdvHandlePanel );
	}

	private void showAndCloseJFrame( Consumer< JFrame > componentAdder )
	{
		JFrame frame = ( showJFrame( componentAdder ) );
		try { Thread.sleep( 100 ); } catch ( InterruptedException ignored ) { }
		closeJFrame(frame);
		System.gc();
	}

	private JFrame showJFrame( Consumer< JFrame > componentAdder )
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setSize(500,500);
		componentAdder.accept( frame );
		frame.setVisible( true );
		return frame;
	}

	private void closeJFrame( JFrame frame )
	{
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	private void addBdvHandlePanel( JFrame frame )
	{
		BdvHandle handle = new BdvHandlePanel( null, Bdv.options() );
		BdvFunctions.show( dummyLargeImage(), "Image", BdvOptions.options().addTo( handle ));
		frame.add( handle.getSplitPanel() );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosed( WindowEvent e )
			{
				handle.close();
			}
		} );
	}

	private RandomAccessibleInterval< ByteType > dummyLargeImage()
	{
		// NB: The image is intended to use on tenth of the maximal memory.
		byte[] array = new byte[ boundedConvertToInt( Runtime.getRuntime().maxMemory() / 5 ) ];
		return ArrayImgs.bytes( array, 10, 10 );
	}

	private int boundedConvertToInt( long dataSize )
	{
		return ( int ) Math.max( Integer.MIN_VALUE, Math.min( Integer.MAX_VALUE, dataSize ) );
	}
}
