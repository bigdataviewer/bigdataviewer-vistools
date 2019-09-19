/*-
 * #%L
 * UI for BigDataViewer.
 * %%
 * Copyright (C) 2017 - 2018 Tim-Oliver Buchholz
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
package bdv.util.uipanel;

import bdv.util.Affine3DHelpers;
import bdv.util.BdvSource;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.BigDataViewer;
import bdv.tools.transformation.ManualTransformActiveListener;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BdvHandle.SourceChangeListener;
import bdv.util.uipanel.SelectionAndGroupingTabs.SelectionChangeListener;
import bdv.viewer.DisplayMode;
import bdv.viewer.Source;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.state.SourceGroup;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import net.miginfocom.swing.MigLayout;

/**
 *
 * Offering the different transformation option of the {@link BigDataViewer}.
 *
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 */
public class TransformationPanel extends JPanel implements SourceChangeListener, SelectionChangeListener
{

	private static final long serialVersionUID = 1L;

	/**
	 * Reset transformation button.
	 */
	private JButton reset;

	/**
	 * Enable individual transformation of sources/groups.
	 */
	private JCheckBox individualTransformation;

	private TriggerBehaviourBindings triggerBindings;

	private ManualTransformationEditor manualTransformationEditor;

	private ViewerPanel viewerPanel;

	private VisibilityAndGrouping visGro;

	private final Map< String, AffineTransform3D > transformationLookup = new HashMap<>();

	private boolean isOverlay = false;

	/**
	 * Panel holding the controls of the viewer and individual transformation.
	 *
	 * @param es
	 *            the event-service
	 * @param controller
	 *            the BDV controller
	 */
	public TransformationPanel( final TriggerBehaviourBindings triggerBindings,
			final ManualTransformationEditor manualTransformationEditor, final ViewerPanel viewerPanel )
	{

		this.triggerBindings = triggerBindings;
		this.manualTransformationEditor = manualTransformationEditor;
		manualTransformationEditor.addManualTransformActiveListener( new ManualTransformActiveListener()
		{

			@Override
			public void manualTransformActiveChanged( final boolean arg0 )
			{
				individualTransformation.setSelected( arg0 );
				if ( !arg0 )
				{
					saveTransformation();
				}
			}
		} );
		this.viewerPanel = viewerPanel;
		visGro = this.viewerPanel.getVisibilityAndGrouping();
		setupPanel();

		final JCheckBox translation = new JCheckBox( "Allow Translation", true );
		final JCheckBox rotation = new JCheckBox( "Allow Rotation", true );
		setupTranslationCheckBox( translation );
		setupRotationCheckBox( rotation );

		reset = new JButton( "Reset Viewer Transformation" );
		setupResetButton();

		individualTransformation = new JCheckBox( "Manipulate Initial Transformation" );
		setupManualTransformationCheckBox();

		this.manualTransformationEditor.addManualTransformActiveListener( new ManualTransformActiveListener()
		{

			@Override
			public void manualTransformActiveChanged( boolean active )
			{
				manualTransformation( active );
			}
		} );

		this.manualTransformationEditor.addManualTransformActiveListener( new ManualTransformActiveListener()
		{

			@Override
			public void manualTransformActiveChanged( boolean active )
			{
				individualTransformation.setSelected( active );
			}
		} );

		translation.doClick();
		rotation.doClick();

		this.add( translation, "wrap" );
		this.add( rotation, "wrap" );
		this.add( individualTransformation, "growx, wrap" );
		this.add( reset );
	}

	/**
	 * Initialize transformation panel with color, title, and layout manager
	 */
	private void setupPanel()
	{
		this.setBackground( Color.white );
		this.setBorder( new TitledBorder( "Transformation" ) );
		this.setLayout( new MigLayout( "fillx", "", "" ) );
	}

	/**
	 * Initialize and configure manual transformation checkbox
	 */
	private void setupManualTransformationCheckBox()
	{
		individualTransformation.setToolTipText( "Only possible if all active sources are shown." );
		individualTransformation.setBackground( Color.white );
		individualTransformation.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent ev )
			{
				if ( ev.getSource() == individualTransformation )
				{
					final boolean selected = individualTransformation.isSelected();
					manualTransformation( selected );
					manualTransformationEditor.setActive( selected );
				}
			}
		} );
	}

	/**
	 * Save transformation on selected source/group.
	 */
	private void saveTransformation()
	{
		final AffineTransform3D t = new AffineTransform3D();
		if ( visGro.getDisplayMode() == DisplayMode.GROUP || visGro.getDisplayMode() == DisplayMode.FUSEDGROUP )
		{
			final SourceGroup currentGroup = viewerPanel.getState().getSourceGroups()
					.get( viewerPanel.getState().getCurrentGroup() );
			final List< SourceState< ? > > sources = viewerPanel.getState().getSources();
			for ( int id : currentGroup.getSourceIds() )
			{
				final Source< ? > s = sources.get( id ).getSpimSource();
				if ( TransformedSource.class.isInstance( s ) )
				{
					( ( TransformedSource< ? > ) s ).getFixedTransform( t );
				}
				transformationLookup.put( s.getName(), t );
			}
		}
		else
		{
			final int currentSource = viewerPanel.getState().getCurrentSource();
			if ( currentSource > -1 )
			{
				final Source< ? > source = viewerPanel.getState().getSources().get( currentSource ).getSpimSource();
				if ( TransformedSource.class.isInstance( source ) )
				{
					( ( TransformedSource< ? > ) source ).getFixedTransform( t );
				}
				transformationLookup
						.put( viewerPanel.getState().getSources().get( currentSource ).getSpimSource().getName(), t );
			}

		}
	}

	/**
	 * Extract the transformation of the source with sourceIdx.
	 *
	 * @param sourceIdx
	 *            index of the source
	 * @return transformation
	 */
	private AffineTransform3D getInitialTransformation( final int sourceIdx )
	{
		final AffineTransform3D t = new AffineTransform3D();
		final Source< ? > source = viewerPanel.getState().getSources().get( sourceIdx ).getSpimSource();
		if ( TransformedSource.class.isInstance( source ) )
		{
			( ( TransformedSource< ? > ) source ).getFixedTransform( t );
		}
		return t;
	}

	/**
	 * Initialize and configure reset button.
	 */
	private void setupResetButton()
	{
		reset.setBackground( Color.WHITE );
		reset.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if ( e.getSource() == reset && !isOverlay )
				{
					if ( individualTransformation.isSelected() )
					{
						manualTransformationEditor.reset();
					}
					else
					{
						final int numSources = viewerPanel.getState().numSources();
						for ( int i = 0; i < numSources; ++i )
						{
							final Source< ? > source = viewerPanel.getState().getSources().get( i ).getSpimSource();
							if ( TransformedSource.class.isInstance( source ) )
							{
								( ( TransformedSource< ? > ) source ).setFixedTransform( new AffineTransform3D() );
								( ( TransformedSource< ? > ) source ).setIncrementalTransform( new AffineTransform3D() );
								( ( TransformedSource< ? > ) source )
										.setIncrementalTransform( transformationLookup.get( source.getName() ) );
							}
						}
						viewerPanel.setCurrentViewerTransform( createViewerInitTransformation() );
					}
				}
			}
		} );
	}

	/**
	 * Compute initial transformation.
	 *
	 * @return the transformation.
	 */
	private AffineTransform3D createViewerInitTransformation()
	{
		final double cX = viewerPanel.getWidth() / 2d;
		final double cY = viewerPanel.getHeight() / 2d;
		ViewerState state = viewerPanel.getState();
		if ( state.getCurrentSource() < 0 ) { return new AffineTransform3D(); }
		final Source< ? > source = state.getSources().get( state.getCurrentSource() ).getSpimSource();
		final int timepoint = state.getCurrentTimepoint();

		final AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( timepoint, 0, sourceTransform );

		final Interval sourceInterval = source.getSource( timepoint, 0 );
		final double sX0 = sourceInterval.min( 0 );
		final double sX1 = sourceInterval.max( 0 );
		final double sY0 = sourceInterval.min( 1 );
		final double sY1 = sourceInterval.max( 1 );
		final double sZ0 = sourceInterval.min( 2 );
		final double sZ1 = sourceInterval.max( 2 );
		final double sX = ( sX0 + sX1 + 1 ) / 2;
		final double sY = ( sY0 + sY1 + 1 ) / 2;
		final double sZ = ( int ) ( sZ0 + sZ1 + 1 ) / 2;

		final double[][] m = new double[ 3 ][ 4 ];

		// rotation
		final double[] qSource = new double[ 4 ];
		final double[] qViewer = new double[ 4 ];
		Affine3DHelpers.extractApproximateRotationAffine( sourceTransform, qSource, 2 );
		LinAlgHelpers.quaternionInvert( qSource, qViewer );
		LinAlgHelpers.quaternionToR( qViewer, m );

		// translation
		final double[] centerSource = new double[] { sX, sY, sZ };
		final double[] centerGlobal = new double[ 3 ];
		final double[] translation = new double[ 3 ];
		sourceTransform.apply( centerSource, centerGlobal );
		LinAlgHelpers.quaternionApply( qViewer, centerGlobal, translation );
		LinAlgHelpers.scale( translation, -1, translation );
		LinAlgHelpers.setCol( 3, translation, m );

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		viewerTransform.set( m );

		if ( ( sX1 - sX0 ) >= ( sY1 - sY0 ) )
		{
			viewerTransform.scale( viewerPanel.getWidth() / ( 4.0 * ( sX1 - sX0 ) ) );
		}
		else
		{
			viewerTransform.scale( viewerPanel.getHeight() / ( 4.0 * ( sY1 - sY0 ) ) );
		}

		// scale
		final double[] pSource = new double[] { sX1 + 0.5, sY1 + 0.5, sZ };
		final double[] pGlobal = new double[ 3 ];
		final double[] pScreen = new double[ 3 ];
		sourceTransform.apply( pSource, pGlobal );
		viewerTransform.apply( pGlobal, pScreen );
		final double scaleX = cX / pScreen[ 0 ];
		final double scaleY = cY / pScreen[ 1 ];
		final double scale;
		scale = Math.min( scaleX, scaleY );
		viewerTransform.scale( scale );

		// window center offset
		viewerTransform.set( viewerTransform.get( 0, 3 ) + cX, 0, 3 );
		viewerTransform.set( viewerTransform.get( 1, 3 ) + cY, 1, 3 );

		return viewerTransform;
	}

	/**
	 * Initialize and configure rotation check box.
	 *
	 * @param rotation
	 */
	private void setupRotationCheckBox( final JCheckBox rotation )
	{
		rotation.setBackground( Color.WHITE );
		rotation.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if ( e.getSource() == rotation )
				{
					if ( !rotation.isSelected() )
					{
						blockRotation();
					}
					else
					{
						triggerBindings.removeBehaviourMap( "blockRotation" );
					}
				}
			}
		} );
	}

	/**
	 * Add empty behaviours to block translation.
	 */
	private void blockTranslation()
	{
		final BehaviourMap blockTranslation = new BehaviourMap();
		blockTranslation.put( "drag translate", new Behaviour()
		{} );

		// 2D
		blockTranslation.put( "2d drag translate", new Behaviour()
		{} );

		triggerBindings.addBehaviourMap( "blockTranslation", blockTranslation );
	}

	/**
	 * Add empty behaviours to block rotation.
	 */
	private void blockRotation()
	{
		final BehaviourMap blockRotation = new BehaviourMap();
		blockRotation.put( "rotate left", new Behaviour()
		{} );
		blockRotation.put( "rotate left slow", new Behaviour()
		{} );
		blockRotation.put( "rotate left fast", new Behaviour()
		{} );

		blockRotation.put( "rotate right", new Behaviour()
		{} );
		blockRotation.put( "rotate right slow", new Behaviour()
		{} );
		blockRotation.put( "rotate right fast", new Behaviour()
		{} );

		blockRotation.put( "drag rotate", new Behaviour()
		{} );
		blockRotation.put( "drag rotate slow", new Behaviour()
		{} );
		blockRotation.put( "drag rotate fast", new Behaviour()
		{} );

		// 2D
		blockRotation.put( "2d drag rotate", new Behaviour()
		{} );
		blockRotation.put( "2d scroll rotate", new Behaviour()
		{} );
		blockRotation.put( "2d scroll rotate slow", new Behaviour()
		{} );
		blockRotation.put( "2d scroll rotate fast", new Behaviour()
		{} );
		blockRotation.put( "2d scroll translate", new Behaviour()
		{} );
		blockRotation.put( "2d rotate left", new Behaviour()
		{} );
		blockRotation.put( "2d rotate right", new Behaviour()
		{} );
		triggerBindings.addBehaviourMap( "blockRotation", blockRotation );
	}

	/**
	 * Initialize and configure translation check box.
	 *
	 * @param translation
	 */
	private void setupTranslationCheckBox( final JCheckBox translation )
	{
		translation.setBackground( Color.WHITE );
		translation.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if ( e.getSource() == translation )
				{
					if ( !translation.isSelected() )
					{
						blockTranslation();
					}
					else
					{
						triggerBindings.removeBehaviourMap( "blockTranslation" );
					}

				}
			}
		} );
	}

	/**
	 * Change text dependent on transformation handler.
	 *
	 * @param active
	 */
	private void manualTransformation( final boolean active )
	{
		if ( active )
		{
			reset.setText( "Reset to Initial Transformation" );
		}
		else
		{
			reset.setText( "Reset Viewer Transformation" );
		}
	}

	@Override
	public void addSource( BdvSource source )
	{
		transformationLookup.put( source.getName(),
				getInitialTransformation( viewerPanel.getVisibilityAndGrouping().getCurrentSource() ) );
	}

	@Override
	public void removeSource( BdvSource source )
	{
		transformationLookup.remove( source.getName() );
	}

	@Override
	public void selectionChanged( boolean isOverlay )
	{
		this.isOverlay = isOverlay;
		reset.setEnabled( !isOverlay );
	}
}
