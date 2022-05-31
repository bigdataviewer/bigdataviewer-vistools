package bdv.util.mask;

import net.imglib2.converter.Converter;
import net.imglib2.type.mask.AbstractMaskedRealType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

public class PairToMaskedRealTypeConverter<  V extends RealType< V >, M extends RealType< M >, T extends AbstractMaskedRealType< V, M, T > >
	implements Converter< Pair< V, M >, T >
{
	@Override
	public void convert( final Pair< V, M > input, final T output )
	{
		output.value().set( input.getA() );
		output.mask().set( input.getB() );
	}
}
