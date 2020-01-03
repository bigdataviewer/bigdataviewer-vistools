package mpicbg.spim.data.sequence;

public class DefaultVoxelDimensions implements VoxelDimensions
{
	private final int numDimensions;

	public DefaultVoxelDimensions( int numDimensions )
	{
		this.numDimensions = numDimensions;
	}

	@Override
	public int numDimensions()
	{
		return numDimensions;
	}

	@Override
	public String unit()
	{
		return "pixel";
	}

	@Override
	public void dimensions( final double[] dims )
	{
		for ( int d = 0; d < dims.length; ++d )
			dims[ d ] = 1;
	}

	@Override
	public double dimension(int d)
	{
		return 1;
	}

}
