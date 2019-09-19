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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Panel which holds named Cards which can be opened and closed.
 *
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class CardPanel extends JPanel
{

	private static final long serialVersionUID = 1833879807294153532L;

	/**
	 * Name to Component lookup.
	 */
	private Map< String, JComponent > lookup = new HashMap<>();

	/**
	 * Map of all added cards.
	 */
	private Map< String, JPanel > cards = new HashMap<>();

	/**
	 * Name to Boolean is card open lookup.
	 */
	private Map< String, Boolean > openState = new HashMap<>();

	/**
	 * Lookup if card is enabled.
	 */
	private Map< String, Boolean > enabledCard = new HashMap<>();

	/**
	 * Color scheme.
	 */
	final Color backgroundTabPanel = Color.white;

	final Color headerColor = new Color( 238, 238, 238 );

	final Color fontColor = Color.darkGray;

	/**
	 * Open card icon.
	 */
	private final ImageIcon downIcon;

	/**
	 * Close card icon.
	 */
	private final ImageIcon upIcon;

	/**
	 * A new card panel with no cards.
	 */
	public CardPanel()
	{
		super();
		downIcon = new ImageIcon( CardPanel.class.getResource( "downbutton.png" ), "Open Dialog." );
		upIcon = new ImageIcon( CardPanel.class.getResource( "upbutton.png" ), "Close Dialog." );

		this.setLayout( new MigLayout( "fillx, ins 2", "[grow]", "[]" ) );
		this.setBackground( backgroundTabPanel );
	}

	/**
	 * Add a new card to this card panel.
	 *
	 * @param name
	 *            of the card
	 * @param open
	 *            card is added open
	 * @param component
	 *            displayed in the card.
	 */
	public synchronized void addNewCard( final JLabel name, final boolean open, final JComponent component )
	{
		if ( lookup.containsKey( name.getText() ) ) { throw new RuntimeException( "A tab with name \"" + name + "\" already exists." ); }

		openState.put( name.getText(), open );
		setBackgroundColor( component, backgroundTabPanel );

		final JPanel card = new JPanel( new MigLayout( "fillx, ins 4, hidemode 3", "[grow]", "[]0lp![]" ) );
		card.setBackground( backgroundTabPanel );

		// Holds the component with insets.
		final JPanel componentPanel = new JPanel( new MigLayout( "fillx, ins 8, hidemode 3", "[grow]", "[]0lp![]" ) );
		componentPanel.setBackground( backgroundTabPanel );
		componentPanel.add( component, "growx" );

		final JComponent header = createHeader( name, componentPanel, card );
		card.add( header, "growx, wrap" );
		card.add( componentPanel, "growx" );

		this.add( card, "growx, wrap" );
		lookup.put( name.getText(), header );
		enabledCard.put( name.getText(), true );
		cards.put( name.getText(), card );
		this.revalidate();
	}

	private void setBackgroundColor( final JComponent comp, final Color color )
	{
		for ( final Component c : comp.getComponents() )
		{
			c.setBackground( color );
			if ( c instanceof JComponent )
			{
				setBackgroundColor( ( JComponent ) c, color );
			}
		}
	}

	/**
	 * Remove card with this name.
	 *
	 * @param name
	 *            of the card to remove
	 */
	public synchronized void removeCard( final String name )
	{
		if ( !lookup.containsKey( name ) ) { throw new RuntimeException( "Tab with name \"" + name + "\" does not exists." ); }

		this.remove( lookup.get( name ) );
		lookup.remove( name );
		cards.remove( name );
		this.revalidate();
	}

	/**
	 * Get all added JPanels. This includes the Selection&Grouping with the
	 * intensity sliders panel, the transformation manipulation panel, and the
	 * interpolation mode panel.
	 *
	 * The panels can be accessed via their label string.
	 *
	 * @return added cards
	 */
	public Map< String, JPanel > getCards()
	{
		return cards;
	}

	/**
	 * Create clickable header.
	 */
	private JComponent createHeader( final JLabel label, final JComponent component, final JPanel tab )
	{
		final JPanel header = new JPanel( new MigLayout( "fillx, aligny center, ins 0 0 0 4", "[grow][]", "" ) );
		header.setPreferredSize( new Dimension( 30, 30 ) );
		header.setBackground( headerColor );

		// Holds the name with insets.
		final JPanel labelPanel = new JPanel( new MigLayout( "fillx, ins 4", "[grow]", "" ) );
		labelPanel.setBackground( headerColor );
		label.setForeground( fontColor );
		labelPanel.add( label );

		final JLabel icon = new JLabel();
		icon.setBackground( Color.WHITE );
		icon.setIcon( downIcon );

		String name = label.getText();

		// By default closed.
		header.addMouseListener( new MouseListener()
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
				if ( enabledCard.get( name ) )
				{
					boolean state = openState.get( name );
					for ( Component c : tab.getComponents() )
					{
						if ( c != header )
						{
							c.setVisible( !state );
						}
					}
					openState.put( name, !state );

					if ( state )
					{
						icon.setIcon( downIcon );
					}
					else
					{
						icon.setIcon( upIcon );
					}
					tab.revalidate();
				}
			}
		} );

		header.add( labelPanel, "growx" );
		header.add( icon );

		component.setVisible( openState.get( name ) );
		if ( openState.get( name ) )
		{
			icon.setIcon( upIcon );
		}
		else
		{
			icon.setIcon( downIcon );
		}

		return header;
	}

	/**
	 * Toggle card open/close.
	 *
	 * @param name
	 *            of the card to toggle.
	 */
	public void toggleCardFold( final String name )
	{
		if ( lookup.containsKey( name ) )
		{
			lookup.get( name ).getMouseListeners()[ 0 ].mouseClicked( null );
			this.revalidate();
		}
	}

	/**
	 * Set mouse-listener active. If not active, the card can't be toggled.
	 *
	 * @param name
	 *            of the card
	 * @param active
	 *            state
	 */
	public void setCardActive( final String name, final boolean active )
	{
		if ( lookup.containsKey( name ) )
		{
			enabledCard.put( name, active );
		}
	}

}
