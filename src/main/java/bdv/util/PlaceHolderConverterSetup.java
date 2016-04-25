package bdv.util;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.RequestRepaint;
import net.imglib2.type.numeric.ARGBType;

public final class PlaceHolderConverterSetup implements ConverterSetup
{
	private final int setupId;

	private double min;

	private double max;

	private final ARGBType color;

	private final boolean supportsColor;

	private RequestRepaint viewer;

	public PlaceHolderConverterSetup(
			final int setupId,
			final double min,
			final double max,
			final int rgb )
	{
		this( setupId, min, max, new ARGBType( rgb ) );
	}

	public PlaceHolderConverterSetup(
			final int setupId,
			final double min,
			final double max,
			final ARGBType color )
	{
		this.setupId = setupId;
		this.min = min;
		this.max = max;
		this.color = new ARGBType();
		if ( color != null )
			this.color.set( color );
		this.supportsColor = color != null;
		this.viewer = null;
	}

	@Override
	public int getSetupId()
	{
		return setupId;
	}

	@Override
	public void setDisplayRange( final double min, final double max )
	{
		this.min = min;
		this.max = max;
		if ( viewer != null )
			viewer.requestRepaint();
	}

	@Override
	public void setColor( final ARGBType color )
	{
		setColor( color.get() );
	}

	public void setColor( final int rgb )
	{
		this.color.set( rgb );
		if ( viewer != null )
			viewer.requestRepaint();
	}

	@Override
	public boolean supportsColor()
	{
		return supportsColor;
	}

	@Override
	public double getDisplayRangeMin()
	{
		return min;
	}

	@Override
	public double getDisplayRangeMax()
	{
		return max;
	}

	@Override
	public ARGBType getColor()
	{
		return color;
	}

	@Override
	public void setViewer( final RequestRepaint viewer )
	{
		this.viewer = viewer;
	}
}
