package bdv.util.mask;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import java.util.Random;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.EuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.mask.DoubleMaskedRealType;
import net.imglib2.type.mask.FloatMaskedRealType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class CreateMaskedSourceExample
{

	public static void main( String[] args )
	{
		final Random random = new Random( 1l );

		final Img< UnsignedByteType > img0 = createImg( random, 100, 100, 100 );
		final SourceAndConverter< DoubleMaskedRealType< UnsignedByteType > > masked0 =
				createMaskedSourceAndConverter( img0, new AffineTransform3D(), "img0" );

		final Img< UnsignedByteType > img1 = createImg( random, 100, 100, 100 );
		final AffineTransform3D t1 = new AffineTransform3D();
		t1.rotate( 2, 25.0 * Math.PI / 180.0 );
		t1.translate( -30, 20, 0 );
		final SourceAndConverter< DoubleMaskedRealType< UnsignedByteType > > masked1 =
				createMaskedSourceAndConverter( img1, t1, "img1" );

		final Bdv bdv = BdvFunctions.show( masked0 );
		BdvFunctions.show( masked1, Bdv.options().addTo( bdv ) );
	}

	public static < T extends RealType< T > > SourceAndConverter< DoubleMaskedRealType< T > > createMaskedSourceAndConverter(
			final RandomAccessibleInterval< T > img,
			final AffineTransform3D sourceTransform,
			final String name )
	{
		final T type = Util.getTypeFromInterval( img );
		return createMaskedSourceAndConverter( img, type, sourceTransform, name );
	}

	public static < T extends RealType< T > > SourceAndConverter< DoubleMaskedRealType< T > > createMaskedSourceAndConverter(
			final RandomAccessibleInterval< T > img,
			final T type,
			final AffineTransform3D sourceTransform,
			final String name )
	{
		final DoubleType one = new DoubleType( 1 );
		final ConstantDoubleMask mask = new ConstantDoubleMask( img.numDimensions(), one );
		final DoubleMaskedRealType< T > maskedType = new DoubleMaskedRealType<>( type.createVariable(), new DoubleType() );
		final RandomAccessibleInterval< DoubleMaskedRealType< T > > maskedImg =
				Views.interval(
						Converters.convert(
								Views.pair( img, mask ),
								new PairToMaskedRealTypeConverter<>(),
								maskedType ),
						img );

		final Source< DoubleMaskedRealType< T > > source = new RandomAccessibleIntervalSource<>( maskedImg, maskedType, sourceTransform, name );
		final Converter< DoubleMaskedRealType< T >, ARGBType > converter = MaskedRealARGBColorConverter.create( maskedType, 0, 255 );
		final SourceAndConverter< DoubleMaskedRealType< T > > soc = BigDataViewer.wrapWithTransformedSource( new SourceAndConverter<>( source, converter ) );

		return soc;
	}

	private static Img< UnsignedByteType > createImg( final Random random, final long... dims )
	{
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( dims );
		img.forEach( t -> t.set( 64 + random.nextInt( 128 ) ) );
		return img;
	}

	// TODO: instead of pairing and converting to MaskedRealType, make a
	//       Converter that just updates the value and leaves the mask
	//       untouched.

	static class ConstantDoubleMask extends AbstractEuclideanSpace implements RandomAccessible< DoubleType >
	{
		private final DoubleType value;

		public ConstantDoubleMask( final int n, final DoubleType value )
		{
			super( n );
			this.value = value;
		}

		@Override
		public RandomAccess< DoubleType > randomAccess()
		{
			return new Access();
		}

		@Override
		public RandomAccess< DoubleType > randomAccess( final Interval interval )
		{
			return new Access();
		}

		class Access implements RandomAccess< DoubleType >
		{
			@Override
			public DoubleType get()
			{
				return value;
			}

			@Override
			public int numDimensions()
			{
				return n;
			}

			@Override
			public RandomAccess< DoubleType > copy()
			{
				return this;
			}

			@Override
			public long getLongPosition( final int d )
			{
				return 0;
			}

			@Override
			public void fwd( final int d ) {}

			@Override
			public void bck( final int d ) {}

			@Override
			public void move( final int distance, final int d ) {}

			@Override
			public void move( final long distance, final int d ) {}

			@Override
			public void move( final Localizable distance ) {}

			@Override
			public void move( final int[] distance ) {}

			@Override
			public void move( final long[] distance ) {}

			@Override
			public void setPosition( final Localizable position ) {}

			@Override
			public void setPosition( final int[] position ) {}

			@Override
			public void setPosition( final long[] position ) {}

			@Override
			public void setPosition( final int position, final int d ) {}

			@Override
			public void setPosition( final long position, final int d ) {}
		}
	}
}
