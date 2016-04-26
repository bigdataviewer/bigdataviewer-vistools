package bdv.util;

import java.util.Arrays;
import java.util.Random;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class InputConfigExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF00FF00 ) );
		final InputTriggerConfig conf = new InputTriggerConfig(
				Arrays.asList(
						/* translate by pressing "A" and moving the mouse */
						new InputTriggerDescription( new String[] {"A"}, "2d drag translate", "bdv" ),
						/* translate by pressing "alt" and scrolling horizontally/vertically */
						new InputTriggerDescription( new String[] {"alt scroll"}, "2d scroll translate", "bdv" ) )
				);
		BdvFunctions.show( img, "greens", Bdv.options().is2D().inputTriggerConfig( conf ) );
	}
}
