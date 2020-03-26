package bdv.util;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerStateChange;
import bdv.viewer.ViewerStateChangeListener;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.TransformListener;
import org.scijava.listeners.Listeners;

public final class PlaceHolderOverlayInfo implements TransformListener< AffineTransform3D >, TimePointListener, ViewerStateChangeListener
{
	private final ViewerPanel viewer;

	private final SourceAndConverter< ? > source;

	private final ConverterSetup converterSetup;

	private final AffineTransform3D viewerTransform;

	private int timePointIndex;

	private boolean wasVisible;

	private final Listeners.List< VisibilityChangeListener > listeners;

	public interface VisibilityChangeListener
	{
		void visibilityChanged();
	}

	/**
	 *
	 * @param viewer
	 * @param source
	 *            used for determining visibility.
	 * @param converterSetup
	 */
	public PlaceHolderOverlayInfo(
			final ViewerPanel viewer,
			final SourceAndConverter< ? > source,
			final ConverterSetup converterSetup )
	{
		this.viewer = viewer;
		this.source = source;
		this.converterSetup = converterSetup;
		this.viewerTransform = new AffineTransform3D();
		this.listeners = new Listeners.SynchronizedList<>();

		viewer.addRenderTransformListener( this );
		viewer.addTimePointListener( this );
		viewer.state().changeListeners().add( this );
	}

	SourceAndConverter< ? > getSource()
	{
		return source;
	}

	@Override
	public void transformChanged( final AffineTransform3D t )
	{
		synchronized( viewerTransform )
		{
			viewerTransform.set( t );
		}
	}

	public boolean isVisible()
	{
		return viewer.state().isSourceVisible( source );
	}

	public void getViewerTransform( final AffineTransform3D t )
	{
		synchronized( viewerTransform )
		{
			t.set( viewerTransform );
		}
	}

	public int getTimePointIndex()
	{
		return timePointIndex;
	}

	/**
	 * Get the (largest) source value that is mapped to the minimum of the
	 * target range.
	 *
	 * @return source value that is mapped to the minimum of the target range.
	 */
	public double getDisplayRangeMin()
	{
		return converterSetup.getDisplayRangeMin();
	}

	/**
	 * Get the (smallest) source value that is mapped to the maximum of the
	 * target range.
	 *
	 * @return source value that is mapped to the maximum of the target range.
	 */
	public double getDisplayRangeMax()
	{
		return converterSetup.getDisplayRangeMax();
	}

	/**
	 * Get the color for this converter.
	 *
	 * @return the color for this converter.
	 */
	public ARGBType getColor()
	{
		return converterSetup.getColor();
	}

	@Override
	public void timePointChanged( final int timePointIndex )
	{
		this.timePointIndex = timePointIndex;
	}

	@Override
	public void viewerStateChanged( final ViewerStateChange change )
	{
		if ( change == ViewerStateChange.VISIBILITY_CHANGED )
		{
			final boolean isVisible = isVisible();
			if ( wasVisible != isVisible )
			{
				wasVisible = isVisible;
				listeners.list.forEach( VisibilityChangeListener::visibilityChanged );
			}
		}
	}

	/**
	 * Register {@code VisibilityChangeListener}s, that will be notified when the
	 * visibility of the source represented by this PlaceHolderOverlayInfo
	 * changes.
	 */
	public Listeners< VisibilityChangeListener > visibilityChangeListeners()
	{
		return listeners;
	}

	/**
	 * Registers a VisibilityChangeListener, that will be notified when the
	 * visibility of the source represented by this PlaceHolderOverlayInfo
	 * changes.
	 *
	 * @deprecated Use {@code visibilityChangeListeners().add(listener)} instead.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	@Deprecated
	public boolean addVisibilityChangeListener( final VisibilityChangeListener listener )
	{
		return listeners.add( listener );
	}

	/**
	 * Removes the specified listener.
	 *
	 * @deprecated Use {@code visibilityChangeListeners().remove(listener)} instead.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of
	 *         this model and was successfully removed.
	 */
	@Deprecated
	public boolean removeVisibilityChangeListener( final VisibilityChangeListener listener )
	{
		return listeners.remove( listener );
	}
}
