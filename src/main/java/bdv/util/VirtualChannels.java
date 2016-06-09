package bdv.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.TypeIdentity;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class VirtualChannels
{
	public static interface VirtualChannel
	{
		public void updateVisibility();

		public void updateSetupParameters();
	}

	// BdvFunctions.
	public static List< BdvVirtualChannelSource > show(
			final RandomAccessibleInterval< ARGBType > img,
			final List< VirtualChannel > virtualChannels,
			final String name,
			final BdvOptions options )
	{
		final Bdv bdv = options.values.addTo();
		final BdvHandle handle = ( bdv == null )
				? new BdvHandleFrame( options )
				: bdv.getBdvHandle();
		final AxisOrder axisOrder = options.values.axisOrder();
		final AffineTransform3D sourceTransform = options.values.getSourceTransform();

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
			setup.addSetupChangeListener( () -> vc.updateSetupParameters() );
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

	public static class BdvVirtualChannelSource extends BdvSource
	{
		protected final PlaceHolderConverterSetup setup;

		private final SourceAndConverter< ARGBType > source;

		private final PlaceHolderOverlayInfo info;

		private final ChannelSourceCoordinator coordinator;

		protected BdvVirtualChannelSource(
				final BdvHandle bdv,
				final int numTimepoints,
				final PlaceHolderConverterSetup setup,
				final SourceAndConverter< ARGBType > source,
				final PlaceHolderOverlayInfo info,
				final ChannelSourceCoordinator coordinator )
		{
			super( bdv, numTimepoints );
			this.setup = setup;
			this.source = source;
			this.info = info;
			this.coordinator = coordinator;
		}

		@Override
		public void removeFromBdv()
		{
			coordinator.sharedInfos.remove( info );
			getBdvHandle().remove(
					Arrays.asList( setup ),
					Arrays.asList( source ),
					Arrays.asList( info ),
					Arrays.asList( info ),
					Arrays.asList( info ),
					null );
			getBdvHandle().removeBdvSource( this );
			setBdvHandle( null );
		}

		@Override
		protected boolean isPlaceHolderSource()
		{
			return false;
		}

		@Override
		public void setDisplayRange( final double min, final double max )
		{
			final SetupAssignments sa = getBdvHandle().getSetupAssignments();
			final MinMaxGroup group = sa.getMinMaxGroup( setup );
			group.getMinBoundedValue().setCurrentValue( min );
			group.getMaxBoundedValue().setCurrentValue( max );
		}

		@Override
		public void setDisplayRangeBounds( final double min, final double max )
		{
			final SetupAssignments sa = getBdvHandle().getSetupAssignments();
			final MinMaxGroup group = sa.getMinMaxGroup( setup );
			group.setRange( min, max );
		}

		@Override
		public void setColor( final ARGBType color )
		{
			setup.setColor( color );
		}

		@Override
		public void setCurrent()
		{
			getBdvHandle().getViewerPanel().getVisibilityAndGrouping().setCurrentSource( source.getSpimSource() );
		}

		@Override
		public void setActive( final boolean isActive )
		{
			getBdvHandle().getViewerPanel().getVisibilityAndGrouping().setSourceActive( source.getSpimSource(), isActive );
		}
	}
}
