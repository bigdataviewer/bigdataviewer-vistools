package bdv.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.TypeIdentity;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;


/**
 * The current application for VirtualChannel sources is the following scenario:
 *
 * We have a labeling and want to display it using lookup tables to convert
 * labels to ARGBType. From the same labeling we want to make multiple channels,
 * e.g., selected ROIs, ROIs having property A, property B, etc. These could be
 * added to BDV as individually converted images. So we could use N lookup
 * tables to display N converted images. Instead, it is more efficient to merge
 * the N lookup tables and then display only one converted image. We still want
 * to be able to control display range and color settings for the N
 * "virtual channels" individually. So {@link VirtualChannels} adds N fake
 * sources to the BDV. Each fake source is used to control visibility and
 * settings for one lookup table. Only one of the fake source will then actually
 * render the converted img.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class VirtualChannels
{
	public static interface VirtualChannel
	{
		public void updateVisibility();

		public void updateSetupParameters();
	}

	static List< BdvVirtualChannelSource > show(
			final RandomAccessibleInterval< ARGBType > img,
			final List< ? extends VirtualChannel > virtualChannels,
			final String name,
			final BdvOptions options )
	{
		final Bdv bdv = options.values.addTo();
		final BdvHandle handle = ( bdv == null )
				? new BdvHandleFrame( options )
				: bdv.getBdvHandle();
		final AffineTransform3D sourceTransform = options.values.getSourceTransform();
		AxisOrder axisOrder = options.values.axisOrder();
		axisOrder = AxisOrder.getAxisOrder( axisOrder, img, handle.is2D() );

		final ArrayList< RandomAccessibleInterval< ARGBType > > stacks = AxisOrder.splitInputStackIntoSourceStacks( img, axisOrder );
		if ( stacks.size() != 1 )
			throw new IllegalArgumentException( "The RandomAccessibleInterval of a VirtualChannelSource must have exactly one channel!" );
		final RandomAccessibleInterval< ARGBType > stack = stacks.get( 0 );

		final List< BdvVirtualChannelSource > bdvSources = new ArrayList<>();

		final int numTimepoints = ( stack.numDimensions() > 3 )	? ( int ) stack.max( 3 ) + 1 : 1;
		final ChannelSourceCoordinator coordinator = new ChannelSourceCoordinator();
		for ( final VirtualChannel vc : virtualChannels )
		{
			final Source< ARGBType > source = ( stack.numDimensions() > 3 )
					? new ChannelSource4D( stack, coordinator, sourceTransform, name )
					: new ChannelSource( stack, coordinator, sourceTransform, name );
			final int setupId = handle.getUnusedSetupId();
			final PlaceHolderConverterSetup setup = new PlaceHolderConverterSetup( setupId, 0, 255, new ARGBType( 0xffffffff ) );
			final SourceAndConverter< ARGBType > soc = new SourceAndConverter<>( source, new TypeIdentity< >() );
			handle.add( Arrays.asList( setup ), Arrays.asList( soc ), numTimepoints );

			final PlaceHolderOverlayInfo info = new PlaceHolderOverlayInfo( handle.getViewerPanel(), source, setup );
			coordinator.sharedInfos.add( info );
			setup.setupChangeListeners().add( s -> vc.updateSetupParameters() );
			info.addVisibilityChangeListener( () -> vc.updateVisibility() );
			final BdvVirtualChannelSource bdvSource = new BdvVirtualChannelSource( handle, numTimepoints, setup, soc, info, coordinator );
			handle.addBdvSource( bdvSource );
			bdvSources.add( bdvSource );
		}

		return bdvSources;
	}

	static class ChannelSourceCoordinator
	{
		List< PlaceHolderOverlayInfo > sharedInfos = new ArrayList<>();

		boolean shouldBePresent( final Source< ? > source )
		{
			for ( final PlaceHolderOverlayInfo info : sharedInfos )
				if ( info.isVisible() )
					return info.getSource().equals( source );
			return false;
		}
	}

	static class ChannelSource extends RandomAccessibleIntervalSource< ARGBType >
	{
		private final ChannelSourceCoordinator coordinator;

		public ChannelSource(
				final RandomAccessibleInterval< ARGBType > img,
				final ChannelSourceCoordinator coordinator,
				final AffineTransform3D sourceTransform,
				final String name )
		{
			super( img, new ARGBType(), sourceTransform, name );
			this.coordinator = coordinator;
		}

		@Override
		public boolean isPresent( final int t )
		{
			return super.isPresent( t ) && coordinator.shouldBePresent( this );
		}
	}

	static class ChannelSource4D extends RandomAccessibleIntervalSource4D< ARGBType >
	{
		private final ChannelSourceCoordinator coordinator;

		public ChannelSource4D(
				final RandomAccessibleInterval< ARGBType > img,
				final ChannelSourceCoordinator coordinator,
				final AffineTransform3D sourceTransform,
				final String name )
		{
			super( img, new ARGBType(), sourceTransform, name );
			this.coordinator = coordinator;
		}

		@Override
		public boolean isPresent( final int t )
		{
			return super.isPresent( t ) && coordinator.shouldBePresent( this );
		}
	}
}
