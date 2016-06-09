package bdv.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.RealARGBColorConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.display.ScaledARGBConverter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

/**
 * all show methods return a {@link Bdv} which can be used to add more stuff to the same window
 *
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class BdvFunctions
{
	public static < T > BdvStackSource< T > show(
			final RandomAccessibleInterval< T > img,
			final String name )
	{
		return show( img, name, Bdv.options() );
	}

	public static < T > BdvStackSource< T > show(
			final RandomAccessibleInterval< T > img,
			final String name,
			final BdvOptions options )
	{
		final Bdv bdv = options.values.addTo();
		final BdvHandle handle = ( bdv == null )
				? new BdvHandleFrame( options )
				: bdv.getBdvHandle();
		final AxisOrder axisOrder = options.values.axisOrder();
		final AffineTransform3D sourceTransform = options.values.getSourceTransform();
		final T type = Util.getTypeFromInterval( img );
		if ( type instanceof ARGBType )
		{
			@SuppressWarnings( "unchecked" )
			final BdvStackSource< T > stackSource = ( BdvStackSource< T > ) addStackSourceARGBType(
					handle,
					( RandomAccessibleInterval< ARGBType > ) img,
					name,
					AxisOrder.getAxisOrder( axisOrder, img, handle.is2D() ),
					sourceTransform );
			return stackSource;
		}
		else if ( type instanceof RealType )
		{
			@SuppressWarnings( { "unchecked", "rawtypes" } )
			final BdvStackSource< ? > tmp = addStackSourceRealType(
					handle,
					( RandomAccessibleInterval< RealType > ) img,
					name,
					AxisOrder.getAxisOrder( axisOrder, img, handle.is2D() ),
					sourceTransform );
			@SuppressWarnings( "unchecked" )
			final BdvStackSource< T > stackSource = ( BdvStackSource< T > ) tmp;
			return stackSource;
		}

		return null;
	}

	private static BdvStackSource< ARGBType > addStackSourceARGBType(
			final BdvHandle handle,
			final RandomAccessibleInterval< ARGBType > img,
			final String name,
			final AxisOrder axisOrder,
			final AffineTransform3D sourceTransform )
	{
		final List< ConverterSetup > converterSetups = new ArrayList<>();
		final List< SourceAndConverter< ARGBType > > sources = new ArrayList<>();
		final ArrayList< RandomAccessibleInterval< ARGBType > > stacks = AxisOrder.splitInputStackIntoSourceStacks( img, axisOrder );
		int numTimepoints = 1;
		for ( final RandomAccessibleInterval< ARGBType > stack : stacks )
		{
			final Source< ARGBType > s;
			if ( stack.numDimensions() > 3 )
			{
				numTimepoints = ( int ) stack.max( 3 ) + 1;
				s = new RandomAccessibleIntervalSource4D<>( stack, new ARGBType(), sourceTransform, name );
			}
			else
			{
				s = new RandomAccessibleIntervalSource<>( stack, new ARGBType(), sourceTransform, name );
			}
			final TransformedSource< ARGBType > ts = new TransformedSource<>( s );
			final ScaledARGBConverter.ARGB converter = new ScaledARGBConverter.ARGB( 0, 255 );
			final SourceAndConverter< ARGBType > soc = new SourceAndConverter<>( ts, converter );

			final int setupId = handle.getUnusedSetupId();
			final RealARGBColorConverterSetup setup = new RealARGBColorConverterSetup( setupId, converter );

			converterSetups.add( setup );
			sources.add( soc );
		}
		handle.add( converterSetups, sources, numTimepoints );
		final BdvStackSource< ARGBType > bdvSource = new BdvStackSource<>( handle, numTimepoints, new ARGBType(), converterSetups, sources );
		handle.addBdvSource( bdvSource );
		return bdvSource;
	}

	private static < T extends RealType< T > > BdvStackSource< T > addStackSourceRealType(
			final BdvHandle handle,
			final RandomAccessibleInterval< T > img,
			final String name,
			final AxisOrder axisOrder,
			final AffineTransform3D sourceTransform )
	{
		final T type = Util.getTypeFromInterval( img );
		final List< ConverterSetup > converterSetups = new ArrayList<>();
		final List< SourceAndConverter< T > > sources = new ArrayList<>();
		final ArrayList< RandomAccessibleInterval< T > > stacks = AxisOrder.splitInputStackIntoSourceStacks( img, axisOrder );
		int numTimepoints = 1;
		for ( final RandomAccessibleInterval< T > stack : stacks )
		{
			final Source< T > s;
			if ( stack.numDimensions() > 3 )
			{
				numTimepoints = ( int ) stack.max( 3 ) + 1;
				s = new RandomAccessibleIntervalSource4D<>( stack, type, sourceTransform, name );
			}
			else
			{
				s = new RandomAccessibleIntervalSource<>( stack, type, sourceTransform, name );
			}
			final TransformedSource< T > ts = new TransformedSource<>( s );
			final double typeMin = Math.max( 0, Math.min( type.getMinValue(), 65535 ) );
			final double typeMax = Math.max( 0, Math.min( type.getMaxValue(), 65535 ) );
			final RealARGBColorConverter< T > converter = new RealARGBColorConverter.Imp1< T >( typeMin, typeMax );
			converter.setColor( new ARGBType( 0xffffffff ) );
			final SourceAndConverter< T > soc = new SourceAndConverter<>( ts, converter );

			final int setupId = handle.getUnusedSetupId();
			final RealARGBColorConverterSetup setup = new RealARGBColorConverterSetup( setupId, converter );

			converterSetups.add( setup );
			sources.add( soc );
		}
		handle.add( converterSetups, sources, numTimepoints );
		final BdvStackSource< T > bdvSource = new BdvStackSource<>( handle, numTimepoints, type, converterSetups, sources );
		handle.addBdvSource( bdvSource );
		return bdvSource;
	}

	private static BdvStackSource< ARGBType > addSourceARGBType(
			final BdvHandle handle,
			final RealRandomAccessible< ARGBType > img,
			final Interval interval,
			final String name,
			final AxisOrder axisOrder,
			final AffineTransform3D sourceTransform )
	{
		final List< ConverterSetup > converterSetups = new ArrayList<>();
		final List< SourceAndConverter< ARGBType > > sources = new ArrayList<>();

		int numTimepoints = 1;
		final Source< ARGBType > s = new RealRandomAccessibleIntervalSource<>( img, interval, new ARGBType(), sourceTransform, name );

		final TransformedSource< ARGBType > ts = new TransformedSource< ARGBType >( s );
		final ScaledARGBConverter.ARGB converter = new ScaledARGBConverter.ARGB( 0, 255 );
		final SourceAndConverter< ARGBType > soc = new SourceAndConverter< ARGBType >( ts, converter );

		final int setupId = handle.getUnusedSetupId();
		final RealARGBColorConverterSetup setup = new RealARGBColorConverterSetup( setupId, converter );

		converterSetups.add( setup );
		sources.add( soc );

		handle.add( converterSetups, sources, numTimepoints );
		final BdvStackSource< ARGBType > bdvSource = new BdvStackSource<>( handle, numTimepoints, new ARGBType(), converterSetups, sources );
		handle.addBdvSource( bdvSource );
		return bdvSource;
	}

	private static < T extends RealType< T > > BdvStackSource< T > addSourceRealType(
			final BdvHandle handle,
			final RealRandomAccessible< T > img,
			final Interval interval,
			final String name,
			final AxisOrder axisOrder,
			final AffineTransform3D sourceTransform )
	{
		final T type = img.realRandomAccess().get();
		final List< ConverterSetup > converterSetups = new ArrayList<>();
		final List< SourceAndConverter< T > > sources = new ArrayList<>();

		int numTimepoints = 1;

		Source< T > s = new RealRandomAccessibleIntervalSource< T >( img, interval, type, sourceTransform, name );

		final TransformedSource< T > ts = new TransformedSource< T >( s );
		final double typeMin = Math.max( 0, Math.min( type.getMinValue(), 65535 ) );
		final double typeMax = Math.max( 0, Math.min( type.getMaxValue(), 65535 ) );
		final RealARGBColorConverter< T > converter = new RealARGBColorConverter.Imp1< T >( typeMin, typeMax );
		converter.setColor( new ARGBType( 0xffffffff ) );
		final SourceAndConverter< T > soc = new SourceAndConverter< T >( ts, converter );

		final int setupId = handle.getUnusedSetupId();
		final RealARGBColorConverterSetup setup = new RealARGBColorConverterSetup( setupId, converter );

		converterSetups.add( setup );
		sources.add( soc );

		handle.add( converterSetups, sources, numTimepoints );
		final BdvStackSource< T > bdvSource = new BdvStackSource<>( handle, numTimepoints, type, converterSetups, sources );
		handle.addBdvSource( bdvSource );
		return bdvSource;
	}

	public static BdvPointsSource showPoints(
			final List< ? extends RealLocalizable > points,
			final String name )
	{
		return showPoints( points, name, Bdv.options() );
	}

	public static BdvPointsSource showPoints(
			final List< ? extends RealLocalizable > points,
			final String name,
			final BdvOptions options )
	{
		final Bdv bdv = options.values.addTo();
		final BdvHandle handle = ( bdv == null )
				? new BdvHandleFrame( options )
				: bdv.getBdvHandle();
		final AffineTransform3D sourceTransform = options.values.getSourceTransform();

		final int setupId = handle.getUnusedSetupId();
		final ARGBType defaultColor = new ARGBType( 0xff00ff00 );
		final PlaceHolderConverterSetup setup = new PlaceHolderConverterSetup( setupId, 0, 255, defaultColor );
		final PlaceHolderSource source = new PlaceHolderSource( name );
		final SourceAndConverter< UnsignedShortType > soc = new SourceAndConverter<>( source, null );

		final List< ConverterSetup > converterSetups = new ArrayList<>( Arrays.asList( setup ) );
		final List< SourceAndConverter< UnsignedShortType > > sources = new ArrayList<>( Arrays.asList( soc ) );

		final int numTimepoints = 1;
		handle.add( converterSetups, sources, numTimepoints );

		final PlaceHolderOverlayInfo info = new PlaceHolderOverlayInfo( handle.getViewerPanel(), source, setup );
		final PointsOverlay overlay = new PointsOverlay();
		overlay.setOverlayInfo( info );
		overlay.setPoints( points );
		overlay.setSourceTransform( sourceTransform );
		handle.getViewerPanel().getDisplay().addOverlayRenderer( overlay );

		final BdvPointsSource bdvSource = new BdvPointsSource( handle, numTimepoints, setup, soc, info, overlay );
		handle.addBdvSource( bdvSource );
		return bdvSource;
	}

	public static < O extends BdvOverlay > BdvOverlaySource< O > showOverlay(
			final O overlay,
			final String name )
	{
		return showOverlay( overlay, name, Bdv.options() );
	}

	public static < O extends BdvOverlay > BdvOverlaySource< O > showOverlay(
			final O overlay,
			final String name,
			final BdvOptions options )
	{
		final Bdv bdv = options.values.addTo();
		final BdvHandle handle = ( bdv == null )
				? new BdvHandleFrame( options )
				: bdv.getBdvHandle();
		final AffineTransform3D sourceTransform = options.values.getSourceTransform();

		final int setupId = handle.getUnusedSetupId();
		final ARGBType defaultColor = new ARGBType( 0xff00ff00 );
		final PlaceHolderConverterSetup setup = new PlaceHolderConverterSetup( setupId, 0, 255, defaultColor );
		final PlaceHolderSource source = new PlaceHolderSource( name );
		final SourceAndConverter< UnsignedShortType > soc = new SourceAndConverter<>( source, null );

		final List< ConverterSetup > converterSetups = new ArrayList<>( Arrays.asList( setup ) );
		final List< SourceAndConverter< UnsignedShortType > > sources = new ArrayList<>( Arrays.asList( soc ) );

		final int numTimepoints = 1;
		handle.add( converterSetups, sources, numTimepoints );

		final PlaceHolderOverlayInfo info = new PlaceHolderOverlayInfo( handle.getViewerPanel(), source, setup );
		overlay.setOverlayInfo( info );
		overlay.setSourceTransform( sourceTransform );
		handle.getViewerPanel().getDisplay().addOverlayRenderer( overlay );

		final BdvOverlaySource< O > bdvSource = new BdvOverlaySource<>( handle, numTimepoints, setup, soc, info, overlay );
		handle.addBdvSource( bdvSource );
		return bdvSource;
	}

	// TODO: move to BdvFunctionUtils
	public static int getUnusedSetupId( final BigDataViewer bdv )
	{
		return getUnusedSetupId( bdv.getSetupAssignments() );
	}

	private static final TObjectIntHashMap< SetupAssignments > maxIds = new TObjectIntHashMap<>( 20, 0.75f, 0 );

	// TODO: move to BdvFunctionUtils
	public static synchronized int getUnusedSetupId( final SetupAssignments setupAssignments )
	{
		int maxId = maxIds.get( setupAssignments );
		for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
			maxId = Math.max( setup.getSetupId(), maxId );
		++maxId;
		maxIds.put( setupAssignments, maxId );
		return maxId;
	}

	public static < T > BdvStackSource< T > show(
			final RealRandomAccessible< T > img,
			final Interval interval,
			final String name,
			final BdvOptions options )
	{
		final Bdv bdv = options.values.addTo();
		final BdvHandle handle = ( bdv == null )
				? new BdvHandleFrame( options )
				: bdv.getBdvHandle();
		final AxisOrder axisOrder = options.values.axisOrder();
		final AffineTransform3D sourceTransform = options.values.getSourceTransform();
		final T type = img.realRandomAccess().get();
		if ( type instanceof ARGBType )
		{
			@SuppressWarnings( "unchecked" )
			final BdvStackSource< T > stackSource = ( BdvStackSource< T > ) addSourceARGBType(
					handle,
					( RealRandomAccessible< ARGBType > ) img,
					interval,
					name,
					AxisOrder.getAxisOrder( axisOrder, img, handle.is2D() ),
					sourceTransform );
			return stackSource;
		}
		else if ( type instanceof RealType )
		{
			@SuppressWarnings( { "unchecked", "rawtypes" } )
			final BdvStackSource< ? > tmp = addSourceRealType(
					handle,
					( RealRandomAccessible< RealType > ) img,
					interval,
					name,
					AxisOrder.getAxisOrder( axisOrder, img, handle.is2D() ),
					sourceTransform );
			@SuppressWarnings( "unchecked" )
			final BdvStackSource< T > stackSource = ( BdvStackSource< T > ) tmp;
			return stackSource;
		}

		return null;
	}
}
