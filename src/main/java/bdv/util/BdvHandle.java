package bdv.util;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.cache.CacheControl.CacheControls;
import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping.UpdateListener;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

/**
 * Represents a BigDataViewer frame or panel and can be used to get to the bdv
 * internals.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class BdvHandle implements Bdv
{
	protected ViewerPanel viewer;

	protected SetupAssignments setupAssignments;

	protected final ArrayList< BdvSource > bdvSources;

	protected final BdvOptions bdvOptions;

	protected boolean hasPlaceHolderSources;

	protected final int origNumTimepoints;

	protected CacheControls cacheControls;

	public BdvHandle( final BdvOptions options )
	{
		bdvOptions = options;
		bdvSources = new ArrayList<>();
		origNumTimepoints = 1;
	}

	@Override
	public BdvHandle getBdvHandle()
	{
		return this;
	}

	public ViewerPanel getViewerPanel()
	{
		return viewer;
	}

	public SetupAssignments getSetupAssignments()
	{
		return setupAssignments;
	}

	CacheControls getCacheControls()
	{
		return cacheControls;
	}

	int getUnusedSetupId()
	{
		return BdvFunctions.getUnusedSetupId( setupAssignments );
	}

	@Override
	public abstract void close();

	public abstract ManualTransformationEditor getManualTransformEditor();

	public abstract InputActionBindings getKeybindings();

	public abstract TriggerBehaviourBindings getTriggerbindings();

	abstract boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints );

	void add(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		final boolean initTransform;
		if ( viewer == null )
		{
			initTransform = createViewer( converterSetups, sources, numTimepoints );
		}
		else
		{
			initTransform = ( viewer.getState().numSources() == 0 ) && !sources.isEmpty();

			if ( converterSetups != null )
			{
				for ( final ConverterSetup setup : converterSetups )
				{
					setupAssignments.addSetup( setup );
					setup.setViewer( viewer );
				}

				final int g = setupAssignments.getMinMaxGroups().size() - 1;
				final MinMaxGroup group = setupAssignments.getMinMaxGroups().get( g );
				for ( final ConverterSetup setup : converterSetups )
					setupAssignments.moveSetupToGroup( setup, group );
			}

			if ( sources != null )
				for ( final SourceAndConverter< ? > soc : sources )
					viewer.addSource( soc );
		}

		if ( initTransform )
		{
			synchronized ( this )
			{
				initTransformPending = true;
				tryInitTransform();
			}
		}

		updateHasPlaceHolderSources();
		updateNumTimepoints();
	}

	private boolean initTransformPending;

	private List<SourceChangeListener> sourceChangeListeners = new ArrayList<>();

	protected synchronized void tryInitTransform()
	{
		if ( viewer.getDisplay().getWidth() <= 0 || viewer.getDisplay().getHeight() <= 0 )
			return;

		if ( initTransformPending )
		{
			initTransformPending = false;

			InitializeViewerState.initTransform( viewer );
			if ( bdvOptions.values.is2D() )
			{
				final AffineTransform3D t = new AffineTransform3D();
				viewer.getState().getViewerTransform( t );
				t.set( 0, 2, 3 );
				viewer.setCurrentViewerTransform( t );
			}
		}
	}

	void remove(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final List< TransformListener< AffineTransform3D > > transformListeners,
			final List< TimePointListener > timepointListeners,
			final List< UpdateListener > visibilityUpdateListeners,
			final List< OverlayRenderer > overlays )
	{
		if ( viewer == null )
			return;

		if ( converterSetups != null )
			for ( final ConverterSetup setup : converterSetups )
				setupAssignments.removeSetup( setup );

		if ( transformListeners != null )
			for ( final TransformListener< AffineTransform3D > l : transformListeners )
				viewer.removeTransformListener( l );

		if ( timepointListeners != null )
			for ( final TimePointListener l : timepointListeners )
				viewer.removeTimePointListener( l );

		if ( visibilityUpdateListeners != null )
			for ( final UpdateListener l : visibilityUpdateListeners )
				viewer.getVisibilityAndGrouping().removeUpdateListener( l );

		if ( overlays != null )
			for ( final OverlayRenderer o : overlays )
				viewer.getDisplay().removeOverlayRenderer( o );

		if ( sources != null )
			for ( final SourceAndConverter< ? > soc : sources )
				viewer.removeSource( soc.getSpimSource() );
	}

	public interface SourceChangeListener
	{
		public void addSource( BdvSource source );

		public void removeSource( BdvSource source );
	}

	void addBdvSource( final BdvSource bdvSource )
	{
		bdvSources.add( bdvSource );
		updateHasPlaceHolderSources();
		updateNumTimepoints();
		updateSourceChangeListenersAdd( bdvSource );
	}

	void removeBdvSource( final BdvSource bdvSource )
	{
		bdvSources.remove( bdvSource );
		updateHasPlaceHolderSources();
		updateNumTimepoints();
		updateSourceChangeListernersRemove( bdvSource );
	}

	void updateHasPlaceHolderSources()
	{
		for ( final BdvSource s : bdvSources )
			if ( s.isPlaceHolderSource() )
			{
				hasPlaceHolderSources = true;
				return;
			}
		hasPlaceHolderSources = false;
	}

	void updateNumTimepoints()
	{
		int numTimepoints = origNumTimepoints;
		for ( final BdvSource s : bdvSources )
			numTimepoints = Math.max( numTimepoints, s.getNumTimepoints() );
		if ( viewer != null )
			viewer.setNumTimepoints( numTimepoints );
	}

	boolean is2D()
	{
		return bdvOptions.values.is2D();
	}

	void addSourceChangeListener( final SourceChangeListener l )
	{
		this.sourceChangeListeners.add( l );
	}

	void updateSourceChangeListenersAdd( final BdvSource source ) {
		for ( final SourceChangeListener l : sourceChangeListeners )
		{
			l.addSource( source );
		}
	}

	void updateSourceChangeListernersRemove( final BdvSource source )
	{
		for ( final SourceChangeListener l : sourceChangeListeners ) {
			l.removeSource( source );
		}
	}
}
