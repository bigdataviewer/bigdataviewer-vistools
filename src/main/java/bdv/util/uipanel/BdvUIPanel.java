package bdv.util.uipanel;

import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.BehaviourTransformEventHandler;
import bdv.BehaviourTransformEventHandlerFactory;
import bdv.BigDataViewer;
import bdv.BigDataViewerActions;
import bdv.cache.CacheControl.CacheControls;
import bdv.tools.HelpDialog;
import bdv.tools.bookmarks.Bookmarks;
import bdv.tools.bookmarks.BookmarksEditor;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.DisplayMode;
import bdv.viewer.NavigationActions;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerPanel.AlignPlane;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;

/**
 * BdvUIPanel embeds {@link ViewerPanel} in a splitpane that additionally holds
 * a reworked Selection & Grouping UI-Component, a Navigation UI-Component that
 * allows locking of rotation/translation, and a new Interpolation UI-Component.
 * It is also possible to get the JPanels of the three components and integrate
 * them into a different UI-setup.
 *
 * Note: It is possible to add additional panels to the existing UI-Panel via
 * {@link #addNewCard(String, boolean, JComponent)}.
 *
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class BdvUIPanel extends BdvHandle
{

	private final ManualTransformationEditor manualTransformationEditor;

	private final Bookmarks bookmarks;

	private final BookmarksEditor bookmarksEditor;

	private final InputActionBindings keyBindings;

	private final TriggerBehaviourBindings triggerbindings;

	private SelectionAndGroupingTabs selectionAndGroupingPanel;

	private JSplitPane splitPane;

	private TransformationPanel transformationPanel;

	private CardPanel controls;

	private InterpolationModePanel interpolationPanel;

	private HelpDialog helpDialog;

	public BdvUIPanel( final Frame dialogOwner, final BdvOptions options )
	{
		super( options );

		final ViewerOptions viewerOptions = options.values.getViewerOptions();
		final InputTriggerConfig inputTriggerConfig = BigDataViewer.getInputTriggerConfig( viewerOptions );

		final TransformEventHandlerFactory< AffineTransform3D > thf = viewerOptions.values
				.getTransformEventHandlerFactory();
		if ( thf instanceof BehaviourTransformEventHandlerFactory )
			( ( BehaviourTransformEventHandlerFactory< ? > ) thf ).setConfig( inputTriggerConfig );

		cacheControls = new CacheControls();

		viewer = new ViewerPanel( new ArrayList<>(), 1, cacheControls, viewerOptions );
		if ( !options.values.hasPreferredSize() )
			viewer.getDisplay().setPreferredSize( null );
		viewer.getDisplay().addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				tryInitTransform();
			}
		} );

		setupAssignments = new SetupAssignments( new ArrayList<>(), 0, 65535 );

		keyBindings = new InputActionBindings();
		SwingUtilities.replaceUIActionMap( viewer, keyBindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( viewer, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				keyBindings.getConcatenatedInputMap() );

		triggerbindings = new TriggerBehaviourBindings();
		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		viewer.getDisplay().addHandler( mouseAndKeyHandler );

		final TransformEventHandler< ? > tfHandler = viewer.getDisplay().getTransformEventHandler();
		if ( tfHandler instanceof BehaviourTransformEventHandler )
			( ( BehaviourTransformEventHandler< ? > ) tfHandler ).install( triggerbindings );

		manualTransformationEditor = new ManualTransformationEditor( viewer, keyBindings );

		bookmarks = new Bookmarks();
		bookmarksEditor = new BookmarksEditor( viewer, keyBindings, bookmarks );

		viewer.setDisplayMode( DisplayMode.FUSED );

		selectionAndGroupingPanel = new SelectionAndGroupingTabs( viewer, viewer.getVisibilityAndGrouping(),
				manualTransformationEditor, setupAssignments );

		final NavigationActions navactions = new NavigationActions( inputTriggerConfig );
		navactions.install( keyBindings, "navigation" );
		navactions.modes( viewer );
		navactions.sources( viewer );
		navactions.time( viewer );
		if ( options.values.is2D() )
			navactions.alignPlaneAction( viewer, AlignPlane.XY, "shift Z" );
		else
			navactions.alignPlanes( viewer );

		final BigDataViewerActions bdvactions = new BigDataViewerActions( inputTriggerConfig );
		bdvactions.install( keyBindings, "bdv" );
		bdvactions.bookmarks( bookmarksEditor );
		bdvactions.manualTransform( manualTransformationEditor );
		this.sourceChangeListeners().add( selectionAndGroupingPanel );

		splitPane = createSplitPane();
		controls = new CardPanel();

		controls.addNewCard( new JLabel( "Selection" ), true, selectionAndGroupingPanel );

		transformationPanel = new TransformationPanel( triggerbindings, manualTransformationEditor, viewer );
		selectionAndGroupingPanel.addSelectionChangeListener( transformationPanel );
		this.sourceChangeListeners().add( transformationPanel );
		controls.addNewCard( new JLabel( "Navigation" ), true, transformationPanel );

		interpolationPanel = new InterpolationModePanel( viewer );
		controls.addNewCard( new JLabel( "Interpolation" ), true, interpolationPanel );

		addStuff( viewer, controls );

		new IntensityMouseOverOverlay<>( viewer );

		helpDialog = new HelpDialog( dialogOwner );
		BigDataViewerActions actions = new BigDataViewerActions( inputTriggerConfig );
		actions.dialog( helpDialog );
		actions.install( keyBindings, "bdv" );
	}

	private void addStuff( final ViewerPanel viewer, JPanel controls )
	{
		final JScrollPane scrollPane = new JScrollPane( controls );
		scrollPane.setPreferredSize( new Dimension( controls.getPreferredSize().width + 20, viewer.getPreferredSize().height ) );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		splitPane.setLeftComponent( this.viewer );
		splitPane.setRightComponent( scrollPane );
		splitPane.getLeftComponent().setMinimumSize( new Dimension( 200, 200 ) );
		splitPane.getLeftComponent().setPreferredSize( viewer.getPreferredSize() );
		splitPane.setDividerLocation( viewer.getOptionValues().getWidth() );
	}

	/**
	 * Create splitpane.
	 *
	 * @return splitpane
	 */
	private JSplitPane createSplitPane()
	{
		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPane.setUI( new BasicSplitPaneUI()
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
						g.setColor( new Color( 238, 238, 238 ) );
						g.fillRect( 0, 0, getSize().width, getSize().height );
						super.paint( g );
					}
				};
			}
		} );

		splitPane.setBackground( new Color( 31, 31, 45 ) );
		splitPane.setDividerLocation( viewer.getPreferredSize().width );
		splitPane.setResizeWeight( 1.0 );

		return splitPane;
	}

	@Override
	public ManualTransformationEditor getManualTransformEditor()
	{
		return manualTransformationEditor;
	}

	@Override
	public InputActionBindings getKeybindings()
	{
		return keyBindings;
	}

	@Override
	public TriggerBehaviourBindings getTriggerbindings()
	{
		return triggerbindings;
	}

	@Override
	public void close()
	{
		viewer.stop();
	}

	public JSplitPane getSplitPane()
	{
		return splitPane;
	}

	/**
	 * Adds a new JComponent to this BdvUIPanel.
	 *
	 * @param name
	 *            displayed as title of the card
	 * @param open
	 * @param comp
	 *            content of the card
	 */
	public void addNewCard( final String name, final boolean open, final JComponent comp )
	{
		controls.addNewCard( new JLabel( name ), open, comp );
	}

	/**
	 * Get the JPanel of a previously added card.
	 *
	 * @param name
	 *            of the card panel
	 * @return the panel
	 */
	public JPanel getCard( final String name )
	{
		return controls.getCards().get( name );
	}

	/**
	 * The selection and grouping dialog of the BdvUIPanel can be embedded in
	 * other UIs. This dialog also contains color & brightness settings.
	 *
	 * @return selection and grouping dialog
	 */
	public SelectionAndGroupingTabs getSelectionAndGroupingPanel()
	{
		return this.selectionAndGroupingPanel;
	}

	/**
	 * The transformation dialog. This enables locking of rotation/translation
	 * as well as changing the initial transformation of the sources.
	 *
	 * @return transformation dialog
	 */
	public TransformationPanel getTransformationPanel()
	{
		return this.transformationPanel;
	}

	/**
	 *
	 * @return the interpolation mode selection panel
	 */
	public InterpolationModePanel getInterpolationPanel()
	{
		return this.interpolationPanel;
	}

	/**
	 * Toggle card fold.
	 *
	 * @param cardName
	 */
	public void toggleCard( final String cardName )
	{
		controls.toggleCardFold( cardName );
	}
}
