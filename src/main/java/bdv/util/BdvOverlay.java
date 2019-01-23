package bdv.util;

import java.awt.Graphics;
import java.awt.Graphics2D;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;

/**
 * An overlay that can be shown with
 * {@link BdvFunctions#showOverlay(BdvOverlay, String, BdvOptions)}: This method
 * will add a dummy {@link Source} and {@link ConverterSetup} to the
 * {@link BigDataViewer} such that the visibility, display range, and color for
 * the overlay can be adjusted by the user, like for a normal {@link Source}.
 * <p>
 * Derived classes need to implement {@link #draw(Graphics2D)}. They can access
 * the current viewer transform using
 * {@link #getCurrentTransform2D(AffineTransform2D)},
 * {@link #getCurrentTransform3D(AffineTransform3D)}. They can access the
 * user-set display range and color via {@link #info}.
 *
 * @author Tobias Pietzsch
 */
public abstract class BdvOverlay implements OverlayRenderer
{
	protected final AffineTransform3D sourceTransform;

	private final AffineTransform3D tmp;

	protected PlaceHolderOverlayInfo info;

	public BdvOverlay()
	{
		this.sourceTransform = new AffineTransform3D();
		this.tmp = new AffineTransform3D();
		this.info = null;
	}

	public void setOverlayInfo( final PlaceHolderOverlayInfo info )
	{
		this.info = info;
	}

	public void setSourceTransform( final AffineTransform3D t )
	{
		sourceTransform.set( t );
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( info == null || !info.isVisible() )
			return;

		draw( ( Graphics2D ) g );
	}

	/**
	 * Can be used by derived classes in the {@link #draw(Graphics2D)} method to
	 * get the current transform from 2D source coordinates to screen
	 * coordinates.
	 *
	 * @param transform
	 */
	protected void getCurrentTransform2D( final AffineTransform2D transform )
	{
		info.getViewerTransform( tmp );
		tmp.concatenate( sourceTransform );
		transform.set(
				tmp.get( 0, 0 ), tmp.get( 0, 1 ), tmp.get( 0, 3 ),
				tmp.get( 1, 0 ), tmp.get( 1, 1 ), tmp.get( 1, 3 ) );
	}

	/**
	 * Can be used by derived classes in the {@link #draw(Graphics2D)} method to
	 * get the current transform from 3D source coordinates to 3D screen
	 * coordinates (where Z coordinate is distance from the slice shown on the
	 * screen).
	 *
	 * @param transform
	 */
	protected void getCurrentTransform3D( final AffineTransform3D transform )
	{
		info.getViewerTransform( transform );
		transform.concatenate( sourceTransform );
	}

	protected abstract void draw( final Graphics2D g );

	@Override
	public void setCanvasSize( final int width, final int height )
	{}
}
