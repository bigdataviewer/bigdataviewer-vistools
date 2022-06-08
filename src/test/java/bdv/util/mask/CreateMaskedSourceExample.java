package bdv.util.mask;

import bdv.BigDataViewer;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import java.util.Random;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.mask.DoubleMaskedRealType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
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
		final DoubleMaskedRealType< T > maskedType = new DoubleMaskedRealType<>( type.createVariable(), new DoubleType( 1 ) );
		final RandomAccessibleInterval< DoubleMaskedRealType< T > > maskedImg =
				Views.interval(
						Converters.convert(
								img,
								new RealToMaskedRealTypeConverter<>(),
								maskedType ),
						img );

		final Source< DoubleMaskedRealType< T > > source = new RandomAccessibleIntervalSource<>( maskedImg, maskedType, sourceTransform, name );

		// TODO: add as new case to BigDataViewer.createConverterToARGB(...)
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
}
