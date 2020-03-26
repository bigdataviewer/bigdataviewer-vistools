package bdv.util;

import java.util.Random;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Util;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

public class ActionExample
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF00FF00 ) );
		Bdv bdv = BdvFunctions.show( img, "greens", Bdv.options().is2D() );

		Actions actions = new Actions( new InputTriggerConfig() );
		actions.install( bdv.getBdvHandle().getKeybindings(), "my-new-actions" );

		actions.runnableAction( () -> {
				final RealPoint pos = new RealPoint( 3 );
				bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( pos );
				System.out.println( "global coordinates: " + Util.printCoordinates(pos) );
			}, "print global pos", "G" );
	}
}
