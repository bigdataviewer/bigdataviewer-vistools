package bdv.util;

import java.util.Arrays;

import bdv.viewer.SourceAndConverter;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.ui.OverlayRenderer;

public abstract class BdvOverlaySource< O extends OverlayRenderer > extends BdvSource
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
}
