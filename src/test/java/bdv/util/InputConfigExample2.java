package bdv.util;

import java.io.IOException;
import java.util.Random;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class InputConfigExample2
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF00FF00 ) );
		InputTriggerConfig conf = null;
		try
		{
			/* load input config from file */
			conf = new InputTriggerConfig( YamlConfigIO.read( "src/test/resources/bdvkeyconfig.yaml" ) );
		}
		catch ( IllegalArgumentException | IOException e )
		{
			e.printStackTrace();
		}
		BdvFunctions.show( img, "greens", Bdv.options().is2D().inputTriggerConfig( conf ) );
	}
}
