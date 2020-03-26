package bdv.util;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import org.junit.Test;

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

	@Test( expected = OutOfMemoryError.class )
	public void testMemoryExaustion()
	{
		assumeFalse( GraphicsEnvironment.isHeadless() );

		for ( int i = 0; i < 8; i++ )
			showJFrame( this::addBdvHandlePanel );
	}

	private void showAndCloseJFrame( final Consumer< JFrame > componentAdder )
	{
		final JFrame frame = ( showJFrame( componentAdder ) );
		try
		{
			Thread.sleep( 100 );
		}
		catch ( final InterruptedException ignored )
		{
		}
		closeJFrame( frame );
		System.gc();
	}

	private JFrame showJFrame( final Consumer< JFrame > componentAdder )
	{
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setSize( 500, 500 );
		componentAdder.accept( frame );
		frame.setVisible( true );
		return frame;
	}

	private void closeJFrame( final JFrame frame )
	{
		frame.dispatchEvent( new WindowEvent( frame, WindowEvent.WINDOW_CLOSING ) );
	}

	private void addBdvHandlePanel( final JFrame frame )
	{
		final BdvHandle handle = new BdvHandlePanel( null, Bdv.options() );
		BdvFunctions.show( dummyLargeImage(), "Image", BdvOptions.options().addTo( handle ) );
		frame.add( handle.getSplitPanel() );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				handle.close();
			}
		} );
	}

	private RandomAccessibleInterval< ByteType > dummyLargeImage()
	{
		// NB: The image is intended to use on tenth of the maximal memory.
		final byte[] array = new byte[ boundedConvertToInt( Runtime.getRuntime().maxMemory() / 5 ) ];
		return ArrayImgs.bytes( array, 10, 10 );
	}

	private int boundedConvertToInt( final long dataSize )
	{
		return ( int ) Math.max( Integer.MIN_VALUE, Math.min( Integer.MAX_VALUE, dataSize ) );
	}
}
