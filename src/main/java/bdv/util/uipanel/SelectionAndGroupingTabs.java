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

import bdv.util.BdvHandle;
import bdv.util.BdvSource;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformActiveListener;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.VisibilityAndGrouping.Event;
import bdv.viewer.VisibilityAndGrouping.UpdateListener;
import bdv.viewer.state.SourceGroup;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

/**
 * The tabbed pane with all BDV-UI components.
 *
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class SelectionAndGroupingTabs extends JTabbedPane implements BdvHandle.SourceChangeListener
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The UI foreground color.
	 */
	private static final Color FOREGROUND_COLOR = Color.darkGray;

	/**
	 * The UI background color.
	 */
	private static final Color BACKGROUND_COLOR = Color.white;

	/**
	 * Item to add new groups.
	 */
	private static final String NEW_GROUP = "<New Group>";

	/**
	 * Combobox displaying all current sources.
	 */
	private JComboBox< String > sourcesComboBox;

	/**
	 * Combobox displaying all groups with an option to create new groups.
	 */
	private JComboBox< String > groupesComboBox;

	/**
	 * Map holding the source names mapped to the {@link SourceProperties}.
	 */
	private final Map< String, BdvSource > sourceLookup = new HashMap<>();

	/**
	 * Map holding the group names mapped to the {@link GroupProperties}.
	 */
	private final Map< String, GroupProperties > groupLookup = new HashMap<>();

	private final Map< String, Integer > sourceIdx = new HashMap<>();

	/**
	 * The currently selected group.
	 */
	private int currentSelection;

	/**
	 * Eye icon normal size.
	 */
	private ImageIcon visibleIcon;

	/**
	 * Crossed eye icon normal size.
	 */
	private ImageIcon notVisibleIcon;

	/**
	 * Eye icon small.
	 */
	private ImageIcon visibleIconSmall;

	/**
	 * Crossed eye icon small.
	 */
	private ImageIcon notVisibleIconSmall;

	/**
	 * Label representing the visibility state of the source.
	 */
	private JLabel sourceVisibilityLabel;

	/**
	 * Only display selected source.
	 */
	private boolean singleSourceMode;

	/**
	 * Single source mode checkbox.
	 */
	private JCheckBox singleSourceModeCheckbox;

	/**
	 * Label representing the visibility state of the group.
	 */
	private JLabel groupVisibilityLabel;

	/**
	 * Single groupp mode checkbox.
	 */
	private JCheckBox singleGroupModeCheckbox;

	/**
	 * Remove the selected group button.
	 */
	private JButton removeGroup;

	/**
	 * Splitpane holding the selected sources and remaining (not selected)
	 * sources of a group.
	 */
	private JSplitPane selection;

	/**
	 * Sources which are part of the selected group.
	 */
	private JPanel selectedSources;

	/**
	 * Sources which are not part of the selected group.
	 */
	private JPanel remainingSources;

	/**
	 * Activity state of manual transformation.
	 */
	private boolean manualTransformationActive;

	/**
	 * The information panel, showing information about the selected source.
	 */
	private InformationPanel informationPanel;

	/**
	 * The min-max range slider-component.
	 */
	private RangeSliderSpinnerPanel intensitySlider;

	private boolean groupMode = false;

	private VisibilityAndGrouping visGro;

	private ViewerPanel viewerPanel;

	private SetupAssignments setupAssignments;

	private List< SelectionChangeListener > selectionChangeListeners = new ArrayList<>();

	/**
	 * This class holds the selection and grouping tab of the big data viewer
	 * UI.
	 *
	 * @param es
	 *            the event service
	 * @param bdvHandlePanel
	 *            the bdv handle panel
	 */
	public SelectionAndGroupingTabs( final ViewerPanel vp, final VisibilityAndGrouping visGro,
			final ManualTransformationEditor manualTE, final SetupAssignments sa )
	{

		initialize();
		this.visGro = visGro;
		this.viewerPanel = vp;
		this.setupAssignments = sa;

		this.viewerPanel.addGroup( new SourceGroup( "All" ) );
		while ( visGro.getSourceGroups().size() > 1 )
		{
			final SourceGroup g = visGro.getSourceGroups().get( 0 );
			viewerPanel.removeGroup( g );
		}

		visGro.setCurrentGroup( 1 );
		visGro.setGroupActive( 1, true );

		setupTabbedPane();
		addListeners( manualTE );

	}

	/**
	 * Initialize components.
	 */
	private void initialize()
	{
		visibleIcon = new ImageIcon( SelectionAndGroupingTabs.class.getResource( "visible.png" ), "Visible" );
		notVisibleIcon = new ImageIcon( SelectionAndGroupingTabs.class.getResource( "notVisible.png" ), "Not Visible" );

		visibleIconSmall = new ImageIcon( SelectionAndGroupingTabs.class.getResource( "visible_small.png" ), "Visible" );
		notVisibleIconSmall = new ImageIcon( SelectionAndGroupingTabs.class.getResource( "notVisible_small.png" ),
				"Not Visible" );

		groupLookup.put( "All", new GroupProperties( "All", true ) );
	}

	/**
	 * Add tabs source and group to tabbed pane.
	 *
	 * Also notify the bdv handle of tab switches.
	 *
	 * @param es
	 *            event service
	 * @param bdvHandlePanel
	 *            bdv handle
	 */
	private void setupTabbedPane()
	{
		UIManager.put( "TabbedPane.contentAreaColor", BACKGROUND_COLOR );
		this.setUI( new CustomTabbedPaneUI() );

		this.setBackground( BACKGROUND_COLOR );
		this.setForeground( FOREGROUND_COLOR );

		this.addTab( "Source Control", createSourceControl() );

		this.addTab( "Group Control", createGroupControl() );

		// Notify panel of the mode change.
		this.addMouseListener( new MouseListener()
		{

			@Override
			public void mouseReleased( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mousePressed( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseExited( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseClicked( MouseEvent e )
			{
				if ( getSelectedIndex() == 1 )
				{
					if ( singleSourceMode )
					{
						viewerPanel.setDisplayMode( DisplayMode.GROUP );
					}
					else
					{
						viewerPanel.setDisplayMode( DisplayMode.FUSEDGROUP );
					}
					groupMode = true;
				}
				else
				{
					sourcesComboBox.setSelectedIndex( visGro.getCurrentSource() );
					if ( singleSourceMode )
					{
						viewerPanel.setDisplayMode( DisplayMode.SINGLE );
					}
					else
					{
						viewerPanel.setDisplayMode( DisplayMode.FUSED );
					}
					groupMode = false;
				}
			}
		} );
	}

	/**
	 * Link the components to the BDV handle components to keep the state of bdv
	 * and UI consistent.
	 *
	 * @param visibilityAndGrouping
	 * @param manualTransformationEditor
	 */
	private void addListeners( final ManualTransformationEditor manualTransformationEditor )
	{

		manualTransformationEditor.addManualTransformActiveListener( new ManualTransformActiveListener()
		{

			@Override
			public void manualTransformActiveChanged( final boolean enabled )
			{
				setEnableSelectionAndGrouping( !enabled );
				manualTransformationActive = enabled;
			}
		} );

		visGro.addUpdateListener( new UpdateListener()
		{

			@Override
			public void visibilityChanged( Event e )
			{
				SwingUtilities.invokeLater( () -> {
					if ( e.id == VisibilityAndGrouping.Event.CURRENT_SOURCE_CHANGED )
					{
						sourcesComboBox.setSelectedIndex( visGro.getCurrentSource() );
					}
					if ( e.id == VisibilityAndGrouping.Event.CURRENT_GROUP_CHANGED )
					{
						groupesComboBox.setSelectedIndex( visGro.getCurrentGroup() + 1 );
					}
					if ( e.id == VisibilityAndGrouping.Event.DISPLAY_MODE_CHANGED )
					{
						final DisplayMode mode = visGro.getDisplayMode();
						if ( mode.equals( DisplayMode.FUSEDGROUP ) )
						{
							singleGroupModeCheckbox.setSelected( false );
							singleSourceModeCheckbox.setSelected( false );
							singleSourceMode = false;

							setEnableVisibilityIcons( true );

							setSelectedIndex( 1 );
						}
						else if ( mode.equals( DisplayMode.FUSED ) )
						{
							singleGroupModeCheckbox.setSelected( false );
							singleSourceModeCheckbox.setSelected( false );
							singleSourceMode = false;

							setEnableVisibilityIcons( true );

							setSelectedIndex( 0 );
						}
						else if ( mode.equals( DisplayMode.GROUP ) )
						{
							singleGroupModeCheckbox.setSelected( true );
							singleSourceModeCheckbox.setSelected( true );
							singleSourceMode = true;

							setEnableVisibilityIcons( false );

							setSelectedIndex( 1 );
						}
						else
						{
							singleGroupModeCheckbox.setSelected( true );
							singleSourceModeCheckbox.setSelected( true );
							sourceVisibilityLabel.setEnabled( false );
							singleSourceMode = true;

							setEnableVisibilityIcons( false );

							setSelectedIndex( 0 );
						}
						sourcesComboBox.repaint();
						groupesComboBox.repaint();
					}
				} );
			}
		} );
	}

	/**
	 * Add information of new source to the UI.
	 *
	 * Put it into the corresponding group, set visibility and add it to the
	 * source selection.
	 *
	 */
	public synchronized void addSource( final BdvSource p )
	{
		int srcIdx = sourceIdx.size();

		String name = p.getName();
		name = uniqueName( name );

		sourceIdx.put( name, srcIdx );
		sourceLookup.put( name, p );

		groupLookup.get( "All" ).addSource( name );

		sourcesComboBox.addItem( name );
		sourcesComboBox.setSelectedIndex( this.viewerPanel.getState().getCurrentSource() );
		informationPanel.setType( p.getTypeAsString() );
		groupesComboBox.setSelectedIndex( 1 );
		intensitySlider.addSource( srcIdx );
		intensitySlider.setSource( sourcesComboBox.getSelectedIndex() );

		this.viewerPanel.getVisibilityAndGrouping().addSourceToGroup( srcIdx, 0 );
	}

	private String uniqueName( final String name )
	{
		String prefix = "";
		int i = 0;
		while ( sourceLookup.containsKey( prefix + name ) )
		{
			prefix = i + "_";
			i++;
		}
		return prefix + name;
	}

	/**
	 * Remove source.
	 */
	public synchronized void removeSource( final BdvSource source )
	{
		intensitySlider.removeSource( source );
		for ( final GroupProperties group : groupLookup.values() )
		{
			group.getSourceNames().remove( source.getName() );
		}
		sourcesComboBox.removeItem( source.getName() );
		sourceLookup.remove( source.getName() );
		sourceIdx.remove( source.getName() );
	}

	private void setEnableVisibilityIcons( final boolean active )
	{
		if ( !groupLookup.isEmpty() )
		{
			if ( !active )
			{
				groupVisibilityLabel.setEnabled( false );
				groupVisibilityLabel.setIcon( visibleIcon );
				sourceVisibilityLabel.setEnabled( false );
				sourceVisibilityLabel.setIcon( visibleIcon );
			}
			else
			{
				groupVisibilityLabel.setEnabled( true );
				if ( groupLookup.get( groupesComboBox.getSelectedItem() ).isVisible() )
				{
					groupVisibilityLabel.setIcon( visibleIcon );
				}
				else
				{
					groupVisibilityLabel.setIcon( notVisibleIcon );
				}
				sourceVisibilityLabel.setEnabled( true );

				boolean visible = visGro.isSourceActive( sourceIdx.get( sourcesComboBox.getSelectedItem() ) );
				if ( visible )
				{
					sourceVisibilityLabel.setIcon( visibleIcon );
				}
				else
				{
					sourceVisibilityLabel.setIcon( notVisibleIcon );
				}
			}
		}
	}

	/**
	 * Toggle component enable.
	 *
	 * @param active
	 *            state
	 */
	private void setEnableSelectionAndGrouping( final boolean active )
	{
		sourcesComboBox.setEnabled( active );
		singleSourceModeCheckbox.setEnabled( active );
		groupesComboBox.setEnabled( active );
		singleGroupModeCheckbox.setEnabled( active );
		selectedSources.setEnabled( active );
		remainingSources.setEnabled( active );
		for ( Component c : selectedSources.getComponents() )
		{
			if ( c instanceof JLabel )
				c.setEnabled( active );
		}
		for ( Component c : remainingSources.getComponents() )
		{
			if ( c instanceof JLabel )
				c.setEnabled( active );
		}
		removeGroup.setEnabled( active );
		this.setEnabled( active );
	}

	/**
	 * Build the source control panel.
	 *
	 * @param bdvHandlePanel
	 *            the bdv handle
	 * @return the source contorl panel
	 */
	private Component createSourceControl()
	{
		final JPanel p = new JPanel( new MigLayout( "fillx", "[grow][][]", "[][]push[][]" ) );
		p.setBackground( BACKGROUND_COLOR );

		// source selection combobox
		sourcesComboBox = new JComboBox<>();
		sourcesComboBox.setMaximumSize( new Dimension( 270, 30 ) );
		sourcesComboBox.setRenderer( new SourceComboBoxRenderer() );
		sourcesComboBox.setBackground( BACKGROUND_COLOR );

		p.add( sourcesComboBox, "growx" );

		// source visibility icon (eye icon)
		sourceVisibilityLabel = new JLabel( visibleIcon );
		sourceVisibilityLabel.setBackground( BACKGROUND_COLOR );
		sourceVisibilityLabel.setToolTipText( "Show source in fused mode." );
		sourceVisibilityLabel.addMouseListener( new MouseListener()
		{

			@Override
			public void mouseReleased( MouseEvent e )
			{
				if ( !singleSourceMode )
				{
					boolean sourceActiveState = visGro.isSourceActive( sourcesComboBox.getSelectedIndex() );
					sourceActiveState = !sourceActiveState;
					if ( sourceActiveState )
					{
						sourceVisibilityLabel.setIcon( notVisibleIcon );
					}
					else
					{
						sourceVisibilityLabel.setIcon( visibleIcon );
					}
					visGro.getSources().get( sourcesComboBox.getSelectedIndex() ).setActive( sourceActiveState );
					viewerPanel.requestRepaint();
					sourcesComboBox.repaint();
				}
			}

			@Override
			public void mousePressed( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseExited( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseClicked( MouseEvent e )
			{
				// nothing
			}
		} );

		// color choser component
		final JButton colorButton = new JButton();
		colorButton.setPreferredSize( new Dimension( 15, 15 ) );
		colorButton.setBackground( BACKGROUND_COLOR );
		colorButton.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent ev )
			{
				Color newColor = null;
				ConverterSetup setup = setupAssignments.getConverterSetups().get( sourcesComboBox.getSelectedIndex() );
				if ( ev.getSource() == colorButton )
				{
					newColor = JColorChooser.showDialog( null, "Select Source Color", getColor( setup ) );
					if ( newColor != null )
					{
						colorButton.setBackground( newColor );

						setColor( setup, newColor );
					}
				}
			}
		} );

		// add action listener to source combobox
		sourcesComboBox.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					sourcesComboBox.setToolTipText( ( String ) sourcesComboBox.getSelectedItem() );
					final BdvSource p = sourceLookup.get( sourcesComboBox.getSelectedItem() );
					notifySelectionChangeListeners( false );
					if ( p != null )
					{
						intensitySlider.setSource( sourcesComboBox.getSelectedIndex() );
						visGro.setCurrentSource( sourcesComboBox.getSelectedIndex() );
						ConverterSetup setup = setupAssignments.getConverterSetups()
								.get( sourcesComboBox.getSelectedIndex() );
						colorButton.setBackground( getColor( setup ) );
						if ( !singleSourceMode )
						{
							sourceVisibilityLabel.setEnabled( true );
							if ( visGro.isSourceActive( sourcesComboBox.getSelectedIndex() ) )
							{
								sourceVisibilityLabel.setIcon( visibleIcon );
							}
							else
							{
								sourceVisibilityLabel.setIcon( notVisibleIcon );
							}
						}
						else
						{
							sourceVisibilityLabel.setIcon( visibleIcon );
							sourceVisibilityLabel.setEnabled( false );
						}
					}
				}
			}
		} );

		p.add( colorButton );
		p.add( sourceVisibilityLabel, "wrap" );

		// add information panel
		informationPanel = new InformationPanel();
		p.add( informationPanel, "span, growx, wrap" );

		// single source mode checkbox to toggle between fused mode and single
		// source
		// mode
		singleSourceModeCheckbox = new JCheckBox( "Single Source Mode" );
		singleSourceModeCheckbox.setBackground( BACKGROUND_COLOR );
		singleSourceModeCheckbox.setToolTipText( "Display only the selected source." );
		singleSourceModeCheckbox.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				singleSourceMode = singleSourceModeCheckbox.isSelected();
				if ( !singleSourceMode && !groupMode )
				{
					visGro.setDisplayMode( DisplayMode.FUSED );
				}
				else if ( !singleSourceMode && groupMode )
				{
					visGro.setDisplayMode( DisplayMode.FUSEDGROUP );
				}
				else if ( singleSourceMode && !groupMode )
				{
					visGro.setDisplayMode( DisplayMode.SINGLE );
				}
				else
				{
					visGro.setDisplayMode( DisplayMode.GROUP );
				}
			}
		} );

		// add range slider for intensity boundaries.
		intensitySlider = new RangeSliderSpinnerPanel( setupAssignments, sourceLookup );
		intensitySlider.setPreferredSize( new Dimension( 20, 20 ) );
		p.add( intensitySlider, "span, growx, wrap" );
		p.add( singleSourceModeCheckbox, "span, growx" );
		return p;
	}

	/**
	 * Build the group control panel.
	 *
	 * @return the group control panel
	 */
	private Component createGroupControl()
	{
		final JPanel p = new JPanel( new MigLayout( "fillx", "[grow][][]", "" ) );
		p.setBackground( BACKGROUND_COLOR );

		groupesComboBox = new JComboBox<>();
		groupesComboBox.setMaximumSize( new Dimension( 269, 30 ) );
		groupesComboBox.setRenderer( new GroupComboBoxRenderer() );
		groupesComboBox.setBackground( BACKGROUND_COLOR );
		groupesComboBox.setForeground( FOREGROUND_COLOR );
		// entry which opens the add-group dialog
		groupesComboBox.addItem( NEW_GROUP );
		// the default group containing all entries
		groupesComboBox.addItem( "All" );

		// remove group button
		removeGroup = new JButton( "-" );
		removeGroup.setForeground( FOREGROUND_COLOR );
		removeGroup.setBackground( BACKGROUND_COLOR );
		removeGroup.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if ( e.getSource() == removeGroup )
				{
					int selectedIndex = groupesComboBox.getSelectedIndex();
					String selected = ( String ) groupesComboBox.getSelectedItem();
					SourceGroup toRemove = null;
					for ( int i = 0; i < visGro.getSourceGroups().size(); i++ )
					{
						if ( visGro.getSourceGroups().get( i ).getName().equals( selected ) )
						{
							toRemove = visGro.getSourceGroups().get( i );
						}
					}
					groupesComboBox.removeItemAt( selectedIndex );
					groupesComboBox.setSelectedIndex( 1 );
					if ( toRemove != null )
					{
						viewerPanel.removeGroup( toRemove );
						groupLookup.remove( selected );
					}
				}
			}
		} );

		// panel which holds all sources which are part of the selected group
		selectedSources = new JPanel( new MigLayout( "fillx", "[grow]", "[]" ) );
		selectedSources.setBackground( BACKGROUND_COLOR );
		selectedSources.setBorder( null );

		// panel which holds all sources which are NOT part of the selected
		// group
		remainingSources = new JPanel( new MigLayout( "fillx", "[grow]", "[]" ) );
		remainingSources.setBackground( BACKGROUND_COLOR );
		remainingSources.setBorder( null );

		// the split pane holding selected and remaining sources
		selection = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		selection.setPreferredSize( new Dimension( selection.getPreferredSize().width, 150 ) );
		selection.setUI( new BasicSplitPaneUI()
		{
			public BasicSplitPaneDivider createDefaultDivider()
			{
				return new BasicSplitPaneDivider( this )
				{

					/**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void paint( Graphics g )
					{
						g.setColor( BACKGROUND_COLOR );
						g.fillRect( 0, 0, getSize().width, getSize().height );
						super.paint( g );
					}
				};
			}
		} );
		selection.setDividerLocation( 70 );
		selection.setBackground( BACKGROUND_COLOR );
		selection.setForeground( FOREGROUND_COLOR );
		selection.setBorder( null );
		final JScrollPane scrollPaneTop = new JScrollPane( selectedSources, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		scrollPaneTop.getVerticalScrollBar().setUI( new WhiteScrollBarUI() );
		scrollPaneTop.getHorizontalScrollBar().setUI( new WhiteScrollBarUI() );
		selection.setTopComponent( scrollPaneTop );
		final JScrollPane scrollPaneBottom = new JScrollPane( remainingSources, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		scrollPaneBottom.getVerticalScrollBar().setUI( new WhiteScrollBarUI() );
		scrollPaneBottom.getHorizontalScrollBar().setUI( new WhiteScrollBarUI() );
		selection.setBottomComponent( scrollPaneBottom );

		// Action listener handling the current group and updating selected and
		// remaining sources.
		// Also handles new group creation.
		groupesComboBox.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					final Object selection = e.getItem();
					if ( selection != null && selection instanceof String )
					{
						final String s = ( String ) selection;

						if ( s.equals( NEW_GROUP ) )
						{
							final String newGroupName = JOptionPane.showInputDialog( p, "New Group Name:" );
							if ( newGroupName != null && !newGroupName.isEmpty() )
							{
								if ( groupLookup.containsKey( newGroupName ) )
								{
									JOptionPane.showMessageDialog( p, "This group already exists." );
									groupesComboBox.setSelectedItem( newGroupName );
								}
								else
								{
									groupLookup.put( newGroupName, new GroupProperties( newGroupName, true ) );

									groupesComboBox.addItem( newGroupName );
									int idx = viewerPanel.getState().getSourceGroups().size();
									viewerPanel.addGroup( new SourceGroup( newGroupName ) );
									visGro.setCurrentGroup( idx );
									groupesComboBox.setSelectedItem( newGroupName );
								}
							}
							else
							{
								groupesComboBox.setSelectedIndex( currentSelection );
							}
						}

						currentSelection = groupesComboBox.getSelectedIndex();
						if ( getSelectedIndex() == 1 )
						{
							visGro.setCurrentGroup( getGroupIndex( ( String ) groupesComboBox.getSelectedItem() ) );
							if ( !singleSourceMode )
							{
								groupVisibilityLabel.setEnabled( true );
								if ( groupLookup.get( groupesComboBox.getSelectedItem() ).isVisible() )
								{
									groupVisibilityLabel.setIcon( visibleIcon );
								}
								else
								{
									groupVisibilityLabel.setIcon( notVisibleIcon );
								}
							}
							else
							{
								groupVisibilityLabel.setIcon( visibleIcon );
								groupVisibilityLabel.setEnabled( false );
							}
						}
						selectedSources.removeAll();
						remainingSources.removeAll();

						sourceLookup.keySet().forEach( new Consumer< String >()
						{

							@Override
							public void accept( String t )
							{
								if ( groupLookup.get( groupesComboBox.getSelectedItem() ).getSourceNames().contains( t ) )
								{
									selectedSources.add( createEntry( t ), "growx, wrap" );
								}
								else
								{
									remainingSources.add( createEntry( t ), "growx, wrap" );
								}
								repaintComponents();
							}

							private Component createEntry( String t )
							{
								final JLabel p = new JLabel( t );
								p.setBackground( BACKGROUND_COLOR );
								p.setForeground( FOREGROUND_COLOR );
								p.setBorder( null );
								p.addMouseListener( new MouseListener()
								{

									@Override
									public void mouseReleased( MouseEvent e )
									{
										if ( !manualTransformationActive )
										{
											final GroupProperties group = groupLookup
													.get( groupesComboBox.getSelectedItem() );
											if ( group.getSourceNames().contains( t ) )
											{
												group.removeSource( t );
												selectedSources.remove( p );
												remainingSources.add( p, "growx, wrap" );
												visGro.removeSourceFromGroup( getSourceIndex( t ),
														getGroupIndex( ( String ) groupesComboBox.getSelectedItem() ) );
											}
											else
											{
												group.addSource( t );
												remainingSources.remove( p );
												selectedSources.add( p, "growx, wrap" );
												visGro.addSourceToGroup( getSourceIndex( t ),
														getGroupIndex( ( String ) groupesComboBox.getSelectedItem() ) );
											}

											repaintComponents();
										}
									}

									@Override
									public void mousePressed( MouseEvent e )
									{
										// nothing
									}

									@Override
									public void mouseExited( MouseEvent e )
									{
										// nothing
									}

									@Override
									public void mouseEntered( MouseEvent e )
									{
										// nothing
									}

									@Override
									public void mouseClicked( MouseEvent e )
									{
										// nothing
									}
								} );
								return p;
							}
						} );

						removeGroup.setEnabled( groupesComboBox.getSelectedIndex() > 1 );
					}
				}
			}

		} );
		groupesComboBox.setSelectedIndex( -1 );
		p.add( groupesComboBox, "growx" );

		// label displaying the visibility state of the current group (eye icon)
		groupVisibilityLabel = new JLabel( visibleIcon );
		groupVisibilityLabel.setBackground( BACKGROUND_COLOR );
		groupVisibilityLabel.setBorder( null );
		groupVisibilityLabel.setToolTipText( "Show group in fused-group mode." );
		groupVisibilityLabel.addMouseListener( new MouseListener()
		{

			@Override
			public void mouseReleased( MouseEvent e )
			{
				if ( !singleSourceMode )
				{
					String selected = ( String ) groupesComboBox.getSelectedItem();
					boolean groupActiveState = groupLookup.get( selected ).isVisible();
					if ( groupActiveState )
					{
						groupActiveState = !groupActiveState;
						groupVisibilityLabel.setIcon( notVisibleIcon );
					}
					else
					{
						groupActiveState = !groupActiveState;
						groupVisibilityLabel.setIcon( visibleIcon );
					}
					groupLookup.get( selected ).setVisible( groupActiveState );
					visGro.setGroupActive( getGroupIndex( selected ), groupActiveState );
				}
			}

			@Override
			public void mousePressed( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseExited( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				// nothing
			}

			@Override
			public void mouseClicked( MouseEvent e )
			{
				// nothing
			}
		} );

		p.add( groupVisibilityLabel );

		p.add( removeGroup, "growx, wrap" );

		// checkbox to toggle between fused group mode and single group mode
		singleGroupModeCheckbox = new JCheckBox( "Single Group Mode" );
		singleGroupModeCheckbox.setBackground( BACKGROUND_COLOR );
		singleGroupModeCheckbox.setToolTipText( "Display only the currently selected group." );
		singleGroupModeCheckbox.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				singleSourceModeCheckbox.setSelected( singleGroupModeCheckbox.isSelected() );
				singleSourceMode = singleSourceModeCheckbox.isSelected();
				if ( !singleSourceMode && !groupMode )
				{
					visGro.setDisplayMode( DisplayMode.FUSED );
				}
				else if ( !singleSourceMode && groupMode )
				{
					visGro.setDisplayMode( DisplayMode.FUSEDGROUP );
				}
				else if ( singleSourceMode && !groupMode )
				{
					visGro.setDisplayMode( DisplayMode.SINGLE );
				}
				else
				{
					visGro.setDisplayMode( DisplayMode.GROUP );
				}
			}
		} );
		p.add( selection, "span, growx, wrap" );

		p.add( singleGroupModeCheckbox, "span, growx" );

		return p;
	}

	private void repaintComponents()
	{
		selectedSources.revalidate();
		remainingSources.revalidate();
		SelectionAndGroupingTabs.this.selection.revalidate();
		selectedSources.repaint();
		remainingSources.repaint();
		SelectionAndGroupingTabs.this.selection.repaint();
	}

	// A white look and feel for scroll bars.
	private final class WhiteScrollBarUI extends BasicScrollBarUI
	{
		@Override
		protected void configureScrollBarColors()
		{
			LookAndFeel.installColors( scrollbar, "ScrollBar.background", "ScrollBar.foreground" );
			thumbHighlightColor = BACKGROUND_COLOR;
			thumbLightShadowColor = BACKGROUND_COLOR;
			thumbDarkShadowColor = BACKGROUND_COLOR;
			thumbColor = Color.lightGray;
			trackColor = BACKGROUND_COLOR;
			trackHighlightColor = BACKGROUND_COLOR;
		}

		@Override
		protected JButton createDecreaseButton( int orientation )
		{
			BasicArrowButton button = new BasicArrowButton( orientation, BACKGROUND_COLOR, BACKGROUND_COLOR,
					Color.lightGray, BACKGROUND_COLOR );
			button.setBorder( new LineBorder( BACKGROUND_COLOR ) );
			button.setBackground( BACKGROUND_COLOR );
			return button;
		}

		@Override
		protected JButton createIncreaseButton( int orientation )
		{
			BasicArrowButton button = new BasicArrowButton( orientation, BACKGROUND_COLOR, BACKGROUND_COLOR,
					Color.lightGray, BACKGROUND_COLOR );
			button.setBorder( new LineBorder( BACKGROUND_COLOR ) );
			button.setBackground( BACKGROUND_COLOR );
			return button;
		}
	}

	// A combobox renderer displaying the visibility state of the sources.
	class SourceComboBoxRenderer extends JLabel implements ListCellRenderer< String >
	{

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent( JList< ? extends String > list, String value, int index,
				boolean isSelected, boolean cellHasFocus )
		{

			if ( value != null )
			{
				this.setText( value );
				this.setToolTipText( value );
				this.setIcon( visibleIconSmall );
				boolean visible = visGro.isSourceActive( sourceIdx.get( value ) );
				if ( !singleSourceMode && !visible )
				{
					this.setIcon( notVisibleIconSmall );
				}
			}
			else
			{
				this.setIcon( null );
			}

			if ( isSelected )
			{
				setForeground( Color.gray );
			}
			else
			{
				setForeground( FOREGROUND_COLOR );
			}

			return this;
		}

	}

	// A combobox renderer displaying the visibility state of the groups.
	class GroupComboBoxRenderer extends JLabel implements ListCellRenderer< String >
	{

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent( JList< ? extends String > list, String value, int index,
				boolean isSelected, boolean cellHasFocus )
		{

			if ( value != null )
			{
				if ( !value.equals( NEW_GROUP ) )
				{
					this.setIcon( visibleIconSmall );
					if ( !singleSourceMode && !groupLookup.get( value ).isVisible() )
					{
						this.setIcon( notVisibleIconSmall );
					}
				}
				else
				{
					this.setIcon( null );
				}
				this.setText( value );
				this.setToolTipText( value );
			}

			if ( isSelected )
			{
				setForeground( Color.gray );
			}
			else
			{
				setForeground( FOREGROUND_COLOR );
			}

			return this;
		}
	}

	private static Color getColor( final ConverterSetup setup )
	{
		if ( setup.supportsColor() )
		{
			final int value = setup.getColor().get();
			return new Color( value );
		}
		else
			return new Color( 0xFFBBBBBB );
	}

	private static void setColor( final ConverterSetup setup, final Color color )
	{
		setup.setColor( new ARGBType( color.getRGB() | 0xff000000 ) );
	}

	/**
	 * Ugly hack to get correct group index.
	 *
	 * @param groupName
	 *            to get index of
	 * @return index
	 */
	private int getGroupIndex( final String groupName )
	{
		final List< String > groupNames = new ArrayList<>();
		viewerPanel.getState().getSourceGroups().forEach( g -> groupNames.add( g.getName() ) );
		return groupNames.indexOf( groupName );
	}

	/**
	 * Ugly hack to get correct source index.
	 *
	 * @param sourceName
	 *            to get index of
	 * @return index
	 */
	private int getSourceIndex( final String sourceName )
	{
		final List< String > sourceNames = new ArrayList<>();
		viewerPanel.getState().getSources().forEach( c -> sourceNames.add( c.getSpimSource().getName() ) );
		return sourceNames.indexOf( sourceName );
	}

	public interface SelectionChangeListener
	{
		public void selectionChanged( final boolean isOverlay );
	}

	private void notifySelectionChangeListeners( final boolean isOverlay )
	{
		for ( final SelectionChangeListener l : this.selectionChangeListeners )
		{
			l.selectionChanged( isOverlay );
		}
	}

	public void addSelectionChangeListener( final SelectionChangeListener l )
	{
		this.selectionChangeListeners.add( l );
	}

	public void removeSelectionChangeListener( final SelectionChangeListener l )
	{
		this.selectionChangeListeners.remove( l );
	}
}
