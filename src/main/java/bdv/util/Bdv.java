package bdv.util;

/**
 * Something that has a {@link BdvHandle}. This includes {@link BdvSource}s, as
 * well as {@link BdvHandle}s (which return themselves with
 * {@link #getBdvHandle()}).
 * <p>
 * Having a {@link Bdv} is useful for adding more stuff to a BigDataViewer
 * window or panel. This is done using
 * {@code BdvFunctions.show(..., Bdv.options().addTo(myBdv))}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
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
