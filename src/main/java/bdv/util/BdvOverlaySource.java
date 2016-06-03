package bdv.util;

import java.util.Arrays;

import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.SourceAndConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.ui.OverlayRenderer;

public class BdvOverlaySource< O extends OverlayRenderer > extends BdvSource
{
	protected final PlaceHolderConverterSetup setup;

	private final SourceAndConverter< UnsignedShortType > source;

	private final PlaceHolderOverlayInfo info;

	protected final O overlay;

	protected BdvOverlaySource(
			final BdvHandle bdv,
			final int numTimepoints,
			final PlaceHolderConverterSetup setup,
			final SourceAndConverter< UnsignedShortType > source,
			final PlaceHolderOverlayInfo info,
			final O overlay )
	{
		super( bdv, numTimepoints );
		this.setup = setup;
		this.source = source;
		this.info = info;
		this.overlay = overlay;
	}

	@Override
	public void removeFromBdv()
	{
		getBdvHandle().remove(
				Arrays.asList( setup ),
				Arrays.asList( source ),
				Arrays.asList( info ),
				Arrays.asList( overlay ) );
		getBdvHandle().removeBdvSource( this );
		setBdvHandle( null );
	}

	@Override
	protected boolean isPlaceHolderSource()
	{
		return true;
	}

	@Override
	public void setDisplayRange( final double min, final double max )
	{
		final SetupAssignments sa = getBdvHandle().getSetupAssignments();
		final MinMaxGroup group = sa.getMinMaxGroup( setup );
		// TODO: fix in BDV. Brightness ranges should all be double
		group.getMinBoundedValue().setCurrentValue( ( int ) min );
		group.getMaxBoundedValue().setCurrentValue( ( int ) max );
	}

	@Override
	public void setDisplayRangeBounds( final double min, final double max )
	{
		final SetupAssignments sa = getBdvHandle().getSetupAssignments();
		final MinMaxGroup group = sa.getMinMaxGroup( setup );
		// TODO: fix in BDV. Brightness ranges should all be double
		group.setRange( ( int ) min, ( int ) max );
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
