package bdv.util;

import java.util.ArrayList;

import bdv.BigDataViewer;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.img.cache.Cache;
import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.RealARGBColorConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ScaledARGBConverter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Util;

public class BdvFunctions
{
	public static < T > void show(
			final RandomAccessibleInterval< T > img,
			final double[] calibration,
			final String title )
	{
		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();
		final int numTimepoints = 1;
		final Cache cache = new Cache.Dummy();
		final ProgressWriter progressWriter = new ProgressWriterConsole();

		final int setupId = 0; // TODO

		final AffineTransform3D sourceTransform = new AffineTransform3D();
		sourceTransform.set(
				calibration[ 0 ], 0, 0, 0,
				0, calibration[ 1 ], 0, 0,
				0, 0, calibration[ 2 ], 0, 0 );

		final T type = Util.getTypeFromInterval( img );
		if ( type instanceof ARGBType )
		{
			@SuppressWarnings( "unchecked" )
			final RandomAccessibleInterval< ARGBType > timg = ( RandomAccessibleInterval< ARGBType > ) img;
			final Source< ARGBType > s = new RandomAccessibleIntervalSource<>( timg, new ARGBType(), sourceTransform, title );
			final TransformedSource< ARGBType > ts = new TransformedSource< ARGBType >( s );
			final ScaledARGBConverter.ARGB converter = new ScaledARGBConverter.ARGB( 0, 255 );
			final SourceAndConverter< ARGBType > soc = new SourceAndConverter< ARGBType >( ts, converter );

			sources.add( soc );
			converterSetups.add( new RealARGBColorConverterSetup( setupId, converter ) );
		}

		final BigDataViewer bdv = new BigDataViewer( converterSetups, sources, null, numTimepoints, cache, title, progressWriter, ViewerOptions.options() );
		bdv.getViewerFrame().setVisible( true );
		InitializeViewerState.initTransform( bdv.getViewer() );
	}

	public static < T > void show4d(
			final RandomAccessibleInterval< T > img,
			final double[] calibration,
			final String title )
	{
		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();
		final int numTimepoints = ( int ) img.dimension( 3 );
		final Cache cache = new Cache.Dummy();
		final ProgressWriter progressWriter = new ProgressWriterConsole();

		final int setupId = 0; // TODO

		final AffineTransform3D sourceTransform = new AffineTransform3D();
		sourceTransform.set(
				calibration[ 0 ], 0, 0, 0,
				0, calibration[ 1 ], 0, 0,
				0, 0, calibration[ 2 ], 0, 0 );

		final T type = Util.getTypeFromInterval( img );
		if ( type instanceof ARGBType )
		{
			@SuppressWarnings( "unchecked" )
			final RandomAccessibleInterval< ARGBType > timg = ( RandomAccessibleInterval< ARGBType > ) img;
			final Source< ARGBType > s = new RandomAccessibleIntervalSource4D<>( timg, new ARGBType(), sourceTransform, title );
			final TransformedSource< ARGBType > ts = new TransformedSource< ARGBType >( s );
			final ScaledARGBConverter.ARGB converter = new ScaledARGBConverter.ARGB( 0, 255 );
			final SourceAndConverter< ARGBType > soc = new SourceAndConverter< ARGBType >( ts, converter );

			sources.add( soc );
			converterSetups.add( new RealARGBColorConverterSetup( setupId, converter ) );
		}

		final BigDataViewer bdv = new BigDataViewer( converterSetups, sources, null, numTimepoints, cache, title, progressWriter, ViewerOptions.options() );
		bdv.getViewerFrame().setVisible( true );
		InitializeViewerState.initTransform( bdv.getViewer() );
	}

	public static void main( final String[] args )
	{

		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();
		final AbstractSpimData< ? > spimData = null;
		final int numTimepoints = 1;
		final Cache cache = new Cache.Dummy();
		final String windowTitle = "BigDataViewer";
		final ProgressWriter progressWriter = new ProgressWriterConsole();
		final ViewerOptions options = ViewerOptions.options();

		final BigDataViewer bdv = new BigDataViewer( converterSetups, sources, spimData, numTimepoints, cache, windowTitle, progressWriter, options );
		bdv.getViewerFrame().setVisible( true );
		InitializeViewerState.initTransform( bdv.getViewer() );
	}

}
