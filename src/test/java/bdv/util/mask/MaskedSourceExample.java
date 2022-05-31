package bdv.util.mask;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
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
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class MaskedSourceExample
{

	public static void main( String[] args )
	{
		final Random random = new Random( 1l );
		final SourceAndConverter< UnsignedByteType > soc = createSourceAndConverter( random );
		final Bdv bdv = BdvFunctions.show( soc );

		final SourceAndConverter< FloatType > msoc = createMaskSourceAndConverter( random );
		BdvFunctions.show( msoc, Bdv.options().addTo( bdv ) );


		final RandomAccessibleInterval< UnsignedByteType > img = soc.getSpimSource().getSource( 0, 0 );
		final RandomAccessibleInterval< FloatType > mask = msoc.getSpimSource().getSource( 0, 0 );

		final FloatMaskedRealType< UnsignedByteType > maskedType = new FloatMaskedRealType<>( new UnsignedByteType() );
		final RandomAccessibleInterval< FloatMaskedRealType< UnsignedByteType > > maskedImg =
				Views.interval(
						Converters.convert(
								Views.pair( img, mask ),
								new PairToMaskedRealTypeConverter<>(),
								maskedType ),
						img );
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		final String name = "maskedImg";
		final Source< FloatMaskedRealType< UnsignedByteType > > maskedSource =
				new RandomAccessibleIntervalSource<>( maskedImg, maskedType, sourceTransform, name );

		final Converter< FloatMaskedRealType< UnsignedByteType >, ARGBType > alphaConverter =
				MaskedRealARGBColorConverter.create( maskedType, 0, 255 );
		final SourceAndConverter< FloatMaskedRealType< UnsignedByteType > > maskedSoc =
				BigDataViewer.wrapWithTransformedSource( new SourceAndConverter<>( maskedSource, alphaConverter ) );
		final ConverterSetup converterSetup = BigDataViewer.createConverterSetup( maskedSoc, 0 );
		final ARGBType color = new ARGBType( random.nextInt() & 0xFFFFFF );
		converterSetup.setColor( color );
		converterSetup.setDisplayRange( 0, 255 );

		BdvFunctions.show( maskedSoc, Bdv.options().addTo( bdv ) );
	}

	private static Img< FloatType > createMask()
	{
		final Img< FloatType > img = ArrayImgs.floats( 100, 100, 100 );
		img.forEach( t -> t.set( 1f ) );
		Views.interval( img, Intervals.createMinSize( 25, 25, 0, 50, 50, 100 ) ).forEach( t -> t.set( 0f ) );
		return img;
	}

	private static Source< FloatType > createMaskSource()
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		final String name = "mask";
		final Img< FloatType > img = createMask();
		final Source< FloatType > source = new RandomAccessibleIntervalSource<>( img, new FloatType(), sourceTransform, name );
		return source;
	}

	public static SourceAndConverter< FloatType > createMaskSourceAndConverter( final Random random )
	{
		final Source< FloatType > source = createMaskSource();
		final SourceAndConverter< FloatType > soc = BigDataViewer.wrapWithTransformedSource(
				new SourceAndConverter<>( source, BigDataViewer.createConverterToARGB( source.getType() ) ) );
		final ConverterSetup converterSetup = BigDataViewer.createConverterSetup( soc, 0 );
		final ARGBType color = new ARGBType( random.nextInt() & 0xFFFFFF );
		converterSetup.setColor( color );
		converterSetup.setDisplayRange( 0, 2 );
		return soc;
	}





	private static Img< UnsignedByteType > createImg( final Random random, final long... dims )
	{
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( dims );
		img.forEach( t -> t.set( 64 + random.nextInt( 128 ) ) );
		return img;
	}

	private static Source< UnsignedByteType > createIntensitySource( final Random random )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		final String name = "img";
		final Img< UnsignedByteType > img = createImg( random, 100, 100, 100 );
		final Source< UnsignedByteType > source = new RandomAccessibleIntervalSource<>( img, new UnsignedByteType(), sourceTransform, name );
		return source;
	}

	public static SourceAndConverter< UnsignedByteType > createSourceAndConverter( final Random random )
	{
		final Source< UnsignedByteType > source = createIntensitySource( random );
		final SourceAndConverter< UnsignedByteType > soc = BigDataViewer.wrapWithTransformedSource(
				new SourceAndConverter<>( source, BigDataViewer.createConverterToARGB( source.getType() ) ) );
		final ConverterSetup converterSetup = BigDataViewer.createConverterSetup( soc, 0 );
		final ARGBType color = new ARGBType( random.nextInt() & 0xFFFFFF );
		converterSetup.setColor( color );
		return soc;
	}

}
