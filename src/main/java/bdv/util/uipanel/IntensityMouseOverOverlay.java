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

import bdv.util.PlaceHolderSource;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import bdv.viewer.DisplayMode;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.ViewerPanel;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.ui.OverlayRenderer;

/**
 *
 * An overlay which shows the intensity value at the mouse position of the
 * current source.
 *
 * Note: Value is only displayed if {@link DisplayMode} is
 * {@link DisplayMode#FUSED} or {@link DisplayMode#SINGLE}.
 *
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class IntensityMouseOverOverlay< L, I extends IntegerType< I > >
{

	/**
	 * The viewer instance.
	 */
	private final ViewerPanel viewer;

	/**
	 * Intensity at mouse position
	 */
	private volatile String intensity = "";

	/**
	 * Overlay painting the source intensity at the mouse position.
	 *
	 * @param viewerPanel
	 *            of the BDV
	 * @param convs
	 *            the converters
	 */
	public IntensityMouseOverOverlay( final ViewerPanel viewerPanel )
	{
		this.viewer = viewerPanel;
		viewerPanel.getDisplay().addHandler( new MouseOver() );
		viewerPanel.getDisplay().addOverlayRenderer( new Overlay() );
	}

	/**
	 * Get the current source and print value.
	 */
	private void printValueUnderMouse()
	{
		int sourceIdx = viewer.getVisibilityAndGrouping().getCurrentSource();
		if ( sourceIdx >= 0 )
		{
			if ( !( viewer.getVisibilityAndGrouping().getSources().get( sourceIdx ).getSpimSource() instanceof PlaceHolderSource ) )
			{
				final RealPoint pos = new RealPoint( 3 );
				viewer.getGlobalMouseCoordinates( pos );

				final ViewerState state = viewer.getState();

				final int currentSourceIndex = sourceIdx;
				if ( currentSourceIndex >= 0 )
				{
					final SourceState< ? > sourceState = state.getSources().get( currentSourceIndex );
					final Source< ? > spimSource = sourceState.getSpimSource();
					typedPrintValueUnderMouse( spimSource, state, pos );
				}
			}
		}
	}

	/**
	 * Extract the value for the source corresponding to the orientation of the
	 * viewer.
	 *
	 * @param source
	 *            to get the value from
	 * @param state
	 *            of the viewer
	 * @param pos
	 *            of the mouse
	 */
	private < R > void typedPrintValueUnderMouse( final Source< R > source, final ViewerState state,
			final RealLocalizable pos )
	{
		final int t = state.getCurrentTimepoint();
		final AffineTransform3D transform = new AffineTransform3D();
		source.getSourceTransform( t, 0, transform );
		final RealRandomAccessible< R > interpolated = source.getInterpolatedSource( t, 0, Interpolation.NEARESTNEIGHBOR );
		final RealRandomAccessible< R > transformed = RealViews.affineReal( interpolated, transform );
		final RealRandomAccess< R > access = transformed.realRandomAccess();

		access.setPosition( pos );
		final R type = access.get();
		if ( type instanceof RealType )
		{
			intensity = String.format( "value = %6.3f", ( ( RealType< ? > ) type ).getRealDouble() );
		}
	}

	private class MouseOver implements MouseMotionListener
	{
		@Override
		public void mouseDragged( final MouseEvent arg0 )
		{
			printValueUnderMouse();
		}

		@Override
		public void mouseMoved( final MouseEvent arg0 )
		{
			printValueUnderMouse();
		}
	}

	/**
	 * The overlay painting the information.
	 *
	 * The information strings are underlyed with a light black rectangle to
	 * improve readability in front of light images.
	 *
	 * Note: {@link ViewerPanel#getSourceInfoOverlayRenderer()} the mouse
	 * coordinates are painted here because the underlying rectangle has to be
	 * painted first.
	 *
	 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
	 *
	 */
	private class Overlay implements OverlayRenderer
	{

		@Override
		public void drawOverlays( final Graphics g )
		{
			final String value = intensity;
			g.setColor( new Color( 0, 0, 0, 125 ) );
			g.fillRoundRect( ( int ) ( g.getClipBounds().getWidth() - 172 ), 0, 172, 42, 4, 4 );

			g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
			g.setColor( Color.white );
			if ( viewer.getState().getDisplayMode() != DisplayMode.GROUP
					&& viewer.getState().getDisplayMode() != DisplayMode.FUSEDGROUP )
			{
				g.drawString( value, ( int ) g.getClipBounds().getWidth() - 170, 39 );
			}

			viewer.getSourceInfoOverlayRenderer().setViewerState( viewer.getState() );
			viewer.getSourceInfoOverlayRenderer().paint( ( Graphics2D ) g );

			final RealPoint gPos = new RealPoint( 3 );
			viewer.getGlobalMouseCoordinates( gPos );
			final String mousePosGlobalString = String.format( "(%6.1f,%6.1f,%6.1f)", gPos.getDoublePosition( 0 ),
					gPos.getDoublePosition( 1 ), gPos.getDoublePosition( 2 ) );

			g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
			g.setColor( Color.white );
			g.drawString( mousePosGlobalString, ( int ) g.getClipBounds().getWidth() - 170, 25 );
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}
}
