package bdv.util;

import java.util.Random;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.ARGBType;

public class ExampleSwitch2D3D
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final ArrayImg< ARGBType, IntArray > img = ArrayImgs.argbs( 100, 100, 100 );
		img.forEach( t -> t.set( random.nextInt() & 0xFF00FF00 ) );
		final Bdv bdv3D = BdvFunctions.show( img, "greens", Bdv.options()
				.transformEventHandlerFactory( BehaviourTransformEventHandlerSwitchable.factory() ) );

		final ArrayImg< ARGBType, IntArray > img2 = ArrayImgs.argbs( 100, 100, 100 );
		img2.forEach( t -> t.set( random.nextInt() & 0xFFFF0000 ) );
		BdvFunctions.show( img2, "reds", Bdv.options().addTo( bdv3D ) );


		final BehaviourTransformEventHandlerSwitchable tfh = ( BehaviourTransformEventHandlerSwitchable ) bdv3D.getBdvHandle().getViewerPanel().getDisplay().getTransformEventHandler();

		final Actions actions = new Actions( new InputTriggerConfig() );
		actions.install( bdv3D.getBdvHandle().getKeybindings(), "switch dims" );
		actions.runnableAction( () -> {
			tfh.set2D( !tfh.is2D() );
			tfh.install( bdv3D.getBdvHandle().getTriggerbindings() );
		}, "switch 2d/3d", "D" );
	}
}
