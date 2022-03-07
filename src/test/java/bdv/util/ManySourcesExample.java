package bdv.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class ManySourcesExample
{
	private static Bdv addSource(final Bdv bdv, final Random random, final int i, final Img< UnsignedByteType > img, final int xOffset, final int yOffset) {
		AffineTransform3D transform = new AffineTransform3D();
		transform.translate( xOffset, yOffset, 0 );
		final BdvSource source = BdvFunctions.show( img, "img " + i,
				Bdv.options()
						.addTo( bdv )
						.preferredSize( 900, 900 )
						.numRenderingThreads( Runtime.getRuntime().availableProcessors() )
						.sourceTransform( transform ) );
		final ARGBType color = new ARGBType( random.nextInt() & 0xFFFFFF );
		source.setColor( color );
		return bdv == null ? source : bdv;
	}

	private static Img< UnsignedByteType > createImg( final Random random )
	{
		final long[] dim = { 100, 100, 100 };
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( dim );
		img.forEach( t -> t.set( 64 + random.nextInt( 128 ) ) );
		return img;
	}

	public static void main( String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final int[] numSources = { 5, 5 };
		final Random random = new Random( 1L );

		final List< Img< UnsignedByteType > > imgs = new ArrayList<>();
		for ( int i = 0; i < numSources[ 0 ] * numSources[ 1 ]; i++ )
			imgs.add( createImg( random ) );

		int i = 0;
		Bdv bdv = null;
		for ( int y = 0; y < numSources[ 1 ]; ++y )
		{
			for ( int x = 0; x < numSources[ 0 ]; ++x )
			{
				final int xOffset = 90 * x;
				final int yOffset = 90 * y;
				bdv = addSource( bdv, random, i, imgs.get( i ), xOffset, yOffset );
				i++;
			}
		}

//		final ViewerPanel viewer = bdv.getBdvHandle().getViewerPanel();
//		final DebugTilingOverlay tilingOverlay = viewer.showDebugTileOverlay();
//		final Runnable toggleShowTiles = () -> {
//			tilingOverlay.setShowTiles( !tilingOverlay.getShowTiles() );
//			viewer.getDisplay().repaint();
//		};
//
//
//		final InputTriggerConfig keyconf = viewer.getInputTriggerConfig();
//		Actions actions = new Actions( keyconf );
//		actions.install( bdv.getBdvHandle().getKeybindings(), "tile overlay" );
//		actions.runnableAction( toggleShowTiles, "toggle draw tiles", "T" );

//		// print current transform
//		viewer.state().changeListeners().add( change -> {
//			if ( change == ViewerStateChange.VIEWER_TRANSFORM_CHANGED )
//			{
//				final AffineTransform3D t = viewer.state().getViewerTransform();
//				System.out.println( "t = " + Arrays.toString( t.getRowPackedCopy() ) );
//			}
//		} );

	}



}
