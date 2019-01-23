package bdv.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;

/**
 * A dummy {@link Source} that represents a {@link BdvOverlay}.
 * <p>
 * When a {@code BdvOverlay} is shown (with
 * {@link BdvFunctions#showOverlay(BdvOverlay, String, BdvOptions)}), a dummy
 * {@code Source} and {@code ConverterSetup} are added to the
 * {@code BigDataViewer} such that the visibility, display range, and color for
 * the overlay can be adjusted by the user, like for a normal {@link Source}.
 * <p>
 * {@code PlaceHolderSource} is not {@link PlaceHolderSource#isPresent(int)
 * present} at any time point, so it is never actually visible.
 *
 * @author Tobias Pietzsch
 */
public final class PlaceHolderSource implements Source< Void >
{
	private final String name;

	public PlaceHolderSource( final String name )
	{
		this.name = name;
	}

	@Override
	public Void getType()
	{
		return null;
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
	public RandomAccessibleInterval< Void > getSource( final int t, final int level )
	{
		return null;
	}

	@Override
	public RealRandomAccessible< Void > getInterpolatedSource( final int t, final int level, final Interpolation method )
	{
		return null;
	}

	@Override
	public void getSourceTransform( final int t, final int level, final AffineTransform3D transform )
	{
		transform.identity();
	}
}
