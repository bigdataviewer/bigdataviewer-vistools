package bdv.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;

public class PointsOverlay implements OverlayRenderer
{
	private final AffineTransform3D sourceTransform;

	private List< ? extends RealLocalizable > points;

	private Color col;

	private PlaceHolderOverlayInfo info;

	public PointsOverlay()
	{
		this.sourceTransform = new AffineTransform3D();
		this.info = null;
	}

	public void setOverlayInfo( final PlaceHolderOverlayInfo info )
	{
		this.info = info;
	}

	public < T extends RealLocalizable > void setPoints( final List< T > points )
	{
		this.points = points;
	}

	public void setSourceTransform( final AffineTransform3D t )
	{
		sourceTransform.set( t );
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( points == null || info == null || !info.isVisible() )
			return;

		col = new Color( info.getColor().get() );

		final AffineTransform3D transform = new AffineTransform3D();
		info.getViewerTransform( transform );
		transform.concatenate( sourceTransform );
		final Graphics2D graphics = ( Graphics2D ) g;
		final double[] lPos = new double[ 3 ];
		final double[] gPos = new double[ 3 ];
		for ( final RealLocalizable p : points )
		{
			p.localize( lPos );
			transform.apply( lPos, gPos );
			final double size = getPointSize( gPos );
			final int x = ( int ) ( gPos[ 0 ] - 0.5 * size );
			final int y = ( int ) ( gPos[ 1 ] - 0.5 * size );
			final int w = ( int ) size;
			graphics.setColor( getColor( gPos ) );
			graphics.fillOval( x, y, w, w );
		}
	}

	/** screen pixels [x,y,z] **/
	private Color getColor( final double[] gPos )
	{
		int alpha = 255 - ( int ) Math.round( Math.abs( gPos[ 2 ] ) );

		if ( alpha < 64 )
			alpha = 64;

		return new Color( col.getRed(), col.getGreen(), col.getBlue(), alpha );
	}

	private double getPointSize( final double[] gPos )
	{
		if ( Math.abs( gPos[ 2 ] ) < 3 )
			return 5.0;
		else
			return 3.0;
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}
}
