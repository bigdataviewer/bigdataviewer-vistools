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
import java.awt.Graphics;

import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * 
 * A custom colored tabbed pane UI.
 * 
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class CustomTabbedPaneUI extends BasicTabbedPaneUI {

	@Override
	protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
		super.paintContentBorder(g, tabPlacement, selectedIndex);
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		highlight = Color.lightGray;
		lightHighlight = Color.lightGray;
		shadow = Color.lightGray;
		darkShadow = Color.white;
		focus = Color.white;

	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
			boolean isSelected) {
		g.setColor(Color.white);
		switch (tabPlacement) {
		case LEFT:
			g.fillRect(x + 1, y + 1, w - 1, h - 3);
			break;
		case RIGHT:
			g.fillRect(x, y + 1, w - 2, h - 3);
			break;
		case BOTTOM:
			g.fillRect(x + 1, y, w - 3, h - 1);
			break;
		case TOP:
		default:
			g.fillRect(x + 1, y + 1, w - 3, h - 1);
		}
	}

}
