package bdv.util;

import static bdv.viewer.VisibilityAndGrouping.Event.VISIBILITY_CHANGED;

import java.util.ArrayList;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping.Event;
import bdv.viewer.VisibilityAndGrouping.UpdateListener;
import bdv.viewer.state.SourceGroup;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.TransformListener;

public final class PlaceHolderOverlayInfo implements TransformListener< AffineTransform3D >, TimePointListener, UpdateListener
{
	private final ViewerPanel viewer;

	private final Source< ? > source;

	private final ConverterSetup converterSetup;

	private final AffineTransform3D viewerTransform;

	private int timePointIndex;

	private boolean wasVisible;

	private final ArrayList< VisibilityChangeListener > listeners;

	public static interface VisibilityChangeListener
	{
		public void visibilityChanged();
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
			final Source< ? > source,
			final ConverterSetup converterSetup )
	{
		this.viewer = viewer;
		this.source = source;
		this.converterSetup = converterSetup;
		this.viewerTransform = new AffineTransform3D();
		this.listeners = new ArrayList<>();

		viewer.addRenderTransformListener( this );
		viewer.addTimePointListener( this );
		viewer.getVisibilityAndGrouping().addUpdateListener( this );
	}

	Source< ? > getSource()
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
		final ViewerState state = viewer.getState();
		int sourceIndex = 0;
		for ( final SourceState< ? > s : state.getSources() )
			if ( s.getSpimSource() == source )
				break;
			else
				++sourceIndex;
		switch ( state.getDisplayMode() )
		{
		case SINGLE:
			return ( sourceIndex == state.getCurrentSource() );
		case GROUP:
			return state.getSourceGroups().get( state.getCurrentGroup() ).getSourceIds().contains( sourceIndex );
		case FUSED:
			return state.getSources().get( sourceIndex ).isActive();
		case FUSEDGROUP:
		default:
			for ( final SourceGroup group : state.getSourceGroups() )
				if ( group.isActive() && group.getSourceIds().contains( sourceIndex ) )
					return true;
		}
		return false;
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
	public void visibilityChanged( final Event e )
	{
		if ( e.id == VISIBILITY_CHANGED )
		{
			final boolean isVisible = isVisible();
			if ( wasVisible != isVisible )
			{
				wasVisible = isVisible;
				synchronized( listeners )
				{
					for ( final VisibilityChangeListener l : listeners )
						l.visibilityChanged();
				}
			}
		}
	}

	/**
	 * Registers a VisibilityChangeListener, that will be notified when the
	 * visibility of the source represented by this PlaceHolderOverlayInfo
	 * changes.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addVisibilityChangeListener( final VisibilityChangeListener listener )
	{
		synchronized( listeners )
		{
			if ( !listeners.contains( listener ) )
			{
				listeners.add( listener );
				return true;
			}
			return false;
		}
	}

	/**
	 * Removes the specified listener.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of
	 *         this model and was successfully removed.
	 */
	public boolean removeVisibilityChangeListener( final VisibilityChangeListener listener )
	{
		synchronized( listeners )
		{
			return listeners.remove( listener );
		}
	}
}
