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
package bdv.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.tools.brightness.SetupAssignments;
import bdv.uicomponents.rangeslider.RangeSlider;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.miginfocom.swing.MigLayout;

/**
 * 
 * A panel holding a two-knob range slider with a lower- and upper-value
 * spinner.
 * 
 * The bounds can be dynamically changed by either entering smaller/larger
 * values into the spinner or resizing the range-slider to the current positions
 * with a resize-button.
 * 
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class RangeSliderSpinnerPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	/**
	 * Upper bound of the range slider.
	 */
	private static final int RS_UPPER_BOUND = 1000;

	/**
	 * Setup assignments of the viewer.
	 */
	private final SetupAssignments setupAssignments;

	/**
	 * The range slider.
	 */
	private final RangeSlider rs;

	/**
	 * Range slider number of steps.
	 */
	final double numberOfSteps = 1001.0;

	/**
	 * Display range upper bound.
	 */
	private ListenableDouble upperBound = new ListenableDouble( 1 );

	/**
	 * Display range lower bound.
	 */
	private ListenableDouble lowerBound = new ListenableDouble( 0 );

	/**
	 * Display range upper value. The currently selected upper value.
	 */
	private ListenableDouble upperValue = new ListenableDouble( 1 );

	/**
	 * Display range lower value. The currently selected lower value.
	 */
	private ListenableDouble lowerValue = new ListenableDouble( 0 );

	/**
	 * Store the lower bound for every source.
	 */
	private final HashMap< Integer, Double > lowerBoundLookup = new HashMap<>();

	/**
	 * Store the upper bound for every source.
	 */
	private final HashMap< Integer, Double > upperBoundLookup = new HashMap<>();

	/**
	 * The minimum spinner.
	 */
	private final JSpinner currentMinSpinner;

	/**
	 * The maximum spinner.
	 */
	private final JSpinner currentMaxSpinner;

	/**
	 * Index of the currently selected source.
	 */
	private int currentSourceIdx;

	/**
	 * State of the current source.
	 */
	private boolean isLabeling;

	/**
	 * Min max of the labeling.
	 */
	private HashMap< Integer, Pair< Double, Double > > labelingMinMax;

	private ChangeListener maxSpinnerCL;

	private ChangeListener minSpinnerCL;

	private class ListenableDouble
	{

		private double d;

		private List< DoubleListener > listeners;

		public ListenableDouble( final double init )
		{
			this.d = init;
			listeners = new ArrayList<>();
		}

		public void setValue( final double val )
		{
			this.d = val;
		}

		public double getValue()
		{
			return this.d;
		}

		public void subscribe( final DoubleListener l )
		{
			listeners.add( l );
		}

		public void updateListeners()
		{
			for ( final DoubleListener l : listeners )
			{
				l.doubleChanged( this.d );
			}
		}

	}

	private interface DoubleListener
	{
		public void doubleChanged( final double d );
	}

	/**
	 * A range slider panel with two knobs and min/max spinners.
	 * 
	 */
	public RangeSliderSpinnerPanel( final SetupAssignments setupAssignments, final Map< String, BdvSource > sourceLookup )
	{
		this.labelingMinMax = new HashMap<>();
		setupPanel();

		this.setupAssignments = setupAssignments;

		currentMinSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ) );
		setupMinSpinner();

		currentMaxSpinner = new JSpinner( new SpinnerNumberModel( 1.0, 0.0, 1.0, 1.0 ) );
		setupMaxSpinner();

		rs = new RangeSlider( 0, RS_UPPER_BOUND );
		setupRangeSlider();

		lowerValue.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				setDisplayRange();
			}
		} );

		upperValue.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				setDisplayRange();
			}
		} );

		final JButton shrinkRange = new JButton( "><" );
		setupShrinkRangeButton( shrinkRange );

		this.add( currentMinSpinner );
		this.add( rs, "growx" );
		this.add( currentMaxSpinner );
		this.add( shrinkRange );
	}

	private void setupPanel()
	{
		this.setLayout( new MigLayout( "fillx, hidemode 3", "[][grow][][]", "" ) );
		this.setBorder( new TitledBorder( new LineBorder( Color.lightGray ), "Display Range" ) );
		this.setBackground( Color.WHITE );
	}

	private void setupShrinkRangeButton( final JButton shrinkRange )
	{
		shrinkRange.setBackground( Color.white );
		shrinkRange.setForeground( Color.darkGray );
		shrinkRange.setBorder( null );
		shrinkRange.setMargin( new Insets( 0, 2, 0, 2 ) );
		shrinkRange.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if ( e.getSource() == shrinkRange )
				{
					lowerBound.setValue( ( double ) ( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).getValue() );
					upperBound.setValue( ( double ) ( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).getValue() );
					upperValue.setValue( upperBound.getValue() );
					lowerValue.setValue( lowerBound.getValue() );
					lowerBound.updateListeners();
					upperBound.updateListeners();
					lowerValue.updateListeners();
					upperValue.updateListeners();
					upperBoundLookup.put( currentSourceIdx, upperValue.getValue() );
					lowerBoundLookup.put( currentSourceIdx, lowerValue.getValue() );
					rs.setValue( 0 );
					rs.setUpperValue( RS_UPPER_BOUND );
				}
			}
		} );
	}

	private void setupRangeSlider()
	{
		rs.setBackground( Color.WHITE );
		rs.setPreferredSize( new Dimension( 50, rs.getPreferredSize().height ) );
		rs.setValue( 0 );
		rs.setUpperValue( RS_UPPER_BOUND );
		rs.setMinorTickSpacing( 1 );

		upperValue.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( d != posToLowerValue( rs.getUpperValue() ) )
				{
					setRangeSlider();
				}
			}
		} );

		lowerValue.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( d != posToLowerValue( rs.getValue() ) )
				{
					setRangeSlider();
				}
			}
		} );

		rs.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( ChangeEvent e )
			{
				if ( e.getSource() == rs )
				{
					upperValue.setValue( posToUpperValue( rs.getUpperValue() ) );
					lowerValue.setValue( posToLowerValue( rs.getValue() ) );
					lowerValue.updateListeners();
					upperValue.updateListeners();
				}
			}
		} );
	}

	private void setupMaxSpinner()
	{
		currentMaxSpinner.setPreferredSize( new Dimension( 65, currentMaxSpinner.getPreferredSize().height ) );

		upperValue.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( d != ( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).getNumber().doubleValue() )
				{
					currentMaxSpinner.setValue( d );
				}
			}
		} );

		lowerBound.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( 0 != ( double ) ( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).getMinimum() )
				{

					( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).setMinimum( d );
				}
			}
		} );

		upperBound.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( 0 != ( double ) ( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).getMaximum() )
				{

					( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).setMaximum( d );
				}
			}
		} );

		maxSpinnerCL = new ChangeListener()
		{

			@Override
			public void stateChanged( ChangeEvent e )
			{
				if ( e.getSource() == currentMaxSpinner )
				{
					upperValue.setValue( ( double ) ( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).getValue() );
					upperValue.updateListeners();
				}
			}
		};
		currentMaxSpinner.addChangeListener( maxSpinnerCL );
		currentMaxSpinner.setEditor( new UpperBoundNumberEditor( currentMaxSpinner ) );
	}

	private void setupMinSpinner()
	{
		currentMinSpinner.setPreferredSize( new Dimension( 65, currentMinSpinner.getPreferredSize().height ) );

		lowerValue.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( d != ( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).getNumber().doubleValue() )
				{
					currentMinSpinner.setValue( d );
				}
			}
		} );

		lowerBound.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( 0 != ( double ) ( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).getMinimum() )
				{
					( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).setMinimum( d );
				}
			}
		} );

		upperBound.subscribe( new DoubleListener()
		{

			@Override
			public void doubleChanged( double d )
			{
				if ( 0 != ( double ) ( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).getMaximum() )
				{
					( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).setMaximum( d );
				}
			}
		} );

		minSpinnerCL = new ChangeListener()
		{

			@Override
			public void stateChanged( ChangeEvent e )
			{
				if ( e.getSource() == currentMinSpinner )
				{
					double value = ( double ) ( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).getValue();
					lowerValue.setValue( value );
					lowerValue.updateListeners();
				}
			}
		};
		currentMinSpinner.addChangeListener( minSpinnerCL );
		currentMinSpinner.setEditor( new LowerBoundNumberEditor( currentMinSpinner ) );
	}

	class UpperBoundNumberEditor extends JSpinner.NumberEditor implements KeyListener
	{

		private static final long serialVersionUID = 1L;

		private JFormattedTextField textField;

		public UpperBoundNumberEditor( JSpinner spinner )
		{
			super( spinner );
			textField = getTextField();
			textField.addKeyListener( this );
		}

		@Override
		public void keyTyped( KeyEvent e )
		{}

		@Override
		public void keyPressed( KeyEvent e )
		{
			final String text = textField.getText();
			if ( !text.isEmpty() )
			{
				try
				{
					if ( e.getKeyCode() == KeyEvent.VK_ENTER )
					{
						double tmp = NumberFormat.getNumberInstance().parse( text ).doubleValue();
						if ( isLabeling )
						{
							tmp = Math.min( tmp, 255 );
						}
						if ( tmp > upperBound.getValue() )
						{
							upperBound.setValue( tmp );
							upperBoundLookup.put( currentSourceIdx, upperBound.getValue() );
							upperValue.setValue( tmp );
							upperBound.updateListeners();
							upperValue.updateListeners();
						}
						else
						{
							upperValue.setValue( tmp );
							upperValue.updateListeners();
						}
					}
				}
				catch ( ParseException e1 )
				{
					textField.setText( Double.toString( upperBound.getValue() ) );
				}
			}
		}

		@Override
		public void keyReleased( KeyEvent e )
		{}
	}

	class LowerBoundNumberEditor extends JSpinner.NumberEditor implements KeyListener
	{

		private static final long serialVersionUID = 1L;

		private JFormattedTextField textField;

		public LowerBoundNumberEditor( JSpinner spinner )
		{
			super( spinner );
			textField = getTextField();
			textField.addKeyListener( this );
		}

		@Override
		public void keyTyped( KeyEvent e )
		{}

		@Override
		public void keyPressed( KeyEvent e )
		{
			final String text = textField.getText();
			if ( !text.isEmpty() )
			{
				try
				{
					if ( e.getKeyCode() == KeyEvent.VK_ENTER )
					{
						double tmp = NumberFormat.getNumberInstance().parse( text ).doubleValue();
						if ( isLabeling )
						{
							tmp = Math.max( tmp, 0 );
						}
						if ( tmp < lowerBound.getValue() )
						{
							lowerBound.setValue( tmp );
							lowerBoundLookup.put( currentSourceIdx, lowerBound.getValue() );
							lowerValue.setValue( tmp );
							lowerBound.updateListeners();
							lowerValue.updateListeners();
						}
						else
						{
							lowerValue.setValue( tmp );
							lowerValue.updateListeners();
						}
					}
				}
				catch ( ParseException e1 )
				{
					textField.setText( Double.toString( lowerBound.getValue() ) );
				}
			}
		}

		@Override
		public void keyReleased( KeyEvent e )
		{}
	}

	/**
	 * Set display range in setup-assignments.
	 * 
	 * @param min
	 *            of display range
	 * @param max
	 *            of display range
	 */
	private void setDisplayRange()
	{
		double min = lowerValue.getValue();
		double max = upperValue.getValue();
		if ( isLabeling )
		{
			labelingMinMax.put( currentSourceIdx, new ValuePair<>( min, max ) );
		}
		else
		{
			setupAssignments.getConverterSetups().get( currentSourceIdx ).setDisplayRange( min, max );
		}
	}

	/**
	 * Convert range-slider position to upper-value.
	 * 
	 * @param pos
	 *            of range-slider
	 * @return value
	 */
	private double posToUpperValue( final int pos )
	{
		double frac = pos / 1000d;
		double val = Math.abs( upperBound.getValue() - lowerBound.getValue() ) * frac + lowerBound.getValue();
		return val;
	}

	/**
	 * Convert range-slider position to lower-value.
	 * 
	 * @param pos
	 *            of range-slider
	 * @return value
	 */
	private double posToLowerValue( final int pos )
	{
		double frac = pos / 1000d;
		double val = Math.abs( upperBound.getValue() - lowerBound.getValue() ) * frac + lowerBound.getValue();
		return val;
	}

	public synchronized void setSource( final int i )
	{
		currentMaxSpinner.removeChangeListener( maxSpinnerCL );
		currentMinSpinner.removeChangeListener( minSpinnerCL );
		currentSourceIdx = i;
		if ( lowerBoundLookup.containsKey( currentSourceIdx ) )
		{
			lowerBound.setValue( lowerBoundLookup.get( currentSourceIdx ) );
			upperBound.setValue( upperBoundLookup.get( currentSourceIdx ) );
			if ( isLabeling )
			{
				final Pair< Double, Double > p = labelingMinMax.get( currentSourceIdx );
				lowerValue.setValue( p.getA() );
				upperValue.setValue( p.getB() );
			}
			else
			{
				final double displayRangeMin = setupAssignments.getConverterSetups().get( currentSourceIdx )
						.getDisplayRangeMin();
				final double displayRangeMax = setupAssignments.getConverterSetups().get( currentSourceIdx )
						.getDisplayRangeMax();

				lowerValue.setValue( displayRangeMin );
				upperValue.setValue( displayRangeMax );
			}

			( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).setMinimum( lowerBound.getValue() );
			( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).setMaximum( upperBound.getValue() );
			( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).setMinimum( lowerBound.getValue() );
			( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).setMaximum( upperBound.getValue() );

			lowerValue.updateListeners();
			upperValue.updateListeners();

			rs.revalidate();
			rs.repaint();

			currentMaxSpinner.addChangeListener( maxSpinnerCL );
			currentMinSpinner.addChangeListener( minSpinnerCL );
		}
	}

	public void addSource( final int srcIdx )
	{
		currentMaxSpinner.removeChangeListener( maxSpinnerCL );
		currentMinSpinner.removeChangeListener( minSpinnerCL );
		currentSourceIdx = srcIdx;
		final double displayRangeMin = setupAssignments.getConverterSetups().get( currentSourceIdx ).getDisplayRangeMin();
		final double displayRangeMax = setupAssignments.getConverterSetups().get( currentSourceIdx ).getDisplayRangeMax();
		lowerBound.setValue( displayRangeMin );
		lowerValue.setValue( displayRangeMin );
		upperBound.setValue( displayRangeMax );
		upperValue.setValue( displayRangeMax );

		lowerBoundLookup.put( currentSourceIdx, displayRangeMin );

		upperBoundLookup.put( currentSourceIdx, displayRangeMax );

		( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).setMinimum( lowerBound.getValue() );
		( ( SpinnerNumberModel ) currentMinSpinner.getModel() ).setMaximum( upperBound.getValue() );
		( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).setMinimum( lowerBound.getValue() );
		( ( SpinnerNumberModel ) currentMaxSpinner.getModel() ).setMaximum( upperBound.getValue() );

		lowerValue.updateListeners();
		upperValue.updateListeners();
		currentMaxSpinner.addChangeListener( maxSpinnerCL );
		currentMinSpinner.addChangeListener( minSpinnerCL );
	}

	public synchronized void removeSource( final BdvSource source )
	{
		final int sourceID = currentSourceIdx;
		lowerBoundLookup.remove( sourceID );
		upperBoundLookup.remove( sourceID );
		if ( labelingMinMax.containsKey( sourceID ) )
		{
			labelingMinMax.remove( sourceID );
		}
		final HashMap< Integer, Pair< Double, Double > > tmp = new HashMap<>();
		final Iterator< Integer > it = labelingMinMax.keySet().iterator();
		while ( it.hasNext() )
		{
			final int idx = it.next();
			Pair< Double, Double > pair = labelingMinMax.get( idx );
			if ( idx > sourceID )
			{
				tmp.put( idx - 1, pair );
			}
			else
			{
				tmp.put( idx, pair );
			}
		}
		labelingMinMax = tmp;
	}

	/**
	 * Set the knobs of the range-slider.
	 */
	private void setRangeSlider()
	{
		double range = upperBound.getValue() - lowerBound.getValue();
		final int upperVal = ( int ) ( ( ( upperValue.getValue() - lowerBound.getValue() ) / range ) * numberOfSteps );
		final int lowerVal = ( int ) ( ( ( lowerValue.getValue() - lowerBound.getValue() ) / range ) * numberOfSteps );
		rs.setUpperValue( upperVal );
		rs.setValue( lowerVal );
	}
}
