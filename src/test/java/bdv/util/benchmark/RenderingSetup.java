package bdv.util.benchmark;

import bdv.BigDataViewer;
import bdv.cache.CacheControl;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerState;
import bdv.viewer.render.AccumulateProjectorARGB;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.render.RenderTarget;
import bdv.viewer.render.awt.BufferedImageRenderResult;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class RenderingSetup
{
	private static Img< UnsignedByteType > createImg()
	{
		return createImg( new Random(), 100, 100, 100 );
	}

	private static Img< UnsignedByteType > createImg( final Random random, final long... dims )
	{
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( dims );
		img.forEach( t -> t.set( 64 + random.nextInt( 128 ) ) );
		return img;
	}


	private static Source< UnsignedByteType > createSource(
			final Random random,
			final int i,
			final int xOffset,
			final int yOffset )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		sourceTransform.translate( xOffset, yOffset, 0 );
		final String name = "img " + i;
		final Img< UnsignedByteType > img = createImg( random, 100, 100, 100 );
		Source< UnsignedByteType > source = new RandomAccessibleIntervalSource( img, new UnsignedByteType(), sourceTransform, name );
		return source;
	}

	public static SourceAndConverter< UnsignedByteType > createSourceAndConverter(
			final Random random,
			final int i,
			final int xOffset,
			final int yOffset )
	{
		final Source< UnsignedByteType > source = createSource( random, i, xOffset, yOffset );
		final SourceAndConverter< UnsignedByteType > soc = BigDataViewer.wrapWithTransformedSource(
				new SourceAndConverter<>( source, BigDataViewer.createConverterToARGB( source.getType() ) ) );
		final ConverterSetup converterSetup = BigDataViewer.createConverterSetup( soc, 0 );
		final ARGBType color = new ARGBType( random.nextInt() & 0xFFFFFF );
		converterSetup.setColor( color );
		return soc;
	}

	public static class BenchmarkRenderTarget implements RenderTarget< BufferedImageRenderResult >
	{
		private final int width;
		private final int height;
		private final BufferedImageRenderResult renderResult = new BufferedImageRenderResult();

		public BenchmarkRenderTarget( final int width, final int height )
		{
			this.width = width;
			this.height = height;
		}

		@Override
		public BufferedImageRenderResult getReusableRenderResult()
		{
			return renderResult;
		}

		@Override
		public BufferedImageRenderResult createRenderResult()
		{
			return new BufferedImageRenderResult();
		}

		@Override
		public void setRenderResult( final BufferedImageRenderResult renderResult )
		{}

		@Override
		public int getWidth()
		{
			return width;
		}

		@Override
		public int getHeight()
		{
			return height;
		}

		public BufferedImageRenderResult getRenderResult()
		{
			return renderResult;
		}
	}

	public static class Renderer
	{
		private final BenchmarkRenderTarget target;
		private final MultiResolutionRenderer renderer;

		public Renderer(final int[] targetSize, final int numRenderingThreads)
		{
			target = new BenchmarkRenderTarget( targetSize[ 0 ], targetSize[ 1 ] );
			final AccumulateProjectorFactory< ARGBType > accumulateProjectorFactory = AccumulateProjectorARGB.factory;
			renderer = new MultiResolutionRenderer(
					target, () -> {}, new double[] { 1 }, 0,
					numRenderingThreads, null, false,
					accumulateProjectorFactory, new CacheControl.Dummy() );
		}

		public void render( final ViewerState state )
		{
			renderer.requestRepaint();
			renderer.paint( state );
		}

		public void writeResult( final String filename ) throws IOException
		{
			final BufferedImage bi = target.getRenderResult().getBufferedImage();
			ImageIO.write( bi, "png", new File( filename ) );
		}
	}
}
