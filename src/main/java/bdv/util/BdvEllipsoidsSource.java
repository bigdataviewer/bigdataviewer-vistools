package bdv.util;

import bdv.viewer.SourceAndConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class BdvEllipsoidsSource extends BdvOverlaySource< EllipsoidsOverlay >
{
	protected BdvEllipsoidsSource(
			final BdvHandle bdv,
			final int numTimepoints,
			final PlaceHolderConverterSetup setup,
			final SourceAndConverter< UnsignedShortType > source,
			final PlaceHolderOverlayInfo info,
			final EllipsoidsOverlay overlay )
	{
		super( bdv, numTimepoints, setup, source, info, overlay );
	}

	public void setColor( final ARGBType color )
	{
		setup.setColor( color );
	}
}
