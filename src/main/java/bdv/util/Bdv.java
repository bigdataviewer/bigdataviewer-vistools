package bdv.util;

public interface Bdv
{
	public BdvHandle getBdvHandle();

	public default void close()
	{
		getBdvHandle().close();
	}

	public static BdvOptions options()
	{
		return BdvOptions.options();
	}
}
