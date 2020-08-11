/*-
 * #%L
 * BigDataViewer quick visualization API.
 * %%
 * Copyright (C) 2016 - 2020 BigDataViewer developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bdv.util;

import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.BehaviourTransformEventHandler;
import bdv.BehaviourTransformEventHandlerFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;

/**
 * A {@link TransformEventHandler} that changes an {@link AffineTransform3D}
 * through a set of {@link Behaviour}s.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class BehaviourTransformEventHandlerPlanar implements BehaviourTransformEventHandler< AffineTransform3D >
{
	public static TransformEventHandlerFactory< AffineTransform3D > factory()
	{
		return new BehaviourTransformEventHandlerPlanarFactory();
	}

	public static class BehaviourTransformEventHandlerPlanarFactory implements BehaviourTransformEventHandlerFactory< AffineTransform3D >
	{
		private InputTriggerConfig config = new InputTriggerConfig();

		@Override
		public void setConfig( final InputTriggerConfig config )
		{
			this.config = config;
		}

		@Override
		public BehaviourTransformEventHandlerPlanar create( final TransformListener< AffineTransform3D > transformListener )
		{
			return new BehaviourTransformEventHandlerPlanar( transformListener, config );
		}
	}

	/**
	 * Current source to screen transform.
	 */
	protected final AffineTransform3D affine = new AffineTransform3D();

	/**
	 * Whom to notify when the {@link #affine current transform} is changed.
	 */
	private TransformListener< AffineTransform3D > listener;

	/**
	 * Copy of {@link #affine current transform} when mouse dragging started.
	 */
	final protected AffineTransform3D affineDragStart = new AffineTransform3D();

	/**
	 * Coordinates where mouse dragging started.
	 */
	protected double oX, oY;

	/**
	 * The screen size of the canvas (the component displaying the image and
	 * generating mouse events).
	 */
	protected int canvasW = 1, canvasH = 1;

	/**
	 * Screen coordinates to keep centered while zooming or rotating with the
	 * keyboard. These are set to <em>(canvasW/2, canvasH/2)</em>
	 */
	protected int centerX = 0, centerY = 0;

	protected final Behaviours behaviours;

	public BehaviourTransformEventHandlerPlanar( final TransformListener< AffineTransform3D > listener, final InputTriggerConfig config )
	{
		this.listener = listener;

		final String DRAG_TRANSLATE = "2d drag translate";
		final String ZOOM_NORMAL = "2d scroll zoom";

		final double[] speed =      { 1.0,     10.0,     0.1 };
		final String[] SPEED_NAME = {  "",  " fast", " slow" };
		final String[] speedMod =   {  "", "shift ", "ctrl " };

		final String DRAG_ROTATE = "2d drag rotate";
		final String SCROLL_ROTATE = "2d scroll rotate";
		final String SCROLL_TRANSLATE = "2d scroll translate";
		final String ROTATE_LEFT = "2d rotate left";
		final String ROTATE_RIGHT = "2d rotate right";
		final String KEY_ZOOM_IN = "2d zoom in";
		final String KEY_ZOOM_OUT = "2d zoom out";

		behaviours = new Behaviours( config, "bdv" );

		behaviours.behaviour( new DragTranslate(), DRAG_TRANSLATE, "button2", "button3" );
		behaviours.behaviour( new Zoom( speed[ 0 ] ), ZOOM_NORMAL, "meta scroll", "ctrl shift scroll" );
		behaviours.behaviour( new ScrollTranslate(), SCROLL_TRANSLATE, "not mapped" );
		behaviours.behaviour( new DragRotate(), DRAG_ROTATE, "button1" );

		for ( int s = 0; s < 3; ++s )
		{
			behaviours.behaviour( new ScrollRotate( 2 * speed[ s ] ), SCROLL_ROTATE + SPEED_NAME[ s ], speedMod[ s ] + "scroll" );
			behaviours.behaviour( new KeyRotate( speed[ s ] ), ROTATE_LEFT + SPEED_NAME[ s ], speedMod[ s ] + "LEFT" );
			behaviours.behaviour( new KeyRotate( -speed[ s ] ), ROTATE_RIGHT + SPEED_NAME[ s ], speedMod[ s ] + "RIGHT" );
			behaviours.behaviour( new KeyZoom( speed[ s ] ), KEY_ZOOM_IN + SPEED_NAME[ s ], speedMod[ s ] + "UP" );
			behaviours.behaviour( new KeyZoom( -speed[ s ] ), KEY_ZOOM_OUT + SPEED_NAME[ s ], speedMod[ s ] + "DOWN" );
		}
	}

	@Override
	public void install( final TriggerBehaviourBindings bindings )
	{
		behaviours.install( bindings, "transform" );
	}

	@Override
	public AffineTransform3D getTransform()
	{
		synchronized ( affine )
		{
			return affine.copy();
		}
	}

	@Override
	public void setTransform( final AffineTransform3D transform )
	{
		synchronized ( affine )
		{
			affine.set( transform );
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height, final boolean updateTransform )
	{
		if ( updateTransform )
		{
			synchronized ( affine )
			{
				affine.set( affine.get( 0, 3 ) - canvasW / 2, 0, 3 );
				affine.set( affine.get( 1, 3 ) - canvasH / 2, 1, 3 );
				affine.scale( ( double ) width / canvasW );
				affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
				affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
				notifyListener();
			}
		}
		canvasW = width;
		canvasH = height;
		centerX = width / 2;
		centerY = height / 2;
	}

	@Override
	public void setTransformListener( final TransformListener< AffineTransform3D > transformListener )
	{
		listener = transformListener;
	}

	@Override
	public String getHelpString()
	{
		return null;
	}

	/**
	 * notifies {@link #listener} that the current transform changed.
	 */
	private void notifyListener()
	{
		if ( listener != null )
			listener.transformChanged( affine );
	}

	/**
	 * One step of rotation (radian).
	 */
	final protected static double step = Math.PI / 180;

	private void scale( final double s, final double x, final double y )
	{
		// center shift
		affine.set( affine.get( 0, 3 ) - x, 0, 3 );
		affine.set( affine.get( 1, 3 ) - y, 1, 3 );

		// scale
		affine.scale( s );

		// center un-shift
		affine.set( affine.get( 0, 3 ) + x, 0, 3 );
		affine.set( affine.get( 1, 3 ) + y, 1, 3 );
	}

	/**
	 * Rotate by d radians around axis. Keep screen coordinates (
	 * {@link #centerX}, {@link #centerY}) fixed.
	 */
	private void rotate( final int axis, final double d )
	{
		// center shift
		affine.set( affine.get( 0, 3 ) - centerX, 0, 3 );
		affine.set( affine.get( 1, 3 ) - centerY, 1, 3 );

		// rotate
		affine.rotate( axis, d );

		// center un-shift
		affine.set( affine.get( 0, 3 ) + centerX, 0, 3 );
		affine.set( affine.get( 1, 3 ) + centerY, 1, 3 );
	}

	private class DragRotate implements DragBehaviour
	{
		@Override
		public void init( final int x, final int y )
		{
			synchronized ( affine )
			{
				oX = x;
				oY = y;
				affineDragStart.set( affine );
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			synchronized ( affine )
			{
				final double dX = x - centerX;
				final double dY = y - centerY;
				final double odX = oX - centerX;
				final double odY = oY - centerY;
				final double theta = Math.atan2( dY, dX ) - Math.atan2( odY, odX );

				affine.set( affineDragStart );
				rotate( 2, theta );
				notifyListener();
			}
		}

		@Override
		public void end( final int x, final int y )
		{}
	}

	private class ScrollRotate implements ScrollBehaviour
	{
		private final double speed;

		public ScrollRotate( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double theta = speed * wheelRotation * Math.PI / 180.0;

				// center shift
				affine.set( affine.get( 0, 3 ) - x, 0, 3 );
				affine.set( affine.get( 1, 3 ) - y, 1, 3 );

				affine.rotate( 2, theta );

				// center un-shift
				affine.set( affine.get( 0, 3 ) + x, 0, 3 );
				affine.set( affine.get( 1, 3 ) + y, 1, 3 );

				notifyListener();
			}
		}
	}

	private class DragTranslate implements DragBehaviour
	{
		@Override
		public void init( final int x, final int y )
		{
			synchronized ( affine )
			{
				oX = x;
				oY = y;
				affineDragStart.set( affine );
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			synchronized ( affine )
			{
				final double dX = oX - x;
				final double dY = oY - y;

				affine.set( affineDragStart );
				affine.set( affine.get( 0, 3 ) - dX, 0, 3 );
				affine.set( affine.get( 1, 3 ) - dY, 1, 3 );
				notifyListener();
			}
		}

		@Override
		public void end( final int x, final int y )
		{}
	}

	private class ScrollTranslate implements ScrollBehaviour
	{
		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double d = -wheelRotation * 10;
				if ( isHorizontal )
					affine.translate( d, 0, 0 );
				else
					affine.translate( 0, d, 0 );
				notifyListener();
			}
		}
	}

	private class Zoom implements ScrollBehaviour
	{
		private final double speed;

		public Zoom( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double s = speed * wheelRotation;
				final double dScale = 1.0 + 0.05;
				if ( s > 0 )
					scale( 1.0 / dScale, x, y );
				else
					scale( dScale, x, y );
				notifyListener();
			}
		}
	}

	private class KeyRotate implements ClickBehaviour
	{
		private final double speed;

		public KeyRotate( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void click( final int x, final int y )
		{
			synchronized ( affine )
			{
				rotate( 2, step * speed );
				notifyListener();
			}
		}
	}

	private class KeyZoom implements ClickBehaviour
	{
		private final double dScale;

		public KeyZoom( final double speed )
		{
			if ( speed > 0 )
				dScale = 1.0 + 0.1 * speed;
			else
				dScale = 1.0 / ( 1.0 - 0.1 * speed );
		}

		@Override
		public void click( final int x, final int y )
		{
			synchronized ( affine )
			{
				scale( dScale, centerX, centerY );
				notifyListener();
			}
		}
	}
}
