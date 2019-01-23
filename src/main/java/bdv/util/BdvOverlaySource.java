package bdv.util;

import java.util.Arrays;
import java.util.List;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.OverlayRenderer;

import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;

public class BdvOverlaySource< O extends OverlayRenderer > extends BdvSource
{
	protected final PlaceHolderConverterSetup setup;

	private final SourceAndConverter< Void > source;

	private final PlaceHolderOverlayInfo info;

	protected final O overlay;

	public O getOverlay()
	{
		return overlay;
	}

	protected BdvOverlaySource(
			final BdvHandle bdv,
			final int numTimepoints,
			final PlaceHolderConverterSetup setup,
			final SourceAndConverter< Void > source,
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
				Arrays.asList( info ),
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
	public boolean isCurrent()
	{
		final ViewerState state = getBdvHandle().getViewerPanel().getState();
		final List< SourceState< ? > > ss = state.getSources();
		final int i = state.getCurrentSource();
		return i >= 0 && i < ss.size() && ss.get( i ).getSpimSource() == source.getSpimSource();
	}

	@Override
	public void setActive( final boolean isActive )
	{
		getBdvHandle().getViewerPanel().getVisibilityAndGrouping().setSourceActive( source.getSpimSource(), isActive );
	}
}
