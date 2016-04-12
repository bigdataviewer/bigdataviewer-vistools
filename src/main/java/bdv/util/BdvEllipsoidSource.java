package bdv.util;

import java.util.List;

import bdv.viewer.SourceAndConverter;
import net.imglib2.RealLocalizable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class BdvEllipsoidSource extends BdvOverlaySource< EllipsoidOverlay >
{
	protected BdvEllipsoidSource(
			final BdvHandle bdv,
			final int numTimepoints,
			final PlaceHolderConverterSetup setup,
			final SourceAndConverter< UnsignedShortType > source,
			final PlaceHolderOverlayInfo info,
			final EllipsoidOverlay overlay )
	{
		super( bdv, numTimepoints, setup, source, info, overlay );
	}

	public void setColor( final ARGBType color )
	{
		setup.setColor( color );
	}
}
