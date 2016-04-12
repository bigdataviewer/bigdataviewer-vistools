package bdv.util;

public interface Bdv
{
	public BdvHandle getBdvHandle();

	public static BdvOptions options()
	{
		return BdvOptions.options();
	}
}
