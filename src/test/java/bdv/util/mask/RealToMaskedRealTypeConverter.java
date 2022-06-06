package bdv.util.mask;

import net.imglib2.converter.Converter;
import net.imglib2.type.mask.AbstractMaskedRealType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

public class RealToMaskedRealTypeConverter< V extends RealType< V >, T extends AbstractMaskedRealType< V, ?, T > >
	implements Converter< V, T >
{
	@Override
	public void convert( final V input, final T output )
	{
		output.value().set( input );
	}
}
