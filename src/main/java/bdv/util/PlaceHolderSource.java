package bdv.util;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public final class PlaceHolderSource implements Source< UnsignedShortType >
{
	private final UnsignedShortType type = new UnsignedShortType();

	private final String name;

	public PlaceHolderSource( final String name )
	{
		this.name = name;
	}

	@Override
	public UnsignedShortType getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return null;
	}

	@Override
	public int getNumMipmapLevels()
	{
		return 1;
	}

	@Override
	public boolean isPresent( final int t )
	{
		return false;
	}

	@Override
	public RandomAccessibleInterval< UnsignedShortType > getSource( final int t, final int level )
	{
		return null;
	}

	@Override
	public RealRandomAccessible< UnsignedShortType > getInterpolatedSource( final int t, final int level, final Interpolation method )
	{
		return null;
	}

	@Override
	public void getSourceTransform( final int t, final int level, final AffineTransform3D transform )
	{
		transform.identity();
	}
}
