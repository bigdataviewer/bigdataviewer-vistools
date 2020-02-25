package bdv.util;

import java.util.Collections;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.OverlayRenderer;

import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.SourceAndConverter;

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
				Collections.singletonList( setup ),
				Collections.singletonList( source ),
				Collections.singletonList( info ),
				Collections.singletonList( info ),
				Collections.singletonList( info ),
				Collections.singletonList( overlay ) );
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
		setup.setDisplayRange( min, max );
	}

	@Override
	public void setDisplayRangeBounds( final double min, final double max )
	{
		getBdvHandle().getConverterSetups().getBounds().setBounds( setup, new Bounds( min, max ) );

		// TODO: REMOVE
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
		getBdvHandle().getViewerPanel().state().setCurrentSource( source );
	}

	@Override
	public boolean isCurrent()
	{
		return getBdvHandle().getViewerPanel().state().isCurrentSource( source );
	}

	@Override
	public void setActive( final boolean isActive )
	{
		getBdvHandle().getViewerPanel().state().setSourceActive( source, isActive );
	}
}
